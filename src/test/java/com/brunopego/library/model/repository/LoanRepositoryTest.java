package com.brunopego.library.model.repository;

import com.brunopego.library.model.entity.Book;
import com.brunopego.library.model.entity.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    LoanRepository repository;

    private Book createNewBook() {
        return Book.builder().author("Bruno").title("Um Livro").isbn("123").build();
    }

    private Loan createNewLoan() {
        return Loan.builder().customer("Bruno Lacerda").loanDate(LocalDate.now()).returned(false).build();
    }

    @Test
    @DisplayName("Deve verificar se existe empréstimo não devolvido para o livro")
    public void shouldVerifyIfLoanExistsForBook() {
        // cenário
        Book book = createNewBook();
        entityManager.persist(book);
        Loan loan = createNewLoan();
        loan.setBook(book);
        entityManager.persist(loan);

        // execução
        boolean exists = repository.existsByBookAndNotReturned(book);

        // verificação
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve buscar empréstimo pelo número do isbn ou pelo customer")
    public void shouldFindByBookIsbnOrCustomer() {
        // cenário
        Book book = createNewBook();
        entityManager.persist(book);
        Loan loan = createNewLoan();
        loan.setBook(book);
        entityManager.persist(loan);

        // execução
        Page<Loan> result = repository.findByBookIsbnOrCustomer(book.getIsbn(), loan.getCustomer(), PageRequest.of(0, 10));

        // verificação
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);

    }
}
