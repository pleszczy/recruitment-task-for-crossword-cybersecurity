import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {
    private static ExecutorService fixedThreadPool;

    @BeforeAll
    static void beforeAll() {
        fixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 2);
    }

    @AfterEach
    void tearDown() {
        fixedThreadPool.shutdownNow();
    }

    @Test
    void should_transfer_between_accounts_atomically_and_with_no_deadlocks() throws ExecutionException, InterruptedException {
        Account accountA = new Account( "A", 1_000_000);
        Account accountB = new Account( "B", 2_000_000);

        CompletableFuture.allOf(IntStream
                .range(0, 1_000_000)
                .parallel()
                .mapToObj(value -> List.of(
                        CompletableFuture.runAsync(() -> {
                            accountA.transfer(accountB, 2);
                            accountB.transfer(accountA, 1);
                        }, fixedThreadPool)
                        , CompletableFuture.runAsync(() -> {
                            accountB.transfer(accountA, 1);
                            accountA.transfer(accountB, 2);
                        }, fixedThreadPool)))
                .flatMap(Collection::stream)
                .toArray(CompletableFuture[]::new)).get();

        Assertions.assertAll(
                () -> assertEquals(0, accountA.getBalance(), "Expected account A to have 1 000 000"),
                () ->  assertEquals(3_000_000, accountB.getBalance(), "Expected account B to have 1 000 000")
        );
    }

    @Test
    void getBalance() {
    }
}