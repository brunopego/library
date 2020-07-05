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
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

        verify(repository, Mockito.never()).save(book);

    }

    @Test
    @DisplayName("Deve obter um livro por id")
    public void shouldGetBookById() {
        // cenário
        Long id = 1L;
        Book book = createNewBook();
        book.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

        // execução
        Optional<Book> bookFound = service.getById(id);

        // verificações
        assertThat(bookFound.isPresent()).isTrue();
        assertThat(bookFound.get().getId()).isEqualTo(book.getId());
        assertThat(bookFound.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(bookFound.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(bookFound.get().getIsbn()).isEqualTo(book.getIsbn());

    }

    @Test
    @DisplayName("Deve retornar vazion ao tentat obter um livro por id que não exist")
    public void shouldReturnEmptyIfBookDoesNotExist() {
        // cenário
        Long id = 1L;

        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        // execução
        Optional<Book> book = service.getById(id);

        // verificações
        assertThat(book.isPresent()).isFalse();

    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void shouldDeleteBook() {
        // cenário
        Long id = 1L;
        Book book = createNewBook();
        book.setId(id);

        // execução
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));

        // verificação
        verify(repository, Mockito.times(1)).delete(book);

    }

    @Test
    @DisplayName("Deve lançar erro ao tentar deletar livro inexistente")
    public void shouldNotDeleteInexistentBook() {
        // cenário
        Book book = new Book();

        // execução
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));

        // verificação
        verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar atualizar livro inexistente")
    public void shouldNotUpdateInexistentBook() {
        // cenário
        Book book = new Book();

        // execução
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));

        // verificação
        verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void shouldUpdateBook() {
        // cenário
        Long id = 1L;

        Book bookToUpdate = Book.builder().id(id).build();

        Book updatedBook = createNewBook();
        updatedBook.setId(id);

        Mockito.when(repository.save(bookToUpdate)).thenReturn(updatedBook);

        // execução
        Book book  = service.update(bookToUpdate);

        // verificação
        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());

    }

    @Test
    @DisplayName("Deve filtrar os livros pelas propriedades")
    public void shouldFilterBooksByProperties() {
        // cenário
        Book book = createNewBook();
        book.setId(1L);

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Book> list = Arrays.asList(book);
        Page<Book> page = new PageImpl<Book>(list, pageRequest, 1);
        Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        // execução
        Page<Book> result = service.find(book, pageRequest);

        // verificação
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(list);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);

    }

    @Test
    @DisplayName("Deve obter um livro pelo isbn")
    public void shoulGetBookByIsbn() {
        // cenário
        String isbn = "123";

        Mockito.when(repository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder().id(1L).isbn("123").build()));

        // execução
        Optional<Book> book = service.getBookByIsbn(isbn);

        // verificação
        assertThat(book.isPresent()).isTrue();
        assertThat(book.get().getId()).isEqualTo(1L);
        assertThat(book.get().getIsbn()).isEqualTo(isbn);

        verify(repository, times(1)).findByIsbn(isbn);

    }

}
