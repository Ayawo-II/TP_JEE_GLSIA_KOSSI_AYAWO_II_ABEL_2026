package com.ayawo.banque.ega.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDTO {

    private String password;  // Nouveau mot de passe (optionnel)
    private Boolean active;   // Activer/désactiver le compte
}