package com.octreetest.game;

import java.util.ArrayList;

public class OctreeNode {
    OctreeNode[] children;
    int size;
    int[] pos;
    int value;

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

    public OctreeNode(int size, int[] pos, int value){
        children = new OctreeNode[8];
        this.size = size;
        this.pos = pos;
        this.value = value;
    }

    public void constructOctree(VoxelData data, int maxLOD, int curLOD){
        //System.out.println("constructing octree level " + curLOD);
        int[] parentPos = this.pos;
        int parentSize = this.size;
        //System.out.println(parentPos[0] + ", " + parentPos[1] + ", " + parentPos[2]);
        //System.out.println(parentSize);
        for(int i=0; i<8; i++){
            int childSize = parentSize / 2;
            //System.out.println(childSize);
            int[] childPos = {parentPos[0] + childOffsets[i][0] * childSize,
                    parentPos[1] + childOffsets[i][1] * childSize, parentPos[2] + childOffsets[i][2] * childSize};
            int first = data.get(childPos[0], childPos[1], childPos[2]); //data[childPos[0]][childPos[1]][childPos[2]];
            children[i] = new OctreeNode(childSize, childPos, first);
            //System.out.println(first);
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
                //System.out.println(first + ", " + test);
                children[i].constructOctree(data, maxLOD, curLOD + 1);
            }
        }
    }

    public ArrayList<OctreeNode> getChildrenAtLOD(int maxLOD, int curLOD){

        ArrayList<OctreeNode> target = new ArrayList<>();
        /*if(children[0] != null && curLOD < maxLOD-1) {
            for (OctreeNode child : children) {
                //System.out.println("found children at " + curLOD);
                if(child.value == 1)
                    target.addAll(child.getChildrenAtLOD(maxLOD, curLOD + 1));
            }
        }else if(children[0] == null){
            //System.out.println("added node at size " + this.size);
            target.add(this);
        }*/

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
