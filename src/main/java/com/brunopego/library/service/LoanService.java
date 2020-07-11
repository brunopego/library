package com.brunopego.library.service;

import com.brunopego.library.api.resource.BookController;
import com.brunopego.library.model.entity.Loan;

import java.util.Optional;

public interface LoanService {
    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);
}
