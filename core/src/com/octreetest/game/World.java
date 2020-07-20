package com.octreetest.game;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class World implements RenderableProvider {

    public static final int CHUNK_SIZE = 16;

    public ChunkCollection chunkCollection;
    public float[] vertices;
    public int[] vertexCount;
    final Material mat;
    public Mesh[] meshes;
    static public int renderedChunks;
    int chunksX;
    int chunksY;
    int chunksZ;
    static byte farLOD = 3;
    static byte nearLOD = 4;
    //Chunk[] chunks;

    public World(int width, int height, int depth){
        chunkCollection = new ChunkCollection(width, height, depth);
        //chunks = new Chunk[width * height * depth];
        chunksX = width;
        chunksY = height;
        chunksZ = depth;
        int i = 0;
        /*for(int y = 0; y < chunksY; y++){
            for(int z = 0; z < chunksZ; z++){
                for(int x = 0; x < chunksX; x++){
                    Chunk chunk = new Chunk(new int[]{x * CHUNK_SIZE, y * CHUNK_SIZE, z * CHUNK_SIZE}, nearLOD,
                            new int[]{x * CHUNK_SIZE, y * CHUNK_SIZE, z * CHUNK_SIZE});
                    chunkCollection.set(x, y, z, chunk);
                    //chunks[i] = chunk;
                    i++;
                }
            }
        }*/

        for(int x = 0; x < chunksX; x++){
            for(int y = 0; y < chunksY; y++){
                for(int z = 0; z < chunksZ; z++){
                    Chunk chunk = new Chunk(new int[]{x * CHUNK_SIZE, y * CHUNK_SIZE, z * CHUNK_SIZE}, nearLOD);
                    chunkCollection.set(x, y, z, chunk);
                }
            }
        }
        int len = CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 12;
        short[] indices = new short[len];
        short j = 0;
        for(i = 0; i < len; i+= 6, j+= 4){
            indices[i] = (j);
            indices[i + 1] = (short)(j + 1);
            indices[i + 2] = (short)(j + 2);
            indices[i + 3] = (short)(j + 2);
            indices[i + 4] = (short)(j + 3);
            indices[i + 5] = (j);
        }
        this.meshes = new Mesh[chunksX * chunksY * chunksZ];
        for (i = 0; i < meshes.length; i++) {
            meshes[i] = new Mesh(true, CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 6 * 4, CHUNK_SIZE * CHUNK_SIZE
                    * CHUNK_SIZE * 36 / 3, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.ColorUnpacked());
            meshes[i].setIndices(indices);
        }
        /*this.dirty = new boolean[chunksX * chunksY * chunksZ];
        for (i = 0; i < dirty.length; i++)
            dirty[i] = true;*/

        this.vertexCount = new int[chunksX * chunksY * chunksZ];
        for (i = 0; i < vertexCount.length; i++)
            vertexCount[i] = 0;

        this.vertices = new float[Chunk.VERTEX_SIZE * 6 * CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];

        mat = new Material(new ColorAttribute(ColorAttribute.Diffuse, 1f, 1f, 1f, 1f));

    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        renderedChunks = 0;
        for (int i = 0; i < chunkCollection.chunks.length; i++) {
            Chunk chunk = chunkCollection.chunks[i];
            Mesh mesh = meshes[i];
            if (chunk.dirty) {
                int numVerts = chunk.getChunkVertices(vertices);
                vertexCount[i] = numVerts / 4 * 6;
                mesh.setVertices(vertices, 0, numVerts * Chunk.VERTEX_SIZE);
                chunk.dirty = false;
            }
            if (vertexCount[i] == 0) continue;

            Renderable renderable = pool.obtain();
            renderable.meshPart.mesh = mesh;
            renderable.meshPart.offset = 0;
            renderable.meshPart.size = vertexCount[i];
            renderable.meshPart.primitiveType = GL20.GL_TRIANGLES;
            renderable.material = mat;
            renderables.add(renderable);
            renderedChunks++;
        }
    }
}
