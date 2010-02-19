package com.sourceallies.service;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class ServiceController {

    @RequestMapping("/secureRequest")
    public void mainRequest(HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        response.getWriter().println("Hello World");
    }

    @RequestMapping("/casLogin")
    public void casLogin(HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        response.getWriter().println("CAS Success");
    }

    @RequestMapping("/casFailed")
    public void casFailed(HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        response.getWriter().println("CAS Failed");
    }

    @RequestMapping("/casLogout")
    public String casLogout() {
        return "redirect:https://localhost:8443/cas/logout";
    }

}


