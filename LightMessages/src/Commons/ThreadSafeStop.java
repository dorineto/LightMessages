package Commons;

public class ThreadSafeStop extends Thread {
    protected boolean stopRunning;
    protected boolean running;

    public ThreadSafeStop(String name){
        super(name);

        this.stopRunning = false;
        this.running = false;
    }

    public boolean isRunning() { return this.running; }

    public void stopRunning(){ this.stopRunning = true; }

    public void unstopRunning(){ this.stopRunning = false; }

}