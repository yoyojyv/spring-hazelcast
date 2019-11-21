package me.jerry.example.springhazelcast.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jerry.example.springhazelcast.domain.Hero;
import me.jerry.example.springhazelcast.repository.HeroRepository;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

public interface HeroService {

    Optional<Hero> getById(String id);

    Hero save(Hero hero);

}

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "heroes")
class  InMemoryHeroService implements HeroService {

    private final HeroRepository heroRepository;

    @Override
    @Cacheable(key="#id", unless = "#result == null")
    public Optional<Hero> getById(String id) {

        log.info("Call InMemoryHeroService.getById({})...", id);

//        try {
//            TimeUnit.SECONDS.sleep(3);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        return heroRepository.findById(id);
    }

    @Override
    @CachePut(key="#hero.id")
    public Hero save(Hero hero) {
        return heroRepository.save(hero);
    }

//    @CacheEvict(key = "#id")
//    public void evict(String id){
//    }

}
