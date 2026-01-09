package com.ayawo.banque.ega.exceptions.user;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("Un utilisateur existe déjà avec le username : " + username);
    }
}