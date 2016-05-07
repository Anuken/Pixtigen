package net.pixelstatic.pixtigen.gui;
import net.pixelstatic.pixtigen.Pixtigen;
import net.pixelstatic.pixtigen.generator.*;
import net.pixelstatic.pixtigen.generator.VertexObject.PolygonType;
import net.pixelstatic.pixtigen.util.ValueMap;
import net.pixelstatic.utils.Hue;
import net.pixelstatic.utils.modules.Module;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;


public class VertexEditor extends Module<Pixtigen>{
	final Color otherVerticeColor = Color.BLUE;
	final Color verticeColor = Color.RED;
	final Color selectColor = Color.PURPLE;
	final Color nodeColor = Color.GREEN;
	final float grabrange = 20;
	ShapeRenderer shape = new ShapeRenderer();
	Array<VertexCanvas> canvases = new Array<VertexCanvas>();
	VertexCanvas selectedCanvas;
	Vector2 vertice;
	VertexGUI gui;
	TreeGenerator tree;
	boolean drawing, drawMode = true;
	float offsetx, offsety, treeScale = 4f;

	@Override
	public void update(){
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		updateCanvases();
		input();
		shape.begin(ShapeType.Line);
		draw();
		shape.end();
		drawTree();
		ActorAlign.updateAll();
	}

	void updateCanvases(){
		for(VertexCanvas canvas : canvases)
			canvas.update(this.selectedCanvas, this, gui);
	
	}

	void drawTree(){
		gui.stage.getBatch().setColor(Color.WHITE);
		gui.stage.getBatch().begin();
		gui.stage.getBatch().draw(tree.getTexture(), 0, 0, tree.getTexture().getWidth() * treeScale, tree.getTexture().getHeight() * treeScale);
		gui.stage.getBatch().end();
	}

	void draw(){
		//draw center of coords
		shape.set(ShapeType.Line);
		shape.setColor(Hue.rgb(106, 162, 246));
		shape.line(Gdx.graphics.getWidth() / 2 + offsetx, 0, Gdx.graphics.getWidth() / 2 + offsetx, Gdx.graphics.getHeight());
		shape.line(0, Gdx.graphics.getHeight() / 2 + offsety, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 2 + offsety);

		for(VertexCanvas canvas : canvases){
			if(canvas == this.selectedCanvas) continue;
			drawVertices(canvas, canvas.vertices(), false);
		}

		drawVertices(selectedCanvas, selectedCanvas.vertices(), false);
		if(selectedCanvas.symmetry && drawing) drawVertices(selectedCanvas, mirror(selectedCanvas.vertices()), true);


		shape.set(ShapeType.Line);
		shape.setColor(Hue.rgb(130, 52, 180));
		if(selectedCanvas.symmetry && drawing) shape.line(centerx(), 0, centerx(), Gdx.graphics.getHeight());

		float lineoffset = 1f;
		shape.setColor(Hue.rgb(76, 52, 255));
		shape.rect(lineoffset, lineoffset, tree.width * treeScale, tree.height * treeScale);
	}

	float centerx(){
		return Gdx.graphics.getWidth() / 2 + offsetx;
	}

	float centery(){
		return Gdx.graphics.getHeight() / 2 + offsety;
	}

