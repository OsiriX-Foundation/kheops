package online.kheops.auth_server.user;

import online.kheops.auth_server.util.Consts.CacheUser;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

public class CacheUserName {

    private static CacheUserName instance = null;
    private static final String CACHE_ALIAS = "userCache";

    private Cache<String, UserCachedData> userCache;

    private CacheUserName() {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(CACHE_ALIAS, CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, UserCachedData.class, ResourcePoolsBuilder.heap(CacheUser.SIZE))
                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(CacheUser.DURATION)))
                .build();
        cacheManager.init();

        userCache = cacheManager.getCache(CACHE_ALIAS, String.class, UserCachedData.class);
    }

    public static synchronized CacheUserName getInstance() {
        if(instance != null) {
            return instance;
        }
        return instance = new CacheUserName();
    }

    public void cacheValue(String email, String lastName, String firstName, String userId) {
        final UserCachedData userCachedData = new UserCachedData(email, firstName, lastName);
        userCache.putIfAbsent(userId, userCachedData);
    }

    public UserCachedData getCachedValue(String userId) {
        return userCache.get(userId);
    }

}
