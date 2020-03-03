package robert.spring.demobookapp;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class BookInitializer implements CommandLineRunner {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BookInitializer.class);

    @Autowired
    private BookRepository bookRepository;

    @Override
    public void run(String... args) throws Exception {

        log.info("Starting to initialize sample data ...");

        Faker faker = new Faker();

        for (int i = 0; i < 10; i++){
            Book book = new Book();
            book.setAuthor(faker.book().author());
            book.setTitle(faker.book().title());
            book.setIsbn(UUID.randomUUID().toString());

            bookRepository.save(book);
        }

        log.info("... finished with data initialization");

    }
}
