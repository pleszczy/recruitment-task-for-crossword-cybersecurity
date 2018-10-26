import java.util.concurrent.atomic.AtomicInteger;

public class Account {
    private final String id;
    private AtomicInteger balance = new AtomicInteger();

    public Account(String id) {
        this.id = id;
    }

    public Account(String id, int balance) {
        this.id = id;
        this.balance = new AtomicInteger(balance);
    }

    public boolean transfer(Account destination, int amount) {
        if (balance.get() < amount) {
            return false;
        }
        balance.getAndUpdate(currentValue -> currentValue - amount);
        destination.balance.getAndUpdate(currentValue -> currentValue + amount);
        return true;
    }

    public int getBalance() {
        return balance.get();
    }

    public void setBalance(int balance) {
        this.balance.set(balance);
    }
}