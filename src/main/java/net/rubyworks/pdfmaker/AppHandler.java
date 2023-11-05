package net.rubyworks.pdfmaker;

import static net.rubyworks.pdfmaker.support.Responses.response;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.RequiredArgsConstructor;
import net.rubyworks.pdfmaker.service.PdfService;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class AppHandler {
    private final PdfService pdfService;

    public Mono<ServerResponse> preview(ServerRequest request) {
        return request.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .flatMap(map -> response(pdfService.make(map),
                        MediaType.APPLICATION_PDF_VALUE,
                        System.currentTimeMillis() + ".pdf")
                );
    }
}
