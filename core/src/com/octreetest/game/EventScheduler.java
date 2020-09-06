package com.octreetest.game;

public class EventScheduler {

    static int curFrame = 0;
    static int frameLimit = 60;
    static SecCounterTask task;

    public static void init(){
        task = new SecCounterTask();
        task.begin();
        //Debugger.log("EventScheduler initialized.", Debugger.DebugType.ANNOUNCEMENT);
    }

    public static void periodic(){
        if(curFrame >= frameLimit){
            curFrame = 0;
        }else{
            curFrame++;
        }
    }

    public static boolean everyXFrames(int frames){
        //Debugger.log("Every " + frames + " frames!", Debugger.DebugType.MISC);
        return (curFrame % frames == 0);
    }

    public String getTimeSinceGameStarted(){
        return task.getTimeSinceGameStarted();
    }
}