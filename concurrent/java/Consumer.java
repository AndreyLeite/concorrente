class Consumer implements Runnable {
    private final Buffer buffer;
    private final int sleepTime;
    private final int id;
    private final boolean isEvenConsumer;

    public Consumer(int id, Buffer buffer, int sleepTime, boolean isEvenConsumer) {
        this.id = id;
        this.buffer = buffer;
        this.sleepTime = sleepTime;
        this.isEvenConsumer = isEvenConsumer;
    }

    @Override
    public void run() {
        String type = isEvenConsumer ? "Even" : "Odd";
        try {
            while (true) {
                int item = buffer.remove(isEvenConsumer);

                if (item == -1) {
                    break;
                }
                
                System.out.println("Consumer " + id + " (" + type + ") consumed item " + item);
                Thread.sleep(sleepTime);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Consumer " + id + " (" + type + ") finished.");
    }
}