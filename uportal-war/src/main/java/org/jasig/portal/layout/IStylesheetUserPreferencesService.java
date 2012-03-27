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

package org.jasig.portal.layout;

import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.IUserProfile;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;

/**
 * Provides access to stylesheet user preference data. This is how any runtime code that needs access to theme or structure
 * stylesheet user preferences. All of the details around default values, scoping and persistence are taken care of here.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IStylesheetUserPreferencesService {
    public enum PreferencesScope {
        THEME {
            @Override
            public int getStylesheetId(IUserProfile userProfile) {
                return userProfile.getThemeStylesheetId();
            }
            @Override
            public IStylesheetUserPreferences getDistributedIStylesheetUserPreferences(IUserLayout userLayout) {
                return userLayout.getDistributedThemeStylesheetUserPreferences();
            }
        },
        STRUCTURE {
            @Override
            public int getStylesheetId(IUserProfile userProfile) {
                return userProfile.getStructureStylesheetId();
            }
            @Override
            public IStylesheetUserPreferences getDistributedIStylesheetUserPreferences(IUserLayout userLayout) {
                return userLayout.getDistributedStructureStylesheetUserPreferences();
            }
        };
        
        public abstract int getStylesheetId(IUserProfile userProfile);
        public abstract IStylesheetUserPreferences getDistributedIStylesheetUserPreferences(IUserLayout userLayout);
        
    }
    
    /**
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * @return The stylesheet descriptor for the current request and scope
     */
    public IStylesheetDescriptor getStylesheetDescriptor(HttpServletRequest request, PreferencesScope prefScope);
    
    /**
     * Get an output property
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#getOutputProperty(String)
     */
    public String getOutputProperty(HttpServletRequest request, PreferencesScope prefScope, String name);
    
    /**
     * Set an output property
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#setOutputProperty(String, String)
     */
    public String setOutputProperty(HttpServletRequest request, PreferencesScope prefScope, String name, String value);
    
    /**
     * Remove an output property
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#removeOutputProperty(String)
     */
    public String removeOutputProperty(HttpServletRequest request, PreferencesScope prefScope, String name);
    
    /**
     * Add all output properties to the provided Properties object
     * 
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#populateOutputProperties(Properties)
     */
    public Properties populateOutputProperties(HttpServletRequest request, PreferencesScope prefScope, Properties properties);
    
    /**
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#clearOutputProperties()
     */
    public void clearOutputProperties(HttpServletRequest request, PreferencesScope prefScope);
    

    /**
     * Get a stylesheet parameter
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#getStylesheetParameter(String)
     */
    public String getStylesheetParameter(HttpServletRequest request, PreferencesScope prefScope, String name);
    
    /**
     * Set a transformer parameter
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#setStylesheetParameter(String, String)
     */
    public String setStylesheetParameter(HttpServletRequest request, PreferencesScope prefScope, String name, String value);
    
    /**
     * Remove a transformer parameter
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#removeStylesheetParameter(String)
     */
    public String removeStylesheetParameter(HttpServletRequest request, PreferencesScope prefScope, String name);
    
    /**
     * @return An iterable of all property names
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#getStylesheetParameter(String)
     */
    public Iterable<String> getStylesheetParameterNames(HttpServletRequest request, PreferencesScope prefScope);
    
    /**
     * Add all stylesheet parameters to the provided Map
     * 
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#populateStylesheetParameters(Map)
     */
    public Map<String, String> populateStylesheetParameters(HttpServletRequest request, PreferencesScope prefScope, Map<String, String> stylesheetParameters);
    
    /**
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#clearStylesheetParameters()
     */
    public void clearStylesheetParameters(HttpServletRequest request, PreferencesScope prefScope);

    
    /**
     * Get a layout attribute
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#getLayoutAttribute(String, String)
     */
    public String getLayoutAttribute(HttpServletRequest request, PreferencesScope prefScope, String nodeId, String name);
    
    /**
     * Set an attribute to add to a layout folder
     *  
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#setLayoutAttribute(String, String, String)
     */
    public String setLayoutAttribute(HttpServletRequest request, PreferencesScope prefScope, String nodeId, String name, String value);
    
    /**
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#removeLayoutAttribute(String, String)
     */
    public String removeLayoutAttribute(HttpServletRequest request, PreferencesScope prefScope, String nodeId, String name);
    
    
    /**
     * @return An iterable of all layout nodeIds
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     */
    public Iterable<String> getLayoutAttributeNodeIds(HttpServletRequest request, PreferencesScope prefScope);
    
    /**
     * Add all layout attributes for the specified nodeId to the provided Map
     * 
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#populateLayoutAttributes(String, Map)
     */
    public Map<String, String> populateLayoutAttributes(HttpServletRequest request, PreferencesScope prefScope, String nodeId, Map<String, String> layoutAttributes);
    
    /**
     * Add all layout attributes for all nodeIds to the provided Map
     * 
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#populateAllLayoutAttributes(Map)
     */
    public Map<String, Map<String, String>> populateAllLayoutAttributes(HttpServletRequest request, PreferencesScope prefScope, Map<String, Map<String, String>> allLayoutAttributes);
    
    /**
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#clearLayoutAttributes(String)
     */
    public void clearLayoutAttributes(HttpServletRequest request, PreferencesScope prefScope, String nodeId);

    
    /**
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * 
     * @see IStylesheetUserPreferences#clearAllLayoutAttributes()
     */
    public void clearAllLayoutAttributes(HttpServletRequest request, PreferencesScope prefScope);

}
