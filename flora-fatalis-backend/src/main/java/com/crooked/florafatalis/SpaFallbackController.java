package com.crooked.florafatalis;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Controller
public class SpaFallbackController {

  @RequestMapping(
      value = {"/{path:[^\\.]*}", "/{path:[^\\.]*}/**"},
      method = RequestMethod.GET)
  public String forward(HttpServletRequest request) throws NoResourceFoundException {
    String path = request.getServletPath();
    if (path.equals("/api") || path.startsWith("/api/")) {
      throw new NoResourceFoundException(HttpMethod.GET, path);
    }
    return "forward:/index.html";
  }
}
