package com.ayawo.banque.ega.services;

import com.ayawo.banque.ega.dto.auth.AuthResponseDTO;
import com.ayawo.banque.ega.dto.auth.LoginRequestDTO;
import com.ayawo.banque.ega.entities.UserEntity;
import com.ayawo.banque.ega.repositories.UserRepository;
import com.ayawo.banque.ega.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authentifier un utilisateur et générer un token JWT
     */
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {

        try {
            // 1. VÉRIFICATION DE L'UTILISATEUR DANS LA BASE
            Optional<UserEntity> userOpt = userRepository.findByUsername(loginRequest.getUsername());

            if (userOpt.isEmpty()) {
                throw new RuntimeException("Utilisateur non trouvé");
            }

            UserEntity user = userOpt.get();

            // 2. VÉRIFICATION MANUELLE DU MOT DE PASSE (debug)
            boolean passwordMatches = passwordEncoder.matches(
                    loginRequest.getPassword(),
                    user.getPassword()
            );

            if (!passwordMatches) {
                throw new BadCredentialsException("Identifiants incorrects");
            }

            // 3. AUTHENTIFICATION SPRING SECURITY
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 4. GÉNÉRATION DU TOKEN JWT
            String token = tokenProvider.generateToken(authentication);

            // 5. MISE À JOUR DE LA DERNIÈRE CONNEXION
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // 6. CONSTRUCTION DE LA RÉPONSE
            AuthResponseDTO.AuthResponseDTOBuilder responseBuilder = AuthResponseDTO.builder()
                    .token(token)
                    .type("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .role(user.getRole());

            // Ajouter les infos du client si c'est un CLIENT
            if (user.getClient() != null) {
                responseBuilder
                        .clientId(user.getClient().getId())
                        .clientNom(user.getClient().getNomComplet());
            }

            AuthResponseDTO response = responseBuilder.build();

            return response;

        } catch (BadCredentialsException e) {
            throw new RuntimeException("Identifiants incorrects: " + e.getMessage());

        } catch (Exception e) {
            throw new RuntimeException("Erreur d'authentification: " + e.getMessage(), e);
        }
    }
}