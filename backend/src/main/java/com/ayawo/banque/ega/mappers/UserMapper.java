package com.ayawo.banque.ega.mappers;

import com.ayawo.banque.ega.dto.user.UserResponseDTO;
import com.ayawo.banque.ega.entities.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toResponseDTO(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        UserResponseDTO.UserResponseDTOBuilder builder = UserResponseDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .role(entity.getRole())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .lastLogin(entity.getLastLogin());

        // Ajouter les infos du client si c'est un CLIENT
        if (entity.getClient() != null) {
            builder.clientId(entity.getClient().getId())
                    .clientNom(entity.getClient().getNomComplet())
                    .clientEmail(entity.getClient().getEmail());
        }

        return builder.build();
    }
}