package com.brunopego.library.api.exception;

import com.brunopego.library.exception.BusinessException;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiErrors {

    private List<String> errors;

    public ApiErrors(BindingResult bindingResult) {
        this.errors = new ArrayList<>();
        bindingResult.getAllErrors().forEach(error -> this.errors.add(error.getDefaultMessage()));
    }

    public ApiErrors(BusinessException e) {
        this.errors = Arrays.asList(e.getMessage());
    }

    public List<String> getErrors() {
        return errors;
    }
}
