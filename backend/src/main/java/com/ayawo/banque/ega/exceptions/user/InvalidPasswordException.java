package com.ayawo.banque.ega.exceptions.user;

public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException() {
        super("Mot de passe incorrect");
    }
}