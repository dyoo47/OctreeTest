package com.octreetest.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class World implements RenderableProvider {

    public static final int CHUNK_SIZE = 16;

    public Chunk[] chunks;
    public float[] vertices;
    public int[] vertexCount;
    public boolean[] dirty;
    public Material[] materials;
    public Mesh[] meshes;
    static public int renderedChunks;
    int chunksX;
    int chunksY;
    int chunksZ;
    static int farLOD = 3;
    static int nearLOD = 4;

    public World(int width, int height, int depth){
        chunks = new Chunk[width * height * depth];
        chunksX = width;
        chunksY = height;
        chunksZ = depth;
        int i = 0;
        for(int y = 0; y < chunksY; y++){
            for(int z = 0; z < chunksZ; z++){
                for(int x = 0; x < chunksX; x++){
                    Chunk chunk = new Chunk(new int[]{x * CHUNK_SIZE, y * CHUNK_SIZE, z * CHUNK_SIZE}, nearLOD,
                            new int[]{x * CHUNK_SIZE, y * CHUNK_SIZE, z * CHUNK_SIZE});
                    chunks[i++] = chunk;
                }
            }
        }
        int len = CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 12;
        short[] indices = new short[len];
        short j = 0;
        for(i = 0; i < len; i+= 6, j+= 4){
            indices[i + 0] = (short)(j + 0);
            indices[i + 1] = (short)(j + 1);
            indices[i + 2] = (short)(j + 2);
            indices[i + 3] = (short)(j + 2);
            indices[i + 4] = (short)(j + 3);
            indices[i + 5] = (short)(j + 0);
        }
        this.meshes = new Mesh[chunksX * chunksY * chunksZ];
        for (i = 0; i < meshes.length; i++) {
            meshes[i] = new Mesh(true, CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 6 * 4, CHUNK_SIZE * CHUNK_SIZE
                    * CHUNK_SIZE * 36 / 3, VertexAttribute.Position(), VertexAttribute.Normal());
            meshes[i].setIndices(indices);
        }
        this.dirty = new boolean[chunksX * chunksY * chunksZ];
        for (i = 0; i < dirty.length; i++)
            dirty[i] = true;

        this.vertexCount = new int[chunksX * chunksY * chunksZ];
        for (i = 0; i < vertexCount.length; i++)
            vertexCount[i] = 0;

        this.vertices = new float[Chunk.VERTEX_SIZE * 6 * CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];

        this.materials = new Material[chunksX * chunksY * chunksZ];
        for (i = 0; i < materials.length; i++) {
            materials[i] = new Material(new ColorAttribute(ColorAttribute.Diffuse, MathUtils.random(0.5f, 1f), MathUtils.random(
                    0.5f, 1f), MathUtils.random(0.5f, 1f), 1));
        }
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        renderedChunks = 0;
        for (int i = 0; i < chunks.length; i++) {
            //System.out.println(renderedChunks);
            Chunk chunk = chunks[i];
            Mesh mesh = meshes[i];
            if (dirty[i]) {
                int numVerts = chunk.getChunkVertices(vertices);
                //if(numVerts == 0) System.out.println(i);
                vertexCount[i] = numVerts / 4 * 6;
                mesh.setVertices(vertices, 0, numVerts * Chunk.VERTEX_SIZE);
                dirty[i] = false;
            }
            if (vertexCount[i] == 0) continue;
            //System.out.println(renderedChunks);

            Renderable renderable = pool.obtain();
            renderable.meshPart.mesh = mesh;
            renderable.meshPart.offset = 0;
            renderable.meshPart.size = vertexCount[i];
            renderable.meshPart.primitiveType = GL20.GL_TRIANGLES;
            renderable.material = materials[i];
            renderables.add(renderable);
            renderedChunks++;
        }
    }
}
