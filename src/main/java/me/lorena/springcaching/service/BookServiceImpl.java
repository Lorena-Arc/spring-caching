package me.lorena.springcaching.service;

import jakarta.transaction.Transactional;
import me.lorena.springcaching.dto.BookDTO;
import me.lorena.springcaching.mappers.BookMapper;
import me.lorena.springcaching.model.Book;
import me.lorena.springcaching.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;

    private final BookRepository bookRepository;
    private final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);

    public BookServiceImpl(BookMapper bookMapper, BookRepository bookRepository) {
        this.bookMapper = bookMapper;
        this.bookRepository = bookRepository;
    }

    @Override
    public BookDTO addBook(BookDTO book) {
        log.info("adding book with id - {}", book.getId());
        return bookMapper.toBookResponse(bookRepository.save(bookMapper.toBook(book)));
    }


    @Transactional
    @Override
    @CachePut(cacheNames = "books", key = "#book.id")
    public BookDTO updateBook(BookDTO book) {
        bookRepository.updateName(book.getId(), book.getName());
        log.info("book updated with new name");
        return book;
    }

    @Override
    @Cacheable(cacheNames = "books", key = "#id", sync = true)
    public BookDTO getBook(long id) {
        log.info("fetching book from db");
        Optional<Book> book = bookRepository.findById(id);
        if (book.isPresent()) {
            return bookMapper.toBookResponse(book.get());
        } else {
            return new BookDTO();
        }
    }

    @Override
    @CacheEvict(cacheNames = "books", key = "#id")
    public String deleteBook(long id) {
        bookRepository.deleteById(id);
        return "Book deleted";
    }
}
