package com.generated.qualityTrace.controllers;

import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.controllers.support.ControllerSupport;
import com.generated.qualityTrace.exceptions.ApiException;
import com.generated.qualityTrace.exceptions.BusinessException;
import com.generated.qualityTrace.services.AuthService;
import com.generated.qualityTrace.types.LoginPayload;
import com.generated.qualityTrace.validators.AuthValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证接口：登录换取 JWT。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody LoginPayload payload) {
    try {
      AuthValidator.validateLogin(payload);
      Map<String, Object> result = authService.login(payload.username, payload.password);
      return ResponseEntity.ok(result);
    } catch (BusinessException e) {
      throw ControllerSupport.wrap(e);
    } catch (IllegalArgumentException e) {
      throw new ApiException(ErrorCodes.INVALID_PARAMETER, e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }
}
