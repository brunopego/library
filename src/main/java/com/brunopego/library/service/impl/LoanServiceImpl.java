package com.brunopego.library.service.impl;

import com.brunopego.library.model.entity.Loan;
import com.brunopego.library.model.repository.LoanRepository;
import com.brunopego.library.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanServiceImpl implements LoanService {

    @Autowired
    private LoanRepository repository;

    @Override
    public Loan save(Loan loan) {
        return repository.save(loan);
    }
}
