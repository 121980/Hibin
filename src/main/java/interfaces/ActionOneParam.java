package interfaces;

/**
 * Обобщенный интерфейс для лямда-выражений принимающих один параметр типа T и не возвращающих результат
 * @param <T>
 */
public interface ActionOneParam<T> {
    void call(T value);
}
