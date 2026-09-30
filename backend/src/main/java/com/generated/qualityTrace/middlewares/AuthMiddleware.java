package com.generated.qualityTrace.middlewares;

import com.generated.qualityTrace.config.CurrentUser;
import com.generated.qualityTrace.config.UserContext;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.utils.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 认证中间件：解析 Authorization: Bearer <token>，校验 JWT 后写入 {@link UserContext}。
 * 登录接口与健康检查放行；缺失/非法凭证返回 401。
 */
@Component
public class AuthMiddleware extends OncePerRequestFilter {

  private final String secret;

  public AuthMiddleware(@Value("${JWT_SECRET:local-dev-secret}") String secret) {
    this.secret = secret;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {
    String path = request.getRequestURI();
    if (isPublic(path)) {
      chain.doFilter(request, response);
      return;
    }

    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) {
      writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
          ErrorCodes.AUTH_REQUIRED, ErrorMessages.AUTH_REQUIRED);
      return;
    }

    String token = header.substring(7);
    String[] claims = JwtTokenService.verify(token, secret);
    if (claims == null) {
      writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
          ErrorCodes.AUTH_INVALID, ErrorMessages.AUTH_INVALID);
      return;
    }

    try {
      Long userId = Long.parseLong(claims[0]);
      String username = claims[1];
      String role = claims[2];
      UserContext.set(new CurrentUser(userId, username, role));
      chain.doFilter(request, response);
    } finally {
      UserContext.clear();
    }
  }

  private boolean isPublic(String path) {
    return path.equals("/health")
        || path.equals("/api/auth/login")
        || !path.startsWith("/api/");
  }

  static void writeError(HttpServletResponse response, int status, String code, String message)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    String body = "{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}";
    response.getWriter().write(body);
  }
}
