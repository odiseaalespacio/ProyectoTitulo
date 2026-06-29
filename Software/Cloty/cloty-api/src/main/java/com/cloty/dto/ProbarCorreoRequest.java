package com.cloty.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProbarCorreoRequest(@NotBlank @Email String email) {
}
