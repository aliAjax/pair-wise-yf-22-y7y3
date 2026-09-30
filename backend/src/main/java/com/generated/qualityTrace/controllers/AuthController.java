package com.generated.qualityTrace.controllers;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.generated.qualityTrace.routes.AuthRoutes;
import com.generated.qualityTrace.services.AuthService;
import com.generated.qualityTrace.types.LoginRequest;

/** 登录换 JWT。种子账号：auditor / inspector / restricted / manager。 */
@RestController
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping(AuthRoutes.LOGIN)
  public Map<String, Object> login(@RequestBody LoginRequest request) {
    return authService.login(request.username(), request.password());
  }
}
