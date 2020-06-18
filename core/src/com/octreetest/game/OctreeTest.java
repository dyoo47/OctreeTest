package com.octreetest.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import java.util.ArrayList;
import java.util.List;

public class OctreeTest extends ApplicationAdapter {

	public Environment environment;
	public PerspectiveCamera cam;
	public CameraInputController camController;
	public ModelBatch modelBatch;
	public Model model;
	public ModelInstance instance;
	public ModelBuilder modelBuilder;
	OctreeNode initNode;
	List<OctreeNode> renderNodes = new ArrayList<OctreeNode>();
	List<ModelInstance> renderInstances = new ArrayList<ModelInstance>();
	int[][][] world;
	int worldSize;
	int curLOD = 4;
	
	@Override
	public void create () {

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		modelBatch = new ModelBatch();

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(10f, 10f, 10f);
		cam.lookAt(0,0,0);
		cam.near = 1f;
		cam.far = 300f;
		cam.update();

		modelBuilder = new ModelBuilder();
		model = modelBuilder.createBox(5f, 5f, 5f,
				new Material(ColorAttribute.createDiffuse(Color.GREEN)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
		instance = new ModelInstance(model);

		camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);

		worldSize = 16;
		initNode = new OctreeNode(worldSize, new int[]{0, 0, 0}, 0);
		world = new int[worldSize][worldSize][worldSize];
		for(int i=0; i<worldSize; i++){
			for(int j=0; j<worldSize; j++){
				for(int k=0; k<worldSize; k++){
					if(SimplexNoise.noise(i, j, k) > -0.5 && SimplexNoise.noise(i, j, k) < 0.5){
						world[i][j][k] = 1;
					}
				}
			}
		}
		initNode.constructOctree(world, 8, 0);
		adjustLOD(0);

	}

	void adjustLOD(int increment){
		curLOD += increment;
		if(curLOD <= 0){
			curLOD = 1;
		}
		renderNodes.clear();
		renderInstances.clear();
		renderNodes.addAll(initNode.getChildrenAtLOD(curLOD, 0));
		for(OctreeNode node : renderNodes){
			renderInstances.add(new ModelInstance(modelBuilder.createBox(node.size, node.size, node.size,
					new Material(ColorAttribute.createDiffuse(Color.GREEN)),
					VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal), node.pos[0], node.pos[1], node.pos[2]));
			//System.out.println(node.size);
		}
	}

	@Override
	public void render () {
		camController.update();
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		modelBatch.begin(cam);
		//modelBatch.render(instance, environment);
		for(ModelInstance instance : renderInstances){
			modelBatch.render(instance, environment);
		}
		modelBatch.end();

		if(Gdx.input.isKeyJustPressed(Input.Keys.Q)){
			adjustLOD(1);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.E)){
			adjustLOD(-1);
		}
	}
	
	@Override
	public void dispose () {
		modelBatch.dispose();
		model.dispose();
	}
}