	void drawVertices(VertexCanvas canvas, Array<Vector2> vertices, boolean mirror){
		shape.set(ShapeType.Line);
		shape.setColor(canvas.list.material.getColor());
		Gdx.gl.glLineWidth(4);
		shape.setAutoShapeType(true);
		for(int i = 0;i < vertices.size;i ++){
			Vector2 current = vertices.get(i);
			Vector2 next = (i == vertices.size - 1 ? (((drawing && canvas == this.selectedCanvas) || canvas.list.type == PolygonType.line) ? null : vertices.get(0)) : vertices.get(i + 1));
			if(next != null) shape.line(current.cpy().add(centerx(), centery()), next.cpy().add(centerx(), centery()));

		}

		if(drawing && vertices.size != 0 && canvas == this.selectedCanvas) shape.line(vertices.peek().cpy().add(centerx(), centery()), mouseVector().scl((mirror ? -1 : 1), 1f).add(centerx(), centery()));

		Gdx.gl.glLineWidth(2);
		shape.setColor(selectColor);

		Vector2 selected = selectedVertice();

		if(vertice != null){
			shape.setColor(selectColor);
			selected = vertice;
		}else if(selected != null){
			shape.setColor(Color.BLUE);
		}

		shape.setColor(selectColor);

		if((selected != null || vertice != null) && !drawing && canvas == this.selectedCanvas) shape.circle(centerx() + selected.x, centery() + selected.y, grabrange);

		shape.set(ShapeType.Filled);
		if(canvas == this.selectedCanvas) for(int i = 0;i < vertices.size;i ++){
			Vector2 current = vertices.get(i);
			if(current == vertice){
				shape.setColor(Color.YELLOW);
			}else{
				shape.setColor(nodeColor);
			}
			shape.circle(centerx() + current.x, centery() + current.y, 10);
		}

		shape.setColor(Color.CYAN);
		if(drawing && canvas == this.selectedCanvas) shape.circle(centerx() + mouseVector().x * (mirror ? -1 : 1), centery() + mouseVector().y, 10);

	}

	Array<Vector2> mirror(Array<Vector2> vertices){
		Array<Vector2> copy = new Array<Vector2>();
		for(Vector2 vector : vertices){
			copy.add(vector.cpy().scl( -1, 1));
		}
		return copy;
	}

	void finishDrawMode(){
		if( !selectedCanvas.symmetry) return;
		selectedCanvas.list.mirrorVertices();
	}

	Vector2 mouseVector(){
		return new Vector2(Gdx.input.getX() - (centerx()), (Gdx.graphics.getHeight() - Gdx.input.getY()) - (centery()));
	}

	Vector2 selectedVertice(){
		float min = Float.MAX_VALUE;
		Vector2 selected = null;
		for(Vector2 vector : selectedCanvas.vertices()){
			float dist = vector.dst(Gdx.input.getX() - centerx(), (Gdx.graphics.getHeight() - Gdx.input.getY()) - centery());
			if(dist < min && dist < grabrange){
				selected = vector;
				min = dist;
			}
		}
		
		return selected;
	}

	void input(){
		
		if(vertice != null){
			vertice.set(Gdx.input.getX() - centerx(), (Gdx.graphics.getHeight() - Gdx.input.getY()) - centery());
		}

		if(Gdx.input.isButtonPressed(Buttons.LEFT) && (Gdx.input.getX() < Gdx.graphics.getWidth() - 130 || Gdx.input.getY() > 30)){
			//	editor.stage.setKeyboardFocus(null);
		}
		if(gui.stage.getKeyboardFocus() != null) return;
		float speed = 6f;
		float offsetx = 0, offsety = 0;
		
/*
		if(Gdx.input.isKeyJustPressed(Keys.R)){
			System.out.println("yay");
			int minvertices = Integer.MAX_VALUE;
			for(VertexCanvas canvas : canvases)
				minvertices = Math.min(canvas.list.vertices.size, minvertices);
			
			if(minvertices >= 3){
				tree.setVertexObject(new VertexObject(canvases));
				tree.generate();
			}else{
				gui.showInfo("Each polygon must have at least 3 vertices\nfor the tree to generate!");
			}
		}
		*/

		if(Gdx.input.isKeyPressed(Keys.W)) offsety += speed;
		if(Gdx.input.isKeyPressed(Keys.D)) offsetx += speed;
		if(Gdx.input.isKeyPressed(Keys.S)) offsety -= speed;
		if(Gdx.input.isKeyPressed(Keys.A)) offsetx -= speed;

		if( !Gdx.input.isKeyPressed(VertexInput.alt_key)){
			this.offsetx -= offsetx;
			this.offsety -= offsety;
		}else{
			selectedCanvas.list.translate(offsetx, offsety);
		}
		if(Gdx.input.isKeyJustPressed(Keys.SPACE)) drawMode = !drawMode;
	}
	
