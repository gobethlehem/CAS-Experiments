/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.integration.restlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.util.HttpClient;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.ticket.InvalidTicketException;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.springframework.beans.factory.annotation.Autowired;
import org.inspektr.common.ioc.annotation.NotNull;

/**
 * Implementation of a Restlet resource for creating Service Tickets from a 
 * TicketGrantingTicket, as well as deleting a TicketGrantingTicket.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.3
 *
 */
public final class TicketGrantingTicketResource extends Resource {
    
    private final static Log log = LogFactory.getLog(TicketGrantingTicketResource.class);

    @Autowired
    private CentralAuthenticationService centralAuthenticationService;
    
    private String ticketGrantingTicketId;

    @Autowired
    @NotNull
    private HttpClient httpClient;

    public void init(final Context context, final Request request, final Response response) {
        super.init(context, request, response);
        this.ticketGrantingTicketId = (String) request.getAttributes().get("ticketGrantingTicketId");
        this.getVariants().add(new Variant(MediaType.APPLICATION_WWW_FORM));
    }

    public boolean allowDelete() {
        return true;
    }

    public boolean allowPost() {
        return true;
    }

    public void setHttpClient(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void removeRepresentations() throws ResourceException {
        this.centralAuthenticationService.destroyTicketGrantingTicket(this.ticketGrantingTicketId);
        getResponse().setStatus(Status.SUCCESS_OK);
    }

    public void acceptRepresentation(final Representation entity)
        throws ResourceException {
        final Form form = getRequest().getEntityAsForm();
        final String serviceUrl = form.getFirstValue("service");
        try {
            final String serviceTicketId = this.centralAuthenticationService.grantServiceTicket(this.ticketGrantingTicketId, new SimpleWebApplicationServiceImpl(serviceUrl, this.httpClient));
            getResponse().setEntity(serviceTicketId, MediaType.TEXT_PLAIN);
        } catch (final InvalidTicketException e) {
            log.error(e,e);
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "TicketGrantingTicket could not be found.");
        } catch (final Exception e) {
            log.error(e,e);
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        }
    }
}
