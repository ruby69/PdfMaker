package net.rubyworks.pdfmaker.service;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.pdf.BaseFont;
import com.samskivert.mustache.Template;

import lombok.Cleanup;
import lombok.SneakyThrows;

@Component
public class PdfGenerator {
    private static final String FONT_WATERMARK = "fonts/MALGUN.TTF";
    private static final String FONT_CONTENTS1 = "fonts/NotoSansKR-Regular.otf";
    private static final String FONT_CONTENTS2 = "fonts/MALGUN.TTF";
    private static final String FONT_CONTENTS3 = "fonts/H2GTRE.TTF";
    private static final String[] FONTs = {FONT_CONTENTS1, FONT_CONTENTS2, FONT_CONTENTS3, FONT_WATERMARK};

    @SneakyThrows
    private ITextRenderer renderer(String... fonts) {
        var renderer = new ITextRenderer();
        var fontResolver = renderer.getFontResolver();
        for (var font : fonts) {
            fontResolver.addFont(font, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        }
        return renderer;
    }

    @SneakyThrows
    private byte[] createPDF(ITextRenderer renderer, String documentString) {
        renderer.setDocumentFromString(documentString);
        renderer.layout();

        @Cleanup var out = new ByteArrayOutputStream();
        renderer.createPDF(out, true);

        return out.toByteArray();
    }

    @SneakyThrows
    public byte[] generate(Template template, Template watermark, Object context) {
        var documentString = template.execute(context);
        var documentBytes = createPDF(renderer(FONTs), documentString);
        @Cleanup var document = Loader.loadPDF(documentBytes);

        var overlays = new HashMap<Integer, PDDocument>();
        if (watermark != null) {
            var watermarkString = watermark.execute(context);
            var watermarkBytes = createPDF(renderer(FONT_WATERMARK), watermarkString);
            var watermarkDoc = Loader.loadPDF(watermarkBytes);
            for(var i = 0; i<document.getNumberOfPages(); i++){
                overlays.put(i+1, watermarkDoc);
            }

            @Cleanup var overlay = new Overlay();
            overlay.setInputPDF(document);
            overlay.setOverlayPosition(Overlay.Position.BACKGROUND);
            overlay.overlayDocuments(overlays);
        }

        @Cleanup var output = new ByteArrayOutputStream();
        document.save(output);

        if (!overlays.isEmpty()) {
            for (var watermarkDoc : overlays.values()) {
                watermarkDoc.close();
            }
        }

        return output.toByteArray();
    }
}
