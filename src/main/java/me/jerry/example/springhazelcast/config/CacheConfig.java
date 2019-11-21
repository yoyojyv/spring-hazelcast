package me.jerry.example.springhazelcast.config;

import com.hazelcast.config.*;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastInstanceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;

@Slf4j
@EnableCaching
@Configuration
@RequiredArgsConstructor
public class CacheConfig {


//    @Value("${cache.cache-names}")
//    private String[] predefinedCacheNames;

    private final ApplicationContext applicationContext;

//    @Bean
//    public CacheManagerCustomizers cacheManagerCustomizers(ObjectProvider<CacheManagerCustomizer<?>> customizers) {
//        return new CacheManagerCustomizers(customizers.orderedStream().collect(Collectors.toList()));
//    }

    @Bean
    public HazelcastInstance hazelcastInstance(Config config) {
        return new HazelcastInstanceFactory(config).getHazelcastInstance();
    }

//    @Bean
//    public HazelcastCacheManager cacheManager(CacheManagerCustomizers customizers, HazelcastInstance existingHazelcastInstance) {
//        HazelcastCacheManager cacheManager = new HazelcastCacheManager(existingHazelcastInstance);
//        return customizers.customize(cacheManager);
//    }


//    @Bean
//    public HazelcastCacheManager cacheManager(HazelcastInstance existingHazelcastInstance) {
//        HazelcastCacheManager cacheManager = new HazelcastCacheManager(existingHazelcastInstance);
//        return cacheManager;
//    }


    @Component
    @ConfigurationProperties(prefix = "cache")
    @Data
    static class CacheSettings {

        private List<String> cacheNames;

    }


    @Bean
    public Config hazelCastConfig() {

        Config config = new Config()
                .setInstanceName("hazelcast-test-instance");
//                .addMapConfig(
//                        new MapConfig()
//                                .setName("heroes")
//                                .setMaxSizeConfig(new MaxSizeConfig(10000, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
//                                .setEvictionPolicy(EvictionPolicy.LRU)
//                                .setTimeToLiveSeconds(60));

        config.setProperty("hazelcast.jmx", "true");

        NetworkConfig network = config.getNetworkConfig()
                .setPort(5701)
                .setPortAutoIncrement(true);

        network.setPortCount(20);
        // network.getInterfaces().setEnabled(true);

        JoinConfig join = network.getJoin();
        join.getMulticastConfig()
                .setMulticastGroup("224.2.2.3")
                .setMulticastPort(54327)
                .setMulticastTimeToLive(32)
                .setMulticastTimeoutSeconds(2)
                .setEnabled(true);

        // near cache
        // NearCacheConfig nearCacheConfig = new NearCacheConfig(300, 120, true, InMemoryFormat.BINARY);
        NearCacheConfig nearCacheConfig = new NearCacheConfig(300, 120, true, InMemoryFormat.BINARY);

        // --- 실제 사용할 캐시 리스트 정의 ---
        // heroes cache
        MapConfig heroesMapConfig = new MapConfig();
        heroesMapConfig.setName("heroes");
        heroesMapConfig.setBackupCount(2);
        heroesMapConfig.getMaxSizeConfig()
                .setSize(10_000)
                .setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE);
        heroesMapConfig.setTimeToLiveSeconds(20);
        heroesMapConfig.setMapStoreConfig(new MapStoreConfig().setClassName("me.jerry.example.springhazelcast.domain.Hero").setEnabled(true));
        heroesMapConfig.setStatisticsEnabled(true);
        heroesMapConfig.setNearCacheConfig(nearCacheConfig);
        config.addMapConfig(heroesMapConfig);

        // properties cache
        MapConfig propertiesMapConfig = new MapConfig();
        propertiesMapConfig.setName("properties");
        propertiesMapConfig.setBackupCount(2);
        propertiesMapConfig.getMaxSizeConfig()
                .setSize(2_000_000)
                .setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE);
        propertiesMapConfig.setTimeToLiveSeconds(60 * 60);
        propertiesMapConfig.setMapStoreConfig(new MapStoreConfig().setClassName("me.jerry.example.springhazelcast.domain.Property").setEnabled(true));
        propertiesMapConfig.setStatisticsEnabled(true);
        propertiesMapConfig.addMapIndexConfig(new MapIndexConfig("id", true));
        propertiesMapConfig.addMapIndexConfig(new MapIndexConfig("grade", true));
        propertiesMapConfig.addMapIndexConfig(new MapIndexConfig("createdAt", true));

        propertiesMapConfig.setNearCacheConfig(nearCacheConfig);
        config.addMapConfig(propertiesMapConfig);

        // --- management center ---
        ManagementCenterConfig managementCenterConfig = new ManagementCenterConfig();
        managementCenterConfig.setEnabled(true);
        managementCenterConfig.setUrl("http://localhost:8080/hazelcast-mancenter");

        config.setManagementCenterConfig(managementCenterConfig);
        return config;
    }

    @PostConstruct
    public void checkCaches() {
        CacheManager cacheManager = applicationContext.getBean("cacheManager", HazelcastCacheManager.class);

//        HazelcastInstance hazelcastInstance = applicationContext.getBean("hazelcastInstance", HazelcastInstance.class);
//        log.info("hazelcastInstance.getDistributedObjects() : {}", hazelcastInstance.getDistributedObjects());
//        log.info("cacheManagerNames : {}", cacheManager.getCacheNames());

        CacheSettings cacheSettings = applicationContext.getBean(CacheSettings.class);
        Collection<String> cacheNames = cacheManager.getCacheNames();
        if (cacheNames.isEmpty()) {
            log.info("Hazelcast cacheNames is empty... Add defined caches : {}", cacheSettings.getCacheNames());
            cacheSettings.getCacheNames().forEach(name -> cacheManager.getCache(name));
        } else {
            log.info("Hazelcast cacheNames exists. cacheNames : {}", cacheNames);
        }

    }

}
