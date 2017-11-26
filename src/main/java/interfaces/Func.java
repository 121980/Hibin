package interfaces;

/**
 * Обобщенный интерфейс для лямда-выражений не принимающих параметров и возвращающих результат типа TResult
 * @param <TResult>
 */
public interface Func<TResult> {
    TResult call();
}
