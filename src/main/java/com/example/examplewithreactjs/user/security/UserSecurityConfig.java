package com.example.examplewithreactjs.user.security;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class UserSecurityConfig extends WebSecurityConfigurerAdapter {


    @Bean
    public UserDetailsService userDetailsService(){
        return new AppUserDetailService();
    }


    @Override
    protected void configure (HttpSecurity http) throws Exception{
         http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(
                        "api/v1/user/login"
                        ,"api/v1/user/register","api/v1/user/**")
                .permitAll()
                 .antMatchers(
                            "api/v1/user/saveChanges/{id}"
                                    ,"api/v1/user/getImage/**",
                                    "api/v1/user/upload/**",
                                    "api/v1/user/getUser/**")
                 .hasAnyRole("ADMIN","USER","MANAGER")
                 .antMatchers("api/v1/user/see_users")
                 .hasAnyRole("ADMIN","MANAGER")
                 .antMatchers("api/v1/user/deleteUser/**")
                 .hasRole("MANAGER")
                 .and()
                .httpBasic();

    }

    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    @Bean
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
