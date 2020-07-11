package com.brunopego.library.service;

import com.brunopego.library.exception.BusinessException;
import com.brunopego.library.model.entity.Book;
import com.brunopego.library.model.entity.Loan;
import com.brunopego.library.model.repository.LoanRepository;
import com.brunopego.library.service.impl.LoanServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    @InjectMocks
    private LoanServiceImpl service;

    @Mock
    private LoanRepository repository;

    private Book createNewBook() {
        return Book.builder().id(1L).author("Bruno").title("Um Livro").isbn("123").build();
    }

    private Loan createNewLoan() {
        return Loan.builder().customer("Bruno Lacerda")
                .book(createNewBook()).loanDate(LocalDate.now()).returned(false).build();
    }

    @Test
    @DisplayName("Deve salvar um emprestimo")
    public void shouldSaveLoan() {
        // cenário
        Loan loanToSave = createNewLoan();
        Loan savedLoan = createNewLoan();
        savedLoan.setId(1L);

        Mockito.when(repository.existsByBookAndNotReturned(loanToSave.getBook())).thenReturn(false);
        Mockito.when(repository.save(loanToSave)).thenReturn(savedLoan);

        // execução
        Loan loan = service.save(loanToSave);

        // verificação
        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(loan.getBook()).isEqualTo(savedLoan.getBook());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());

    }

    @Test
    @DisplayName("Deve lançar um erro de negócio ao tentar salvar empréstimo de livro já emprestado")
    public void shouldNotSaveLoanWithBookAlreadyLoaned() {
        // cenário
        Loan loanToSave = createNewLoan();
        Loan savedLoan = createNewLoan();
        savedLoan.setId(1L);

        Mockito.when(repository.existsByBookAndNotReturned(loanToSave.getBook())).thenReturn(true);

        // execução
        Throwable exception = Assertions.catchThrowable(() -> service.save(loanToSave));

        // verificações
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");

        verify(repository, Mockito.never()).save(loanToSave);

    }

    @Test
    @DisplayName("Deve obter as informações de um empréstimo pelo Id")
    public void shoulGetLoanDetailsById()  {
        // cenário
        Long id = 1L;
        Loan loan = createNewLoan();
        loan.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(loan));

        // execução
        Optional<Loan> result = service.getById(id);

        // verificação
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

    }

    @Test
    @DisplayName("Deve atualizar um empréstimo")
    public void shouldUpdateLoan() {
        // cenário
        Loan loan = createNewLoan();
        loan.setId(1L);
        loan.setReturned(true);

        Mockito.when(repository.save(loan)).thenReturn(loan);

        // execução
        Loan updatedLoan = service.update(loan);

        // verificação
        assertThat(updatedLoan.getReturned()).isTrue();
        verify(repository).save(loan);

    }

}
