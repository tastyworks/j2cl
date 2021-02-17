package java.lang;

public class ThreadLocal<T> {

  private T value;

  private boolean init;

  protected T initialValue() {
    return null;
  }

  public T get() {
    if (init) {
      return value;
    }

    value = initialValue();
    init = true;
    return value;
  }
}
