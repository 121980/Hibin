package interfaces;

/**
 * Обобщенный интерфейс для лямда-выражений принимающих три параметра типа T1,T2,T3 и возвращающих результат типа TResult
 * @param <T1>
 * @param <T2>
 * @param <T3>
 * @param <TResult>
 */
public interface FuncThreeParams<T1,T2,T3,TResult> {
    TResult call(T1 firstParam, T2 secondParam, T3 thirdParam);
}
