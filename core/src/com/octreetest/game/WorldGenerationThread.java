package com.octreetest.game;

import java.util.*;

public class WorldGenerationThread implements Runnable{

    public static class QueuedChunk{
        int[] pos;
        byte lod = 4;
        QueuedChunk(int[] pos){
            this.pos = pos;
        }
    }

    private Thread thread;
    private final String threadName;
    final SortedMap<String, Chunk> chunks;
    ArrayList<Chunk> packages;
    final ArrayList<QueuedChunk> queue;
    boolean cloning;
    int CHUNK_SIZE;
    static int QUEUE_LIMIT = 800;
    boolean running;
    boolean firstCycle;
    int chunksX;
    int chunksY;
    int chunksZ;
    public static final Object sSyncObj;
    static{
        sSyncObj = new Object();
    }

    public WorldGenerationThread(String threadName, int chunksX, int chunksY, int chunksZ, int CHUNK_SIZE){
        this.threadName = threadName;
        this.chunksX = chunksX;
        this.chunksY = chunksY;
        this.chunksZ = chunksZ;
        //chunks = new ArrayList<>(chunksX * chunksY * chunksZ);
        chunks = new TreeMap<>();
        packages = new ArrayList<>();
        queue = new ArrayList<>(QUEUE_LIMIT);
        cloning = false;
        this.CHUNK_SIZE = CHUNK_SIZE;
        running = true;
        firstCycle = true;
        start();
    }

    public void requestChunk(QueuedChunk chunk){
        synchronized(queue){
            queue.add(chunk);
        }
    }

    public void start(){

        if(thread == null){
            long start = System.currentTimeMillis();
            System.out.println("Beginning world gen on thread " + threadName + "...");
            int i = 0;
            for(int x = 0; x < chunksX; x++){
                for(int y = 0; y < chunksY; y++){
                    for(int z = 0; z < chunksZ; z++){
                        //requestChunk(new QueuedChunk(new int[]{x * CHUNK_SIZE, y * CHUNK_SIZE, z * CHUNK_SIZE}));
                        Chunk chunk = new Chunk(new int[]{x * CHUNK_SIZE, y * CHUNK_SIZE, z * CHUNK_SIZE}, (byte)4);
                        chunks.put(Utility.posToString(chunk.pos), chunk);
                        packages.add(chunk);
                        i++;
                    }
                }
            }
            System.out.println("Done! Finished in " + (System.currentTimeMillis() - start) + " ms.");
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
            synchronized(queue){
                if(queue.size() > 0 && !cloning){
                    QueuedChunk q = queue.get(0);
                    String key = Utility.posToString(q.pos);

                    if(!chunks.containsKey(key)){
                        Chunk chunk = new Chunk(q.pos, q.lod);
                        synchronized(sSyncObj){
                            chunks.put(Utility.posToString(chunk.pos), chunk);
                            packages.add(chunk);
                        }
                    }else if(q.lod != chunks.get(key).curLOD){
                        /*chunks.remove(key);
                        Chunk chunk = new Chunk(q.pos, q.lod);
                        synchronized(sSyncObj){
                            chunks.put(key, chunk);
                            packages.add(chunk);
                        }*/
                        //PROBLEM HERE--------------------
                        synchronized(sSyncObj){
                            chunks.get(key).curLOD = q.lod;
                            //chunk.curLOD = q.lod;
                            //chunk.dirty = true;
                            packages.add(chunks.get(key));
                        }
                    }



                    queue.remove(0);
                }
            }
        }
        System.out.println("Exiting thread " + threadName + ".");
    }

    public synchronized ArrayList<Chunk> getChunks(){
        synchronized(sSyncObj){
            ArrayList<Chunk> ret = new ArrayList<>(packages);
            /*System.out.println("START___-----------");
            for(Chunk c : ret){
                System.out.println(Utility.posToString(c.pos));
            }*/
            if(ret.size() > 0){
                packages.clear();
                /*for(Chunk c : ret){
                    System.out.println(Utility.posToString(c.pos));
                }*/
                return ret;
            }
            return ret;
        }
    }
}
