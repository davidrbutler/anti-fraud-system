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
                // --- Define Final Stage 6 Authorization Rules ---
                .authorizeHttpRequests(requests -> requests
                        // Publicly accessible paths
                        .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll()      // User registration
                        .requestMatchers(HttpMethod.POST, "/actuator/shutdown").permitAll()  // Shutdown endpoint
                        .requestMatchers("/h2-console/**").permitAll()                     // H2 Console access

                        // Role-specific paths from Stage 3 & 4
                        .requestMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole(UserRole.ADMINISTRATOR.name(), UserRole.SUPPORT.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/user/**").hasRole(UserRole.ADMINISTRATOR.name())
                        .requestMatchers(HttpMethod.PUT, "/api/auth/role").hasRole(UserRole.ADMINISTRATOR.name())
                        .requestMatchers(HttpMethod.PUT, "/api/auth/access").hasRole(UserRole.ADMINISTRATOR.name())
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole(UserRole.MERCHANT.name()) // Transaction validation
                        .requestMatchers("/api/antifraud/suspicious-ip/**").hasRole(UserRole.SUPPORT.name())              // IP Management
                        .requestMatchers("/api/antifraud/stolencard/**").hasRole(UserRole.SUPPORT.name())                 // Card Management

                        // --- Stage 6 Rules (Corrected) ---
                        // Transaction Feedback (SUPPORT only) - Reverted temporary change
                        .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction").hasRole(UserRole.SUPPORT.name())
                        // Transaction History (SUPPORT only)
                        .requestMatchers("/api/antifraud/history/**").hasRole(UserRole.SUPPORT.name())
                        // --- End Stage 6 Rules ---

                        // Secure all other requests - require authentication
                        .anyRequest().authenticated()
                )
                // --- End Authorization Rules ---
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .userDetailsService(userDetailsService())
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Load user details including role and lock status (Unchanged from Stage 3)
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