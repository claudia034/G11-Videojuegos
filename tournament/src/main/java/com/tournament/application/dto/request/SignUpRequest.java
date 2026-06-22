package com.tournament.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignUpRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email invalido")
    @Size(max = 150, message = "El email no puede superar 150 caracteres")
    private String email;

    @NotBlank(message = "El gamertag es obligatorio")
    @Size(min = 3, max = 30)
    private String username;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, max = 100, message = "La contrasena debe tener entre 8 y 100 caracteres")
    private String password;

    @NotBlank(message = "La confirmacion de contrasena es obligatoria")
    private String confirmPassword;
}