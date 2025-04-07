package antifraud.config;

import antifraud.model.UserRole; // Ensure UserRole is imported
import antifraud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final UserRepository userRepository;

    @Autowired
    public SecurityConfig(RestAuthenticationEntryPoint restAuthenticationEntryPoint, UserRepository userRepository) {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic(customizer -> customizer.authenticationEntryPoint(restAuthenticationEntryPoint))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(Customizer.withDefaults()).disable())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                        .requestMatchers(HttpMethod.POST, "/actuator/shutdown").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole(UserRole.ADMINISTRATOR.name(), UserRole.SUPPORT.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/user/**").hasRole(UserRole.ADMINISTRATOR.name())
                        .requestMatchers(HttpMethod.PUT, "/api/auth/role").hasRole(UserRole.ADMINISTRATOR.name())
                        .requestMatchers(HttpMethod.PUT, "/api/auth/access").hasRole(UserRole.ADMINISTRATOR.name())
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole(UserRole.MERCHANT.name())
                        .requestMatchers("/api/antifraud/suspicious-ip/**").hasRole(UserRole.SUPPORT.name())
                        .requestMatchers("/api/antifraud/stolencard/**").hasRole(UserRole.SUPPORT.name())

                        .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction").hasRole(UserRole.SUPPORT.name())
                        .requestMatchers("/api/antifraud/history/**").hasRole(UserRole.SUPPORT.name())

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .userDetailsService(userDetailsService())
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsernameIgnoreCase(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .accountLocked(user.isAccountLocked())
                        .disabled(false)
                        .accountExpired(false)
                        .credentialsExpired(false)
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}