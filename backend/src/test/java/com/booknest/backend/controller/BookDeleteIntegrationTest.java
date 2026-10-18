package com.booknest.backend.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BookDeleteIntegrationTest {

    private static final int TEST_BOOK_ID = 7777;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        jdbcTemplate.update("DELETE FROM books WHERE book_id = ?", TEST_BOOK_ID);
        jdbcTemplate.update(
                "INSERT INTO books (book_id, title, author, total_copies, available_copies) VALUES (?, ?, ?, ?, ?)",
                TEST_BOOK_ID, "Book To Delete", "Test Author", 3, 3
        );
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM books WHERE book_id = ?", TEST_BOOK_ID);
    }

    @Test
    @DisplayName("DELETE /api/books/{id} deletes book and removes record from MySQL database")
    void testDeleteBook_RemovesFromMySQL() throws Exception {
        // Confirm book exists in MySQL prior to deletion
        Integer initialCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM books WHERE book_id = ?",
                Integer.class,
                TEST_BOOK_ID
        );
        assertEquals(1, initialCount);

        // Perform DELETE request
        mockMvc.perform(delete("/api/books/" + TEST_BOOK_ID))
                .andExpect(status().isNoContent());

        // Verify record is completely removed from MySQL
        Integer finalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM books WHERE book_id = ?",
                Integer.class,
                TEST_BOOK_ID
        );
        assertEquals(0, finalCount);
    }

    @Test
    @DisplayName("DELETE /api/books/{id} returns 404 when book does not exist in MySQL")
    void testDeleteBook_NotFoundInMySQL() throws Exception {
        mockMvc.perform(delete("/api/books/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Book with ID 999999 not found."));
    }
}
