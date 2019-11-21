package me.jerry.example.springhazelcast.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import lombok.RequiredArgsConstructor;
import me.jerry.example.springhazelcast.domain.Property;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public interface PropertyService {

    Optional<Property> getCachedPropertyById(Long id);

    List<Property> getCachedEnabledPropertiesByCreatedAtRange(LocalDateTime inclusiveStart, LocalDateTime exclusiveEnd);

    Property saveCache(Property property);

}

@Service
@CacheConfig(cacheNames = "properties")
@RequiredArgsConstructor
class PropertyServiceImpl implements PropertyService {

    private final HazelcastInstance hazelcastInstance;

    @Override
    public Optional<Property> getCachedPropertyById(Long id) {

        IMap<String, Property> map = hazelcastInstance.getMap("properties");
        EntryObject e = new PredicateBuilder().getEntryObject();
        Predicate predicate = e.get("id").equal(id);

        List<Property> properties = new ArrayList<>(map.values( predicate ));
        return Optional.ofNullable(!properties.isEmpty() ? properties.get(0) : null );
    }

    @Override
    public List<Property> getCachedEnabledPropertiesByCreatedAtRange(LocalDateTime inclusiveStart, LocalDateTime exclusiveEnd) {

        IMap<String, Property> map = hazelcastInstance.getMap("properties");

        EntryObject e = new PredicateBuilder().getEntryObject();
        Predicate predicate = e.get("createdAt").greaterEqual(inclusiveStart)
                .and(e.get("createdAt").lessEqual(exclusiveEnd));
        List<Property> properties = new ArrayList<>(map.values( predicate ));
        return properties;
    }

    @Override
    @CachePut(key="#property.id")
    public Property saveCache(Property property) {
        return property;
    }

}
