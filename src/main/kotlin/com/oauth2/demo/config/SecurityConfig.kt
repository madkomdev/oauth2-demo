package com.oauth2.demo.config

import com.oauth2.demo.security.JwtAuthenticationConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthenticationConverter: JwtAuthenticationConverter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { authorize ->
                authorize
                    // Public endpoints - no authentication required
                    .requestMatchers("/", "/home", "/public/**").permitAll()
                    .requestMatchers("/h2-console/**", "/actuator/**").permitAll()
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                    
                    // API endpoints - require JWT token (stateless)
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/manager/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PUT, "/api/manager/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.DELETE, "/api/manager/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers("/api/manager/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers("/api/user/**").hasAnyRole("USER", "MANAGER", "ADMIN")
                    
                    // Web pages - require OAuth2 login (stateful)
                    .requestMatchers("/dashboard", "/profile", "/admin/**").authenticated()
                    
                    // All other requests need authentication
                    .anyRequest().authenticated()
            }
            // Configure different session management for web vs API
            .sessionManagement { session ->
                // Use sessions for web pages, stateless for APIs
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
            }
            // OAuth2 Login for web pages
            .oauth2Login { oauth2 ->
                oauth2
                    .loginPage("/")
                    .defaultSuccessUrl("/dashboard", true)
                    .failureUrl("/?error")
            }
            // OAuth2 Resource Server for APIs
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                }
            }
            // OAuth2 Client for token refresh
            .oauth2Client { }
            // Logout configuration
            .logout { logout ->
                logout
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
            }

        // For H2 console (development only)
        http.headers { headers -> headers.frameOptions { it.sameOrigin() } }
        
        // Disable CSRF for API endpoints, enable for web
        http.csrf { csrf ->
            csrf.ignoringRequestMatchers("/api/**", "/h2-console/**")
        }

        return http.build()
    }
}
