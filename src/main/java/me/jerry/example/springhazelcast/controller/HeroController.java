package me.jerry.example.springhazelcast.controller;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.monitor.LocalMapStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jerry.example.springhazelcast.domain.Hero;
import me.jerry.example.springhazelcast.service.HeroService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HeroController {

    private final HeroService heroService;
    private final HazelcastInstance hazelcastInstance;

    @GetMapping("/heroes/{id}")
    public Hero one(@PathVariable String id) {
        Optional<Hero> hero = heroService.getById(id);
        return hero.orElse(null);
    }

    @PostMapping("/heroes")
    public Mono<Hero> save(@RequestBody Hero hero) {
        Hero saved = heroService.save(hero);
        return Mono.just(saved);
    }


    @PostMapping("/heroes/bulk")
    public Mono<Boolean> saveBulk() {
        for (int i = 1; i <= 10_000; i++) {
            String id = i + "";
            String name = "name_" + i;
            heroService.save(new Hero(id, name));
        }
        return Mono.just(true);
    }

    @GetMapping("/test")
    public String test() {
        log.info("hazelcastInstance.getDistributedObjects() : {}", hazelcastInstance.getDistributedObjects());
        return hazelcastInstance.getDistributedObjects().toString();
    }

    @GetMapping("/cache-metrics")
    public Object cacheMetrics() {

        List<String> cacheNames = hazelcastInstance.getDistributedObjects().stream()
                .map(it -> it.getName())
                .collect(toList());

        log.info("cacheNames : {}", cacheNames);

        Map<String, LocalMapStats> stats = cacheNames.stream()
                .collect(toMap(String::toString, it -> hazelcastInstance.getMap(it).getLocalMapStats()));

        return stats;
    }



}
