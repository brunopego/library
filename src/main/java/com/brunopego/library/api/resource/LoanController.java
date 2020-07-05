package com.brunopego.library.api.resource;

import com.brunopego.library.api.dto.LoanDTO;
import com.brunopego.library.model.entity.Book;
import com.brunopego.library.model.entity.Loan;
import com.brunopego.library.service.BookService;
import com.brunopego.library.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private BookService bookService;

    @Autowired
    private LoanService loanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long crate(@RequestBody LoanDTO dto) {
        Book book = bookService.getBookByIsbn(dto.getIsbn()).get();
        Loan loan = Loan.builder().book(book).customer(dto.getCustomer()).loanDate(LocalDate.now()).build();
        loan = loanService.save(loan);
        return  loan.getId();
    }

}
