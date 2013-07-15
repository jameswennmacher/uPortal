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

package org.jasig.portal.portlets.backgroundpreference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.portlet.PortletRequest;

/**
 * This strategy determines appropriate views to render by determining if the user is in a portal 'mobile device'
 * group.  This implementation allows the portlet to delegate user agent inspection to the portal.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */
@Component
public class MobileViewSelectionStrategyImpl implements ViewSelectionStrategy {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String MOBILE_DEVICE_USER = "mobileUserGroup";
    private static final String DEFAULT_VIEW = "viewBackgroundPreference";
    private static final String MOBILE_VIEW = "viewBackgroundPreferenceMobile";

    /*
     * Returns a view name based on the theme
     */
    @Override
    public String getViewName(PortletRequest request) {
        return isMobile(request) ? MOBILE_VIEW : DEFAULT_VIEW;
    }

    @Override
    public String getEditViewName(PortletRequest request) {
        if (isMobile(request)) {
            return "edit-jQM";
        } else {
            return "edit";
        }
    }

    protected boolean isMobile(PortletRequest request) {
        return request.isUserInRole(MOBILE_DEVICE_USER);
    }

}
