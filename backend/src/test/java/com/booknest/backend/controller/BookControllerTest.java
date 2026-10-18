package com.booknest.backend.controller;

import com.booknest.backend.exception.BookNotFoundException;
import com.booknest.backend.exception.BookValidationException;
import com.booknest.backend.exception.DuplicateBookException;
import com.booknest.backend.exception.GlobalExceptionHandler;
import com.booknest.backend.model.Book;
import com.booknest.backend.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/books returns 200 OK with list of books")
    void testGetAllBooks_Success() throws Exception {
        List<Book> books = List.of(
                new Book(1, "Clean Code", "Robert C. Martin", 5, 5),
                new Book(2, "The Pragmatic Programmer", "Andy Hunt", 3, 2)
        );
        when(bookService.getAllBooks()).thenReturn(books);

        mockMvc.perform(get("/api/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Clean Code"))
                .andExpect(jsonPath("$[0].author").value("Robert C. Martin"))
                .andExpect(jsonPath("$[0].totalCopies").value(5))
                .andExpect(jsonPath("$[0].availableCopies").value(5))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("The Pragmatic Programmer"));
    }

    @Test
    @DisplayName("GET /api/books/{id} returns 200 OK when found")
    void testGetBookById_Found() throws Exception {
        Book book = new Book(1, "Clean Code", "Robert C. Martin", 5, 5);
        when(bookService.getBookById(1)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @DisplayName("GET /api/books/{id} returns 404 NOT FOUND when missing")
    void testGetBookById_NotFound() throws Exception {
        when(bookService.getBookById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/books/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/books returns 201 CREATED for valid book")
    void testAddBook_Success() throws Exception {
        Book book = new Book(1, "Clean Code", "Robert C. Martin", 5, 5);
        when(bookService.addBook(any(Book.class))).thenReturn(book);

        String jsonPayload = """
                {
                    "id": 1,
                    "title": "Clean Code",
                    "author": "Robert C. Martin",
                    "totalCopies": 5,
                    "availableCopies": 5
                }
                """;

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert C. Martin"))
                .andExpect(jsonPath("$.totalCopies").value(5))
                .andExpect(jsonPath("$.availableCopies").value(5));
    }

    @Test
    @DisplayName("POST /api/books returns 400 BAD REQUEST when validation fails")
    void testAddBook_ValidationFailure() throws Exception {
        when(bookService.addBook(any(Book.class)))
                .thenThrow(new BookValidationException("Book title cannot be blank."));

        String jsonPayload = """
                {
                    "id": 1,
                    "title": "",
                    "author": "Robert C. Martin",
                    "totalCopies": 5,
                    "availableCopies": 5
                }
                """;

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Book title cannot be blank."))
                .andExpect(jsonPath("$.path").value("/api/books"));
    }

    @Test
    @DisplayName("POST /api/books returns 409 CONFLICT when book ID already exists")
    void testAddBook_DuplicateId() throws Exception {
        when(bookService.addBook(any(Book.class)))
                .thenThrow(new DuplicateBookException("A book with ID 1 already exists."));

        String jsonPayload = """
                {
                    "id": 1,
                    "title": "Clean Code",
                    "author": "Robert C. Martin",
                    "totalCopies": 5,
                    "availableCopies": 5
                }
                """;

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("A book with ID 1 already exists."))
                .andExpect(jsonPath("$.path").value("/api/books"));
    }

    @Test
    @DisplayName("PUT /api/books/{id} returns 200 OK for valid update")
    void testUpdateBook_Success() throws Exception {
        Book updatedBook = new Book(1, "Clean Code Updated", "Robert C. Martin", 10, 8);
        when(bookService.updateBook(eq(1), any(Book.class))).thenReturn(updatedBook);

        String jsonPayload = """
                {
                    "title": "Clean Code Updated",
                    "author": "Robert C. Martin",
                    "totalCopies": 10,
                    "availableCopies": 8
                }
                """;

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code Updated"))
                .andExpect(jsonPath("$.author").value("Robert C. Martin"))
                .andExpect(jsonPath("$.totalCopies").value(10))
                .andExpect(jsonPath("$.availableCopies").value(8));
    }

    @Test
    @DisplayName("PUT /api/books/{id} returns 404 NOT FOUND when book does not exist")
    void testUpdateBook_NotFound() throws Exception {
        when(bookService.updateBook(eq(99), any(Book.class)))
                .thenThrow(new BookNotFoundException("Book with ID 99 not found."));

        String jsonPayload = """
                {
                    "title": "Missing Book",
                    "author": "Unknown",
                    "totalCopies": 5,
                    "availableCopies": 5
                }
                """;

        mockMvc.perform(put("/api/books/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Book with ID 99 not found."))
                .andExpect(jsonPath("$.path").value("/api/books/99"));
    }

    @Test
    @DisplayName("PUT /api/books/{id} returns 400 BAD REQUEST when validation fails")
    void testUpdateBook_ValidationFailure() throws Exception {
        when(bookService.updateBook(eq(1), any(Book.class)))
                .thenThrow(new BookValidationException("Available copies cannot exceed total copies."));

        String jsonPayload = """
                {
                    "title": "Clean Code",
                    "author": "Robert C. Martin",
                    "totalCopies": 5,
                    "availableCopies": 10
                }
                """;

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Available copies cannot exceed total copies."))
                .andExpect(jsonPath("$.path").value("/api/books/1"));
    }

    @Test
    @DisplayName("DELETE /api/books/{id} returns 204 NO CONTENT when book deleted")
    void testDeleteBook_Success() throws Exception {
        doNothing().when(bookService).deleteBook(1);

        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/books/{id} returns 404 NOT FOUND when book does not exist")
    void testDeleteBook_NotFound() throws Exception {
        doThrow(new BookNotFoundException("Book with ID 99 not found."))
                .when(bookService).deleteBook(99);

        mockMvc.perform(delete("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Book with ID 99 not found."))
                .andExpect(jsonPath("$.path").value("/api/books/99"));
    }
}
