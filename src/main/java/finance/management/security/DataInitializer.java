package finance.management.security;

import finance.management.entity.Category;
import finance.management.entity.CategoryType;
import finance.management.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the database with default system categories on startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.findByCustomFalse().isEmpty()) {
            List<Category> defaults = List.of(
                    Category.builder().name("Salary").type(CategoryType.INCOME).custom(false).build(),
                    Category.builder().name("Food").type(CategoryType.EXPENSE).custom(false).build(),
                    Category.builder().name("Rent").type(CategoryType.EXPENSE).custom(false).build(),
                    Category.builder().name("Transportation").type(CategoryType.EXPENSE).custom(false).build(),
                    Category.builder().name("Entertainment").type(CategoryType.EXPENSE).custom(false).build(),
                    Category.builder().name("Healthcare").type(CategoryType.EXPENSE).custom(false).build(),
                    Category.builder().name("Utilities").type(CategoryType.EXPENSE).custom(false).build()
            );
            categoryRepository.saveAll(defaults);
            log.info("Default categories initialized.");
        }
    }
}