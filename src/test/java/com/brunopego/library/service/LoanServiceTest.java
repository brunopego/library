package com.brunopego.library.service;

import com.brunopego.library.model.entity.Book;
import com.brunopego.library.model.entity.Loan;
import com.brunopego.library.model.repository.LoanRepository;
import com.brunopego.library.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

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

        Mockito.when(repository.save(loanToSave)).thenReturn(savedLoan);

        // execução
        Loan loan = service.save(loanToSave);

        // verificação
        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(loan.getBook()).isEqualTo(savedLoan.getBook());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());

    }

}
