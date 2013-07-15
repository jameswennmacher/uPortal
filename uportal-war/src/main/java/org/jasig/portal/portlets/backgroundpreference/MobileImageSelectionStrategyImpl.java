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
 * Determines set of images to display by checking to see if user is in a mobile user group as determined by the
 * portal's security roles.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */
@Component
public class MobileImageSelectionStrategyImpl implements BackgroundImageSetSelectionStrategy {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String DEFAULT_DEVICE = "default";
    private static final String MOBILE_DEVICE = "mobile";
    private static final String MOBILE_DEVICE_USER = "mobileUserGroup";

    public String getImageSelectionCategory (PortletRequest request) {
        return request.isUserInRole(MOBILE_DEVICE_USER) ? MOBILE_DEVICE : DEFAULT_DEVICE;
    }

}
