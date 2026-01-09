package com.ayawo.banque.ega.dto.user;

import com.ayawo.banque.ega.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDTO {

    @NotBlank(message = "Le username est obligatoire")
    @Size(min = 3, max = 50, message = "Le username doit contenir entre 3 et 50 caractères")
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    private Long clientId;  // Obligatoire seulement si role = CLIENT
}