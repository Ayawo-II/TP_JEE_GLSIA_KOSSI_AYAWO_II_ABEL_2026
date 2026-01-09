package com.ayawo.banque.ega.dto.user;

import com.ayawo.banque.ega.enums.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String username;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    // Informations du client si c'est un CLIENT
    private Long clientId;
    private String clientNom;
    private String clientEmail;
}