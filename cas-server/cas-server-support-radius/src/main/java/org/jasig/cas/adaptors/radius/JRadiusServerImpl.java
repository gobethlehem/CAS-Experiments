/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.radius;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import net.sf.jradius.client.RadiusClient;
import net.sf.jradius.client.auth.PAPAuthenticator;
import net.sf.jradius.client.auth.RadiusAuthenticator;
import net.sf.jradius.dictionary.Attr_UserName;
import net.sf.jradius.dictionary.Attr_UserPassword;
import net.sf.jradius.exception.RadiusException;
import net.sf.jradius.exception.UnknownAttributeException;
import net.sf.jradius.packet.AccessAccept;
import net.sf.jradius.packet.AccessRequest;
import net.sf.jradius.packet.RadiusPacket;
import net.sf.jradius.packet.attribute.AttributeFactory;
import net.sf.jradius.packet.attribute.AttributeList;

/**
 * Implementation of a RadiusServer that utilizes the JRadius packages available
 * at <a href="http://jradius.sf.net">http://jradius.sf.net</a>.
 * 
 * @author Scott Battaglia
 * @version $Revision: 42053 $ $Date: 2007-06-10 09:17:55 -0400 (Sun, 10 Jun 2007) $
 * @since 3.1
 */
public final class JRadiusServerImpl implements RadiusServer {

    private static final Log LOG = LogFactory.getLog(JRadiusServerImpl.class);

    /** Default PAP Authenticator if no other one is specified. */
    private static final RadiusAuthenticator DEFAULT_RADIUS_AUTHENTICATOR = new PAPAuthenticator();

    /** Default port to do authentication on. */
    private static final int DEFAULT_AUTHENTICATION_PORT = 1812;

    /** Default port to do accounting on. */
    private static final int DEFAULT_ACCOUNTING_PORT = 1813;

    /** Default socket timeout time. */
    private static final int DEFAULT_SOCKET_TIMEOUT = 5;

    /** Default number of retries. */
    private static final int DEFAULT_NUMBER_OF_RETRIES = 3;

    /** The port to do accounting on. */
    private final int accountingPort;

    /** The port to do authentication on. */
    private final int authenticationPort;

    /** The timeout for sockets. */
    private final int socketTimeout;

    /** The conversion from hostname to an InetAddress. */
    private final InetAddress inetAddress;

    /** The shared secret to send to the RADIUS server. */
    private final String sharedSecret;

    /** The number of retries to do per authentication request. */
    private final int retries;

    /** The RADIUS Authenticator to use. */
    private final RadiusAuthenticator radiusAuthenticator;

    /** Load the dictionary implementation. */
    static {
        AttributeFactory
            .loadAttributeDictionary("net.sf.jradius.dictionary.AttributeDictionaryImpl");
    }

    /**
     * Simplest constructor to set the hostname and the shared secret. Uses
     * default values for everything else.
     * 
     * @param hostName the host name of the RADIUS server.
     * @param sharedSecret the shared secret with that server.
     * @throws UnknownHostException if the hostname cannot be resolved.
     */
    public JRadiusServerImpl(final String hostName, final String sharedSecret)
        throws UnknownHostException {
        this(hostName, sharedSecret, DEFAULT_RADIUS_AUTHENTICATOR,
            DEFAULT_AUTHENTICATION_PORT);
    }

    /**
     * Constructor to set the host name, shared secret and authentication type.
     * 
     * @param hostName the host name of the RADIUS server.
     * @param sharedSecret the shared secret with that server.
     * @param radiusAuthenticator the RADIUS authenticator to use.
     * @throws UnknownHostException if the hostname cannot be resolved.
     */
    public JRadiusServerImpl(final String hostName, final String sharedSecret,
        final RadiusAuthenticator radiusAuthenticator)
        throws UnknownHostException {
        this(hostName, sharedSecret, radiusAuthenticator,
            DEFAULT_AUTHENTICATION_PORT);
    }

