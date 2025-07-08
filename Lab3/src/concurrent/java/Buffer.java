import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

class Buffer {
    private final List<Integer> data = new ArrayList<>();
    private final Semaphore mutex = new Semaphore (1);
    private final Semaphore full = new Semaphore(data.size());
    private final Semaphore empty = new Semaphore (0);
    
    public void put(int value) {
        data.add(value);
        System.out.println("Inserted: " + value + " | Buffer size: " + data.size());
    }
    
    public int remove() {
        if (!data.isEmpty()) {
            int value = data.remove(0);
            System.out.println("Removed: " + value + " | Buffer size: " + data.size());
            return value;
        }
        return -1;
    }
}