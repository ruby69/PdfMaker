package net.rubyworks.pdfmaker.support;

import static org.springframework.web.reactive.function.server.ServerResponse.noContent;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import java.io.InputStream;
import java.net.URLEncoder;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.SerializationUtils;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.SneakyThrows;
import reactor.core.publisher.Mono;

public final class Responses {
    private Responses() {}

    @SneakyThrows
    public static String encode(String name) {
        return URLEncoder.encode(name, "utf-8");
    }

    @SneakyThrows
    public static String sha256(byte[] bytes) {
        return DigestUtils.sha256Hex(bytes);
    }

    public static Mono<ServerResponse> empty() {
        return noContent().build();
    }

    public static Mono<ServerResponse> json(Object body) {
        return ok().contentType(MediaType.APPLICATION_JSON).bodyValue(body);
    }

    public static Mono<ServerResponse> response(byte[] bytes, String contentType, String name) {
        if (bytes.length < 1) {
            return empty();
        }
        return response(contentType, name).bodyValue(bytes);
    }

    public static Mono<ServerResponse> response(InputStream inputStream, String contentType, String name) {
        return response(contentType, name).bodyValue(new InputStreamResource(inputStream));
    }

    public static ServerResponse.BodyBuilder response(String contentType, String name) {
        return ok()
                .cacheControl(CacheControl.noCache())
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=utf-8''" + encode(name));
    }

    public static Mono<ServerResponse> response(Object body) {
        byte[] bodyValue = SerializationUtils.serialize(body);
        if (bodyValue == null) {
            return empty();
        }

        return ok()
                .cacheControl(CacheControl.noCache())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .bodyValue(bodyValue);
    }
}
