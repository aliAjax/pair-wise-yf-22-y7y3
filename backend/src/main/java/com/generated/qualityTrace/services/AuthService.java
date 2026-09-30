package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.exceptions.BusinessException;
import com.generated.qualityTrace.models.User;
import com.generated.qualityTrace.repositories.UserRepository;
import com.generated.qualityTrace.utils.Formatters;
import com.generated.qualityTrace.utils.JwtTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 认证服务：校验用户名密码并签发 JWT。
 */
@Service
public class AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepo;
  private final String secret;
  private final long ttlMillis;

  public AuthService(UserRepository userRepo,
                     @Value("${JWT_SECRET:local-dev-secret}") String secret,
                     @Value("${JWT_TTL_MILLIS:28800000}") long ttlMillis) {
    this.userRepo = userRepo;
    this.secret = secret;
    this.ttlMillis = ttlMillis;
  }

  public Map<String, Object> login(String username, String password) {
    User user = userRepo.findByUsername(username)
        .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_INVALID,
            ErrorMessages.AUTH_INVALID));
    if (!user.password.equals(password)) {
      throw new BusinessException(ErrorCodes.AUTH_INVALID, ErrorMessages.AUTH_INVALID);
    }
    String token = JwtTokenService.issue(user.id, user.username, user.role, ttlMillis, secret);
    log.info(Formatters.format(LogTemplates.AUTH_LOGIN, username, user.role));

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("token", token);
    Map<String, Object> userView = new LinkedHashMap<>();
    userView.put("id", user.id);
    userView.put("username", user.username);
    userView.put("displayName", user.displayName);
    userView.put("role", user.role);
    result.put("user", userView);
    return result;
  }
}
