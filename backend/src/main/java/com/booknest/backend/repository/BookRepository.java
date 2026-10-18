package com.booknest.backend.repository;

import com.booknest.backend.model.Book;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Book> bookRowMapper = (rs, rowNum) -> new Book(
            rs.getInt("book_id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getInt("total_copies"),
            rs.getInt("available_copies")
    );

    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Book> findAll() {
        String sql = "SELECT book_id, title, author, total_copies, available_copies FROM books ORDER BY book_id ASC";
        return jdbcTemplate.query(sql, bookRowMapper);
    }

    public Optional<Book> findById(int id) {
        String sql = "SELECT book_id, title, author, total_copies, available_copies FROM books WHERE book_id = ?";
        List<Book> books = jdbcTemplate.query(sql, bookRowMapper, id);
        return books.stream().findFirst();
    }

    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM books WHERE book_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public int save(Book book) {
        String sql = "INSERT INTO books (book_id, title, author, total_copies, available_copies) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(
                sql,
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getTotalCopies(),
                book.getAvailableCopies()
        );
    }

    public int update(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, total_copies = ?, available_copies = ? WHERE book_id = ?";
        return jdbcTemplate.update(
                sql,
                book.getTitle(),
                book.getAuthor(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getId()
        );
    }

    public int deleteById(int id) {
        String sql = "DELETE FROM books WHERE book_id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int decrementAvailableCopies(int id) {
        String sql = "UPDATE books SET available_copies = available_copies - 1 WHERE book_id = ? AND available_copies > 0";
        return jdbcTemplate.update(sql, id);
    }

    public int incrementAvailableCopies(int id) {
        String sql = "UPDATE books SET available_copies = available_copies + 1 WHERE book_id = ? AND available_copies < total_copies";
        return jdbcTemplate.update(sql, id);
    }
}

