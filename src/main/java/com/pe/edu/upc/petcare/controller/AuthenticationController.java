package com.pe.edu.upc.petcare.controller;

import com.pe.edu.upc.petcare.service.communication.AuthenticationRequest;
import com.pe.edu.upc.petcare.service.communication.AuthenticationResponse;
import com.pe.edu.upc.petcare.service.impl.UserDetailsServiceImpl;
import com.pe.edu.upc.petcare.util.jwt.JwtCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@CrossOrigin
@RequestMapping("/api/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtCenter tokenCenter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @PostMapping("/sign-in")
    public ResponseEntity<?> generateAuthenticationToken(@RequestBody AuthenticationRequest request) throws Exception {
        authenticate(request.getUsername(), request.getPassword());
        final UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.getUsername());
        System.out.println("Password: " + userDetails.getPassword());
        String token = tokenCenter.generateToken(userDetails);
        return ResponseEntity.ok(new AuthenticationResponse(
                userDetails.getUsername(),
                token));
    }

    private void authenticate(String username, String password)
            throws Exception {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
