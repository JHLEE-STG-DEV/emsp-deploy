package com.chargev.emsp.entity.authenticationentity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TokenRequest {
    @JsonProperty("grant_type")
    private String grantType;
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("client_secret")
    private String clientSecret;
    private String scope;
    private String username;
    private String password;
    @JsonProperty("refresh_token")
    private String refreshToken;
    private String code;
    @JsonProperty("redirect_uri")
    private String redirectUri;
    private String state;
}
