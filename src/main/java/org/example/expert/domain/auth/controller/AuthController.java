package org.example.expert.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.Token;
import org.example.expert.config.jwt.dto.TokenDto;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/signup")
    public SignupResponse signup(@Valid @RequestBody SignupRequest signupRequest, HttpServletResponse response) {
        SignupResponse signupResponse = authService.signup(signupRequest);
        response.addHeader("Set-Cookie", "Refresh-Token="+ signupResponse.getRefreshToken() + "; path=/; HttpOnly; SameSite=Strict"); //Secure; 테스트를 위해 제거
        return signupResponse;
    }

    @PostMapping("/auth/signin")
    public SigninResponse signin(@Valid @RequestBody SigninRequest signinRequest, HttpServletResponse response) {
        SigninResponse signinResponse = authService.signin(signinRequest);
        response.addHeader("Set-Cookie", "Refresh-Token="+ signinResponse.getRefreshToken() + "; path=/; HttpOnly; SameSite=Strict"); //Secure; 테스트를 위해 제거
        return signinResponse;
    }

    @PostMapping("/refresh")
    public TokenDto refresh(@CookieValue("Refresh-Token") String refreshToken, HttpServletResponse response){
        TokenDto tokenDto = authService.refresh(refreshToken);
        response.addHeader("Set-Cookie", "Refresh-Token="+ tokenDto.getRefreshToken() + "; path=/; HttpOnly; SameSite=Strict");//Secure; 테스트를 위해 제거
        return tokenDto;
    }
}
