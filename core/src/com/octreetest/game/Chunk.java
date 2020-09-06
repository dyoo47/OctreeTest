package com.octreetest.game;

import java.util.ArrayList;


public class Chunk {

    static byte chunkSize = 16;
    public static final int VERTEX_SIZE = 10;
    int[] pos;
    byte curLOD;
    boolean dirty;
    EfficientOctree octree;

    public Chunk(int[] pos, byte curLOD){

        this.pos = pos;
        this.curLOD = curLOD;
        VoxelData voxelData = new VoxelData(chunkSize, chunkSize, chunkSize);
        voxelData.sampleRidges(pos[0], pos[1], pos[2]);

        dirty = true;
        octree = new EfficientOctree(35, chunkSize, voxelData);
        octree.constructOctree(voxelData, curLOD, 0, 0);
    }

    public int getChunkVertices(float[] vertices) {

        ArrayList<Integer> renderNodes = new ArrayList<Integer>(octree.getChildrenAtLOD(curLOD, 0, 0));
        int vertexOffset = 0;
        VoxelData voxelData = new VoxelData(chunkSize, chunkSize, chunkSize);

        for (int node : renderNodes) {
            for (int i = octree.getPos(node)[0]; i < octree.getPos(node)[0] + octree.getSize(node); i++) {
                for (int j = octree.getPos(node)[1]; j < octree.getPos(node)[1] + octree.getSize(node); j++) {
                    for (int k = octree.getPos(node)[2]; k < octree.getPos(node)[2] + octree.getSize(node); k++) {
                        voxelData.set(i, j, k, octree.getValue(node));
                    }
                }
            }
        }

        for (int node : renderNodes) {
            boolean tvis = false, dvis = false, rvis = false, lvis = false, fvis = false, bvis = false;
            for (int i = 0; i < octree.getSize(node); i++) {
                for (int j = 0; j < octree.getSize(node); j++) {

                    //check top
                    if (octree.getPos(node)[1] + octree.getSize(node) == chunkSize ||
                            voxelData.get(octree.getPos(node)[0] + i, octree.getPos(node)[1] + octree.getSize(node), octree.getPos(node)[2] + j) == 0) {
                        tvis = true;
                    }

                    //check bottom
                    if (octree.getPos(node)[1] - octree.getSize(node) < 0 ||
                            voxelData.get(octree.getPos(node)[0] + i, octree.getPos(node)[1] - octree.getSize(node), octree.getPos(node)[2] + j) == 0) {
                        dvis = true;
                    }

                    //check right
                    if (octree.getPos(node)[0] + octree.getSize(node) == chunkSize ||
                            voxelData.get(octree.getPos(node)[0] + octree.getSize(node), octree.getPos(node)[1] + i, octree.getPos(node)[2] + j) == 0) {
                        rvis = true;
                    }

                    //check left
                    if (octree.getPos(node)[0] - octree.getSize(node) < 0 ||
                            voxelData.get(octree.getPos(node)[0] - octree.getSize(node), octree.getPos(node)[1] + i, octree.getPos(node)[2] + j) == 0) {
                        lvis = true;
                    }

                    //check front
                    if (octree.getPos(node)[2] + octree.getSize(node) == chunkSize ||
                            voxelData.get(octree.getPos(node)[0] + i, octree.getPos(node)[1] + j, octree.getPos(node)[2] + octree.getSize(node)) == 0) {
                        fvis = true;
                    }

                    //check back
                    if (octree.getPos(node)[2] - octree.getSize(node) < 0 ||
                            voxelData.get(octree.getPos(node)[0] + i, octree.getPos(node)[1] + j, octree.getPos(node)[2] - octree.getSize(node)) == 0) {
                        bvis = true;
                    }
                    if (tvis && dvis && rvis && lvis && fvis && bvis) break;
                }
                if (tvis && dvis && rvis && lvis && fvis && bvis) break;
            }
            if(tvis){
                vertexOffset = createTop(pos, octree.getPos(node), vertexOffset, vertices, octree.getSize(node));
            }
            if(dvis){
                vertexOffset = createBottom(pos, octree.getPos(node), vertexOffset, vertices, octree.getSize(node));
            }
            if(rvis){
                vertexOffset = createRight(pos, octree.getPos(node), vertexOffset, vertices, octree.getSize(node));
            }
            if(lvis){
                vertexOffset = createLeft(pos, octree.getPos(node), vertexOffset, vertices, octree.getSize(node));
            }
            if(fvis){
                vertexOffset = createFront(pos, octree.getPos(node), vertexOffset, vertices, octree.getSize(node));
            }
            if(bvis){
                vertexOffset = createBack(pos, octree.getPos(node), vertexOffset, vertices, octree.getSize(node));
            }
        }

        return vertexOffset / VERTEX_SIZE;
    }

    static int createBottom(int[] offset, byte[] pos, int vertexOffset, float[] vertices, int size){
        int x = pos[0];
        int y = pos[1];
        int z = pos[2];
        int ox = offset[0];
        int oy = offset[1];
        int oz = offset[2];

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;
        return vertexOffset;
    }

    static int createTop(int[] offset, byte[] pos, int vertexOffset, float[] vertices, int size){
        int x = pos[0];
        int y = pos[1];
        int z = pos[2];
        int ox = offset[0];
        int oy = offset[1];
        int oz = offset[2];

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;
        return vertexOffset;
    }

    static int createRight(int[] offset, byte[] pos, int vertexOffset, float[] vertices, int size){
        int x = pos[0];
        int y = pos[1];
        int z = pos[2];
        int ox = offset[0];
        int oy = offset[1];
        int oz = offset[2];

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;
        return vertexOffset;
    }

    static int createLeft(int[] offset, byte[] pos, int vertexOffset, float[] vertices, int size){
        int x = pos[0];
        int y = pos[1];
        int z = pos[2];
        int ox = offset[0];
        int oy = offset[1];
        int oz = offset[2];

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;
        return vertexOffset;
    }

    static int createFront(int[] offset, byte[] pos, int vertexOffset, float[] vertices, int size){
        int x = pos[0];
        int y = pos[1];
        int z = pos[2];
        int ox = offset[0];
        int oy = offset[1];
        int oz = offset[2];

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;
        return vertexOffset;
    }

    static int createBack(int[] offset, byte[] pos, int vertexOffset, float[] vertices, int size){
        int x = pos[0];
        int y = pos[1];
        int z = pos[2];
        int ox = offset[0];
        int oy = offset[1];
        int oz = offset[2];

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = (x + ox) / (16*16f);
        vertices[vertexOffset++] = (y + oy) / (16*16f);
        vertices[vertexOffset++] = (z + oz) / (16*16f);
        vertices[vertexOffset++] = 1f;
        return vertexOffset;
    }
}
