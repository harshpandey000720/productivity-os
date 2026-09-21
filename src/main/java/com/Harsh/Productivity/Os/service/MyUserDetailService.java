package com.Harsh.Productivity.Os.service;
import com.Harsh.Productivity.Os.entity.User;
import com.Harsh.Productivity.Os.repository.UserRepository;
import com.Harsh.Productivity.Os.entity.UserPrincipal;
import com.Harsh.Productivity.Os.entity.User;
import com.Harsh.Productivity.Os.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Service
public class MyUserDetailService implements UserDetailsService {
    @Autowired
    private UserRepository repo;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new UserPrincipal(user);
    }
}
