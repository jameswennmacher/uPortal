/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.security.provider;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.security.PortalSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Eric Dalquist
 */
public abstract class AbstractPersonManager implements IPersonManager {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Assigns the System account as the person for the incoming request if no person assigned.
     * WARNING!!! This account is not restricted.  Invoke this method with EXTREME care!
     * @param request the servlet request object
     */
    public void useSystemAccount (HttpServletRequest request) throws PortalSecurityException {
        HttpSession session = request.getSession(false);
        IPerson person = null;
        // Return the person object if it exists in the user's session
        if (session != null && session.getAttribute(PERSON_SESSION_KEY) == null) {
            try {
                // Create a system person account
                person = PersonFactory.createSystemPerson();
            } catch (Exception e) {
                // Log the exception
                logger.error("Exception creating System person.", e);
            }
            // Add this person object to the user's session
            if (person != null && session != null)
                session.setAttribute(PERSON_SESSION_KEY, person);
        }
    }
}