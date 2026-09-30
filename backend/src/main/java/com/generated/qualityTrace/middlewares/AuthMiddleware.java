package com.generated.qualityTrace.middlewares;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.exceptions.ApiException;
import com.generated.qualityTrace.services.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证中间件：校验 Authorization: Bearer &lt;jwt&gt;。
 * 白名单（/health、登录）在 WebMvcConfig 中放行。
 */
@Component
public class AuthMiddleware implements HandlerInterceptor {

  private static final String BEARER = "Bearer ";

  private final JwtService jwtService;

  public AuthMiddleware(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith(BEARER)) {
      throw new ApiException(ErrorCodes.AUTH_REQUIRED, ErrorMessages.AUTH_REQUIRED, 401);
    }
    String token = header.substring(BEARER.length()).trim();
    JwtService.Claims claims = jwtService.verify(token);
    AuthContext.set(claims);
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                              Object handler, Exception ex) {
    AuthContext.clear();
  }
}
