package interfaces;

/**
 * Обобщенный интерфейс для лямда-выражений принимающих три параметра типа T, R и M, и не возвращающих результат
 * @param <T>
 * @param <R>
 * @param <M>
 */
public interface ActionThreeParams<T,R,M> {
    void call(T t, R r, M m);
}
