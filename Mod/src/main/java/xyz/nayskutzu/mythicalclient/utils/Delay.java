package xyz.nayskutzu.mythicalclient.utils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Delay
        extends TimerTask {
    private Timer timer = new Timer();

    public Delay(int milliseconds) {
        int randomDelay = ThreadLocalRandom.current().nextInt(10, 1501);
        this.timer.schedule((TimerTask) this, milliseconds + randomDelay);
    }

    @Override
    public void run() {
        this.onTick();
        this.timer.cancel();
    }

    public abstract void onTick();
}