	public void init(){
		gui = getModule(VertexGUI.class);
		gui.editor = this;
		shape = new ShapeRenderer();
		tree = new TreeGenerator();
		
		selectedCanvas = new VertexCanvas("leafsegment", 0);
		selectedCanvas.list.material = Material.leaves;
		canvases.add(selectedCanvas);

		VertexCanvas trunk = addCanvas("trunk");
		trunk.list.material = Material.wood;
		
	}
	
	@SuppressWarnings("unchecked")
	void loadState(FileHandle file){
		EditorState save = EditorState.readState(file);
		
		//note: this random casting is needed because JSON serializes enums as strings?
		ObjectMap<String, ObjectMap<String, ValueMap>> fmap = (ObjectMap<String, ObjectMap<String, ValueMap>>)((Object)save.filtervalues);
		
		for(Filter filter : Filter.values()){
			for(Material material : Material.values()){
				ValueMap values = fmap.get(filter.toString()).get(material.toString());
				
				for(String key : values.valueNames()){
					filter.materialValueMap().get(material).add(key, values.get(key));
				}
			}
		}
		
		for(Material material : Material.values()){
			for(Filter filter : Filter.values()){
				tree.setFilter(material, filter, save.filters.get(material.toString()).get(filter.toString()));
			}
		}
		
		ObjectMap<String, Color> map = (ObjectMap<String, Color>)((Object)save.colors);
		for(Material material : Material.values()){
			material.color = map.get(material.toString());
		}
		
		loadObject(save.vertexobject);
	}

	void saveState(FileHandle file){
		EditorState save = new EditorState();
		
		for(Filter filter : Filter.values())
			save.filtervalues.put(filter, filter.materialValueMap());

		for(Material material : Material.values()){
			save.colors.put(material, material.getColor());
			System.out.println(material.getColor());
		}
		
		for(Material material : Material.values()){
			save.filters.put(material.toString(), new ObjectMap<String, Boolean>());
			for(Filter filter : Filter.values()){
				save.filters.get(material.toString()).put(filter.toString(), tree.isFilterEnabled(material, filter));
			}
		}

		save.vertexobject = new VertexObject(canvases);
		
		EditorState.writeState(save, file);
	}

	void exportImage(String path){
		if( !path.endsWith(".png")) path += ".png";
		PixmapIO.writePNG(Gdx.files.absolute(path), tree.getPixmap());
	}

	void loadObject(VertexObject object){
		for(VertexCanvas canvas : canvases)
			canvas.delete();
		canvases.clear();
		for(String string : object.lists.keys()){
			VertexCanvas canvas = addCanvas(string);
			canvas.list.vertices = object.lists.get(string).vertices;
			canvas.list.material = object.lists.get(string).material;
			canvas.list.type = object.lists.get(string).type;
		}
		selectedCanvas = canvases.first();
		selectedCanvas.updateBoxes(gui);
	}

	void fixCanvases(){
		for(int i = 0;i < canvases.size;i ++){
			VertexCanvas canvas = canvases.get(i);
			canvas.index = i;
			canvas.update(canvas, this, gui);
		}
	}

	public VertexCanvas addCanvas(String name){
		VertexCanvas canvas = new VertexCanvas(name, canvases.size);
		canvases.add(canvas);
		return canvas;
	}

	ActorAlign align(Actor actor, int align, float wscl, float hscl, float xoffset, float yoffset){
		return new ActorAlign(actor, align, wscl, hscl, xoffset, yoffset);
	}

	@Override
	public void resize(int width, int height){
		shape.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		shape.updateMatrices();
	}

}
