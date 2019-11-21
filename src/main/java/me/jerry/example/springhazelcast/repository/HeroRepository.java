package me.jerry.example.springhazelcast.repository;

import lombok.extern.slf4j.Slf4j;
import me.jerry.example.springhazelcast.domain.Hero;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface HeroRepository {

    Optional<Hero> findById(String id);

    Hero save(Hero hero);

}


@Slf4j
@Repository
class InMemoryHeroRepository implements HeroRepository {

    private Map<String, Hero> heroes = new HashMap<>();

    @Override
    public Optional<Hero> findById(String id) {
        log.info("Call InMemoryHeroRepository.findById({})", id);
        log.info("heroes : {}", heroes);
        return Optional.ofNullable(heroes.get(id));
    }

    @Override
    public Hero save(Hero hero) {
        heroes.put(hero.getId(), hero);
        return hero;
    }

}
