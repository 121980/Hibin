package services;


import interfaces.ActionTwoParams;
import listeners.ISubscriber;

/**
 * Интерфес сервиса предоставляемого хранилищем Redis.
 * ВАЖНО: ВСЯ РАБОТА С REDIS в ЕСИА ДОЛЖНА ВЫПОЛНЯТЬСЯ ЧЕРЕЗ ЭТОТ ИНТЕРФЕЙС, ПРЯМАЯ РАБОТА С НАТИВНМИ КЛИЕНТАМИ ЗАПРЕЩЕНА.
 */
public interface IRedisService {

    //region Базовые операции

    /**
     * Сохранить объект в Redis, как сериализованную в json строку
     * https://redis.io/commands/set
     * @param key - идентификатор
     * @param object - объект
     * @param <T> - тип объекта
     * @param <TKey> - тип идентификатора
     */
    <T, TKey> void put(TKey key, T object);

    /**
     * Сохранить объект в Redis, как поток байт
     * https://redis.io/commands/set
     * @deprecated для СЭ будет нагляднее видеть json в redis-cli консоли, и нормальная сериализация сильно экономит память
     * @param key - идентификатор
     * @param object - объект
     * @param <T> - тип объекта
     * @param <TKey> - тип идентификатора
     */
    <T, TKey> void bput(TKey key, T object);

    /**
     * Сохранить объект в Redis на заданное время TTL(time to life), как сериализованную в json строку
     * @param key - идентификатор
     * @param object - объект
     * @param expire - TTL время в секундах через которое объект будет уничтожен
     * @param <T> - тип объекта
     * @param <TKey> - тип идентификатора
     */
    <T, TKey> void put(TKey key, T object, long expire);

    /**
     * Задать объекту по ключу время жизни, в секундах
     * https://redis.io/commands/expire
     * @param key - идентификатор
     * @param expire - TTL время в секундах через которое объект будет уничтожен
     * @param <TKey> - тип идентификатора
     * @return true при успешном завершении, false если ключа не существует
     */
    <TKey> boolean expire(TKey key, int expire);

    /**
     * Задать объекту по ключу время жизни, в абсолютном выражении в виде unix time (количество секунд с начала эпохи)
     * https://redis.io/commands/expireat
     * @param key - идентификатор
     * @param unixTime - TTL время в секундах c начала эпохи(с 1 января 1970) когда объект будет уничтожен
     * @param <TKey> - тип идентификатора
     * @return
     */
    <TKey> boolean expireAt(TKey key, long unixTime);

    /**
     * Возвращает оставшееся TTL время жизни объекта(по ключу) в секундах до его уничтожения в Redis
     * https://redis.io/commands/ttl
     * @param key - идентификатор
     * @param <TKey> - тип идентификатора
     * @return Если объект имеет expired возвращается количество оставшихся секунд, если ключ не существует или у него не выставлено expired возвращается -1
     */
    <TKey> long ttl(TKey key);

    /**
     * Очень быстро проверить существует ли ключ в Redis
     * https://redis.io/commands/exists
     * @param key - идентификатор
     * @param <TKey> - тип идентификатора
     * @return
     */
    <TKey> boolean exist(TKey key);

    /**
     * Очень быстро проверить существует ли ключ в Redis
     * https://redis.io/commands/exists
     * @param keys - коллекция идентификаторов
     * @param <TKey> - тип идентификатора
     * @return число существующих ключей
     */
    <TKey> long exists(TKey... keys);

    /**
     * Получить объект из Redis по идентификатору
     * @param <T> - тип объекта
     * @param <TKey> - тип идентификатора
     * @param objectType - тип объекта в который десериализуем, пример: Foo.class
     *@param key - идентификатор  @return объект с типом T
     */
    <T, TKey> T get(Class<T> objectType, TKey key);

    /**
     * Удалить объект из Redis
     * https://redis.io/commands/del
     * @param key
     * @param <TKey>
     * @return число удалённых объектов
     */
    <TKey> long delete(TKey key);

    /**
     * Удалить объекты из Redis
     * https://redis.io/commands/del
     * @param key
     * @param <TKey>
     * @return число удалённых объектов
     */
    <TKey> long delete(TKey... key);

    //endregion

    //region Очереди(списки)

    /**
     * Возвращает длину очереди
     * https://redis.io/commands/llen
     * @param queue - имя очереди
     * @return
     */
    long queueSize(String queue);

    /**
     * Добавить объект в начало очереди(слева начало, справа конец)
     * https://redis.io/commands/lpush
     * @param queue - имя очереди
     * @param object - объект
     * @param <T> - тип объекта
     * @return Длина списка после добавления
     */
    <T> long queueLeftPush(String queue, T object);

    /**
     * Добавить объект в конец очереди(слева начало, справа конец)
     * https://redis.io/commands/rpush
     * @param queue - имя очереди
     * @param object - объект
     * @param <T> - тип объекта
     * @return Длина списка после добавления
     */
    <T> long queueRightPush(String queue, T object);

