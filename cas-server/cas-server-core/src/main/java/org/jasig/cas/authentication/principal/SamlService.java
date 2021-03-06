/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.util.HttpClient;
import org.springframework.util.StringUtils;

/**
 * Class to represent that this service wants to use SAML. We use this in
 * combination with the CentralAuthenticationServiceImpl to choose the right
 * UniqueTicketIdGenerator.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.6 $ $Date: 2007/02/27 19:31:58 $
 * @since 3.1
 */
public final class SamlService extends AbstractWebApplicationService {

    /** Constant representing service. */
    private static final String CONST_PARAM_SERVICE = "TARGET";

    /** Constant representing artifact. */
    private static final String CONST_PARAM_TICKET = "SAMLart";
    
    private static final String CONST_START_ARTIFACT_XML_TAG = "<samlp:AssertionArtifact>";
    
    private static final String CONST_END_ARTIFACT_XML_TAG = "</samlp:AssertionArtifact>";

    /**
     * Unique Id for serialization.
     */
    private static final long serialVersionUID = -6867572626767140223L;

    protected SamlService(final String id) {
        super(id, id, null, new HttpClient());
    }

    protected SamlService(final String id, final String originalUrl,
        final String artifactId, final HttpClient httpClient) {
        super(id, originalUrl, artifactId, httpClient);
    }

    /**
     * This always returns true because a SAML Service does not receive the TARGET value on validation.
     */
    public boolean matches(final Service service) {
        return true;
    }

    public static SamlService createServiceFrom(
        final HttpServletRequest request, final HttpClient httpClient) {
        final String service = request.getParameter(CONST_PARAM_SERVICE);
        final String artifactId;
        final String requestBody = getRequestBody(request);
        
        if (!StringUtils.hasText(service) && !StringUtils.hasText(requestBody)) {
            return null;
        }

        final String id = cleanupUrl(service);
        
        if (StringUtils.hasText(requestBody)) {
            final int startTagLocation = requestBody.indexOf(CONST_START_ARTIFACT_XML_TAG);
            final int artifactStartLocation = startTagLocation + CONST_START_ARTIFACT_XML_TAG.length();
            final int endTagLocation = requestBody.indexOf(CONST_END_ARTIFACT_XML_TAG);

            artifactId = requestBody.substring(artifactStartLocation, endTagLocation).trim();
        } else {
            artifactId = null;
        }

        return new SamlService(id, service, artifactId, httpClient);
    }

    public Response getResponse(final String ticketId) {
        final Map<String, String> parameters = new HashMap<String, String>();

        parameters.put(CONST_PARAM_TICKET, ticketId);
        parameters.put(CONST_PARAM_SERVICE, getOriginalUrl());

        return Response.getRedirectResponse(getOriginalUrl(), parameters);
    }
    
    protected static String getRequestBody(final HttpServletRequest request) {
        final StringBuilder builder = new StringBuilder();
        try {
            final BufferedReader reader = request.getReader();
            
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (final Exception e) {
           return null;
        }
    }
}
