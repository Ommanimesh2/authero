package com.authero.authserver.dto.github;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GithubUserResponseDto {
    private String login;
    private Long id;
    private String name;
    private String email;
    private String avatar_url;
}
