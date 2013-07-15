package org.jasig.portal.portlets.backgroundpreference;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.portlet.ActionRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import javax.portlet.RenderRequest;
import javax.portlet.PortletPreferences;

import org.springframework.beans.factory.annotation.Autowired;

@Controller
@RequestMapping("VIEW")
public class ViewBackgroundPreferenceController { 
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String[] EMPTY_STRINGS = new String[]{};
    private static final String SELECTED_IMAGE_SUFFIX = "SelectedBackgroundImage";
    private static final String IMAGES_SUFFIX = "BackgroundImages";

    BackgroundImageSetSelectionStrategy imageSetSelectionStrategy = new MobileImageSelectionStrategyImpl();

    ViewSelectionStrategy viewSelectionStrategy = new MobileViewSelectionStrategyImpl();

    public void setImageSetSelectionStrategy(BackgroundImageSetSelectionStrategy imageSetSelectionStrategy) {
        this.imageSetSelectionStrategy = imageSetSelectionStrategy;
    }

    public void setViewSelectionStrategy(ViewSelectionStrategy viewSelectionStrategy) {
        this.viewSelectionStrategy = viewSelectionStrategy;
    }

    /**
     * Display the main user-facing view of the portlet.
     * 
     * @param request
     * @return
     */
	@RenderMapping
	public String getView(RenderRequest request, Model model){

        String imageSetName = imageSetSelectionStrategy.getImageSelectionCategory(request);
		PortletPreferences prefs = request.getPreferences();
        String[] images = prefs.getValues(imageSetName + IMAGES_SUFFIX, EMPTY_STRINGS);
		String preferredBackgroundImage = prefs.getValue(imageSetName + SELECTED_IMAGE_SUFFIX, "");
		model.addAttribute("backgroundImage", preferredBackgroundImage);
		model.addAttribute("images", images);
        model.addAttribute("overlayImageOn", prefs.getValue("overlayImageOn", null));
        model.addAttribute("applyOpacityTo", prefs.getValue("applyOpacityTo", null));
        model.addAttribute("opacityCssValue", prefs.getValue("opacityCssValue", "1.0"));
		return "/jsp/BackgroundPreference/" + viewSelectionStrategy.getViewName(request);
	}
	
    @ActionMapping(params = {"action=savePreferences"})
    public void savePreferences(ActionRequest request, @RequestParam String backgroundImage) throws Exception {
        String imageSetName = imageSetSelectionStrategy.getImageSelectionCategory(request);
        PortletPreferences prefs = request.getPreferences();
        prefs.setValue(imageSetName + SELECTED_IMAGE_SUFFIX, (backgroundImage != null ? backgroundImage : ""));
        prefs.store();
    }

}
