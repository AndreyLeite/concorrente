import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        if (args.length != 6) {
            System.out.println("Use: java Main <num_producers> <max_items_per_producer> <producing_time> <num_even_consumers> <num_odd_consumers> <consuming_time>");
            return;
        }

        int numProducers = Integer.parseInt(args[0]);
        int maxItemsPerProducer = Integer.parseInt(args[1]);
        int producingTime = Integer.parseInt(args[2]);
        int numEvenConsumers = Integer.parseInt(args[3]);
        int numOddConsumers = Integer.parseInt(args[4]);
        int consumingTime = Integer.parseInt(args[5]);

        Buffer buffer = new Buffer();
        List<Thread> producerThreads = new ArrayList<>();
        List<Thread> consumerThreads = new ArrayList<>();

        for (int i = 0; i < numProducers; i++) {
            Thread t = new Thread(new Producer(i + 1, buffer, maxItemsPerProducer, producingTime));
            producerThreads.add(t);
            t.start();
        }

        for (int i = 0; i < numEvenConsumers; i++) {
            Thread t = new Thread(new Consumer(i + 1, buffer, consumingTime, true));
            consumerThreads.add(t);
            t.start();
        }

        for (int i = 0; i < numOddConsumers; i++) {
            Thread t = new Thread(new Consumer(i + 1 + numEvenConsumers, buffer, consumingTime, false));
            consumerThreads.add(t);
            t.start();
        }

        for (Thread t : producerThreads) {
            t.join();
        }
        System.out.println("All producers finished.");

        buffer.setProductionFinished();

        for (Thread t : consumerThreads) {
            t.join();
        }
        System.out.println("All consumers finished.");
    }
}
