class Producer implements Runnable {
    private final Buffer buffer;
    private final int maxItems;
    private final int sleepTime;
    private final int id;

    public Producer(int id, Buffer buffer, int maxItems, int sleepTime) {
        this.id = id;
        this.buffer = buffer;
        this.maxItems = maxItems;
        this.sleepTime = sleepTime;
    }

    @Override
    public void run() {
        for (int i = 0; i < maxItems; i++) {
            try {
                int item = (int) (Math.random() * 100);
                buffer.put(item);
                System.out.println("Producer " + id + " produced item " + item);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Producer " + id + " was interrupted.");
                break;
            }
        }
        System.out.println("Producer " + id + " finished producing.");
    }
}
