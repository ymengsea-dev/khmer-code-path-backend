package com.mengsea.khmercodepath.api.ai.service;

import com.mengsea.khmercodepath.api.ai.gateway.LlmGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRagServiceImpl implements AiRagService {

    private final LlmGateway llmGateway;
    private final VectorStore vectorStore;

    @Override
    public void ingestClasspathDocuments() {
        try {
            var resolver = new PathMatchingResourcePatternResolver();
            var splitter = new TokenTextSplitter();

            Resource[] pdfs = resolver.getResources("classpath:documents/*.pdf");
            for (Resource pdf : pdfs) {
                var reader = new PagePdfDocumentReader(pdf);
                List<Document> chunks = splitter.apply(reader.get());
                vectorStore.add(chunks);
                log.info("Ingested PDF {} ({} chunks)", pdf.getFilename(), chunks.size());
            }

            Resource[] txts = resolver.getResources("classpath:documents/*.txt");
            for (Resource txt : txts) {
                String content = new String(txt.getInputStream().readAllBytes());
                List<Document> chunks = splitter.apply(List.of(new Document(content)));
                vectorStore.add(chunks);
                log.info("Ingested text {} ({} chunks)", txt.getFilename(), chunks.size());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to ingest classpath documents for RAG", ex);
        }
    }

    @Override
    public String query(String question) {
        return llmGateway.completeRagQuery(question);
    }

    @Override
    public String query(String question, int topK) {
        return llmGateway.completeRagQuery(question, topK);
    }
}
