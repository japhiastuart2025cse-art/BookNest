package com.booknest.backend.controller;

import com.booknest.backend.model.Book;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BookUpdateIntegrationTest {

    private static final int TEST_BOOK_ID = 8888;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        // Clean up before each test to guarantee fresh state
        jdbcTemplate.update("DELETE FROM books WHERE book_id = ?", TEST_BOOK_ID);
        // Insert a known baseline book in MySQL
        jdbcTemplate.update(
                "INSERT INTO books (book_id, title, author, total_copies, available_copies) VALUES (?, ?, ?, ?, ?)",
                TEST_BOOK_ID, "Initial Title", "Initial Author", 5, 3
        );
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM books WHERE book_id = ?", TEST_BOOK_ID);
    }

    @Test
    @DisplayName("PUT /api/books/{id} updates record and persists in MySQL database")
    void testUpdateBook_PersistsInMySQL() throws Exception {
        String updatePayload = """
                {
                    "title": "Updated Modern Title",
                    "author": "Updated Author Name",
                    "totalCopies": 10,
                    "availableCopies": 7
                }
                """;

        mockMvc.perform(put("/api/books/" + TEST_BOOK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_BOOK_ID))
                .andExpect(jsonPath("$.title").value("Updated Modern Title"))
                .andExpect(jsonPath("$.author").value("Updated Author Name"))
                .andExpect(jsonPath("$.totalCopies").value(10))
                .andExpect(jsonPath("$.availableCopies").value(7));

        // Directly query MySQL database to verify persistence
        String sql = "SELECT book_id, title, author, total_copies, available_copies FROM books WHERE book_id = ?";
        Book dbBook = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Book(
                rs.getInt("book_id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getInt("total_copies"),
                rs.getInt("available_copies")
        ), TEST_BOOK_ID);

        assertEquals("Updated Modern Title", dbBook.getTitle());
        assertEquals("Updated Author Name", dbBook.getAuthor());
        assertEquals(10, dbBook.getTotalCopies());
        assertEquals(7, dbBook.getAvailableCopies());
    }

    @Test
    @DisplayName("PUT /api/books/{id} returns 404 when book does not exist in MySQL")
    void testUpdateBook_NotFoundInMySQL() throws Exception {
        String updatePayload = """
                {
                    "title": "Non-existent Book",
                    "author": "Ghost Writer",
                    "totalCopies": 5,
                    "availableCopies": 5
                }
                """;

        mockMvc.perform(put("/api/books/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Book with ID 999999 not found."));
    }

    @Test
    @DisplayName("PUT /api/books/{id} returns 400 when available copies exceeds total copies")
    void testUpdateBook_ValidationExceedsTotalCopies() throws Exception {
        String updatePayload = """
                {
                    "title": "Invalid Book",
                    "author": "Tester",
                    "totalCopies": 5,
                    "availableCopies": 10
                }
                """;

        mockMvc.perform(put("/api/books/" + TEST_BOOK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Available copies cannot exceed total copies."));
    }

    @Test
    @DisplayName("PUT /api/books/{id} returns 400 when available copies is negative")
    void testUpdateBook_ValidationNegativeCopies() throws Exception {
        String updatePayload = """
                {
                    "title": "Invalid Book",
                    "author": "Tester",
                    "totalCopies": 5,
                    "availableCopies": -1
                }
                """;

        mockMvc.perform(put("/api/books/" + TEST_BOOK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Available copies cannot be negative."));
    }
}
