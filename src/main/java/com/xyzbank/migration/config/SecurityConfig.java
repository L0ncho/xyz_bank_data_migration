package com.xyzbank.migration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/atm/**").hasRole("ATM")
                        .requestMatchers("/api/mobile/**").hasRole("MOBILE")
                        .requestMatchers("/api/web/**").hasRole("WEB")
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails atmUser = User.builder()
                .username("atm")
                .password("{noop}atm")
                .roles("ATM")
                .build();
        UserDetails webUser = User.builder()
                .username("web")
                .password("{noop}web")
                .roles("WEB")
                .build();
        UserDetails mobileUser = User.builder()
                .username("mobile")
                .password("{noop}mobile")
                .roles("MOBILE")
                .build();
        return new InMemoryUserDetailsManager(atmUser, webUser, mobileUser);
    }
}
