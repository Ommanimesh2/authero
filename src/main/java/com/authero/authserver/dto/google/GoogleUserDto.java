package com.authero.authserver.dto.google;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GoogleUserDto {
    private String id;
    private String email;
    private String name;
    private String picture;
}
