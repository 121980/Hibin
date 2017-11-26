package clients;

import redis.clients.jedis.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;

/**
 * Реализация Jedis клиента с необходимыми методами, которых не написали авторы
 * todo подумать и возможно предложить pull request автору https://github.com/xetorthio/jedis/pulls
 */
public class EsiaJedis extends Jedis {

    //region Конструкторы

    public EsiaJedis() {
    }

    public EsiaJedis(String host) {
        super(host);
    }

    public EsiaJedis(String host, int port) {
        super(host, port);
    }

    public EsiaJedis(String host, int port, boolean ssl) {
        super(host, port, ssl);
    }

    public EsiaJedis(String host, int port, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        super(host, port, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
    }

    public EsiaJedis(String host, int port, int timeout) {
        super(host, port, timeout);
    }

    public EsiaJedis(String host, int port, int timeout, boolean ssl) {
        super(host, port, timeout, ssl);
    }

    public EsiaJedis(String host, int port, int timeout, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        super(host, port, timeout, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
    }

    public EsiaJedis(String host, int port, int connectionTimeout, int soTimeout) {
        super(host, port, connectionTimeout, soTimeout);
    }

    public EsiaJedis(String host, int port, int connectionTimeout, int soTimeout, boolean ssl) {
        super(host, port, connectionTimeout, soTimeout, ssl);
    }

    public EsiaJedis(String host, int port, int connectionTimeout, int soTimeout, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        super(host, port, connectionTimeout, soTimeout, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
    }

    public EsiaJedis(JedisShardInfo shardInfo) {
        super(shardInfo);
    }

    public EsiaJedis(URI uri) {
        super(uri);
    }

    public EsiaJedis(URI uri, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        super(uri, sslSocketFactory, sslParameters, hostnameVerifier);
    }

    public EsiaJedis(URI uri, int timeout) {
        super(uri, timeout);
    }

    public EsiaJedis(URI uri, int timeout, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        super(uri, timeout, sslSocketFactory, sslParameters, hostnameVerifier);
    }

    public EsiaJedis(URI uri, int connectionTimeout, int soTimeout) {
        super(uri, connectionTimeout, soTimeout);
    }

    public EsiaJedis(URI uri, int connectionTimeout, int soTimeout, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        super(uri, connectionTimeout, soTimeout, sslSocketFactory, sslParameters, hostnameVerifier);
    }

    //endregion

    /**
     * Отписаться от каналов
     * @param channels
     */
    public void unsubscribe(String... channels){
        client.unsubscribe(channels);
    }

}
