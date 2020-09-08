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
import java.util.SortedMap;
import java.util.TreeMap;

import com.octreetest.game.WorldGenerationThread.QueuedChunk;

public class World implements RenderableProvider {

    class ChunkPacket{
        Chunk chunk;
        Mesh mesh;
        int vertexCount = 0;
        ChunkPacket(Chunk chunk, Mesh mesh){
            this.chunk = chunk;
            this.mesh = mesh;
            //refresh();
        }

        public void refresh(){
            int numVerts = chunk.getChunkVertices(vertices);
            vertexCount = numVerts / 4 * 6;
            mesh.setVertices(vertices, 0, numVerts * Chunk.VERTEX_SIZE);
            chunk.dirty = false;
        }
    }

    public static final int CHUNK_SIZE = 16;

    //public ArrayList<Chunk> cache;
    public HashMap<String, ChunkPacket> chunks;
    //public HashMap<String, Chunk> chunks;
    //public HashMap<String, Chunk> cache;
    public float[] vertices;
    int len = CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 12;
    short[] indices = new short[len];
    public ArrayList<Integer> vertexCount;
    final Material mat;
    //public SortedMap<String, Mesh> meshes;
    static public int renderedChunks;
    int chunksX;
    int chunksY;
    int chunksZ;
    static byte farLOD = 2;
    static byte midLOD = 3;
    static byte nearLOD = 4;
    static WorldGenerationThread worldGen;

    public World(int width, int height, int depth){
        chunksX = width;
        chunksY = height;
        chunksZ = depth;
        //chunks = new ArrayList<>(width * depth * height);
        chunks = new HashMap<>();

        worldGen = new WorldGenerationThread("WorldGeneration", chunksX, chunksY, chunksZ, CHUNK_SIZE);
        worldGen.start();
        //chunks = worldGen.getChunks();

        //System.out.println(worldGen.getChunks().size());
        for(Chunk c : worldGen.getChunks()){
            //chunks.add(new ChunkPacket(c, addMesh()));
            chunks.put(Utility.posToString(c.pos), new ChunkPacket(c, addMesh()));
        }

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

        //this.meshes = new ArrayList<>(chunksX * chunksY * chunksZ);
        //this.meshes = new TreeMap<>();
        /*for (i = 0; i < chunksX * chunksY * chunksZ; i++) {
            addMesh();
            meshes.get(i).setIndices(indices);
        }*/
        /*for(Chunk c : chunks){
            addMesh(Utility.posToString(c.pos));
        }*/

        this.vertexCount = new ArrayList<>(chunksX * chunksY * chunksZ);
        this.vertices = new float[Chunk.VERTEX_SIZE * 6 * CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
        mat = new Material(new ColorAttribute(ColorAttribute.Diffuse, 1f, 1f, 1f, 1f));

    }

    public Mesh addMesh(){
        Mesh mesh = new Mesh(true, CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 6 * 4, CHUNK_SIZE * CHUNK_SIZE
                * CHUNK_SIZE * 36 / 3, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.ColorUnpacked());
        mesh.setIndices(indices);
        //meshes.add(mesh);
        //meshes.put(key, mesh);
        return mesh;
    }

    public void dispose(){
        worldGen.stop();
    }

    public void addChunk(int[] pos){
        addChunk(new QueuedChunk(pos));
    }

    public void addChunk(QueuedChunk chunk){
        String key = Utility.posToString(chunk.pos);
        worldGen.requestChunk(chunk);
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        synchronized(worldGen.sSyncObj) {

            /*if(worldGen.getChunks() != null){
                chunks = worldGen.getChunks();
            }
            //worldGen.cloning = true;
            chunks = worldGen.getChunks();*/

            ArrayList<ChunkPacket> values = new ArrayList<>(chunks.values());

            for(Chunk c : worldGen.getChunks()){
                //chunks.add(new ChunkPacket(c, addMesh()));
                String key = Utility.posToString(c.pos);
                if(!chunks.containsKey(key)){
                    chunks.put(key, new ChunkPacket(c, addMesh()));
                }else{
                    chunks.get(key).chunk.dirty = true;
                }

            }

            renderedChunks = 0;

            /*System.out.println("AFTER--------------------");
            for(Chunk c : chunks){
                System.out.println(Utility.posToString(c.pos));
            }*/

            //ArrayList<Mesh> meshArr = new ArrayList<>(meshes.values());
            for (ChunkPacket chunkPacket : values) {
                //Chunk chunk = chunks.get(i);
                //Mesh mesh = meshArr.get(i);
                Chunk chunk = chunkPacket.chunk;
                Mesh mesh = chunkPacket.mesh;

                if (chunk.dirty) {
                    chunkPacket.refresh();
                }
                //if (vertexCount.get(i) == 0) continue;
                if (chunkPacket.vertexCount == 0) continue;

                Renderable renderable = pool.obtain();
                renderable.meshPart.mesh = mesh;
                renderable.meshPart.offset = 0;
                renderable.meshPart.size = chunkPacket.vertexCount;
                renderable.meshPart.primitiveType = GL20.GL_TRIANGLES;
                renderable.material = mat;
                renderables.add(renderable);
                renderedChunks++;
            }
            //worldGen.cloning = false;
        }
    }
}
