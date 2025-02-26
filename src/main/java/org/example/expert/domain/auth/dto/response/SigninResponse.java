package org.example.expert.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class SigninResponse {

    private final String accessToken;
    private final String refreshToken;

    public SigninResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
