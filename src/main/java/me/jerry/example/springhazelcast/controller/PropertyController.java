package me.jerry.example.springhazelcast.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jerry.example.springhazelcast.domain.Property;
import me.jerry.example.springhazelcast.service.PropertyService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping("/properties/{id}")
    public Property one(@PathVariable Long id) {
        Optional<Property> property = propertyService.getCachedPropertyById(id);
        return property.orElse(null);
    }


    @GetMapping("/properties/byCreatedAtRange")
    public List<Property> one(
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            @RequestParam(required = false) LocalDateTime start,
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            @RequestParam(required = false) LocalDateTime end) {

        if (start == null || end == null) {
            start = LocalDateTime.of(2019, 9, 1, 0, 0);
            end = LocalDateTime.of(2019, 12, 20, 0, 0);
        }

        log.info("start : {}", start);
        log.info("end : {}", end);

        List<Property> properties = propertyService.getCachedEnabledPropertiesByCreatedAtRange(start, end);
        log.info("properties.size() : {}", properties.size());
        return properties;
    }

}
