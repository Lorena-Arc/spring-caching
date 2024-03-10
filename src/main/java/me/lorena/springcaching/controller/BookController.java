package me.lorena.springcaching.controller;

import me.lorena.springcaching.dto.BookDTO;
import me.lorena.springcaching.service.BookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("/book")
    public BookDTO addBook(@RequestBody BookDTO book){
        return bookService.addBook(book);
    }

    @PutMapping("/book")
    public BookDTO updateBook(@RequestBody BookDTO book) {
        return bookService.updateBook(book);
    }

    @GetMapping("/book/{id}")
    public BookDTO getBook(@PathVariable long id){
        return bookService.getBook(id);
    }

    @DeleteMapping("/book/{id}")
    public String deleteBook(@PathVariable long id){
        return bookService.deleteBook(id);
    }

    @GetMapping("/books")
    public List<BookDTO> getAllBooks() {
        return bookService.getAllBooks();
    }
}
