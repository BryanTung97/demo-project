package localhost.demoproject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
class LoadDatabase {

  @Bean
  CommandLineRunner initDatabase(EmployeeRepository repository) {
    return args -> {
        //log.info("Preloading " + repository.save(new Employee("Richard", "Lewis", "comedian", 200.00)));
        //log.info("Preloading " + repository.save(new Employee("Lebron", "James", "basketball player", 500.00)));
        log.info("Preloading " + repository.save(new Employee("Richard", "Lewis", "comedian")));
        log.info("Preloading " + repository.save(new Employee("Lebron", "James", "basketball player")));
    };
  }
}