package com.brunopego.library.api.resource;

import com.brunopego.library.api.dto.LoanDTO;
import com.brunopego.library.api.dto.LoanFilterDTO;
import com.brunopego.library.api.dto.ReturnedLoanDTO;
import com.brunopego.library.exception.BusinessException;
import com.brunopego.library.model.entity.Book;
import com.brunopego.library.model.entity.Loan;
import com.brunopego.library.service.BookService;
import com.brunopego.library.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

    static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    private Book createNewBook() {
        return Book.builder().id(1L).author("Bruno").title("Um Livro").isbn("123").build();
    }

    private LoanDTO createNewLoanDTO() {
        return LoanDTO.builder().isbn("123").customer("Bruno Lacerda").build();
    }

    private Loan createNewLoan() {
        return Loan.builder().id(1L).customer("Bruno Lacerda").book(createNewBook()).loanDate(LocalDate.now()).build();
    }

    @Test
    @DisplayName("Deve realizar um emprestimo")
    public void shouldBeAbleToLoanBook() throws Exception {
        // cenário
        LoanDTO dto = createNewLoanDTO();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(createNewBook()));

        Loan loan = createNewLoan();
        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
            .perform(request)
            .andExpect(status().isCreated())
            .andExpect(content().string("1"));

    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro inexistente")
    public void shouldNotLoanInexistentBook() throws Exception {
        // cenário
        LoanDTO dto = createNewLoanDTO();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.empty());

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book not found for passed isbn"));

    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro já emprestado ")
    public void shouldNotLoanLoanedBook() throws Exception {
        // cenário
        LoanDTO dto = createNewLoanDTO();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(createNewBook()));

        BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
                .willThrow(new BusinessException("Book already loaned"));

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book already loaned"));

    }

    @Test
    @DisplayName("Deve devolver um livro emprestado")
    public void ShouldReturnLoanedBook() throws Exception {
        // cenário
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Loan loan = createNewLoan();
        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.of(loan));

        loan.setReturned(true);
        BDDMockito.given(loanService.update(Mockito.any(Loan.class))).willReturn(loan);

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isOk());

        Mockito.verify(loanService, times(1)).update(loan);

    }

    @Test
    @DisplayName("Deve retornar erro 404 ao tentar devolver um livro inexistente")
    public void ShouldNotReturnInexistentLoanedBook() throws Exception {
        // cenário
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());

        Mockito.verify(loanService, Mockito.never()).update(Mockito.any(Loan.class));

    }

    @Test
    @DisplayName("Deve filtrar empréstimos")
    public void shouldFilterLoans() throws Exception {
        // cenário
        Long id = 1L;
        Loan loan = createNewLoan();

        BDDMockito.given(loanService.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0, 10), 1));

        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=10",
                loan.getBook().getIsbn(),
                loan.getCustomer());

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(10))
                .andExpect(jsonPath("pageable.pageNumber").value(0));

    }

}
