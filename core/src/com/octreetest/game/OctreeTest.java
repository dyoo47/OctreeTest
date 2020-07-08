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
	World world;

	List<OctreeNode> renderNodes = new ArrayList<OctreeNode>();
	List<ModelInstance> renderInstances = new ArrayList<ModelInstance>();
	int curLOD = 6;

	@Override
	public void create () {
		spriteBatch = new SpriteBatch();
		DefaultShader.Config config = new DefaultShader.Config();
		font = new BitmapFont();
		config.defaultCullFace = GL20.GL_FRONT;
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(128f, 128f, 128f);
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


		world = new World(16, 16, 16);
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

		controller.update();
		spriteBatch.begin();
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
	}
	
	@Override
	public void dispose () {
		modelBatch.dispose();
		model.dispose();
	}
}
