import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class AccountPessimisticLockingTest {
    private static ExecutorService threadPool;

    @BeforeAll
    static void beforeAll() {
        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @AfterEach
    void tearDown() {
        threadPool.shutdownNow();
    }

    @Test
    void should_transfer_between_accounts_atomically_and_with_no_deadlocks() throws ExecutionException, InterruptedException {
        AccountPessimisticLocking accountA = new AccountPessimisticLocking("A", 40_000_031);
        AccountPessimisticLocking accountB = new AccountPessimisticLocking("B", 1_000_031);
        List<Supplier<CompletableFuture>> accountTransferFactory = List.of(() -> CompletableFuture.runAsync(() -> {
            accountA.transfer(accountB, 2);
            accountB.transfer(accountA, 1);
        }, threadPool), () -> CompletableFuture.runAsync(() -> {
            accountB.transfer(accountA, 1);
            accountA.transfer(accountB, 2);
        }, threadPool));

        CompletableFuture.allOf(IntStream
                .range(0, 11_000_000)
                .mapToObj(it -> accountTransferFactory.get(it % 2).get())
                .toArray(CompletableFuture[]::new)).get();

        Assertions.assertAll(
                () -> assertEquals(29000031, accountA.getBalance(), "Expected account A to have 19 000 031"),
                () -> assertEquals(12000031, accountB.getBalance(), "Expected account B to have 12 0000 31")
        );
    }

}