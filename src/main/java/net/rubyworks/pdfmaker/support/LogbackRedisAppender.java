package net.rubyworks.pdfmaker.support;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.time.FastDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import lombok.Getter;
import lombok.Setter;

public class LogbackRedisAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private static final String KEY_DEFAULT = "logstash";

    @Getter @Setter private String uri;   // standalone uri syntax => redis :// [[username :] password@] host [:port][/database][?[timeout=timeout[d|h|m|s|ms|us|ns]]
    @Getter @Setter private String key = KEY_DEFAULT;
    @Getter @Setter private String type;
    @Getter @Setter private String hostname;
    @Getter @Setter private String slot;

    private RedisClient client;
    private StatefulRedisConnection<String, String> connection;
    private RedisAsyncCommands<String, String> commands;

    public LogbackRedisAppender() {
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {}
    }

    @Override
    protected void append(ILoggingEvent event) {
        try {
            commands.rpush(key, log(event));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final FastDateFormat DF = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
    private String log(ILoggingEvent event) {
        var map = new HashMap<String, Object>();

        map.put("type", type);
        map.put("hostname", hostname);
        map.put("slot", slot);
        map.put("message", event.getFormattedMessage());
        map.put("logger", event.getLoggerName());
        map.put("level", event.getLevel().toString());
        map.put("thread", event.getThreadName());
        map.put("@timestamp", DF.format(event.getTimeStamp()));

        Optional.ofNullable(event.getThrowableProxy())
          .ifPresent(proxy -> map.put("throwable", ThrowableProxyUtil.asString(proxy)));
        Optional.ofNullable(event.getMDCPropertyMap())
          .filter(properties -> !properties.isEmpty())
          .ifPresent(properties -> properties.entrySet().forEach(entry -> map.put(entry.getKey(), entry.getValue())));

        return jsonOf(map);
    }

    @Override
    public void start() {
        super.start();
        client = RedisClient.create(uri);
        connection = client.connect();
        commands = connection.async();
    }

    @Override
    public void stop() {
        super.stop();
        commands = null;
        Optional.ofNullable(connection).ifPresent(StatefulRedisConnection::close);
        Optional.ofNullable(client).ifPresent(RedisClient::shutdown);
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final String jsonOf(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            return null;
        }
    }
}
