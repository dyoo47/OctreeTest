package com.octreetest.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import java.util.ArrayList;
import java.util.List;

public class OctreeTest extends ApplicationAdapter {

	public Environment environment;
	public PerspectiveCamera cam;
	public ModelBatch modelBatch;
	public Model model;
	public ModelInstance instance;
	public ModelBuilder modelBuilder;

	SpriteBatch spriteBatch;
	BitmapFont font;
	FirstPersonCameraController controller;

	PlayerMonitor playerMonitor;
	World world;
	Runtime runtime;

	List<OctreeNode> renderNodes = new ArrayList<OctreeNode>();
	List<ModelInstance> renderInstances = new ArrayList<ModelInstance>();
	int curLOD = 6;

	@Override
	public void create () {
		runtime = Runtime.getRuntime();
		spriteBatch = new SpriteBatch();
		DefaultShader.Config config = new DefaultShader.Config();
		font = new BitmapFont();
		config.defaultCullFace = GL20.GL_FRONT;
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		playerMonitor = new PlayerMonitor(cam);
		cam.position.set(16, 16, 16);
		//cam.lookAt(0,0,0);
		cam.near = 0.5f;
		cam.far = 1000;
		controller = new FirstPersonCameraController(cam);
		controller.setVelocity(10);
		Gdx.input.setInputProcessor(controller);

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		modelBatch = new ModelBatch();
		modelBuilder = new ModelBuilder();
		model = modelBuilder.createBox(5f, 5f, 5f,
				new Material(ColorAttribute.createDiffuse(Color.GREEN)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
		instance = new ModelInstance(model);
		EventScheduler.init();

		world = new World(0, 0, 0);
		adjustLOD(0);

	}

	void adjustLOD(int increment){
		curLOD += increment;
		if(curLOD <= 0){
			curLOD = 1;
		}
		renderNodes.clear();
		renderInstances.clear();
	}

	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		modelBatch.begin(cam);
		modelBatch.render(world, environment);
		modelBatch.end();


		playerMonitor.update();
		controller.update();
		EventScheduler.periodic();
		if(EventScheduler.everyXFrames(60)){
			ArrayList<WorldGenerationThread.QueuedChunk> chunks;
			chunks = playerMonitor.getNearChunks(5, 2, 3, 4);
			for(WorldGenerationThread.QueuedChunk chunk : chunks){
				world.addChunk(chunk);
			}
		}


		spriteBatch.begin();
		font.draw(spriteBatch, "Total Memory: " + Double.toString(Runtime.getRuntime().totalMemory() / 1e9).substring(0, 4) + " GB", 0, 120);
		font.draw(spriteBatch, "Memory Usage: " + Double.toString((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1e9).substring(0, 4) + " GB", 0, 100);
		font.draw(spriteBatch, "X: " + cam.position.x, 0, 80);
		font.draw(spriteBatch, "Y: " + cam.position.y, 0, 60);
		font.draw(spriteBatch, "Z: " + cam.position.z, 0, 40);
		font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond() + ", renderedChunks: " + World.renderedChunks, 0, 20);
		spriteBatch.end();

		if(Gdx.input.isKeyJustPressed(Input.Keys.Q)){
			adjustLOD(1);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.E)){
			adjustLOD(-1);
		}
		if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)){
			//getNode(Gdx.input.getX(), Gdx.input.getY());
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.M)){
			ArrayList<WorldGenerationThread.QueuedChunk> chunks;
			chunks = playerMonitor.getNearChunks(5, 2, 3, 4);
			for(WorldGenerationThread.QueuedChunk chunk : chunks){
				world.addChunk(chunk);
			}
		}
	}
	
	@Override
	public void dispose () {
		modelBatch.dispose();
		model.dispose();
		world.dispose();
	}

	/*OctreeNode getNode(int screenX, int screenY){
		Ray ray = cam.getPickRay(screenX, screenY);
		int csx = (int) ((cam.position.x - (cam.position.x % 16)) / 16);
		int csy = (int) ((cam.position.y - (cam.position.y % 16)) / 16);
		int csz = (int) ((cam.position.z - (cam.position.z % 16)) / 16);
		boolean found = false;
		OctreeNode target = new OctreeNode((byte) -1, new byte[]{0, 0, 0}, (byte) -1);
		Chunk targetChunk = new Chunk(new int[]{-1, -1, -1}, (byte) -1);
		float distance = 16;
		System.out.println("Querying with center chunk " + csx + ", " + csy + ", " + csz);
		for(int i=-1; i<2; i++){
			for(int j=-1; j<2; j++){
				for(int k=-1; k<2; k++){
					System.out.print("	Checking chunk at: " + (i+csx) + ", " + (j+csy) + ", " + (k+csz) + "...");
					if(world.chunkCollection.get(i + csx, j + csy, k + csz) == null){
						System.out.println("	Failed.");
						continue;
					}
					System.out.println("	Found!");
					Chunk chunk = world.chunkCollection.get(i + csx, j + csy, k + csz);
					ArrayList<OctreeNode> nodes = chunk.origin.getChildrenAtLOD(4, 0);
					for(OctreeNode node : nodes){
						BoundingBox box = new BoundingBox(new Vector3(node.pos[0] + (csx + i) * 16, node.pos[1] + (csy + j) * 16, node.pos[2] + (csz + k) * 16),
								new Vector3(node.pos[0] + node.size + (csx + i) * 16, node.pos[1] + node.size + (csy + j) * 16, node.pos[2] + node.size + (csz + k) * 16));
						Vector3 intersection = new Vector3(0, 0, 0);
						if(Intersector.intersectRayBounds(ray, box, intersection) && cam.position.dst(intersection) < distance){
							distance = cam.position.dst(intersection);
							found = true;
							target = node;
							targetChunk = chunk;
							System.out.println("		Found voxel at: " + intersection.x + ", " + intersection.y + ", " + intersection.z);
						}
					}
				}
			}
		}
		if(distance != 16){
			System.out.println("		Deleting voxel at: " + target.pos[0] + ", " + target.pos[1] + ", " + target.pos[2]);
			targetChunk.voxelData.set(target.pos[0], target.pos[1], target.pos[2], (byte) 0);
			//targetChunk.voxelData.set(ix, iy, iz, (byte) 0);
			targetChunk.dirty = true;
			targetChunk.restructOctree();
		}
		if(found) return target;
		else return null;
	}*/
}
