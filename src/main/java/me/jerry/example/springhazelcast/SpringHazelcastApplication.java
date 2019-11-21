package me.jerry.example.springhazelcast;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jerry.example.springhazelcast.domain.Property;
import me.jerry.example.springhazelcast.service.HeroService;
import me.jerry.example.springhazelcast.service.PropertyService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

// import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@Slf4j
@SpringBootApplication
// @EnableDiscoveryClient
public class SpringHazelcastApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringHazelcastApplication.class, args);
    }

    @Component
	@RequiredArgsConstructor
    class HeroApplicationRunner implements ApplicationRunner {

		private final HeroService heroService;
        private final PropertyService propertyService;

        @Override
        public void run(ApplicationArguments args) throws Exception {

            log.info("HeroApplicationRunner.run()...");
            List<String> grades = Arrays.asList("1", "2", "3", "4", "5", "6");
            LocalDateTime startDate = LocalDateTime.of(2019, 9, 1, 0, 0);

            Flux.range(1, 1_000_000)
                    .flatMap(i -> {
                        String id = i + "";
                        String name = "name_" + i;
                        int randomIndex = (int) (Math.random() * grades.size());
                        String randomGrade = grades.get(randomIndex);
                        boolean enabled = (int) (Math.random() * 2) == 1;
                        LocalDateTime createdAt = startDate.plusMinutes(i);
                        Property p = Property.builder()
                                .id((long) i)
                                .name("name_" + i)
                                .grade(randomGrade)
                                .enabled(enabled)
                                .createdAt(createdAt)
                                .updatedAt(createdAt)
                                .build();
                        return Flux.just(p);
                    })
                    .flatMap(p -> {
                        return Mono.fromCallable(() -> propertyService.saveCache(p))
                                .subscribeOn(Schedulers.elastic());
                    })
                    .subscribe();

//            for (int i = 1; i <= 2_000_000; i++) {
//
//                String id = i + "";
//				String name = "name_" + i;
//				heroService.save(new Hero(id, name));
//
//                int randomIndex = (int) (Math.random() * grades.size());
//                String randomGrade = grades.get(randomIndex);
//
//                boolean enabled = (int) (Math.random() * 2) == 1;
//
//                LocalDateTime createdAt = startDate.plusMinutes(i);
//                Property p = Property.builder()
//                        .id((long) i)
//                        .name("name_" + i)
//                        .grade(randomGrade)
//                        .enabled(enabled)
//                        .createdAt(createdAt)
//                        .updatedAt(createdAt)
//                        .build();
//                propertyService.saveCache(p);
//
//            }
        }
    }

}
