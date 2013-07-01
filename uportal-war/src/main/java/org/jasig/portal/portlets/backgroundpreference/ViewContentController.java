package org.jasig.portal.portlets.backgroundpreference;

import javax.portlet.PortletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RenderMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("VIEW")
public class ViewContentController { 
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Display the main user-facing view of the portlet.
     * 
     * @param request
     * @return
     */
    
    @RequestMapping
    public String viewContent() {
    	logger.warn("getting viewContent");
    	return "jsp/BackgroundPreference/viewContent";
    }
    
    /**
     * Get the configured user-facing content for this portlet configuration.
     * 
     * @param request
     * @return
     */
    @ModelAttribute("content")
    public String getContent(PortletRequest request){
    	logger.warn("getting content");
    	return "hello";
    }
    
}
