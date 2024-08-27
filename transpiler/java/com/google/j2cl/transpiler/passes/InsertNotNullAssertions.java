/*
 * Copyright 2021 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.j2cl.transpiler.passes;

import com.google.j2cl.common.SourcePosition;
import com.google.j2cl.transpiler.ast.AbstractRewriter;
import com.google.j2cl.transpiler.ast.AssertStatement;
import com.google.j2cl.transpiler.ast.CompilationUnit;
import com.google.j2cl.transpiler.ast.ConditionalExpression;
import com.google.j2cl.transpiler.ast.Expression;
import com.google.j2cl.transpiler.ast.MultiExpression;
import com.google.j2cl.transpiler.ast.Node;
import com.google.j2cl.transpiler.ast.NullLiteral;
import com.google.j2cl.transpiler.ast.StringLiteral;
import com.google.j2cl.transpiler.ast.TypeDescriptor;
import com.google.j2cl.transpiler.ast.TypeDescriptors;
import com.google.j2cl.transpiler.ast.TypeVariable;
import com.google.j2cl.transpiler.ast.Variable;
import com.google.j2cl.transpiler.ast.VariableDeclarationExpression;
import com.google.j2cl.transpiler.passes.ConversionContextVisitor.ContextRewriter;

/**
 * Inserts NOT_NULL_ASSERTION (!!) in places where Java performs implicit null-check, and when
 * conversion is needed from nullable to non-null type.
 */
public final class InsertNotNullAssertions extends NormalizationPass {
  @Override
  public void applyTo(CompilationUnit compilationUnit) {
    // Insert non-null assertions when converting from nullable to non-null type.
    // We run this first before adding any other not null assertions since the surrounding context
    // is obscured from this rewriter. If we ran this later we may emit double not-null assertions
    // we would be unaware that we're already surrounded by one.
    compilationUnit.accept(
        new ConversionContextVisitor(
            new ContextRewriter() {
              @Override
              public Expression rewriteTypeConversionContext(
                  TypeDescriptor inferredTypeDescriptor,
                  TypeDescriptor declaredTypeDescriptor,
                  Expression expression) {
                return !TypeDescriptors.isJavaLangVoid(inferredTypeDescriptor)
                        && (!inferredTypeDescriptor.canBeNull()
                            || !declaredTypeDescriptor.canBeNull())
                    ? insertNotNullAssertionIfNeeded(getSourcePosition(), expression)
                    : expression;
              }

              // Insert null assertions if necessary on places where the construct requires them.
              // TODO(b/253062274): Revisit when the bug is fixed. The method above should be enough
              // to emit most of these assertions. But in the current state the inferred types do
              // not take into consideration the nullability of the original type variable
              // in the inference.
              @Override
              public Expression rewriteNonNullTypeConversionContext(
                  TypeDescriptor inferredTypeDescriptor,
                  TypeDescriptor declaredTypeDescriptor,
                  Expression expression) {
                return insertNotNullAssertionIfNeeded(getSourcePosition(), expression);
              }
            }));

    compilationUnit.accept(
        new AbstractRewriter() {
          @Override
          public Node rewriteAssertStatement(AssertStatement assertStatement) {
            Expression message = assertStatement.getMessage();
            return message == null
                ? assertStatement
                : AssertStatement.Builder.from(assertStatement)
                    .setMessage(insertElvisIfNeeded(message, new StringLiteral("null")))
                    .build();
          }
        });
  }

  private Expression insertNotNullAssertionIfNeeded(
      SourcePosition sourcePosition, Expression expression) {
    if (isInferredAsNonNullInKotlin(expression)) {
      // Don't insert null-check for expressions which are known to be non-null, regardless of
      // nullability annotations.
      return expression;
    }
    if (expression instanceof NullLiteral) {
      getProblems().warning(sourcePosition, "Non-null assertion applied to null.");
    }

    return expression.postfixNotNullAssertion();
  }

  private static Expression insertElvisIfNeeded(
      Expression expression, Expression nonNullExpression) {
    if (isInferredAsNonNullInKotlin(expression)) {
      // Don't insert null-check for expressions which are known to be non-null, regardless of
      // nullability annotations.
      return expression;
    }

    if (expression instanceof NullLiteral) {
      return nonNullExpression;
    }

    MultiExpression.Builder elvisExpressionBuilder = MultiExpression.newBuilder();
    if (!expression.isIdempotent()) {
      Variable elvisVariable =
          Variable.newBuilder()
              .setName("tmp")
              .setFinal(true)
              .setTypeDescriptor(expression.getTypeDescriptor())
              .build();
      elvisExpressionBuilder.addExpressions(
          VariableDeclarationExpression.newBuilder()
              .addVariableDeclaration(elvisVariable, expression)
              .build());
      expression = elvisVariable.createReference();
    }

    return elvisExpressionBuilder
        .addExpressions(
            ConditionalExpression.newBuilder()
                .setConditionExpression(expression.infixEqualsNull())
                .setTrueExpression(nonNullExpression)
                .setFalseExpression(expression.clone())
                .setTypeDescriptor(TypeDescriptors.get().javaLangObject.toNonNullable())
                .build())
        .build();
  }

  private static boolean isInferredAsNonNullInKotlin(Expression expression) {
    return !expression.canBeNull()
        && !isWildcardOrCaptureAnnotatedNonNullable(expression.getTypeDescriptor());
  }

  // TODO(b/361088311): Remove this method when the Kotlin bug is fixed.
  private static boolean isWildcardOrCaptureAnnotatedNonNullable(TypeDescriptor typeDescriptor) {
    if (typeDescriptor instanceof TypeVariable) {
      TypeVariable typeVariable = (TypeVariable) typeDescriptor;
      return typeVariable.isWildcardOrCapture() && typeVariable.isAnnotatedNonNullable();
    }
    return false;
  }
}
