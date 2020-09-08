package com.octreetest.game;

public class Utility {
    public static String posToString(int[] pos){
        return (pos[0] + "," + pos[1] + "," + pos[2]);
    }

    class Constants{
        public static final int CHUNK_SIZE = 16;
        public static final int MAX_LOD = 4;
    }
}
