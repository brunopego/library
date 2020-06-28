package com.brunopego.library.service;

import com.brunopego.library.exception.BusinessException;
import com.brunopego.library.model.entity.Book;
import com.brunopego.library.model.repository.BookRepository;
import com.brunopego.library.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    @InjectMocks
    BookServiceImpl service;

    @Mock
    BookRepository repository;

    private Book createNewBook() {
        return Book.builder().author("Bruno").title("Um Livro").isbn("002").build();
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void shouldSaveBook() {
        // cenário
        Book book = createNewBook();
        Book savedBook = Book.builder().id(1L).author("Bruno").title("Um Livro").isbn("002").build();
        Mockito.when(repository.existsByIsbn((Mockito.anyString()))).thenReturn(false);
        Mockito.when(repository.save(book)).thenReturn(savedBook);

        // execução
        savedBook = service.save(book);

        // verificação
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getAuthor()).isEqualTo("Bruno");
        assertThat(savedBook.getTitle()).isEqualTo("Um Livro");
        assertThat(savedBook.getIsbn()).isEqualTo("002");

    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar salvar um livro com isbn já cadastrado")
    public void shouldNotSaveBookIfDuplicatedIsbn() {
        // cenário
        Book book = createNewBook();
        Mockito.when(repository.existsByIsbn((Mockito.anyString()))).thenReturn(true);

        // execução
        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        // verificações
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado");

        Mockito.verify(repository, Mockito.never()).save(book);

    }

}
