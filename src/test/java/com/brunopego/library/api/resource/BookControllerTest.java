package com.brunopego.library.api.resource;

import com.brunopego.library.api.dto.BookDTO;
import com.brunopego.library.exception.BusinessException;
import com.brunopego.library.model.entity.Book;
import com.brunopego.library.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    private BookDTO createNewBookDto() {
        return BookDTO.builder().author("Bruno").title("Um Livro").isbn("001").build();
    }

    private Book createNewBook() {
        return Book.builder().author("Bruno").title("Um Livro").isbn("123").build();
    }

    @Test
    @DisplayName("Deve criar um livro com sucesso")
    public void shouldCreateBook() throws Exception {

        BookDTO dto = createNewBookDto();

        Book savedBook = Book.builder().id(1L).author("Bruno").title("Um Livro").isbn("001").build();
        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
            .perform(request)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("id").isNotEmpty())
            .andExpect(jsonPath("title").value(dto.getTitle()))
            .andExpect(jsonPath("author").value(dto.getAuthor()))
            .andExpect(jsonPath("isbn").value(dto.getIsbn()));

    }

    @Test
    @DisplayName("Deve lançar erro de validação quando não houver dados suficientes para criação do livro ")
    public void shouldNotCreateInvalidBook() throws Exception {

        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
            .perform(request)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("errors", hasSize(3)));

    }

    @Test
    @DisplayName("Deve lançar erro se isbn já estiver cadastrado")
    public void shouldNotCreateBookIfDuplicatedIsbn() throws Exception {

        BookDTO dto = createNewBookDto();

        String json = new ObjectMapper().writeValueAsString(dto);

        String msgErro = "Isbn já cadastrado";
        BDDMockito.given(service.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(msgErro));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
            .perform(request)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("errors", hasSize(1)))
            .andExpect(jsonPath("errors[0]").value(msgErro));

    }

    @Test
    @DisplayName("Deve obter informações de um livro")
    public void shouldGetBookDetails() throws Exception {
        // cenário
        Long id = 1L;

        Book book = createNewBook();
        book.setId(id);
        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc
            .perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("id").value(id))
            .andExpect(jsonPath("title").value(book.getTitle()))
            .andExpect(jsonPath("author").value(book.getAuthor()))
            .andExpect(jsonPath("isbn").value(book.getIsbn()));

    }

    @Test
    @DisplayName("Deve retornar resource not found quando o livro procurado não existir")
    public void shouldReturnNotFoundWhenBookDoesNotExist() throws Exception {
        // cenário
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
            .perform(request)
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void shouldDeleteBook() throws Exception {
        // cenário
        BDDMockito.given(service.getById(Mockito.anyLong()))
                .willReturn(Optional.of(Book.builder().id(1L).build()));

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1));

        mvc
            .perform(request)
            .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("Deve retornar resource not found ao tentar deletar um livro não cadastrado")
    public void shouldReturnNotFoundWhenBookDoesNotExistToDelete() throws Exception {
        // cenário
        BDDMockito.given(service.getById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1));

        mvc
                .perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void shouldUpdateBook() throws Exception {
        // cenário
        Long id = 1L;
        Book book = createNewBook();
        book.setId(id);

        Book bookWithUpdatedInfo = Book.builder().id(1L).author("Bruno").title("Um Livro a Mais").isbn("123").build();

        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));
        BDDMockito.given(service.update(book)).willReturn(bookWithUpdatedInfo);

        String json = new ObjectMapper().writeValueAsString(book);

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(bookWithUpdatedInfo.getTitle()))
                .andExpect(jsonPath("author").value(bookWithUpdatedInfo.getAuthor()))
                .andExpect(jsonPath("isbn").value(book.getIsbn()));
    }

    @Test
    @DisplayName("Deve retornar not found ao tentar atualizar um livro inexistente")
    public void shouldNotUpdateInexistentBook() throws Exception {
        // cenário
        Long id = 1L;
        Book book = createNewBook();
        book.setId(id);
        String json = new ObjectMapper().writeValueAsString(book);

        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve filtrar livros")
    public void shouldFilterBooks() throws Exception {
        // cenário
        Long id = 1L;
        Book book = createNewBook();
        book.setId(id);

        BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));

        String queryString = String.format("?title=%s&author=%s&page=0&size=100",
                book.getTitle(),
                book.getAuthor());

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc
            .perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("content", hasSize(1)))
            .andExpect(jsonPath("totalElements").value(1))
            .andExpect(jsonPath("pageable.pageSize").value(100))
            .andExpect(jsonPath("pageable.pageNumber").value(0));

    }

}
