/*
 * Copyright 2015 Google Inc.
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
package com.google.j2cl.ast;

import com.google.j2cl.ast.processors.Visitable;

import java.util.List;

import javax.annotation.Nullable;

/**
 * For Statement.
 */
@Visitable
public class ForStatement extends Statement {
  @Visitable @Nullable Expression conditionExpression;
  @Visitable Block block;
  @Visitable List<Expression> initializers;
  @Visitable List<Expression> updaters;

  public ForStatement(
      Expression conditionExpression,
      Block block,
      List<Expression> initializers,
      List<Expression> updaters) {
    this.conditionExpression = conditionExpression;
    this.block = block;
    this.initializers = initializers;
    this.updaters = updaters;
  }

  public Expression getConditionExpression() {
    return conditionExpression;
  }

  public Block getBlock() {
    return block;
  }

  public List<Expression> getInitializers() {
    return initializers;
  }

  public List<Expression> getUpdaters() {
    return updaters;
  }

  @Override
  public ForStatement accept(Processor processor) {
    return Visitor_ForStatement.visit(processor, this);
  }
}
