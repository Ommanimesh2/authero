package com.authero.authserver.dto.github;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GithubUserEmailResponseDto {
    private String email;
    private boolean primary;
    private boolean verified;
}
