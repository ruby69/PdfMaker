package net.rubyworks.pdfmaker.service;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PdfService {
    private final PdfGenerator pdfGenerator;
    private final Mustache.Compiler mustacheCompiler;

    private Template templates(Object obj) {
        return obj instanceof String s ? mustacheCompiler.defaultValue("").compile(s) : null;
    }

    public byte[] make(Map<String, Object> map) {
        var template = templates(map.get("template"));
        var watermark = templates(map.get("watermark"));
        var bytes = pdfGenerator.generate(template, watermark, map.get("context"));
        return bytes;
    }

}
