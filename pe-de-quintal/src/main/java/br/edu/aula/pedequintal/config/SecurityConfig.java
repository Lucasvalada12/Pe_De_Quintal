package br.edu.aula.pedequintal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Login por sessão (formulário) com senha em BCrypt.
 * - /login, /cadastro, CSS e a página de erro são públicos;
 * - todo o resto exige usuário autenticado;
 * - CSRF fica LIGADO: o th:action do Thymeleaf injeta o token nos formulários sozinho.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/cadastro", "/css/**", "/error").permitAll()
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")                 // nossa página (W1), não a padrão do Spring
                .usernameParameter("email")
                .passwordParameter("senha")
                .defaultSuccessUrl("/itens", true)   // M1 -> M2: após o login, cai direto na lista
                .failureUrl("/login?erro")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?saiu")
                .permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
