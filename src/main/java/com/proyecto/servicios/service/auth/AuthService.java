package com.proyecto.servicios.service.auth;

import com.proyecto.servicios.model.auth.request.LoginRequest;
import com.proyecto.servicios.model.auth.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
