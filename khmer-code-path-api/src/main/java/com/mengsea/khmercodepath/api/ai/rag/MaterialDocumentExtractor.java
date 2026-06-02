package com.mengsea.khmercodepath.api.ai.rag;

import com.mengsea.khmercodepath.api.storage.MaterialUploadValidator;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MaterialDocumentExtractor {

    public List<Document> extract(InputStream inputStream, String fileName) {
        if (!MaterialUploadValidator.isAllowedExtension(fileName)) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        String ext = MaterialUploadValidator.extension(fileName);
        return switch (ext) {
            case "pdf" -> extractPdf(inputStream, fileName);
            case "docx", "doc" -> extractDocx(inputStream, fileName);
            case "pptx", "ppt" -> extractPptx(inputStream, fileName);
            default -> throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        };
    }

    private List<Document> extractPdf(InputStream inputStream, String fileName) {
        try {
            var reader = new PagePdfDocumentReader(new InputStreamResource(inputStream) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            });
            return reader.get();
        } catch (Exception ex) {
            log.warn("PDF extraction failed for {}", fileName, ex);
            throw new BusinessException(ExceptionCode.MATERIAL_RAG_INDEX_FAILED);
        }
    }

    private List<Document> extractDocx(InputStream inputStream, String fileName) {
        try (XWPFDocument doc = new XWPFDocument(inputStream)) {
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                text.append(paragraph.getText()).append('\n');
            }
            if (text.isEmpty()) {
                return List.of(new Document("(empty document)", java.util.Map.of("fileName", fileName)));
            }
            return List.of(new Document(text.toString().trim(), java.util.Map.of("fileName", fileName)));
        } catch (Exception ex) {
            log.warn("DOCX extraction failed for {}", fileName, ex);
            throw new BusinessException(ExceptionCode.MATERIAL_RAG_INDEX_FAILED);
        }
    }

    private List<Document> extractPptx(InputStream inputStream, String fileName) {
        try (XMLSlideShow ppt = new XMLSlideShow(inputStream)) {
            List<String> slides = new ArrayList<>();
            int index = 0;
            for (XSLFSlide slide : ppt.getSlides()) {
                StringBuilder slideText = new StringBuilder();
                for (var shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        slideText.append(textShape.getText()).append(' ');
                    }
                }
                String t = slideText.toString().trim();
                if (!t.isBlank()) {
                    slides.add("Slide " + (++index) + ": " + t);
                }
            }
            if (slides.isEmpty()) {
                return List.of(new Document("(empty presentation)", java.util.Map.of("fileName", fileName)));
            }
            return slides.stream()
                    .map(s -> new Document(s, java.util.Map.of("fileName", fileName)))
                    .toList();
        } catch (Exception ex) {
            log.warn("PPTX extraction failed for {}", fileName, ex);
            throw new BusinessException(ExceptionCode.MATERIAL_RAG_INDEX_FAILED);
        }
    }
}
