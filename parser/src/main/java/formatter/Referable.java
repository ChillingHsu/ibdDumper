package formatter;

public interface Referable<T, R> {
    R deref(T context, Class<R> targetClass);
}
