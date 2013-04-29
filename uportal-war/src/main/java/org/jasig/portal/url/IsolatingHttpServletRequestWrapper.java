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

package org.jasig.portal.url;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.ArrayEnumerator;
import org.jasig.portal.utils.web.AbstractHttpServletRequestWrapper;

import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.*;

/**
 * Wrapper around an HttpRequest to catch updates to the request object so they can be discarded.  Used because
 * current UrlSyntaxProviderImpl code places several objects into session.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */
public class IsolatingHttpServletRequestWrapper extends HttpServletRequestWrapper {

    protected final Log logger = LogFactory.getLog(this.getClass());

    private final Map<String, Object> additionalAttributes = new HashMap<String,Object>();

    /**
     * Construct a writable this.request wrapping a real this.request
     * @param request Request to wrap, can not be null.
     */
    public IsolatingHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public Object getAttribute(String name) {
        final Object value = this.additionalAttributes.get(name);
        return value == null ? super.getAttribute(name) : value;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        Set names = additionalAttributes.keySet();
        Enumeration<String> requestAttrs = super.getAttributeNames();
        while (requestAttrs.hasMoreElements()) {
            names.add(requestAttrs.nextElement());
        }
        return Collections.enumeration(names);
    }

    @Override
    public void setAttribute(String name, Object o) {
        additionalAttributes.put(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        additionalAttributes.remove(name);
        // Don't remove from real request
    }
}