package com.huynqb.laundrylockerbackend.core.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huynqb.laundrylockerbackend.core.constant.MessageConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.i18n.MessageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;
  private final MessageService messageService;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {

    log.warn("Unauthorized error: {}", authException.getMessage());

    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(HttpStatus.UNAUTHORIZED.value());

    ApiResponse<Void> apiResponse =
        ApiResponse.error(MessageConstants.E_COM004, messageService.get(MessageConstants.E_COM004));

    response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
  }
}
