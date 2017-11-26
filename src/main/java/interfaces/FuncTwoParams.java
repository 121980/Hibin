package interfaces;

/**
 * Обобщенный интерфейс для лямда-выражений принимающих два параметра типа T1 и T2 и возвращающих результат типа TResult
 * @param <T1>
 * @param <T2>
 * @param <TResult>
 */
public interface FuncTwoParams<T1,T2,TResult> {
    TResult call(T1 paramOne, T2 paramTwo);
}
