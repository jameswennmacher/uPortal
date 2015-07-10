/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.layout.dlm;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.utils.cache.UsernameTaggedCacheEntryPurger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Provides API for layout caching service
 */
@Service("layoutCachingService")
public class LayoutCachingService implements ILayoutCachingService {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private Ehcache layoutCache;
    
    @Autowired
    public void setLayoutCache(@Qualifier("org.jasig.portal.layout.dlm.LAYOUT_CACHE") Ehcache layoutCache) {
        this.layoutCache = layoutCache;
    }

    @Override
    public void cacheLayout(IPerson owner, IUserProfile profile, DistributedUserLayout layout) {
        final CacheKey cacheKey = this.getCacheKey(owner, profile);
        logger.debug("Caching layout for {}, key {}", owner.getUserName(), cacheKey.getKey());
        this.layoutCache.put(new Element(cacheKey, layout));
    }
    
    @Override
    public DistributedUserLayout getCachedLayout(IPerson owner, IUserProfile profile) {
        final CacheKey cacheKey = this.getCacheKey(owner, profile);
        final Element element = this.layoutCache.get(cacheKey);
        if (element != null) {
            logger.debug("Obtaining Cached layout for {}, key {}", owner.getUserName(), cacheKey.getKey());
            return (DistributedUserLayout)element.getObjectValue();
        }
        return null;
    }
    
    @Override
    public void removeCachedLayout(IPerson owner, IUserProfile profile) {
        final CacheKey cacheKey = this.getCacheKey(owner, profile);
        logger.debug("Removing cached layout for {}, key {}", owner.getUserName(), cacheKey.getKey());
        this.layoutCache.remove(cacheKey);
    }
    
    protected CacheKey getCacheKey(IPerson owner, IUserProfile profile) {
        return CacheKey.buildTagged(LayoutCachingService.class.getName(),
                UsernameTaggedCacheEntryPurger.createCacheEntryTag(owner.getUserName()),
                owner.getAttributeSensitiveUsername(),
                profile.getProfileId());
    }
}
