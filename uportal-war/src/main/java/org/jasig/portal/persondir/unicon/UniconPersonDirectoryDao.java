package org.jasig.portal.persondir.unicon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao;
import org.jasig.services.persondir.support.CaseInsensitiveNamedPersonImpl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UniconPersonDirectoryDao extends
        AbstractQueryPersonAttributeDao<UniconPersonQuery> {
    
    protected final Log log = LogFactory.getLog(getClass());
    
    protected final String UNICON_PERSON_CACHE_PREFIX = "uniconperson.";
    protected final String UNICON_PERSON_LIST_CACHE_KEY = "uniconers";
    
    protected Cache cache;
    
    public void setCache(Cache cache) {
        this.cache = cache;
    }
    
    protected RestTemplate restTemplate;
    
    public UniconPersonDirectoryDao() {
        restTemplate = new RestTemplate();
        final MappingJacksonHttpMessageConverter converter = new MappingJacksonHttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        restTemplate.setMessageConverters(Collections.<HttpMessageConverter<?>>singletonList(converter));
    }

    @Scheduled(fixedRate=90000)
    protected void fetchUserList() {

        log.debug("Fetching unicon person attribute feed");
        final HttpHeaders requestHeaders = new HttpHeaders();
        final String authString = "apiuser".concat(":").concat("v31er@n2");
        final String encodedAuthString = new Base64().encodeToString(authString
                .getBytes());
        requestHeaders.set("Authorization", "Basic ".concat(encodedAuthString));
        final HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

        try {
	        final HttpEntity<Map> response = restTemplate.exchange("https://api.unicon.net/unisearch/v1/?filter=all",
	                HttpMethod.GET, requestEntity, Map.class);
	        log.debug("Received response from Unicon contacts server");
	        log.debug("Response has body: " + response.hasBody());
	        
	        @SuppressWarnings("unchecked")
	        final Map<String, Object> map = response.getBody();
	        
	        @SuppressWarnings("unchecked")
	        final List<Map<String, Object>> uniconers = (List<Map<String, Object>>) map.get("result");
	        cache.put(new Element(UNICON_PERSON_LIST_CACHE_KEY, uniconers));
	        
	        final List<IPersonAttributes> people = new ArrayList<IPersonAttributes>();
	        
	        for (Map<String, Object> entry : uniconers) {
	            
	            final Map<String, List<Object>> attributes = new HashMap<String, List<Object>>();
	            for (Map.Entry<String, Object> attribute : entry.entrySet()) {
	                attributes.put(attribute.getKey(), Collections.singletonList(attribute.getValue()));
	            }
	            final String displayName = ((String) entry.get("first_name") + " " + entry.get("last_name"));
	            attributes.put("displayName", Collections.singletonList((Object) displayName));
	            
	            final String username = (String) entry.get("username");
	            final IPersonAttributes person = new CaseInsensitiveNamedPersonImpl(username, attributes);
	            people.add(person);
	            final String cacheKey = UNICON_PERSON_CACHE_PREFIX.concat(username);
	            cache.put(new Element(cacheKey, person));
	        }
	        cache.put(new Element(UNICON_PERSON_LIST_CACHE_KEY, people));
        
        } catch (Exception e) {
        	log.error("Unable to collect person attributes from Unicon API", e);
        }
        
    }
    
    @Override
    protected UniconPersonQuery appendAttributeToQuery(
            UniconPersonQuery qb, String dataAttribute, List<Object> queryValues) {

        if (qb == null) {
            qb = new UniconPersonQuery();
        }
        if (queryValues.size() > 0) {
            qb.addAttribute(dataAttribute, queryValues.get(0));
        }
        
        return qb;
    }

    @Override
    protected List<IPersonAttributes> getPeopleForQuery(
            UniconPersonQuery qb, String username) {
        
        if (qb.getAttributes().containsKey("username")) {
            final String cacheKey = UNICON_PERSON_CACHE_PREFIX.concat(qb.getAttributes().get("username"));
            final Element element = cache.get(cacheKey);
            if (element != null) {
                final IPersonAttributes person = (IPersonAttributes) element.getValue();
                return Collections.singletonList(person);
            }
        } 
        
        else {
            final Element element = cache.get(UNICON_PERSON_LIST_CACHE_KEY);
            if (element != null) {
                @SuppressWarnings("unchecked")
                final List<IPersonAttributes> uniconers = (List<IPersonAttributes>) element.getValue();
                
                final List<IPersonAttributes> results = new ArrayList<IPersonAttributes>();
                for (IPersonAttributes uniconer : uniconers) {
                    for (Map.Entry<String, String> searchAttribute : qb.getAttributes().entrySet()) {
                        final String value = (String) uniconer.getAttributeValue(searchAttribute.getKey());
                        if (value != null && value.toLowerCase().contains(searchAttribute.getValue())) {
                            results.add(uniconer);
                        }
                    }
                }
                
                return results;
            }
        }
        
        return Collections.emptyList();
    }

}