    /**
     * Атомарно получить один объект из конца списка(из списка удаляется) и добавить его же в новый локальный список. Отказоустойчиво.
     * @param queue - имя очереди из которой удаляется и возвращается объект
     * @param to - имя локальной очередь в которую добавляется копия объекта
     * @param <T> - тип объекта в который десериализуется строка извлекаемая из списка Redis
     * @return объект из очереди
     */
    <T> T dequeueAndPush(Class<T> objectType, String queue, String to);

    /**
     * Удаляет из списка заданное количество объектов равных заданному, с начала списка или с конца
     * https://redis.io/commands/lrem
     * @param queue - имя очереди(списка) откуда удаляются объекты
     * @param count - число удаляемых объектов:
     *              count > 0: Remove elements equal to value moving from head to tail.
     *              count > 0: Remove elements equal to value moving from tail to head.
     *              count = 0: Remove all elements equal to value.
     * @param object - объект равный которому необходимо удалять, сериализуется в json строку для сравнения
     * @param <T> - тип объекта
     * @return число удалённых из очереди
     */
    <T> long removeFromQueue(String queue, int count, T object );

    /**
     * Обрезка очереди(списка)
     * https://redis.io/commands/ltrim
     * Trim an existing list so that it will contain only the specified range of elements specified. Both start and stop are zero-based indexes, where 0 is the first element of the list (the head), 1 the next element and so on.
     * For example: LTRIM foobar 0 2 will modify the list stored at foobar so that only the first three elements of the list will remain.
     * start and end can also be negative numbers indicating offsets from the end of the list, where -1 is the last element of the list, -2 the penultimate element and so on.
     * @param queue - имя очереди(списка)
     * @param start
     * @param stop
     */
    void trimQueue(String queue, long start, long stop);

    //endregion

    //region Pub/Sub

    /**
     * Подписка на канал
     * Десериализацию нужно делать по месту использования, потому что сообщения в json потенциально могут содержать любой объект
     * @param channel - имя канал, на котрый подписываемся
     * @param onMessage - обработчик полученного сообщения
     * @param onSubscribe - обработчик события(hook'а) подписки
     * @param onUnsubscribe - обработчик события(hook'а) отписки
     */
    void subscribe(final String channel, ActionTwoParams<String,String> onMessage, ActionTwoParams<String,Integer> onSubscribe, ActionTwoParams<String,Integer> onUnsubscribe);

    /**
     * Подписка на несколько каналов
     * Десериализацию нужно делать по месту использования, потому что сообщения в json потенциально могут содержать любой объект
     * @param onMessage - обработчик полученного сообщения
     * @param onSubscribe - обработчик события(hook'а) подписки
     * @param onUnsubscribe - обработчик события(hook'а) отписки
     * @param channels - коллекция имён каналов, на котрые подписываемся
     */
    void subscribe(ActionTwoParams<String,String> onMessage, ActionTwoParams<String,Integer> onSubscribe, ActionTwoParams<String,Integer> onUnsubscribe, final String... channels);

    /**
     * Подписка на несколько каналов
     * Десериализацию нужно делать по месту использования, потому что сообщения в json потенциально могут содержать любой объект
     * @param listener - объект слушатель, должен реализовывать интерфейс {@link ISubscriber}
     * @param channels - коллекция имён каналов, на котрые подписываемся
     */
    void subscribe(ISubscriber listener, final String... channels);

    /**
     * Подписка на несколько каналов по паттрнам в именах
     * Десериализацию нужно делать по месту использования, потому что сообщения в json потенциально могут содержать любой объект
     * @param onMessage - обработчик полученного сообщения
     * @param onSubscribe - обработчик события(hook'а) подписки
     * @param onUnsubscribe - обработчик события(hook'а) отписки
     * @param patterns - коллекция паттернов имён каналов, на котрые подписываемся
     */
    void psubscribe(ActionTwoParams<String,String> onMessage, ActionTwoParams<String,Integer> onSubscribe, ActionTwoParams<String,Integer> onUnsubscribe, final String... patterns);

    /**
     * Подписка на несколько каналов по паттрнам в именах
     * Десериализацию нужно делать по месту использования, потому что сообщения в json потенциально могут содержать любой объект
     * @param listener - объект слушатель, должен реализовывать интерфейс {@link ISubscriber}
     * @param patterns - коллекция паттернов имён каналов, на котрые подписываемся
     */
    void psubscribe(ISubscriber listener, final String... patterns);

    /**
     * Отправить сообщение в указанный канал
     * @param channel - канал
     * @param message - сообщение
     * @return возвращает количество получивших клиентов, 0 если никто не получил
     */
    long publish(final String channel, final String message);

    /**
     * Отправить объект(уведомление-контекст) в указанный канал
     * @param channel - канал
     * @param message - объект отправляемый в канал, будет сериализован в json строку
     * @param <T> - тип объекта
     * @return возвращает количество получивших клиентов, 0 если никто не получил
     */
    <T> long publish(final String channel, final T message);

    /**
     * Отписка от указанных каналов
     * @param channels
     */
    void unsubscribe(final String... channels);



    //endregion

}
