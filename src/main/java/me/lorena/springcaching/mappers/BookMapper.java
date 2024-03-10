package me.lorena.springcaching.mappers;

import me.lorena.springcaching.dto.BookDTO;
import me.lorena.springcaching.model.Book;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookMapper {
     BookDTO toBookResponse(Book book);
     Book toBook(BookDTO bookDTO);
}
