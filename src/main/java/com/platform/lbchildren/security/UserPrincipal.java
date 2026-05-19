package com.platform.lbchildren.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPrincipal {
    private String username;
    private String role;
    private Long userId;
}
