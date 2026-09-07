package com.xyzbank.migration.config;

import org.springframework.beans.factory.annotation.Value;
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

    private final String atmPassword;
    private final String mobilePassword;
    private final String webPassword;

    public SecurityConfig(
            @Value("${app.security.atm.password}") String atmPassword,
            @Value("${app.security.mobile.password}") String mobilePassword,
            @Value("${app.security.web.password}") String webPassword) {
        this.atmPassword = atmPassword;
        this.mobilePassword = mobilePassword;
        this.webPassword = webPassword;
    }

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
                .password("{noop}" + atmPassword)
                .roles("ATM")
                .build();
        UserDetails webUser = User.builder()
                .username("web")
                .password("{noop}" + webPassword)
                .roles("WEB")
                .build();
        UserDetails mobileUser = User.builder()
                .username("mobile")
                .password("{noop}" + mobilePassword)
                .roles("MOBILE")
                .build();
        return new InMemoryUserDetailsManager(atmUser, webUser, mobileUser);
    }
}
