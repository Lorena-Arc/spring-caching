package me.lorena.springcaching.service;

import jakarta.transaction.Transactional;
import me.lorena.springcaching.distributed.DistributedCaching;
import me.lorena.springcaching.dto.BookDTO;
import me.lorena.springcaching.mappers.BookMapper;
import me.lorena.springcaching.model.Book;
import me.lorena.springcaching.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@CacheConfig(cacheNames = "books")
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;

    private final BookRepository bookRepository;
    private final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);

    public BookServiceImpl(BookMapper bookMapper, BookRepository bookRepository) {
        this.bookMapper = bookMapper;
        this.bookRepository = bookRepository;
    }

    @Override
    @CacheEvict(key = "'allBooks'")
    public BookDTO addBook(BookDTO book) {
        log.info("adding book with id - {}", book.getId());
        return bookMapper.toBookDTO(bookRepository.save(bookMapper.toBook(book)));
    }


    @Transactional
    @Override
    @Caching(put = {
            @CachePut(key = "#book.id"),
    }, evict = {
            @CacheEvict(key = "'allBooks'")
    })
    public BookDTO updateBook(BookDTO book) {
        bookRepository.updateName(book.getId(), book.getName());
        log.info("book updated with new name");
        return book;
    }

    @Override
    @Cacheable(value = "books", key = "#id", sync = true)
    @DistributedCaching
    public BookDTO getBook(long id) {
        log.info("fetching book from db");
        Optional<Book> book = bookRepository.findById(id);
        if (book.isPresent()) {
            return bookMapper.toBookDTO(book.get());
        } else {
            return new BookDTO();
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "#id"),
            @CacheEvict(key = "'allBooks'")
    })
    public String deleteBook(long id) {
        bookRepository.deleteById(id);
        return "Book deleted";
    }

    @Override
    @Cacheable(value = "books", key = "'allBooks'")
    @DistributedCaching
    public List<BookDTO> getAllBooks() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
           log.error("Error sleeping");
        }
        return bookMapper.toListBookDTO(bookRepository.findAll());
    }
}
