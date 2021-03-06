package com.brunopego.library.model.repository;

import com.brunopego.library.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    private Book createNewBook() {
        return Book.builder().author("Bruno").title("Um Livro").isbn("123").build();
    }

    @Test
    @DisplayName("Deve retornar verdadeiro quando já existir um livro com isbn informado")
    public void shouldReturnTrueIfBookWithIsbnAlreadyExists() {
        // cenário
        Book book = createNewBook();
        String isbn = book.getIsbn();
        entityManager.persist(book);

        // execução
        boolean exists = repository.existsByIsbn(isbn);

        // verificação
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não existir um livro com isbn informado")
    public void shouldReturnFalseIfBookWithIsbnDoesNotExist() {
        // cenário
        String isbn = "3242";

        // execução
        boolean exists = repository.existsByIsbn(isbn);

        // verificação
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter livro por id")
    public void shoudFindBookById() {
        // cenário
        Book book = createNewBook();
        entityManager.persist(book);

        // execução
        Optional<Book> bookFound = repository.findById(book.getId());

        // verificação
        assertThat(bookFound.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void shouldSaveBook() {
        // cenário
        Book book = createNewBook();
        
        // execução
        Book savedBook = repository.save(book);

        // verificação
        assertThat(savedBook.getId()).isNotNull();

    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void shouldDeleteBook() {
        // cenário
        Book book = createNewBook();
        entityManager.persist(book);

        // execução
        //Optional<Book> bookFound = repository.findById(book.getId());
        Book bookFound = entityManager.find(Book.class, book.getId());
        repository.delete(bookFound);

        Book deletedBook = entityManager.find(Book.class, book.getId());

        // verificação
        assertThat(deletedBook).isNull();
    }

}
