package com.octreetest.game;

import java.util.ArrayList;

public class Chunk {

    static int chunkSize = 16;
    public static final int VERTEX_SIZE = 6;
    //Mesh mesh;
    int[] pos;
    int[] offset;
    VoxelData voxelData;
    //float[] renderVertices;
    int curLOD;
    OctreeNode origin;


    public Chunk(int[] pos, int curLOD, int[] offset){
        this.offset = offset;
        this.pos = pos;
        this.curLOD = curLOD;
        voxelData = new VoxelData(chunkSize, chunkSize, chunkSize);
        voxelData.sample(pos[0], pos[1], pos[2]);
        //renderVertices = new float[chunkSize * chunkSize * chunkSize * 6 * 4 * VERTEX_SIZE];
        origin = new OctreeNode(chunkSize, new int[]{0, 0, 0}, voxelData.get(0, 0, 0));
        origin.constructOctree(voxelData, curLOD, 0);
        System.out.println(origin.getChildrenAtLOD(curLOD, 0).size());
        /*mesh = new Mesh(true, chunkSize * chunkSize * chunkSize * 6 * 4,
                chunkSize * chunkSize * chunkSize * 6 * 6 * 3, VertexAttribute.Position(), VertexAttribute.Normal());
        int len = 32 * 32 * 32 * 6 * 6 / 3;
        short[] indices = new short[len];
        short j = 0;
        for (int i = 0; i < len; i += 6, j += 4) {
            indices[i + 0] = (short)(j + 0);
            indices[i + 1] = (short)(j + 1);
            indices[i + 2] = (short)(j + 2);
            indices[i + 3] = (short)(j + 2);
            indices[i + 4] = (short)(j + 3);
            indices[i + 5] = (short)(j + 0);
        }
        mesh.setIndices(indices);*/
    }

    public int getChunkVertices(float[] vertices){

        ArrayList<OctreeNode> renderNodes = new ArrayList<OctreeNode>(origin.getChildrenAtLOD(curLOD, 0));

        int vertexOffset = 0;

        for(OctreeNode node : renderNodes){

            boolean tvis = false, dvis = false, rvis = false, lvis = false, fvis = false, bvis = false;
            for(int i=0; i<node.size; i++){
                for(int j=0; j<node.size; j++){

                    //check top
                    if(node.pos[1] + node.size == chunkSize ||
                            voxelData.get(node.pos[0] + i, node.pos[1] + node.size, node.pos[2] + j) == 0){
                        tvis = true;
                    }

                    //check bottom
                    if(node.pos[1] - 1 == -1 ||
                            voxelData.get(node.pos[0] + i, node.pos[1] - 1, node.pos[2] + j) == 0){
                        dvis = true;
                    }

                    //check right
                    if(node.pos[0] + node.size == chunkSize ||
                            voxelData.get(node.pos[0] + node.size, node.pos[1] + i, node.pos[2] + j) == 0){
                        rvis = true;
                    }

                    //check left
                    if(node.pos[0] - 1 == -1 ||
                            voxelData.get(node.pos[0] - 1, node.pos[1] + i, node.pos[2] + j) == 0){
                        lvis = true;
                    }

                    //check front
                    if(node.pos[2] + node.size == chunkSize ||
                            voxelData.get(node.pos[0] + i, node.pos[1] + j, node.pos[2] + node.size) == 0){
                        fvis = true;
                    }

                    //check back
                    if(node.pos[2] - 1 == -1 ||
                            voxelData.get(node.pos[0] + i, node.pos[1] + j, node.pos[2] - 1) == 0){
                        bvis = true;
                    }
                    if(tvis && dvis && rvis && lvis && fvis && bvis) break;
                }
                if(tvis && dvis && rvis && lvis && fvis && bvis) break;
            }
            if(tvis){
                vertexOffset = createTop(offset, node.pos, vertexOffset, vertices, node.size);
            }
            if(dvis){
                vertexOffset = createBottom(offset, node.pos, vertexOffset, vertices, node.size);
            }
            if(rvis){
                vertexOffset = createRight(offset, node.pos, vertexOffset, vertices, node.size);
            }
            if(lvis){
                vertexOffset = createLeft(offset, node.pos, vertexOffset, vertices, node.size);
            }
            if(fvis){
                vertexOffset = createFront(offset, node.pos, vertexOffset, vertices, node.size);
            }
            if(bvis){
                vertexOffset = createBack(offset, node.pos, vertexOffset, vertices, node.size);
            }
        }
        //System.out.println(vertexOffset / VERTEX_SIZE);
        //System.out.println(faces);
        return vertexOffset / VERTEX_SIZE;
    }

    static int createBottom(int[] offset, int[] pos, int vertexOffset, float[] vertices, int size){
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

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        return vertexOffset;
    }

    static int createTop(int[] offset, int[] pos, int vertexOffset, float[] vertices, int size){
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

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        return vertexOffset;
    }

    static int createRight(int[] offset, int[] pos, int vertexOffset, float[] vertices, int size){
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

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        return vertexOffset;
    }

    static int createLeft(int[] offset, int[] pos, int vertexOffset, float[] vertices, int size){
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

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        return vertexOffset;
    }

    static int createFront(int[] offset, int[] pos, int vertexOffset, float[] vertices, int size){
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

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z + size;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        return vertexOffset;
    }

    static int createBack(int[] offset, int[] pos, int vertexOffset, float[] vertices, int size){
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

        vertices[vertexOffset++] = ox + x;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y + size;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;

        vertices[vertexOffset++] = ox + x + size;
        vertices[vertexOffset++] = oy + y;
        vertices[vertexOffset++] = oz + z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        return vertexOffset;
    }
}
