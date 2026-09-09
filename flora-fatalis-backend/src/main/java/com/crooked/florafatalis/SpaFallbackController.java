package com.crooked.florafatalis;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SpaFallbackController {

  @RequestMapping(
      value = {"/{path:[^\\.]*}", "/{path:[^\\.]*}/**"},
      method = RequestMethod.GET)
  public String forward() {
    return "forward:/index.html";
  }
}
