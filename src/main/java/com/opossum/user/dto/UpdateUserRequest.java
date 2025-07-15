package com.opossum.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String avatarUrl;
}