    /**
     * Constructor that aceps the host name, shared secret, authenticaion type,
     * and port.
     * 
     * @param hostName the host name of the RADIUS server.
     * @param sharedSecret the shared secret with that server.
     * @param radiusAuthenticator the RADIUS authenticator to use.
     * @param authenticationPort the port to use to authenticate on.
     * @throws UnknownHostException if the hostname cannot be resolved.
     */
    public JRadiusServerImpl(final String hostName, final String sharedSecret,
        final RadiusAuthenticator radiusAuthenticator,
        final int authenticationPort) throws UnknownHostException {
        this(hostName, sharedSecret, radiusAuthenticator, authenticationPort,
            DEFAULT_ACCOUNTING_PORT);
    }

    /**
     * Constructor that aceps the host name, shared secret, authenticaion type,
     * authentication port, and accounting port.
     * 
     * @param hostName the host name of the RADIUS server.
     * @param sharedSecret the shared secret with that server.
     * @param radiusAuthenticator the RADIUS authenticator to use.
     * @param authenticationPort the port to use to authenticate on.
     * @param accountingPort the port to use to do accounting.
     * @throws UnknownHostException if the hostname cannot be resolved.
     */
    public JRadiusServerImpl(final String hostName, final String sharedSecret,
        final RadiusAuthenticator radiusAuthenticator,
        final int authenticationPort, final int accountingPort)
        throws UnknownHostException {
        this(hostName, sharedSecret, radiusAuthenticator, authenticationPort,
            accountingPort, DEFAULT_SOCKET_TIMEOUT, DEFAULT_NUMBER_OF_RETRIES);
    }

    public JRadiusServerImpl(final String hostName, final String sharedSecret,
        final RadiusAuthenticator radiusAuthenticator,
        final int authenticationPort, final int accountingPort,
        final int socketTimeout, final int retries) throws UnknownHostException {
        this.sharedSecret = sharedSecret;
        this.authenticationPort = authenticationPort;
        this.accountingPort = accountingPort;
        this.socketTimeout = socketTimeout;
        this.retries = retries;
        this.radiusAuthenticator = radiusAuthenticator;
        this.inetAddress = InetAddress.getByName(hostName);
    }

    public boolean authenticate(
        final UsernamePasswordCredentials usernamePasswordCredentials) {
        final RadiusClient radiusClient = getNewRadiusClient();

        final AttributeList attributeList = new AttributeList();
        attributeList.add(new Attr_UserName(usernamePasswordCredentials
            .getUsername()));
        attributeList.add(new Attr_UserPassword(usernamePasswordCredentials
            .getPassword()));

        final AccessRequest request = new AccessRequest(radiusClient,
            attributeList);

        try {
            final RadiusPacket response = radiusClient.authenticate(request,
                this.radiusAuthenticator, this.retries);

            // accepted
            if (response instanceof AccessAccept) {
                LOG.debug("Authentication request suceeded for host:"
                    + this.inetAddress.getCanonicalHostName()
                    + " and username "
                    + usernamePasswordCredentials.getUsername());
                return true;
            }

            // rejected
            LOG.debug("Authentication request failed for host:"
                + this.inetAddress.getCanonicalHostName() + " and username "
                + usernamePasswordCredentials.getUsername());
            return false;
        } catch (final UnknownAttributeException e) {
            throw new IllegalArgumentException(
                "Passed an unknown attribute to RADIUS client: "
                    + e.getMessage());
        } catch (final RadiusException e) {
            throw new IllegalStateException(
                "Received response that puts RadiusClient into illegal state: "
                    + e.getMessage());
        }
    }

    private RadiusClient getNewRadiusClient() {
        return new RadiusClient(this.inetAddress, this.sharedSecret,
            this.authenticationPort, this.accountingPort, this.socketTimeout);
    }
}
