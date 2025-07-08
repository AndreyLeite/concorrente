class Consumer implements Runnable{
    private final Buffer buffer;
    private final int sleepTime;
    private final int id;
    private final boolean ehPar;
    
    public Consumer(int id, Buffer buffer, int sleepTime, boolean par) {
        this.id = id;
        this.buffer = buffer;
        this.sleepTime = sleepTime;
        this.ehPar = par;
    }
    
    

    @Override
    public void run() {
        while (true) {
            int item = buffer.remove();
            int resto = item % 2;
            if(ehPar){
                if(resto == 0){
                    if (item == -1) break;
                    System.out.println("Consumer " + id + " consumed item " + item);
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }else{
                    buffer.put(item);
                }
            }else{
                if(resto != 0){
                    if (item == -1) break;
                    System.out.println("Consumer " + id + " consumed item " + item);
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }else{
                    buffer.put(item);
                }
            }

    
        }
    }
        

    

}