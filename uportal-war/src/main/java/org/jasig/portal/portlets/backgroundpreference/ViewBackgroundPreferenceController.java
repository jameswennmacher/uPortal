package org.jasig.portal.portlets.backgroundpreference;

import java.util.List;
import java.util.Enumeration;
import javax.portlet.PortletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import org.springframework.web.portlet.ModelAndView;

import javax.portlet.RenderRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.PortletPreferences;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@Controller
@RequestMapping("VIEW")
public class ViewBackgroundPreferenceController { 
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired(required=true)
	private ApplicationContext applicationContext;
	
    /**
     * Display the main user-facing view of the portlet.
     * 
     * @param request
     * @return
     */
	@RenderMapping
	public ModelAndView getView(RenderRequest request){
		final ModelAndView mv = new ModelAndView();
		final List<String> images =  (List<String>) applicationContext.getBean("backgroundPreferenceImages", List.class);

		PortletPreferences prefs = request.getPreferences();
		String preferedBackgroundImage = prefs.getValue("backgroundImage", "");
		mv.addObject("backgroundImage", preferedBackgroundImage);
		mv.addObject("images", images);
		mv.setView("jsp/BackgroundPreference/viewBackgroundPreference");
		return mv;
	}    
	
    @RequestMapping(params = {"action=savePreferences"})
    public void savePreferences(ActionRequest request, ActionResponse response) throws Exception {
        PortletPreferences prefs = request.getPreferences();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            String parameterValue = request.getParameter(parameterName);
            prefs.setValue(parameterName, (parameterValue != null ? parameterValue : ""));
            prefs.store();
        }
    }
}
