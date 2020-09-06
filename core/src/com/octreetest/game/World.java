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
import java.util.ArrayList;
import java.util.HashMap;

import com.octreetest.game.WorldGenerationThread.QueuedChunk;

public class World implements RenderableProvider {

    public static final int CHUNK_SIZE = 16;

    //public ChunkCollection chunkCollection;
    //public Chunk[] cache;
    public ArrayList<Chunk> cache;
    //public Chunk[] chunks;
    public ArrayList<Chunk> chunks;
    public float[] vertices;
    int len = CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 12;
    short[] indices = new short[len];
    public ArrayList<Integer> vertexCount;
    final Material mat;
    public ArrayList<Mesh> meshes;
    static public int renderedChunks;
    int chunksX;
    int chunksY;
    int chunksZ;
    static byte farLOD = 3;
    static byte nearLOD = 4;
    static WorldGenerationThread worldGen;
    static ArrayList<QueuedChunk> queue;
    static HashMap<QueuedChunk, String> chunkMap;
    static final int QUEUE_LIMIT = 800;
    //Chunk[] chunks;

    public World(int width, int height, int depth){
        chunksX = width;
        chunksY = height;
        chunksZ = depth;
        chunks = new ArrayList<>(width * depth * height);

        worldGen = new WorldGenerationThread("WorldGeneration", chunksX, chunksY, chunksZ, CHUNK_SIZE);
        worldGen.start();

        cache = new ArrayList<>(2);
        cache.add(0, new Chunk(new int[]{0, 0, 0}, nearLOD));
        cache.add(1, new Chunk(new int[]{0, 16, 0}, nearLOD));

        queue = new ArrayList<>(QUEUE_LIMIT);
        chunkMap = new HashMap<>();

        int i = 0;
        short j = 0;
        for(i = 0; i < len; i+= 6, j+= 4){
            indices[i] = (j);
            indices[i + 1] = (short)(j + 1);
            indices[i + 2] = (short)(j + 2);
            indices[i + 3] = (short)(j + 2);
            indices[i + 4] = (short)(j + 3);
            indices[i + 5] = (j);
        }

        this.meshes = new ArrayList<>(chunksX * chunksY * chunksZ);
        for (i = 0; i < chunksX * chunksY * chunksZ; i++) {
            addMesh();
            meshes.get(i).setIndices(indices);
        }

        this.vertexCount = new ArrayList<>(chunksX * chunksY * chunksZ);
        this.vertices = new float[Chunk.VERTEX_SIZE * 6 * CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
        mat = new Material(new ColorAttribute(ColorAttribute.Diffuse, 1f, 1f, 1f, 1f));

    }

    public void addMesh(){
        Mesh mesh = new Mesh(true, CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 6 * 4, CHUNK_SIZE * CHUNK_SIZE
                * CHUNK_SIZE * 36 / 3, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.ColorUnpacked());
        mesh.setIndices(indices);
        meshes.add(mesh);
    }

    public void dispose(){
        worldGen.stop();
    }

    public synchronized void addChunk(int[] pos){
        addChunk(new QueuedChunk(pos));
    }

    public void addChunk(QueuedChunk chunk){
        String value = chunk.pos[0] + "," + chunk.pos[1] + "," + chunk.pos[2];
        if(!chunkMap.containsValue(value)){
            if(queue.size() == QUEUE_LIMIT){
                queue.remove(QUEUE_LIMIT - 1);
                System.out.println("Queue overflow!");
            }
            chunkMap.put(chunk, value);
            addMesh();
            //queue.add(chunk);
            worldGen.requestChunk(chunk);
        }
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {

        if(worldGen.getChunks() != null){
            chunks = worldGen.getChunks();
        }else{
            chunks = cache;
        }
        renderedChunks = 0;

        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            Mesh mesh = meshes.get(i);

            if (chunk.dirty) {
                int numVerts = chunk.getChunkVertices(vertices);
                //vertexCount[i] = numVerts / 4 * 6;
                vertexCount.add(i, numVerts / 4 * 6);
                mesh.setVertices(vertices, 0, numVerts * Chunk.VERTEX_SIZE);
                chunk.dirty = false;
            }
            if (vertexCount.get(i) == 0) continue;

            Renderable renderable = pool.obtain();
            renderable.meshPart.mesh = mesh;
            renderable.meshPart.offset = 0;
            renderable.meshPart.size = vertexCount.get(i);
            renderable.meshPart.primitiveType = GL20.GL_TRIANGLES;
            renderable.material = mat;
            renderables.add(renderable);
            renderedChunks++;
        }
    }
}
