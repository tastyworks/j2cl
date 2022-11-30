/*
 * Copyright 2022 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javaemul.internal;

/** Backend-specific utils for Throwable. */
public final class ThrowableUtils {

  /**
   * Gets the Java {@link Throwable} of the specified js {@code Error}. Returns {@code null} if not
   * available.
   */
  public static Throwable getJavaThrowable(Object e) {
    // Wasm doesn't yet support conversion from JS errors.
    throw new UnsupportedOperationException();
  }

  /** JavaScript {@code Error}. Placeholder in WASM. */
  public static class NativeError {
    public static boolean hasCaptureStackTraceProperty;

    public static void captureStackTrace(Object error) {}

    public String stack;
  }

  /** JavaScript {@code TypeError}. Placeholder in WASM. */
  public static class NativeTypeError {}

  private ThrowableUtils() {}
}
