package com.booknest.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CorsIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("Preflight OPTIONS request from http://localhost:5173 returns 200 with CORS headers")
    void testCorsPreflight_AllowedOrigin_5173() throws Exception {
        mockMvc.perform(options("/api/books")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    @Test
    @DisplayName("Preflight OPTIONS request from http://localhost:5175 returns 200 with CORS headers")
    void testCorsPreflight_AllowedOrigin_5175() throws Exception {
        mockMvc.perform(options("/api/books")
                        .header("Origin", "http://localhost:5175")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5175"))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    @Test
    @DisplayName("GET /api/books from http://localhost:5173 includes Access-Control-Allow-Origin header")
    void testCorsGet_AllowedOrigin_5173() throws Exception {
        mockMvc.perform(get("/api/books")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    @DisplayName("GET /api/books from http://localhost:5175 includes Access-Control-Allow-Origin header")
    void testCorsGet_AllowedOrigin_5175() throws Exception {
        mockMvc.perform(get("/api/books")
                        .header("Origin", "http://localhost:5175"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5175"));
    }

    @Test
    @DisplayName("POST /api/books from http://localhost:5173 is allowed through CORS filter")
    void testCorsPost_AllowedOrigin_5173() throws Exception {
        String jsonPayload = """
                {
                    "id": -1,
                    "title": "",
                    "author": "CORS Tester",
                    "totalCopies": 2,
                    "availableCopies": 2
                }
                """;

        mockMvc.perform(post("/api/books")
                        .header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    @DisplayName("POST /api/books from http://localhost:5175 is allowed through CORS filter")
    void testCorsPost_AllowedOrigin_5175() throws Exception {
        String jsonPayload = """
                {
                    "id": -1,
                    "title": "",
                    "author": "CORS Tester",
                    "totalCopies": 2,
                    "availableCopies": 2
                }
                """;

        mockMvc.perform(post("/api/books")
                        .header("Origin", "http://localhost:5175")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5175"));
    }

    @Test
    @DisplayName("OPTIONS from unapproved origin returns 403 Forbidden")
    void testCorsPreflight_DisallowedOrigin() throws Exception {
        mockMvc.perform(options("/api/books")
                        .header("Origin", "http://untrusted-website.com")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isForbidden());
    }
}
