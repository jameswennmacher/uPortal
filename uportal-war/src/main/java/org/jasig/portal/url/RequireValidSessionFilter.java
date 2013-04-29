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

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.security.IPersonManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Redirects the user to the Login servlet if they don't already have a session, retaining the originally-requested
 * URL in a refUrl parameter.  If an external login Url and encoder are specified the user will be redirected to it
 * instead for authentication.
 * 
 * @author Eric Dalquist
 * @author James Wennmacher, jwennmacher@unicon.net
 */
public class RequireValidSessionFilter extends OncePerRequestFilter {

    static final String PORTLET_PATH_SEPARATOR = "/p/";

    // Specifies an external url to redirect to for user authentication (typically CAS)
    private String externalLoginUrl;
    // Specifies an encoder to encode the originally-requested Url.
    private LoginRefUrlEncoder externalLoginRefUrlEncoder;
    private LoginRefUrlEncoder localLoginUrlEncoder = new LocalLoginRefUrlEncoder();
    private IPersonManager personManager;

    @Autowired
    private IUrlSyntaxProvider urlSyntaxProvider;

    private String forceLoginParameterName = "forceUpExternalLogin";

    public void setExternalLoginUrl(String externalLoginUrl) {
        this.externalLoginUrl = externalLoginUrl;
    }

    public void setExternalLoginRefUrlEncoder(LoginRefUrlEncoder urlEncoder) {
        this.externalLoginRefUrlEncoder = urlEncoder;
    }

    public void setLocalLoginUrlEncoder(LoginRefUrlEncoder localLoginUrlEncoder) {
        this.localLoginUrlEncoder = localLoginUrlEncoder;
    }

    public void setForceLoginParameterName(String forceLoginParameterName) {
        this.forceLoginParameterName = forceLoginParameterName;
    }

    public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
    }

    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        final HttpSession session = request.getSession(false);
        if (session != null && !session.isNew()) {
            //Session exists and is not new, don't bother filtering
            return true;
        }
        
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        //Assume shouldNotFilter was called first and returned false, session is invalid and user needs login
        
        String loginRedirectUrl;
        
        if (StringUtils.isNotBlank(externalLoginUrl) && externalLoginRefUrlEncoder != null
                && (urlIsRestrictedAccess(request) || checkForForceLoginParam(request))) {
            loginRedirectUrl = externalLoginRefUrlEncoder.encodeLoginAndRefUrl(request, externalLoginUrl);
        } else {
            loginRedirectUrl = localLoginUrlEncoder.encodeLoginAndRefUrl(request, request.getContextPath() + "/Login");
        }
        response.sendRedirect(response.encodeRedirectURL(loginRedirectUrl));
    }

    // Return true if the url contains a parameter indicating we should force a login
    private boolean checkForForceLoginParam(HttpServletRequest request) {
        String forceLogin = request.getParameter(forceLoginParameterName);
        return "true".equals(forceLogin);
    }

    /**
     * Determines if the url is a restricted URL.  It does this by comparing the canonicalized url of the admin
     * user and of a guest user.  If they are identical then the url is not restricted.
     * @param request
     * @return
     */
    private boolean urlIsRestrictedAccess(HttpServletRequest request) {
        try {
            // Create a session and a wrapped request.  The wrapped request insures no attributes saved to the request are
            // retained, ESPECIALLY while as the super account.
            request.getSession(true);
            IsolatingHttpServletRequestWrapper wrappedRequest = new IsolatingHttpServletRequestWrapper(request);

            // First determine the canonical url for an unrestricted account.
            personManager.useSystemAccount(wrappedRequest);
            String unrestrictedUserUrl = trimFoldersAndSubscribeIdInfo(urlSyntaxProvider.getCanonicalUrl(wrappedRequest));

            // Now, invalidate session and recreate it.  This insures nothing from the previous calls, especially
            // anything related to the super user, is persisted.
            request.getSession().invalidate();
            request.getSession(true);
            String guestUserUrl = trimFoldersAndSubscribeIdInfo(urlSyntaxProvider.getCanonicalUrl(request));
            return !guestUserUrl.equalsIgnoreCase(unrestrictedUserUrl);

        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        } finally {
            if (request.getSession(false) != null) {
                request.getSession().invalidate();
            }
        }
        return true;
    }


    /**
     * Filters out folder and subscriber id to make url comparable for access permission decisions.
     *
     * Use Cases:
     * 1. /uPortal
     * 2. /uPortal/
     * 3. /uPortal/f/Welcome/normal/render.uP
     * 4. porlet-specified (id of subitem optional)
     *    /uPortal/p/test-portlet-1.ctf1/max/render.uP?pP_testId=1
     * 5. porlet and single or multi-level folder specified (id of subitem optional)
     *    /uPortal/f/folder[/folder]+/p/test-portlet-1.ctf1/max/render.uP?pP_testId=1
     * 6. portlet and subitem specified as parameters
     *    /uPortal/f/s5/normal/render.uP?pCt=RestrictedAnnouncementsDisplay.n18&pP_action=displayFullAnnouncement&pP_announcementId=1
     *    Not currently handled. May or may not force auth depending upon url.
     *    If s5 is in the admin's layout, then auth will be required because the above would be turned into
     *       admin: /uPortal/render.uP?pCt=RestrictedAnnouncementsDisplay.n18&pP_action=displayFullAnnouncement&pP_announcementId=1
     *       guest: /uPortal/p/AnnouncementsDisplay/max/render.uP?pP_announcementId=1&pP_action=displayFullAnnouncement
     *
     * @param fullUrl canonical url as returned from UrlSyntaxProvider
     * @return url with user-specific information such as folders and subscriberId removed
     **/
    private String trimFoldersAndSubscribeIdInfo(String fullUrl) {
        // Remove any folder and subfolder names
        String cleanedUrl = fullUrl.replaceFirst("/f/.*?/p/", "/p/");
        // Scenario 3, 5, 6
        cleanedUrl = cleanedUrl.replaceFirst("/f/.*/","/");

        // If there is a subscribeId in the portlet name then remove it
        int portletIndicator = cleanedUrl.indexOf(PORTLET_PATH_SEPARATOR);
        int dot = cleanedUrl.indexOf(".", portletIndicator + PORTLET_PATH_SEPARATOR.length());
        int slash = cleanedUrl.indexOf("/", portletIndicator + PORTLET_PATH_SEPARATOR.length());
        if (dot != -1 && dot < slash) {
            cleanedUrl = cleanedUrl.substring(0, dot) + cleanedUrl.substring(slash);
        }

        return cleanedUrl;
    }
}
