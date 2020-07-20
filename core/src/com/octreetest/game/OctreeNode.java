package com.octreetest.game;

import java.util.ArrayList;

public class OctreeNode {
    OctreeNode[] children;
    byte size;
    byte[] pos;
    byte value;

    static int[][] childOffsets = {
            {0, 0, 0},
            {0, 0, 1},
            {0, 1, 0},
            {0, 1, 1},
            {1, 0, 0},
            {1, 0, 1},
            {1, 1, 0},
            {1, 1, 1}
    };

    public OctreeNode(byte size, byte[] pos, byte value){
        children = new OctreeNode[8];
        this.size = size;
        this.pos = pos;
        this.value = value;
    }

    public void constructOctree(VoxelData data, int maxLOD, int curLOD){

        byte[] parentPos = this.pos;
        byte parentSize = this.size;

        for(int i=0; i<8; i++){
            byte childSize = (byte) (parentSize / 2);
            byte[] childPos = {
                    (byte) (parentPos[0] + childOffsets[i][0] * childSize),
                    (byte) (parentPos[1] + childOffsets[i][1] * childSize),
                    (byte) (parentPos[2] + childOffsets[i][2] * childSize)
            };
            byte first = data.get(childPos[0], childPos[1], childPos[2]);
            children[i] = new OctreeNode(childSize, childPos, first);
            boolean empty = true;
            for(int j=pos[0]; j<pos[0] + size; j++){
                for(int k=pos[1]; k<pos[1] + size; k++){
                    for(int l=pos[2]; l<pos[2] + size; l++){
                        if(data.get(j, k, l) != first){
                            empty = false;
                            break;
                        }
                    }
                    if(!empty) break;
                }
                if(!empty) break;
            }
            if(!empty && curLOD < maxLOD && childSize >= 2){
                children[i].constructOctree(data, maxLOD, curLOD + 1);
            }
        }
    }

    public ArrayList<OctreeNode> getChildrenAtLOD(int maxLOD, int curLOD){

        ArrayList<OctreeNode> target = new ArrayList<>();
        if(children[0] == null || maxLOD == curLOD || size == 1){
            if(value == 1){
                target.add(this);
            }
        }else{
            for(OctreeNode child : children){
                target.addAll(child.getChildrenAtLOD(maxLOD, curLOD + 1));
            }
        }

        return target;
    }
}
