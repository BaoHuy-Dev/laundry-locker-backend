package com.huynqb.laundrylockerbackend.module.user.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = TagConstants.ROOT_TAG_ADMIN)
@RequestMapping(UriParamConstants.ROOT_URI_ADMIN)
@RestController
public class AdminController {

  @GetMapping(UriParamConstants.HELLO)
  @PreAuthorize("hasRole('ADMIN')")
  public String helloAdmin() {
    return "Hello, admin!";
  }
}
