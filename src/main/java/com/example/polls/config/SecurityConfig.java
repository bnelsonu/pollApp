package com.example.polls.config;

import com.example.polls.security.CustomUserDetailsService;
import com.example.polls.security.JwtAuthenticationEntryPoint;
import com.example.polls.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)

/*
This class contains almost all the security configurations that are required for our project.
Let’s understand the meaning of the annotations and configurations used in the SecurityConfig class -

 @EnableWebSecurity
This is the primary spring security annotation that is used to enable web security in a project.
@EnableGlobalMethodSecurity
This is used to enable method level security based on annotations.
You can use following three types of annotations for securing your methods -
securedEnabled: It enables the @Secured annotation using which you can protect your
controller/service methods like so -
@Secured("ROLE_ADMIN")
public User getAllUsers() {}

@Secured({"ROLE_USER", "ROLE_ADMIN"})
public User getUser(Long id) {}

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
public boolean isUsernameAvailable() {}

jsr250Enabled: It enables the @RolesAllowed annotation that can be used like this -
@RolesAllowed("ROLE_ADMIN")
public Poll createPoll() {}

prePostEnabled: It enables more complex expression based access control syntax with
@PreAuthorize and @PostAuthorize annotations -
@PreAuthorize("isAnonymous()")
public boolean isUsernameAvailable() {}

@PreAuthorize("hasRole('USER')")
public Poll createPoll() {}
 WebSecurityConfigurerAdapter

This class implements Spring Security’s WebSecurityConfigurer interface.
 It provides default security configurations and allows other classes to extend it
 and customize the security configurations by overriding its methods.
Our SecurityConfig class extends WebSecurityConfigurerAdapter and overrides some of its methods to provide
custom security configurations.

To authenticate a User or perform various role-based checks, Spring security needs to load users details somehow.
For this purpose, It consists of an interface called UserDetailsService which has a single method that loads a user
based on username-
UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
We’ll define a CustomUserDetailsService that implements UserDetailsService interface and provides the implementation
for loadUserByUsername() method.

Note that, the loadUserByUsername() method returns a UserDetails object that Spring Security uses for performing
various authentication and role based validations.

In our implementation, We’ll also define a custom UserPrincipal class that will implement UserDetails interface,
and return the UserPrincipal object from loadUserByUsername() method.
*/

public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/",
                        "/favicon.ico",
                        "/**/*.png",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.jpg",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js")
                .permitAll()
                .antMatchers("/api/auth/**")
                .permitAll()
                .antMatchers("/api/user/checkUsernameAvailability", "/api/user/checkEmailAvailability")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/api/polls/**", "/api/users/**")
                .permitAll()
                .anyRequest()
                .authenticated();

        // Add our custom JWT security filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    }

}
