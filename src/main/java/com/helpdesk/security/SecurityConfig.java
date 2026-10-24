package com.helpdesk.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/error", "/error/**", "/favicon.ico", "/images/**", "/css/**", "/js/**", "/static/**").permitAll()
                // Somente administradores
                .requestMatchers("/admin/usuarios/**", "/admin/motivos/**").hasRole("ADMIN")
                .requestMatchers("/admin/analytics/**").hasAnyRole("ADMIN", "TECNICO")
                // Equipe de atendimento (admin + técnico)
                .requestMatchers("/admin/**", "/chamados/**", "/clientes/**").hasAnyRole("ADMIN", "TECNICO")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/admin/analytics", true)
                .failureUrl("/login?error")
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll());
        return http.build();
    }
}
