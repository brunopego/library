package com.brunopego.library.api.resource;

import com.brunopego.library.api.dto.LoanDTO;
import com.brunopego.library.api.dto.LoanFilterDTO;
import com.brunopego.library.api.dto.ReturnedLoanDTO;
import com.brunopego.library.model.entity.Book;
import com.brunopego.library.model.entity.Loan;
import com.brunopego.library.service.BookService;
import com.brunopego.library.service.LoanService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private BookService bookService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long crate(@RequestBody LoanDTO dto) {
        Book book = bookService.getBookByIsbn(dto.getIsbn())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));
        Loan loan = Loan.builder().book(book).customer(dto.getCustomer()).loanDate(LocalDate.now()).build();
        loan = loanService.save(loan);
        return  loan.getId();
    }

    @PatchMapping("{id}")
    public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO dto) {
        Loan loan = loanService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        loan.setReturned(dto.getReturned());
        loanService.update(loan);
    }

    @GetMapping
    public Page<LoanDTO> find(LoanFilterDTO dto, Pageable pageRequest) {
        Page<Loan> result = loanService.find(dto, pageRequest);
        List<LoanDTO> list = result
                .getContent()
                .stream()
                .map(entity -> modelMapper.map(entity, LoanDTO.class))
                .collect(Collectors.toList());

        return new PageImpl<LoanDTO>(list, pageRequest, result.getTotalElements());
    }

}
