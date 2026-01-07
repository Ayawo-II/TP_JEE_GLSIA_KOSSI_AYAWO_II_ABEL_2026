package com.ayawo.banque.configuration;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiError {

    private String message;
    private int code;
    private LocalDateTime timestamp;

}
