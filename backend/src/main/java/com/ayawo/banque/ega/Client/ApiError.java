package com.ayawo.banque.ega.Client;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ApiError {

    private String message;
    private int code;
    private LocalDateTime timestamp;

}
