package com.sourceallies.user;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jasig.cas.client.util.CommonUtils;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;


@Controller
public class UserController {

    @RequestMapping("/secure/makeRequest")
    public void makeRequest(HttpServletResponse response) throws IOException {
        HttpClient client = new HttpClient();

        /*CasAuthenticationToken token = (CasAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String ticket = token.getAssertion().getPrincipal().getProxyTicketFor("https://localhost:8443/service/login/cas");
        


        HttpMethod m = new GetMethod("https://localhost:8443/service/login/cas?ticket=" + ticket);
        m.setFollowRedirects(false);

        client.executeMethod(m);

        PrintWriter out = response.getWriter();
        renderResponse(m, out);

        HttpMethod serviceRequest = new GetMethod("https://localhost:8443/service/secureRequest.htm");
        serviceRequest.setFollowRedirects(false);

        client.executeMethod(serviceRequest);

        renderResponse(serviceRequest, out);*/
//        String targetUrl = "https://sso.lifengyun.com:8443/service/secureRequest.htm";
        String targetUrl = "http://localhost:8080/service/secureRequest.htm";
        CasAuthenticationToken token = (CasAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String ticket = token.getAssertion().getPrincipal().getProxyTicketFor(targetUrl);
        HttpMethod m = new GetMethod(targetUrl+"?ticket=" + ticket);
        m.setFollowRedirects(false);
        client.executeMethod(m);
        PrintWriter out = response.getWriter();
        renderResponse(m, out);
        
        m = new GetMethod(targetUrl);
        m.setFollowRedirects(false);
        client.executeMethod(m);
        out = response.getWriter();
        renderResponse(m, out);
    }

    private void renderResponse(HttpMethod m, PrintWriter out) throws IOException {
        out.println("Response For: " + m.getPath());
        out.println("Response Code: " +  m.getStatusCode());
        out.println("Reason Phrase: " +  m.getStatusLine().getReasonPhrase());
        out.println("Response Headers:");
        for(Header h: m.getResponseHeaders()) {
            out.println("\t" + h.getName() + ":" + h.getValue());
        }
        out.println("Body: " +  m.getResponseBodyAsString());
        out.println();
        out.println();
        out.println();
    }
}
