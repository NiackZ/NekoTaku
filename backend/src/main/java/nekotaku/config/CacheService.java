package nekotaku.config;

import jakarta.annotation.PostConstruct;
import nekotaku.genres.GenreService;
import nekotaku.status.StatusService;
import nekotaku.studios.StudioService;
import nekotaku.types.TypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
public class CacheService {

    @Autowired
    private CacheManager cacheManager;

    @PostConstruct
    public void clearCacheAtStartup() {
        Objects.requireNonNull(cacheManager.getCache(TypeService.CACHE_NAME)).clear();
        Objects.requireNonNull(cacheManager.getCache(StudioService.CACHE_NAME)).clear();
        Objects.requireNonNull(cacheManager.getCache(StatusService.CACHE_NAME)).clear();
        Objects.requireNonNull(cacheManager.getCache(GenreService.CACHE_NAME)).clear();
    }

}
