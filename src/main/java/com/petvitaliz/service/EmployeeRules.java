package com.petvitaliz.service;

import com.petvitaliz.exception.ApiException;

final class EmployeeRules {

    static final String CORPORATE_DOMAIN = "@petvitalizfuncionario.com";
    static final String CORPORATE_EMAIL_REQUIRED =
            "O e-mail deve ser corporativo (@petvitalizfuncionario.com)";
    static final String EMAIL_IN_USE = "E-mail já está em uso por outro colaborador";

    private EmployeeRules() {
    }

    static void requireCorporateEmail(String normalizedEmail, ApiException error) {
        if (!normalizedEmail.endsWith(CORPORATE_DOMAIN) || normalizedEmail.length() == CORPORATE_DOMAIN.length()) {
            throw error;
        }
    }
}
