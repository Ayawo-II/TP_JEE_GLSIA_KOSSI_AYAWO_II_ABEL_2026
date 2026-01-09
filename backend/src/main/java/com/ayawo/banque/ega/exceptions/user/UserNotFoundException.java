package com.ayawo.banque.ega.exceptions.user;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("Utilisateur non trouvé avec l'ID : " + id);
    }

    public UserNotFoundException(String username) {
        super("Utilisateur non trouvé avec le username : " + username);
    }
}