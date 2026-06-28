package com.mengsea.khmercodepath.api;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class KhmerCodePathApplicationTests {

    @MockBean
    VectorStore vectorStore;

    @Test
    void contextLoads() {
    }
}
