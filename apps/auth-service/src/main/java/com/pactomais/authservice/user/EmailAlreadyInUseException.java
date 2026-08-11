package com.pactomais.authservice.user;

public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException(String email) {
        super("Já existe uma conta com o e-mail " + email);
    }
}
