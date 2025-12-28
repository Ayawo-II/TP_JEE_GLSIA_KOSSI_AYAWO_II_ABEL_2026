package com.ayawo.banque.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan("com.ayawo.banque.ega.**")
@EnableJpaRepositories(basePackages = "com.ayawo.banque.ega.**")
@SpringBootApplication(scanBasePackages = {
        "com.ayawo.banque"
})
public class EgaApplication {

	public static void main(String[] args) {
		SpringApplication.run(EgaApplication.class, args);
	}

}
