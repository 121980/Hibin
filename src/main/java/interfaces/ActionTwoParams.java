package interfaces;

/**
 * Обобщенный интерфейс для лямда-выражений принимающих два параметра типа T и R, и не возвращающих результат
 * @param <T>
 * @param <R>
 */
public interface ActionTwoParams<T,R> {
    void call(T t, R r);
}
