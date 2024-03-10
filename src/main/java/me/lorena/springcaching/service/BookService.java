package me.lorena.springcaching.service;

import me.lorena.springcaching.dto.BookDTO;

public interface BookService {
    BookDTO addBook(BookDTO book);

    BookDTO updateBook(BookDTO book);

    BookDTO getBook(long id);

    String deleteBook(long id);
}
