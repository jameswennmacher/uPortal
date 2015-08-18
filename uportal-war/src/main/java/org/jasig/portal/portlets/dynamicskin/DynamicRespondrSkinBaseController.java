/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.portlets.dynamicskin;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract controller class containing common methods for the Dynamic Skin view and configuration controllers.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 * @since 4.2.1
 */

public class DynamicRespondrSkinBaseController {
    private static final String DEFAULT_SKIN_NAME = "defaultSkin";
    private static final String PREF_SKIN_NAME = DynamicSkinService.CONFIGURABLE_PREFIX + "dynamicSkinName";
    private static final String PREF_DYNAMIC = DynamicSkinService.CONFIGURABLE_PREFIX +"dynamicSkinEnabled";
    private static final String PREF_USER_ATTRIBUTE = DynamicSkinService.CONFIGURABLE_PREFIX + "dynamicSkinUserAttribute";
    private static final String DYNAMIC_SKIN_FILENAME_BASE = "skin";
    private static final MessageFormat CSS_PATH_FORMAT = new MessageFormat("{0}/{1}.css");

    @Autowired
    DynamicSkinService service;

    /**
     * Calculate the URL to the configured skin CSS file to use.  If dynamic skins enabled, generate the CSS
     * file if needed.
     *
     * dynamic=false: load the pre-built css file from the skins directory based on the configured LeSS filename;
     *                e.g. RELATIVE_ROOT/{defaultSkin}.css
     * dynamic=true: Process the default skin less file if needed at RELATIVE_ROOT/{defaultSkin}.less
     *               to create a customized skin css file (RELATIVE_ROOT/skin-ID#.css to load.
     * @param request
     * @param prefs
     * @return
     * @throws IOException
     */
    protected String getCssUrlAndCreateCssFileIfNeeded(PortletRequest request, PortletPreferences prefs) throws IOException {
        final Boolean dynamicSkin = Boolean.valueOf(prefs.getValue(PREF_DYNAMIC, "false"));
        final String lessFilename = prefs.getValue(PREF_SKIN_NAME, DEFAULT_SKIN_NAME);

        // If not dynamic, the CSS filename is the Less filename.  Otherwise it is the base name plus a hash of the
        // configured values.  This is necessary for multi-tenant scenarios where there is not a user attribute
        // specifying a discriminating name and the values in the portlet's definition distinguish one skin from
        // another.
        String cssName = dynamicSkin ? DYNAMIC_SKIN_FILENAME_BASE : lessFilename;
        boolean addHash = dynamicSkin;

        // If a user attribute is supposed to provide the name of the CSS skin file to use, use it instead of the
        // default and don't add a hash to the filename.  If the user attribute has no value, default to dynamic
        // behavior.
        final String attributeName = prefs.getValue(PREF_USER_ATTRIBUTE, "");
        if (dynamicSkin && StringUtils.isNotBlank(attributeName)) {
            final Map userInfo = (Map) request.getAttribute(PortletRequest.USER_INFO);
            final String name = (String)userInfo.get(attributeName);
            if (StringUtils.isNotBlank(name)) {
                cssName = name;
                addHash = false;
            }
        }

        cssName = addHash ? cssName + service.calculateTokenForCurrentSkin(prefs) : cssName;
        if (dynamicSkin) {
            generateCssIfNotExistsFromLess(request, lessFilename, cssName);
        }

        return calculateCssLocationInWebapp(cssName);
    }
    /**
     * Check to see if the CSS file exists on disk.  If not, generate it from the less file and specified preferences.
     *
     * @param request PortletRequest
     * @param lessfileBaseName name of the Less file to process
     * @param cssName Name of the css file to generate.
     * @return true if the CSS file was generated
     * @throws IOException
     */
    protected boolean generateCssIfNotExistsFromLess(PortletRequest request, String lessfileBaseName, String cssName)
                                                      throws IOException {
        final String locationOnDisk = calculateCssLocationOnDisk(request, cssName);
        boolean fileGenerated = false;

        if (!service.skinFileExists(locationOnDisk)) {
            // Trigger the LESS compilation
            service.generateSkinCssFile(request, locationOnDisk, cssName, lessfileBaseName);
            fileGenerated = true;
        }
        return fileGenerated;
    }

    /**
     * Calculates the location of the CSS file on disk.  The file may or may not actually exist.
     * @param request Portlet Request
     * @param cssFilename CSS filename to use
     * @return Path to the CSS file on disk.
     */
    private String calculateCssLocationOnDisk(PortletRequest request, String cssFilename) {
        final String relativePath = calculateCssLocationInWebapp(cssFilename);
        return request.getPortletSession().getPortletContext().getRealPath(relativePath);
    }

    /**
     * Calculates the relative URL of the CSS file.
     * @param cssFilename skin filename
     * @return Relative URL of the CSS file for the user.
     */
    private String calculateCssLocationInWebapp(String cssFilename) {
        return CSS_PATH_FORMAT.format(new Object[] {service.getRootFolder(), cssFilename});
    }

}
