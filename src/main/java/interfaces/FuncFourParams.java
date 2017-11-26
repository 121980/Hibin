package interfaces;

/**
 * Обобщенный интерфейс для лямда-выражений принимающих четыре параметра типа T1,T2,T3,T4 и возвращающих результат типа TResult
 * @param <T1>
 * @param <T2>
 * @param <T3>
 * @param <T4>
 * @param <TResult>
 */
public interface FuncFourParams<T1,T2,T3,T4,TResult> {
    TResult call(T1 firstParam, T2 secondParam, T3 thirdParam, T4 fourParam);
}
