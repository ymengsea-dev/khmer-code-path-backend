package com.mengsea.khmercodepath.api.llm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RAGService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public void ingestDocument() throws IOException {
        var resolver = new PathMatchingResourcePatternResolver();
        var splitter = new TokenTextSplitter();

        Resource[] pdfs = resolver.getResources("classpath:documents/*.pdf");
        for (Resource pdf : pdfs) {
            var reader = new PagePdfDocumentReader(pdf);
            List<Document> chunks = splitter.apply(reader.get());
            vectorStore.add(chunks);
            System.out.println("Ingested: " + pdf.getFilename() + " → " + chunks.size() + " chunks");
        }

        Resource[] txts = resolver.getResources("classpath:documents/*.txt");
        for (Resource txt : txts) {
            String content = new String(txt.getInputStream().readAllBytes());
            List<Document> docs = List.of(new Document(content));
            List<Document> chunks = splitter.apply(docs);
            vectorStore.add(chunks);
            System.out.println("Ingested: " + txt.getFilename() + " → " + chunks.size() + " chunks");
        }
    }

    public String query(String question) {
        return chatClient.prompt()
                .user(question)
                .advisors(
                        QuestionAnswerAdvisor
                                .builder(vectorStore)
                                .build()
                )
                .call()
                .content();
    }

    public String queryWithOptions(String question, int topK) {
        var searchRequest = SearchRequest
                .builder()
                .query(question)
                .topK(topK)
                .build();

        return chatClient.prompt()
                .user(question)
                .advisors(
                        QuestionAnswerAdvisor
                                .builder(vectorStore)
                                .searchRequest(searchRequest)
                                .build()
                )
                .call()
                .content();
    }
}
