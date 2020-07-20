package com.octreetest.game;

import java.util.ArrayList;

public class Chunk {

    static byte chunkSize = 16;
    public static final int VERTEX_SIZE = 10;
    int[] pos;
    //int[] offset;
    //VoxelData voxelData;
    byte curLOD;
    //OctreeNode origin;
    boolean dirty;
    EfficientOctree octree;

    public Chunk(int[] pos, byte curLOD){
        //this.offset = offset;
        this.pos = pos;
        this.curLOD = curLOD;
        VoxelData voxelData = new VoxelData(chunkSize, chunkSize, chunkSize);
        voxelData.sampleMod(pos[0], pos[1], pos[2]);
        //origin = new OctreeNode(chunkSize, new byte[]{0, 0, 0}, voxelData.get(0, 0, 0));
        //origin.constructOctree(voxelData, curLOD, 0);
        dirty = true;
        octree = new EfficientOctree(35, chunkSize, voxelData);
        octree.constructOctree(voxelData, curLOD, 0, 0);
        //if(pos[0] == 0 && pos[1] == 0 && pos[2] == 16)
        //System.out.println(pos[0] + ", " + pos[1] + ", " + pos[2]);
        //octree.logBuffer(8);
    }

    /*public void restructOctree(){
        origin.constructOctree(voxelData, curLOD, 0);
    }*/

    public int getChunkVertices(float[] vertices) {

        //ArrayList<OctreeNode> renderNodes = new ArrayList<OctreeNode>(origin.getChildrenAtLOD(curLOD, 0));
        ArrayList<Integer> renderNodes = new ArrayList<Integer>(octree.getChildrenAtLOD(curLOD, 0, 0));
        //System.out.println(renderNodes.size());

        int vertexOffset = 0;
        VoxelData voxelData = new VoxelData(chunkSize, chunkSize, chunkSize);

        /*for(OctreeNode node : renderNodes){
            for(int i = node.pos[0]; i < node.pos[0] + node.size; i++){
                for(int j = node.pos[1]; j < node.pos[1] + node.size; j++){
                    for(int k = node.pos[2]; k < node.pos[2] + node.size; k++){
                        voxelData.set(i, j, k, node.value);
                    }
                }
            }
        }*/

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

        /*for(OctreeNode node : renderNodes){

            boolean tvis = false, dvis = false, rvis = false, lvis = false, fvis = false, bvis = false;
            for(int i=0; i<node.size; i++){
                for(int j=0; j<node.size; j++){

                    //check top
                    if(node.pos[1] + node.size == chunkSize ||
                            voxelData.get(node.pos[0] + i, node.pos[1] + node.size, node.pos[2] + j) == 0){
                        tvis = true;
                    }

                    //check bottom
                    if(node.pos[1] - node.size < 0 ||
                            voxelData.get(node.pos[0] + i, node.pos[1] - node.size, node.pos[2] + j) == 0){
                        dvis = true;
                    }

                    //check right
                    if(node.pos[0] + node.size == chunkSize ||
                            voxelData.get(node.pos[0] + node.size, node.pos[1] + i, node.pos[2] + j) == 0){
                        rvis = true;
                    }

                    //check left
                    if(node.pos[0] - node.size < 0 ||
                            voxelData.get(node.pos[0] - node.size, node.pos[1] + i, node.pos[2] + j) == 0){
                        lvis = true;
                    }

                    //check front
                    if(node.pos[2] + node.size == chunkSize ||
                            voxelData.get(node.pos[0] + i, node.pos[1] + j, node.pos[2] + node.size) == 0){
                        fvis = true;
                    }

                    //check back
                    if(node.pos[2] - node.size < 0 ||
                            voxelData.get(node.pos[0] + i, node.pos[1] + j, node.pos[2] - node.size) == 0){
                        bvis = true;
                    }
                    if(tvis && dvis && rvis && lvis && fvis && bvis) break;
                }
                if(tvis && dvis && rvis && lvis && fvis && bvis) break;
            }
            if(tvis){
                vertexOffset = createTop(pos, node.pos, vertexOffset, vertices, node.size);
            }
            if(dvis){
                vertexOffset = createBottom(pos, node.pos, vertexOffset, vertices, node.size);
            }
            if(rvis){
                vertexOffset = createRight(pos, node.pos, vertexOffset, vertices, node.size);
            }
            if(lvis){
                vertexOffset = createLeft(pos, node.pos, vertexOffset, vertices, node.size);
            }
            if(fvis){
                vertexOffset = createFront(pos, node.pos, vertexOffset, vertices, node.size);
            }
            if(bvis){
                vertexOffset = createBack(pos, node.pos, vertexOffset, vertices, node.size);
            }
        }
        //System.out.println(vertexOffset / VERTEX_SIZE);
        //System.out.println(faces);
        return vertexOffset / VERTEX_SIZE;
    }*/

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
