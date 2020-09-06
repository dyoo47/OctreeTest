package com.octreetest.game;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.octreetest.game.WorldGenerationThread.QueuedChunk;
import java.util.ArrayList;

public class PlayerMonitor {

    float x;
    float y;
    float z;
    PerspectiveCamera player;
    public PlayerMonitor(PerspectiveCamera cam){
        player = cam;
    }

    public void update(){
        x = player.position.x;
        y = player.position.y;
        z = player.position.z;
    }

    public ArrayList<QueuedChunk> getNearChunks(int radius){
        ArrayList<QueuedChunk> chunks = new ArrayList<>();
        //System.out.println("Requesting chunks with center " + x + ", " + y + ", " + z);
        for(int i = -radius; i < radius; i++){
            for(int j = -radius; j < radius; j++){
                for(int k = -radius; k < radius; k++){
                    chunks.add(new QueuedChunk(new int[]{
                            (int)(i * 16 + (x - (x % 16))),
                            (int)(j * 16 + (y - (y % 16))),
                            (int)(k * 16 + (z - (z % 16)))
                    }));
                }
            }
        }
        return chunks;
    }
}
