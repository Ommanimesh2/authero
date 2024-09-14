package com.authero.authserver.dto.github;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GithubTokenResponseDto {
    private String access_token;
    private String scope;
    private String token_type;
}
