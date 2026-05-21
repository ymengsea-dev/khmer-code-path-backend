package com.mengsea.khmercodepath.api.ai.service;

public interface AiRagService {

    void ingestClasspathDocuments();

    String query(String question);

    String query(String question, int topK);
}
