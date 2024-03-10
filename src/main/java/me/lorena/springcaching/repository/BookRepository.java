package me.lorena.springcaching.repository;

import me.lorena.springcaching.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Long> {
    @Modifying
    @Query("update Book u set u.name=?2 where u.id=?1")
    int updateName(long id, String name);
}
