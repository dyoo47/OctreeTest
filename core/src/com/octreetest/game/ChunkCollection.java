package com.octreetest.game;

import java.util.ArrayList;

public class ChunkCollection {
    ArrayList<Chunk> chunks;
    public final int width;
    public final int height;
    public final int depth;

    public ChunkCollection(int width, int height, int depth){
        //chunks = new Chunk[width * height * depth];
        chunks = new ArrayList<>(width * height * depth);
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public Chunk get(int x, int y, int z){
        /*if(x < 0 || x >= width){
            System.out.println("Tried to access a chunk that doesn't exist!");
            return null;
        }
        if(y < 0 || y >= height){
            System.out.println("Tried to access a chunk that doesn't exist!");
            return null;
        }
        if(z < 0 || z >= depth){
            System.out.println("Tried to access a chunk that doesn't exist!");
            return null;
        }*/
        //return chunks[x + z * width + y * (width * height)];
        return chunks.get(x + z * width + y * (width * height));
    }

    public void set (int x, int y, int z, Chunk chunk) {
        /*if (x < 0 || x >= width) {
            System.out.println("Tried to access a chunk that doesn't exist!");
            return;
        }
        if (y < 0 || y >= height) {
            System.out.println("Tried to access a chunk that doesn't exist!");
            return;
        }
        if (z < 0 || z >= depth) {
            System.out.println("Tried to access a chunk that doesn't exist!");
            return;
        }*/
        fastSet(x, y, z, chunk);
    }

    public void fastSet(int x, int y, int z, Chunk chunk){
        //chunks[x + z * width + y * (width * height)] = chunk;
        final int index = x + z * width + y * (width * height);
        //chunks.add(index, chunk);
        boolean added = false;
        while(!added){
            try{
                chunks.set(index, chunk);
                added = true;
            }catch(Exception e){
                chunks.add(null);
            }
        }

    }
}
