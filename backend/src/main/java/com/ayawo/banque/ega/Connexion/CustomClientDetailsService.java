package com.ayawo.banque.ega.Connexion;

import com.ayawo.banque.ega.Client.ClientEntity;
import com.ayawo.banque.ega.Client.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomClientDetailsService implements UserDetailsService {

    private final ClientRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        ClientEntity client = clientRepository.findByEmail(email);
        if (client == null) {
            throw new UsernameNotFoundException("Client introuvable");
        }
        return new org.springframework.security.core.userdetails.User(
                client.getEmail(),
                client.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(client.getRole()))
        );
    }
}
