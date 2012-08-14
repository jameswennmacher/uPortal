package org.jasig.portal.persondir.unicon;

import java.util.HashMap;
import java.util.Map;

public class UniconPersonQuery {
    
    private final Map<String, String> attributes = new HashMap<String, String>();
    
    public void addAttribute(String name, Object value) {
        attributes.put(name, value.toString().toLowerCase());
    }
    
    public Map<String, String> getAttributes() {
        return attributes;
    }

}
