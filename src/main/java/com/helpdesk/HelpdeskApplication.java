package com.helpdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class HelpdeskApplication {

    /** Fuso horário oficial de Brasília. */
    public static final String FUSO_BRASILIA = "America/Sao_Paulo";

    public static void main(String[] args) {
        // Deve ser definido antes de iniciar o Spring: todos os LocalDateTime.now()/LocalDate.now()
        // da aplicação passam a usar o horário de Brasília, independentemente do fuso do servidor (UTC no Render).
        TimeZone.setDefault(TimeZone.getTimeZone(FUSO_BRASILIA));
        SpringApplication.run(HelpdeskApplication.class, args);
    }
}
