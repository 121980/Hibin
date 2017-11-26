package services;


import clients.EsiaJedis;
import com.google.gson.Gson;
import common.JsonHelper;
import interfaces.ActionTwoParams;
import interfaces.Func;
import listeners.ISubscriber;
import redis.clients.jedis.JedisPubSub;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RedisService implements IRedisService {

    protected EsiaJedis client = null;

    private Gson gson = JsonHelper.GetGson();

    private Logger logger = Logger.getLogger(RedisService.class.getName());

    // TODO ЗАИНЖЕКТИТЬ КОНФИГУРАЦИЮ REDIS ИЗ КОНФИГА
    // TODO транзакции

    public RedisService() {
        client = new EsiaJedis();
    }

    /**
     * Дсериализует json строку в объект заданного типа
     *
     * @param type  - тип объекта результата
     * @param value - json строка
     * @param toLog - делегат формирующий сигнатуру для логирования
     * @param <T>   - тип объекта результата
     * @return
     */
    protected <T> T deserialize(Class<T> type, String value, Func<String> toLog) {
        T result = null;
        try {
            result = gson.fromJson(value, type);
        } catch (Exception sex) {
            logger.log(Level.ALL, "Failed deserialize " + toLog.call() + " to type: " + type.getName() + " value: " + value + " ex: " + sex.getMessage());
        }
        return result;
    }

    /**
     * Сериализует объект в json
     *
     * @param object - объект
     * @param toLog  - делегат формирующий сигнатуру для логирования
     * @param <T>    - тип объекта результата
     * @return
     */
    protected <T> String serialize(T object, Func<String> toLog) {
        String result = null;
        try {
            result = gson.toJson(object);
        } catch (Exception sex) {
            logger.log(Level.ALL, "Failed serialize " + toLog.call() + " to json, ex: " + sex.getMessage());
        }
        return result;
    }

    private void fail(String msg){
        logger.log(Level.ALL, "Failed Redis operation: " + msg);
    }

    private void info(String msg){
        logger.log(Level.INFO, "Redis operation: " + msg);
    }

    //region Базовые операции

    /**
     * Сохранить объект в Redis, как сериализованную в json строку
     * https://redis.io/commands/set
     *
     * @param key    - идентификатор
     * @param object - объект
     */
    public <T, TKey> void put(TKey key, T object) {
        String msg = "Save object key: " + key.toString() + " type: " + object.getClass().getName();
        try {
            String json = serialize(object, () -> msg);
            String result = client.set(key.toString(), json);
            info("code: " + result + " " + msg);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
    }

    /**
     * Сохранить объект в Redis, как поток байт
     * https://redis.io/commands/set
     * @deprecated для СЭ будет нагляднее видеть json в redis-cli консоли, и нормальная сериализация сильно экономит память
     * @param key    - идентификатор
     * @param object - объект
     */
    public <T, TKey> void bput(TKey key, T object) {
        String msg = "Save object key: " + key.toString() + " type: " + object.getClass().getName();
        try {
            String json = serialize(object, () -> msg);
            String result = client.set(key.toString().getBytes(), json.getBytes());
            info("code: " + result + " " + msg);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
    }

    /**
     * Сохранить объект в Redis на заданное время TTL(time to life), как сериализованную в json строку
     *
     * @param key    - идентификатор
     * @param object - объект
     * @param expire - TTL время в секундах через которое объект будет уничтожен
     */
    public <T, TKey> void put(TKey key, T object, long expire) {
        String msg = "Save object key: " + key.toString() + " type: " + object.getClass();
        try {
            String json = serialize(object, () -> msg);
            String result = client.set(key.toString(), json, "", "", expire);
            info("code: " + result + " " + msg);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
    }

    /**
     * Задать объекту по ключу время жизни, в секундах
     * https://redis.io/commands/expire
     *
     * @param key    - идентификатор
     * @param expire - TTL время в секундах через которое объект будет уничтожен
     * @return true при успешном завершении, false если ключа не существует
     */
    public <TKey> boolean expire(TKey key, int expire) {
        boolean result = false;
        String msg = "change expire for key: " + key.toString() + " to: " + expire + " sec";
        try {
            long code = client.expire(key.toString(), expire);
            result = code > 0;
            info("result: " + result + " code: " + code + " " + msg);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
        return result;
    }

    /**
     * Задать объекту по ключу время жизни, в абсолютном выражении в виде unix time (количество секунд с начала эпохи)
     * https://redis.io/commands/expireat
     *
     * @param key      - идентификатор
     * @param unixTime - TTL время в секундах c начала эпохи(с 1 января 1970) когда объект будет уничтожен
     * @return
     */
    public <TKey> boolean expireAt(TKey key, long unixTime) {
        boolean result = false;
        String msg = "change expire for key: " + key.toString() + " to unix time: " + unixTime;
        try {
            long code = client.expireAt(key.toString(), unixTime);
            result = code > 0;
            info("result: " + result + " code: " + code + " " + msg);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
        return result;
    }

    /**
     * Возвращает оставшееся TTL время жизни объекта(по ключу) в секундах до его уничтожения в Redis
     * https://redis.io/commands/ttl
     * @param key - идентификатор
     * @return Если объект имеет expired возвращается количество оставшихся секунд, если ключ не существует или у него не выставлено expired возвращается -1
     */
    @Override
    public <TKey> long ttl(TKey key) {
        long result = 0;
        String msg = "get ttl for key: " + key.toString();
        try {
            result = client.ttl(key.toString());
            info("result: " + result + " " + msg);
            if(result < 0) result = -1;
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
        return result;
    }

    /**
     * Очень быстро проверить существует ли ключ в Redis
     * https://redis.io/commands/exists
     *
     * @param key - идентификатор
     * @return
     */
    public <TKey> boolean exist(TKey key) {
        boolean result = false;
        String msg = "check exist for key: " + key.toString();
        try {
            result = client.exists(key.toString());
            info("result: " + result + " " + msg);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
        return result;
    }

    /**
     * Очень быстро проверить существует ли ключ в Redis
     * https://redis.io/commands/exists
     * @param keys - коллекция идентификаторов
     * @return число существующих ключей
     */
    @Override
    public <TKey> long exists(TKey... keys) {
        long result = 0;
        StringBuilder sb = new StringBuilder();
        String[] sKeys = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            sb.append(keys[i]);
            sKeys[i] = keys[i].toString();
            if(i < keys.length - 1) sb.append(", ");
        }
        String msg = "check exists for keys: " + sb.toString();
        try {
            result = client.exists(sKeys);
            info("result: " + result + " " + msg);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
        return result;
    }

    /**
     * Получить объект из Redis по идентификатору, сериализовав его в указанный тип
     * @param objectType - тип объекта в который десериализуем, пример: Foo.class
     * @param key - идентификатор
     * @return объект с типом T
     */
    public <T, TKey> T get(Class<T> objectType, TKey key) {
        T result = null;
        String msg = "get object by key: " + key.toString() + " type: " + objectType.getName();
        try {
            String json = client.get(key.toString());
            result = deserialize(objectType, json, () -> msg);
            info("success: " + msg + " json: " + json);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
        return result;
    }

    /**
     * Удалить объект из Redis
     * https://redis.io/commands/del
     * @param key
     * @return число удалённых объектов, 0 если указаного ключа не существует
     */
    public <TKey> long delete(TKey key) {
        long result = 0;
        String msg = "delete all with key: " + key.toString();
        try {
            result = client.del(key.toString());
            info("result: " + result + " " + msg);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
        return result;
    }

    /**
     * Удалить объекты из Redis
     * https://redis.io/commands/del
     *
     * @param keys
     * @return число удалённых объектов
     */
    public <TKey> long delete(TKey... keys) {
        long result = 0;
        StringBuilder sb = new StringBuilder();
        String[] sKeys = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            sb.append(keys[i]);
            sKeys[i] = keys[i].toString();
            if(i < keys.length - 1) sb.append(", ");
        }
        String msg = "delete keys: " + sb.toString();
        try {
            result = client.del(sKeys);
            info("result: " + result + " " + msg);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
        return result;
    }

    //endregion

    //region Очереди

    /**
     * Возвращает длину очереди
     * https://redis.io/commands/llen
     * @param queue - имя очереди
     * @return
     */
    public long queueSize(String queue) {
        long result = 0;
        String msg = "get queue length: " + queue;
        try {
            result = client.llen(queue);
            info("result: " + result + " " + msg);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
        return result;
    }

    /**
     * Добавить объект в начало очереди(слева начало, справа конец)
     * https://redis.io/commands/lpush
     * @param queue  - имя очереди
     * @param object - объект
     * @return Длина списка после добавления
     */
    public <T> long queueLeftPush(String queue, T object) {
        long result = 0;
        String msg = "left push to queue: " + queue + " object type: " + object.getClass().getName();
        try {
            String json = serialize(object, () -> msg);
            result = client.lpush(queue, json);
            info("result: " + result + " " + msg);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
        return result;
    }

    /**
     * Добавить объект в конец очереди(слева начало, справа конец)
     * https://redis.io/commands/rpush
     *
     * @param queue  - имя очереди
     * @param object - объект
     * @return Длина списка после добавления
     */
    public <T> long queueRightPush(String queue, T object) {
        long result = 0;
        String msg = "right push to queue: " + queue + " object type: " + object.getClass().getName();
        try {
            String json = serialize(object, () -> msg);
            result = client.rpush(queue, json);
            info("result: " + result + " " + msg);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
        return result;
    }

    /**
     * Атомарно получить один объект из конца списка(из списка удаляется) и добавить его же в новый локальный список. Отказоустойчиво.
     *
     * @param queue - имя очереди из которой удаляется и возвращается объект
     * @param to    - имя локальной очередь в которую добавляется копия объекта
     * @return объект из очереди
     */
    public <T> T dequeueAndPush(Class<T> objectType, String queue, String to) {
        T result = null;
        String msg = "get and remove from queue: " + queue + " and push to temp queue: " + to + " object type: " + objectType.getName();
        try {
            String json = client.rpoplpush(queue, to);
            result = deserialize(objectType, json, () -> msg);
            info("success: " + msg + " json: " + json);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
        return result;
    }

    /**
     * Удаляет из списка заданное количество объектов равных заданному, с начала списка или с конца
     * https://redis.io/commands/lrem
     *
     * @param queue  - имя очереди(списка) откуда удаляются объекты
     * @param count  - число удаляемых объектов:
     *               count > 0: Remove elements equal to value moving from head to tail.
     *               count > 0: Remove elements equal to value moving from tail to head.
     *               count = 0: Remove all elements equal to value.
     * @param object - объект равный которому необходимо удалять, сериализуется в json строку для сравнения
     * @return число удалённых из очереди
     */
    public <T> long removeFromQueue(String queue, int count, T object) {
        long result = 0;
        String msg = "remove from queue: " + queue + " count: " + count + " removed object type: " + object.getClass().getName();
        try {
            String json = serialize(object, () -> msg);
            result = client.lrem(queue, count, json);
            info("result: " + result + " " + msg);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
        return result;
    }

    /**
     * Обрезка очереди(списка)
     * https://redis.io/commands/ltrim
     * Trim an existing list so that it will contain only the specified range of elements specified. Both start and stop are zero-based indexes, where 0 is the first element of the list (the head), 1 the next element and so on.
     * For example: LTRIM foobar 0 2 will modify the list stored at foobar so that only the first three elements of the list will remain.
     * start and end can also be negative numbers indicating offsets from the end of the list, where -1 is the last element of the list, -2 the penultimate element and so on.
     *
     * @param queue - имя очереди(списка)
     * @param start
     * @param stop
     */
    public void trimQueue(String queue, long start, long stop) {
        String msg = "trim queue: " + queue + " to size from: " + start + " end: " + stop;
        try {
            String result = client.ltrim(queue, start, stop);
            info("code: " + result + " " + msg);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
    }


    //endregion

    //region PUB/SUB

    /**
     * Подписка на канал
     * Десериализацию нужно делать по месту использования, потому что сообщения в json потенциально могут содержать любой объект
     * @param channel       - имя канал, на котрый подписываемся
     * @param onMessage     - обработчик полученного сообщения
     * @param onSubscribe   - обработчик события(hook'а) подписки
     * @param onUnsubscribe - обработчик события(hook'а) отписки
     */
    public void subscribe(String channel, ActionTwoParams<String, String> onMessage, ActionTwoParams<String, Integer> onSubscribe, ActionTwoParams<String, Integer> onUnsubscribe) {
        subscribe(onMessage, onSubscribe, onUnsubscribe, channel);
    }

    /**
     * Подписка на несколько каналов
     * Десериализацию нужно делать по месту использования, потому что сообщения в json потенциально могут содержать любой объект
     * @param onMessage     - обработчик полученного сообщения
     * @param onSubscribe   - обработчик события(hook'а) подписки
     * @param onUnsubscribe - обработчик события(hook'а) отписки
     * @param channels      - коллекция имён каналов, на котрые подписываемся
     */
    public void subscribe(ActionTwoParams<String, String> onMessage, ActionTwoParams<String, Integer> onSubscribe, ActionTwoParams<String, Integer> onUnsubscribe, String... channels) {
        client.subscribe(
                new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        super.onMessage(channel, message);
                        onMessage.call(channel, message);
                    }

                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {
                        super.onSubscribe(channel, subscribedChannels);
                        onSubscribe.call(channel, subscribedChannels);
                    }

                    @Override
                    public void onUnsubscribe(String channel, int subscribedChannels) {
                        super.onUnsubscribe(channel, subscribedChannels);
                        onUnsubscribe.call(channel, subscribedChannels);
                    }
                }
                , channels);
    }

    /**
     * Подписка на несколько каналов
     * Десериализацию нужно делать по месту использования, потому что сообщения в json потенциально могут содержать любой объект
     * @param listener - объект слушатель, должен реализовывать интерфейс {@link ISubscriber}
     * @param channels - коллекция имён каналов, на котрые подписываемся
     */
    public void subscribe(ISubscriber listener, String... channels) {
        client.subscribe(
                new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        super.onMessage(channel, message);
                        listener.onMessage(channel, message);
                    }

                    @Override
                    public void onPMessage(String pattern, String channel, String message) {
                        super.onPMessage(pattern, channel, message);
                        listener.onPMessage(pattern, channel, message);
                    }

                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {
                        super.onSubscribe(channel, subscribedChannels);
                        listener.onSubscribe(channel, subscribedChannels);
                    }

                    @Override
                    public void onUnsubscribe(String channel, int subscribedChannels) {
                        super.onUnsubscribe(channel, subscribedChannels);
                        listener.onUnsubscribe(channel, subscribedChannels);
                    }

                    @Override
                    public void onPUnsubscribe(String pattern, int subscribedChannels) {
                        super.onPUnsubscribe(pattern, subscribedChannels);
                        listener.onPUnsubscribe(pattern, subscribedChannels);
                    }

                    @Override
                    public void onPSubscribe(String pattern, int subscribedChannels) {
                        super.onPSubscribe(pattern, subscribedChannels);
                        listener.onPSubscribe(pattern, subscribedChannels);
                    }
                }
                , channels);
    }

    /**
     * Подписка на несколько каналов по паттрнам в именах
     * Десериализацию нужно делать по месту использования, потому что сообщения в json потенциально могут содержать любой объект
     * @param onMessage     - обработчик полученного сообщения
     * @param onSubscribe   - обработчик события(hook'а) подписки
     * @param onUnsubscribe - обработчик события(hook'а) отписки
     * @param patterns      - коллекция паттернов имён каналов, на котрые подписываемся
     */
    public void psubscribe(ActionTwoParams<String, String> onMessage, ActionTwoParams<String, Integer> onSubscribe, ActionTwoParams<String, Integer> onUnsubscribe, String... patterns) {
        client.subscribe(
                new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        super.onMessage(channel, message);
                        onMessage.call(channel, message);
                    }

                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {
                        super.onSubscribe(channel, subscribedChannels);
                        onSubscribe.call(channel, subscribedChannels);
                    }

                    @Override
                    public void onUnsubscribe(String channel, int subscribedChannels) {
                        super.onUnsubscribe(channel, subscribedChannels);
                        onUnsubscribe.call(channel, subscribedChannels);
                    }
                }
                , patterns);
    }

    /**
     * Подписка на несколько каналов по паттрнам в именах
     * Десериализацию нужно делать по месту использования, потому что сообщения в json потенциально могут содержать любой объект
     * @param listener - объект слушатель, должен реализовывать интерфейс {@link ISubscriber}
     * @param patterns - коллекция паттернов имён каналов, на котрые подписываемся
     */
    public void psubscribe(ISubscriber listener, String... patterns) {
        client.subscribe(
                new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        super.onMessage(channel, message);
                        listener.onMessage(channel, message);
                    }

                    @Override
                    public void onPMessage(String pattern, String channel, String message) {
                        super.onPMessage(pattern, channel, message);
                        listener.onPMessage(pattern, channel, message);
                    }

                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {
                        super.onSubscribe(channel, subscribedChannels);
                        listener.onSubscribe(channel, subscribedChannels);
                    }

                    @Override
                    public void onUnsubscribe(String channel, int subscribedChannels) {
                        super.onUnsubscribe(channel, subscribedChannels);
                        listener.onUnsubscribe(channel, subscribedChannels);
                    }

                    @Override
                    public void onPUnsubscribe(String pattern, int subscribedChannels) {
                        super.onPUnsubscribe(pattern, subscribedChannels);
                        listener.onPUnsubscribe(pattern, subscribedChannels);
                    }

                    @Override
                    public void onPSubscribe(String pattern, int subscribedChannels) {
                        super.onPSubscribe(pattern, subscribedChannels);
                        listener.onPSubscribe(pattern, subscribedChannels);
                    }
                }
                , patterns);
    }

    /**
     * Отправить сообщение в указанный канал
     * @param channel - канал
     * @param message - сообщение
     * @return возвращает количество получивших клиентов, 0 если никто не получил
     */
    public long publish(String channel, String message) {
        long result = 0;
        String msg = "publish message to channel: " + channel + " message type: " + message.getClass().getName();
        try {
            result = client.publish(channel, message);
            info("result: received to " + result + " subscriber " + msg);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
        return result;
    }

    /**
     * Отправить объект(уведомление-контекст) в указанный канал
     * @param channel - канал
     * @param message - объект отправляемый в канал, будет сериализован в json строку
     * @return возвращает количество получивших клиентов, 0 если никто не получил
     */
    public <T> long publish(String channel, T message) {
        long result = 0;
        String msg = "publish message to channel: " + channel + " message type: " + message.getClass().getName();
        try {
            String json = serialize(message, () -> msg);
            result = client.publish(channel, json);
            info("result: received to " + result + " subscriber " + msg);
        } catch (Exception sex) {
            fail(msg + " ex: " + sex.getMessage());
        }
        return result;
    }

    /**
     * Отписка от указанных каналов
     *
     * @param channels
     */
    public void unsubscribe(String... channels) {
        client.unsubscribe(channels);
    }

    //endregion

}
