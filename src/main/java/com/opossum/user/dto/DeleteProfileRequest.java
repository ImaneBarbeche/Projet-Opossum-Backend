package com.opossum.user.dto;

import jakarta.validation.constraints.NotBlank;

public class DeleteProfileRequest {
    @NotBlank
    private String password;
    private boolean confirmDeletion;

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public boolean isConfirmDeletion() {
        return confirmDeletion;
    }
    public void setConfirmDeletion(boolean confirmDeletion) {
        this.confirmDeletion = confirmDeletion;
    }
}