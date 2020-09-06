package com.octreetest.game;

import java.util.Timer;
import java.util.TimerTask;

class SecCounterTask extends TimerTask {

    Timer timer = new Timer();
    int[] timeElapsed = {0, 0, 0};

    public void run(){
        if(timeElapsed[0] == 60){
            timeElapsed[1]++;
            timeElapsed[0] = 0;
        }else{
            timeElapsed[0]++;
        }

        if(timeElapsed[1] == 60){
            timeElapsed[2]++;
            timeElapsed[1] = 0;
        }

    }

    public void begin(){
        timer.schedule(new SecCounterTask(), 0, 1000);
    }

    public String getTimeSinceGameStarted(){
        return timeElapsed[2] + ":" + timeElapsed[1] + ":" + timeElapsed[0];
    }


}