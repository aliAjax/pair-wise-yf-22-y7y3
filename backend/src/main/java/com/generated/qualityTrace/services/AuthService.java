package com.generated.qualityTrace.services;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.exceptions.ValidationException;
import com.generated.qualityTrace.models.SystemUser;
import com.generated.qualityTrace.repositories.AuditLogRepository;
import com.generated.qualityTrace.repositories.UserRepository;
import com.generated.qualityTrace.utils.Validators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 登录换发 JWT。 */
@Service
public class AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final AuditLogRepository auditLogRepository;

  public AuthService(UserRepository userRepository, JwtService jwtService,
                     AuditLogRepository auditLogRepository) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
    this.auditLogRepository = auditLogRepository;
  }

  public Map<String, Object> login(String username, String password) {
    String name = Validators.requireText(username, "username");
    String pwd = Validators.requireText(password, "password");
    SystemUser user = userRepository.findByUsername(name)
        .orElseThrow(() -> new ValidationException("invalid username or password"));
    if (!user.password.equals(pwd)) {
      throw new ValidationException("invalid username or password");
    }
    String token = jwtService.issue(user.username, user.role);
    log.info(LogTemplates.STATUS + ": user login actor={} role={}", user.username, user.role);
    auditLogRepository.append(user.username, "LOGIN", "SystemUser", user.username,
        "role=" + user.role);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("token", token);
    result.put("tokenType", "Bearer");
    result.put("username", user.username);
    result.put("displayName", user.displayName);
    result.put("role", user.role);
    return result;
  }
}
