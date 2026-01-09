package com.ayawo.banque.ega.dto.auth;

import com.ayawo.banque.ega.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {

    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private Role role;
    private Long clientId;  // NULL si ADMIN
    private String clientNom;
}