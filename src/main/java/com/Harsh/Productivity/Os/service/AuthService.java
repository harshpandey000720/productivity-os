package com.Harsh.Productivity.Os.service;
import com.Harsh.Productivity.Os.dto.AuthResponse;
import com.Harsh.Productivity.Os.dto.LoginRequest;
import com.Harsh.Productivity.Os.dto.RegisterRequest;
import com.Harsh.Productivity.Os.entity.User;
import com.Harsh.Productivity.Os.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class AuthService {
    @Autowired
    UserRepository urepo;
    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private JWTService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    public AuthResponse register(RegisterRequest request){
        if(urepo.findByEmail(request.getEmail()).isPresent()) return new AuthResponse("Email already exists");
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        urepo.save(user);
        return new AuthResponse("Registration Successful");
    }
    public AuthResponse login(LoginRequest request){
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
        if (authentication.isAuthenticated()) {
            return new AuthResponse(jwtService.generateToken(request.getEmail()));
        } else {
            return new AuthResponse("fail");
        }
    }
}
