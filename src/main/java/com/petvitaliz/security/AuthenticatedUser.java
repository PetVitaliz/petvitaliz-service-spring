package com.petvitaliz.security;

import com.petvitaliz.util.Texts;

public record AuthenticatedUser(Integer id, String email, String firstName, String lastName, String role) {

    public String fullName() {
        return Texts.fullName(firstName, lastName);
    }
}
