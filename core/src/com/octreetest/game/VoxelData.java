package com.octreetest.game;

public class VoxelData {

    byte[] data;
/*    private final int topOffset;
    private final int botOffset;
    private final int leftOffset;
    private final int rightOffset;
    private final int frontOffset;
    private final int backOffset;*/
    private final int width;
    private final int height;
    private final int depth;

    public VoxelData(int width, int height, int depth){
        data = new byte[width * height * depth];
/*        topOffset = width * depth;
        botOffset = -width * depth;
        rightOffset = 1;
        leftOffset = -1;
        frontOffset = -width;
        backOffset = width;*/
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public byte get(int x, int y, int z){
        if(x < 0 || x >= width) return 0;
        if(y < 0 || y >= height) return 0;
        if(z < 0 || z >= depth) return 0;
        return data[x + z * width + y * (width * height)];
    }

    public void set (int x, int y, int z, byte voxel) {
        if (x < 0 || x >= width) return;
        if (y < 0 || y >= height) return;
        if (z < 0 || z >= depth) return;
        fastSet(x, y, z, voxel);
    }

    public void fastSet(int x, int y, int z, byte voxel){
        data[x + z * width + y * (width * height)] = voxel;
    }

    public void sample(int x, int y, int z){
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                for(int k = 0; k < depth; k++){
                    int sample = (int) Math.round(SimplexNoise.noise(i + x, j + y, k + z));
                    if(sample > 0){
                        fastSet(i, j, k, (byte) 1);
                    }else{
                        fastSet(i, j, k, (byte) 0);
                    }
                }
            }
        }
    }
}
