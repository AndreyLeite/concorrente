import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        if (args.length != 5) {
            System.out.println("Use: java Main <num_producers> <max_items_per_producer> <producing_time> <num_consumers> <consuming_time>");
            return;
        }
        
        int numProducers = Integer.parseInt(args[0]);
        int maxItemsPerProducer = Integer.parseInt(args[1]);
        int producingTime = Integer.parseInt(args[2]);
        int numConsumers = Integer.parseInt(args[3]);
        int consumingTime = Integer.parseInt(args[4]);
        
        Buffer buffer = new Buffer();

        List<Thread> threads = new ArrayList<>();
        
        for (int i = 1; i <= numProducers; i++) {
            ProdRun p = new ProdRun(i, buffer, maxItemsPerProducer, producingTime);
            Thread pThread = new Thread(p);
            threads.add(pThread);
        }
        
        boolean par = true;
        for (int i = 1; i <= numConsumers; i++) {
            Consumer consumer = new Consumer(i, buffer, consumingTime, par);
            Thread c = new Thread(consumer);
            threads.add(c);   
            c.start();         
            boolean newPar = par;
            if(par){
                newPar = false;
            }else{
                newPar = true;
            }
            par = newPar;
        }
        for(int i = 1; i <= threads.size(); i++){
            threads.get(i).join();
        }



    }

    public static class ProdRun implements Runnable{
        
        private int id;
        private Buffer buffer;
        private int max;
        private int time;

        public ProdRun (int a, Buffer b, int c, int d){
            this.id = a;
            this.buffer = b;
            this.max = c;
            this.time = d;
        }

        @Override
        public void run() {
            Producer producer = new Producer(id, buffer, max, time);
            producer.produce();
        }
    }

}
