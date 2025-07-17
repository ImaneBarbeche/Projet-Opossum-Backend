package com.opossum.user.dto;

public class UpdateUserRequest {

    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String avatarUrl;

    // Constructeur
    public UpdateUserRequest(String firstname, String lastname, String email, String phone, String avatarUrl) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
    }

    // Getters
    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    // Setters
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
