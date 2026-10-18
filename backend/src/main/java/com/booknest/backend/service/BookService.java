package com.booknest.backend.service;

import com.booknest.backend.exception.BookNotFoundException;
import com.booknest.backend.exception.BookValidationException;
import com.booknest.backend.exception.DuplicateBookException;
import com.booknest.backend.model.Book;
import com.booknest.backend.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(int id) {
        return bookRepository.findById(id);
    }

    public Book addBook(Book book) {
        if (book == null) {
            throw new BookValidationException("Book details must not be null.");
        }

        // Validate Book ID
        if (book.getId() == null) {
            throw new BookValidationException("Book ID is required.");
        }
        if (book.getId() <= 0) {
            throw new BookValidationException("Book ID must be greater than zero.");
        }
        if (bookRepository.existsById(book.getId())) {
            throw new DuplicateBookException("A book with ID " + book.getId() + " already exists.");
        }

        // Validate Title
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new BookValidationException("Book title cannot be blank.");
        }

        // Validate Author
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new BookValidationException("Book author cannot be blank.");
        }

        // Validate Total Copies
        if (book.getTotalCopies() == null || book.getTotalCopies() <= 0) {
            throw new BookValidationException("Total copies must be greater than zero.");
        }

        // Validate Available Copies
        if (book.getAvailableCopies() == null) {
            book.setAvailableCopies(book.getTotalCopies());
        } else {
            if (book.getAvailableCopies() <= 0) {
                throw new BookValidationException("Available copies must be greater than zero.");
            }
            if (book.getAvailableCopies() > book.getTotalCopies()) {
                throw new BookValidationException("Available copies cannot exceed total copies.");
            }
        }

        book.setTitle(book.getTitle().trim());
        book.setAuthor(book.getAuthor().trim());

        bookRepository.save(book);
        return book;
    }

    public Book updateBook(int id, Book book) {
        if (book == null) {
            throw new BookValidationException("Book details must not be null.");
        }

        if (id <= 0) {
            throw new BookValidationException("Book ID must be greater than zero.");
        }

        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book with ID " + id + " not found.");
        }

        // Validate Title
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new BookValidationException("Book title cannot be blank.");
        }

        // Validate Author
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new BookValidationException("Book author cannot be blank.");
        }

        // Validate Total Copies
        if (book.getTotalCopies() == null || book.getTotalCopies() <= 0) {
            throw new BookValidationException("Total copies must be greater than zero.");
        }

        // Validate Available Copies
        if (book.getAvailableCopies() == null) {
            book.setAvailableCopies(book.getTotalCopies());
        } else {
            if (book.getAvailableCopies() < 0) {
                throw new BookValidationException("Available copies cannot be negative.");
            }
            if (book.getAvailableCopies() > book.getTotalCopies()) {
                throw new BookValidationException("Available copies cannot exceed total copies.");
            }
        }

        book.setId(id);
        book.setTitle(book.getTitle().trim());
        book.setAuthor(book.getAuthor().trim());

        bookRepository.update(book);
        return book;
    }

    public void deleteBook(int id) {
        if (id <= 0) {
            throw new BookValidationException("Book ID must be greater than zero.");
        }

        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book with ID " + id + " not found.");
        }

        bookRepository.deleteById(id);
    }
}
