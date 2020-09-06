package com.octreetest.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.octreetest.game.World.nearLOD;

public class WorldGenerationThread implements Runnable{

    public static class QueuedChunk{
        int[] pos;
        QueuedChunk(int[] pos){
            this.pos = pos;
        }
    }

    private Thread thread;
    private final String threadName;
    HashMap<QueuedChunk, String> chunkMap;
    ArrayList<Chunk> chunks;
    final ArrayList<QueuedChunk> queue;
    int CHUNK_SIZE;
    static int QUEUE_LIMIT = 800;
    boolean running;
    boolean firstCycle;
    int chunksX;
    int chunksY;
    int chunksZ;

    public WorldGenerationThread(String threadName, int chunksX, int chunksY, int chunksZ, int CHUNK_SIZE){
        this.threadName = threadName;
        this.chunksX = chunksX;
        this.chunksY = chunksY;
        this.chunksZ = chunksZ;
        chunks = new ArrayList<>(chunksX * chunksY * chunksZ);
        chunkMap = new HashMap<>();
        queue = new ArrayList<>(QUEUE_LIMIT);
        this.CHUNK_SIZE = CHUNK_SIZE;
        running = true;
        firstCycle = true;
        start();
    }

    public synchronized void requestChunk(QueuedChunk chunk){
        queue.add(chunk);
    }

    public void start(){
        if(thread == null){
            thread = new Thread(this, threadName);
            thread.start();
        }
    }

    public void stop(){
        running = false;
    }

    @Override
    public void run() {
        while(running){
            if(firstCycle){
                long start = System.currentTimeMillis();
                System.out.println("Beginning world gen on thread " + threadName + "...");
                int i = 0;
                for(int x = 0; x < chunksX; x++){
                    for(int y = 0; y < chunksY; y++){
                        for(int z = 0; z < chunksZ; z++){
                            requestChunk(new QueuedChunk(new int[]{x * CHUNK_SIZE, y * CHUNK_SIZE, z * CHUNK_SIZE}));
                            i++;
                        }
                    }
                }
                System.out.println("Done! Finished in " + (System.currentTimeMillis() - start) + " ms.");
                firstCycle = false;
            }
            synchronized(queue){
                if(queue.size() > 0){
                    QueuedChunk q = queue.get(0);
                    Chunk chunk = new Chunk(q.pos, nearLOD);
                    chunks.add(chunk);
                    queue.remove(0);
                }
            }
        }
        System.out.println("Exiting thread " + threadName + ".");
    }

    public ArrayList<Chunk> getChunks(){
        if(chunks.size() > 0){
            return chunks;
        }
        else{
            return null;
        }
    }
}
