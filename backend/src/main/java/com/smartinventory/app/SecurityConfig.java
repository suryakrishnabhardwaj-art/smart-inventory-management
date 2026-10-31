package com.smartinventory.app;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration @EnableMethodSecurity
class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean UserDetailsService users(UserRepository repo){return username->repo.findByUsername(username).map(u->User.withUsername(u.username).password(u.password).authorities(u.role).build()).orElseThrow(()->new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));}
 @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
  http.csrf(c->c.disable()).cors(c->{}).authorizeHttpRequests(a->a.requestMatchers("/api/health","/api/auth/login").permitAll().requestMatchers(HttpMethod.GET,"/api/**").authenticated().requestMatchers("/api/products/**","/api/suppliers/**","/api/dashboard/**").hasRole("ADMIN").anyRequest().authenticated()).httpBasic(h->{});
  return http.build();
 }
 @Bean CorsConfigurationSource cors(@Value("${app.cors.allowed-origin}") String origin){CorsConfiguration c=new CorsConfiguration();c.setAllowedOrigins(List.of(origin));c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("*"));c.setAllowCredentials(true);UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;}
 @Bean CommandLineRunner bootstrap(UserRepository users,PasswordEncoder encoder,@Value("${app.admin.username}") String username,@Value("${app.admin.password}") String password){return args->{if(users.findByUsername(username).isEmpty())users.save(new AppUser(username,encoder.encode(password),"ROLE_ADMIN"));};}
}
