package com.huynqb.laundrylockerbackend.module.user.controller;

import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

  @RequestMapping("/")
  public String home() {
    return "Welcome to the Home Page!";
  }

  @RequestMapping(UriParamConstants.SECURED)
  public String secured() {
    return "Welcome to the Secured Page!";
  }
}
