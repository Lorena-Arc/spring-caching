package me.lorena.springcaching.mappers;

import me.lorena.springcaching.dto.BookDTO;
import me.lorena.springcaching.model.Book;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {
     BookDTO toBookDTO(Book book);
     Book toBook(BookDTO bookDTO);
     List<BookDTO> toListBookDTO(List<Book> books);
}
