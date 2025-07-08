import java.util.ArrayList;
import java.util.List;

class Buffer {
    private final List<Integer> data = new ArrayList<>();
    private final int capacity = 100;
    private boolean productionFinished = false;

    public synchronized void put(int value) throws InterruptedException {
        while (data.size() == capacity) {
            wait();
        }
        data.add(value);
        notifyAll();
    }

    public synchronized int remove(boolean isEvenConsumer) throws InterruptedException {
        while (true) {
            int item = -1;
            int index = -1;
            
            for (int i = 0; i < data.size(); i++) {
                boolean itemIsEven = data.get(i) % 2 == 0;
                if (itemIsEven == isEvenConsumer) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                item = data.remove(index);
                notifyAll();
                return item;
            }

            if (productionFinished) {
                return -1;
            }
            
            wait();
        }
    }

    public synchronized void setProductionFinished() {
        this.productionFinished = true;
        notifyAll();
    }
}
