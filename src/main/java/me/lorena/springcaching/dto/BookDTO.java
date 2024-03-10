package me.lorena.springcaching.dto;

public class BookDTO {
    private long id;

    private String name;
    private String category;
    private String author;
    private String publisher;
    private String edition;

    public BookDTO() {
    }

    public BookDTO(String name, String category, String author, String publisher, String edition) {
        this.name = name;
        this.category = category;
        this.author = author;
        this.publisher = publisher;
        this.edition = edition;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }
}
