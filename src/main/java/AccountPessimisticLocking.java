import java.util.concurrent.locks.StampedLock;

public class AccountPessimisticLocking implements Comparable {
    private final String id;
    private volatile int balance;
    private final StampedLock lock = new StampedLock();

    public AccountPessimisticLocking(String id, int balance) {
        this.id = id;
        this.balance = balance;
    }

    public boolean transfer(AccountPessimisticLocking destination, int amount) {
        if (this.compareTo(destination) > 0) {
            return destination.transfer(this, -amount);
        } else {
            long stampSource = lock.writeLock();
            long stampDestination = destination.lock.writeLock();

            try {
                if (balance < amount) {
                    return false;
                }
                balance = balance - amount;
                destination.balance = destination.balance + amount;
                return true;
            } finally {
                lock.unlockWrite(stampSource);
                destination.lock.unlockWrite(stampDestination);
            }
        }
    }

    public int getBalance() {
        return balance;
    }

    @Override
    public int compareTo(Object o) {
        return id.compareTo(String.valueOf(o));
    }
}
