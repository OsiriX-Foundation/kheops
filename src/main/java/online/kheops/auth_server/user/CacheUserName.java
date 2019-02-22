package online.kheops.auth_server.user;

import online.kheops.auth_server.util.Consts.CACHE_USER;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;


public class CacheUserName {


    private static CacheUserName instance = null;
    private static Cache<String, String> userCache;
    private static CacheManager cacheManager;
    private static final String CACHE_ALIAS = "userCache";

    private CacheUserName() {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(CACHE_ALIAS, CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class, ResourcePoolsBuilder.heap(CACHE_USER.SIZE))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(CACHE_USER.DURATION)))
                .build();
        cacheManager.init();

        userCache = cacheManager.getCache(CACHE_ALIAS, String.class, String.class);
    }

    public static synchronized CacheUserName getInstance() {
        if(instance != null) {
            return instance;
        }
        return instance = new CacheUserName();
    }

    public void cacheValue(String userName, String userId) {
        userCache.putIfAbsent(userName, userId);
    }

    public String getCachedValue(String userId) {
        return userCache.get(userId);
    }

}
