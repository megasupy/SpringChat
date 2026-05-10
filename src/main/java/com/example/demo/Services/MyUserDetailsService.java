package com.example.demo.Services;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.GrantedAuthority;

import com.example.demo.Models.MyUser;
import com.example.demo.Repositories.UserRepository;

@Service
public class MyUserDetailsService implements UserDetailsService {
    public class MyUserDetails implements UserDetails {
        final private MyUser u;

        MyUserDetails(MyUser u) {
            this.u = u;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // return empy authorities since the app isn't gonna have that for now.
            return new ArrayList<GrantedAuthority>();
        }
        @Override
        public String getPassword() {
            return u.password_hashed;
        }
        @Override
        public String getUsername() {
            return u.username;
        }
    }
    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MyUser u;
        try {
            u = userRepository.getByUsername(username);
        }
        catch (DataAccessException e) {
            throw new UsernameNotFoundException(username);
        }

        return new MyUserDetails(u);
    }
}
