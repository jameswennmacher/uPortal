package org.jasig.portal.portlets.search.confluence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlets.search.IPortalSearchService;
import org.jasig.portal.search.SearchRequest;
import org.jasig.portal.search.SearchResult;
import org.jasig.portal.search.SearchResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class ConfluenceSearchService implements IPortalSearchService {
	
	protected Log log = LogFactory.getLog(getClass());

    private RestTemplate restTemplate;
    
    @Autowired(required = true)
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    private String urlTemplate = "https://confluence.unicon.net/confluence/rest/prototype/1/search.json?query={query}";
    
    public void setUrlTemplate(String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }

    @Override
    public SearchResults getSearchResults(PortletRequest request,
            SearchRequest query) {
        
        final Map<String,String> userInfo = (Map<String,String>) request.getAttribute(PortletRequest.USER_INFO);
        final String username = request.getRemoteUser();
        final String password = userInfo.get("password");
        
        final Map<String, String> vars = new HashMap<String, String>();
        vars.put("query", query.getSearchTerms());

        final HttpHeaders requestHeaders = new HttpHeaders();
        final String authString = username.concat(":").concat(password);
        final String encodedAuthString = new Base64().encodeToString(authString
                .getBytes());
        requestHeaders.set("Authorization", "Basic ".concat(encodedAuthString));
        final HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

        final SearchResults results =  new SearchResults();

        try {
	        final HttpEntity<Map> response = restTemplate.exchange(urlTemplate,
	                HttpMethod.GET, requestEntity, Map.class, vars);
	
	        final List<Map<String, Object>> matches = (List<Map<String, Object>>) response.getBody().get("result");
	
	        for (Map<String, Object> match : matches) {
	            
	            final SearchResult result = new SearchResult();
	            result.setTitle((String) match.get("title"));
	                
	            final List<Map<String,Object>> links = (List<Map<String, Object>>) match.get("link");
	            if (links.size() > 0) {
	                result.setExternalUrl((String) links.get(0).get("href"));
	            }
	            result.getType().add(this.resultType);
	            results.getSearchResult().add(result);
	            
	        }
	        
	        results.setQueryId(query.getQueryId());
	        results.setWindowId(request.getWindowID());
        
        } catch (Exception ex) {
        	log.error("Failed to retrieve search results from Confluence", ex);
        }
        
        return results;
    }

    private String resultType = "wiki";
    
    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

}
