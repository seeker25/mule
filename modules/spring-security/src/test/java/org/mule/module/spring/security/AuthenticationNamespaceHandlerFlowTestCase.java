/*
 * $Id: AuthenticationNamespaceHandlerTestCase.java 22414 2011-07-14 13:24:46Z dirk.olmes $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.spring.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.mule.api.config.MuleProperties;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.security.SecurityProvider;
import org.mule.construct.Flow;
import org.mule.module.spring.security.filters.http.HttpBasicAuthenticationFilter;
import org.mule.security.MuleSecurityManager;
import org.mule.service.ServiceCompositeMessageSource;
import org.mule.tck.junit4.FunctionalTestCase;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

public class AuthenticationNamespaceHandlerFlowTestCase extends AuthenticationNamespaceHandlerServiceTestCase
{
    @Override
    protected String getConfigResources()
    {
        return "authentication-config-flow.xml";
    }
 
    @Test
    public void testEndpointConfiguration()
    {
        Flow flow = muleContext.getRegistry().lookupObject("echo");
        assertNotNull(flow);       

        ImmutableEndpoint endpoint = (ImmutableEndpoint) flow.getMessageSource();
        assertNotNull(endpoint.getSecurityFilter());
        assertEquals(HttpBasicAuthenticationFilter.class, endpoint.getSecurityFilter().getClass());
    }
}
