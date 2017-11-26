package interfaces;

/**
 * Обобщенный интерфейс для лямда-выражений принимающих один параметр типа T и возвращающих результат типа TResult
 * @param <T>
 * @param <TResult>
 */
public interface FuncOneParam<T, TResult> {
    TResult call(T value);
}

