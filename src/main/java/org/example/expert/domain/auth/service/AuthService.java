package org.example.expert.domain.auth.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.config.jwt.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.config.jwt.dto.TokenDto;
import org.example.expert.config.jwt.entity.RefreshToken;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.RefreshTokenRepository;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new InvalidRequestException("이미 존재하는 이메일입니다.");
        }
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        UserRole userRole = UserRole.of(signupRequest.getUserRole());

        User newUser = new User(
                signupRequest.getEmail(),
                encodedPassword,
                userRole
        );
        User savedUser = userRepository.save(newUser);
        TokenDto tokenDto = jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), userRole);

        refreshTokenRepository.findByUserId(newUser.getId()).ifPresentOrElse(
                existingToken -> existingToken.updateToken(tokenDto.getRefreshToken()),
                () -> refreshTokenRepository.save(new RefreshToken(tokenDto.getRefreshToken(), newUser))
        );

        return new SignupResponse(tokenDto.getAccessToken(), tokenDto.getRefreshToken());
    }

    @Transactional
    public SigninResponse signin(SigninRequest signinRequest) {
        User user = userRepository.findByEmail(signinRequest.getEmail()).orElseThrow(
                () -> new InvalidRequestException("가입되지 않은 유저입니다."));

        // 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401을 반환합니다.
        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            throw new AuthException("잘못된 비밀번호입니다.");
        }
        TokenDto tokenDto = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

        refreshTokenRepository.findByUserId(user.getId()).ifPresentOrElse(
                existingToken -> existingToken.updateToken(tokenDto.getRefreshToken()),
                () -> refreshTokenRepository.save(new RefreshToken(tokenDto.getRefreshToken(), user))
        );

        return new SigninResponse(tokenDto.getAccessToken(), tokenDto.getRefreshToken());
    }

    public TokenDto refresh(String token) {
        Claims claims = jwtUtil.extractClaims(token);

        if (claims.getExpiration().before(new Date())) {
            throw new IllegalArgumentException("리프레시 토큰이 만료되었습니다.");
        }

        Long userId = Long.parseLong(claims.getSubject());


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        RefreshToken storedRefreshToken = refreshTokenRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("리프레시 토큰을 찾을 수 없습니다."));

        if(!storedRefreshToken.getRefreshToken().equals(token)) {
            log.error("비정상적인 접근 감지");
            refreshTokenRepository.delete(storedRefreshToken);
            throw new IllegalArgumentException("리프레시 토큰이 일치하지 않습니다.");
        }

        refreshTokenRepository.delete(storedRefreshToken);

        TokenDto newTokens = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

        RefreshToken newRefreshToken = new RefreshToken(newTokens.getRefreshToken(), user);
        refreshTokenRepository.save(newRefreshToken);

        return newTokens;
    }
}
