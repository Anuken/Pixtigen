package net.pixelstatic.pixtigen.gui;

import net.pixelstatic.pixtigen.generator.Material;
import net.pixelstatic.pixtigen.generator.VertexList;
import net.pixelstatic.pixtigen.generator.VertexObject.PolygonType;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

public class VertexCanvas{
	int index;
	public VertexList list;
	TextButton button;
	public String name;
	ActorAlign align;
	boolean symmetry;

	public VertexCanvas(String name, int index){
		this.index = index;
		this.name = name;
		list = new VertexList(new Array<Vector2>(), PolygonType.polygon, Material.leaves);
	}
	
	public Array<Vector2> vertices(){
		return list.vertices;
	}

	public void update(VertexCanvas selected, VertexEditor editor, VertexGUI gui){
		if(button == null){
			button = new TextButton(name, gui.skin, "toggle");
			button.setSize(100, 30);
			align = new ActorAlign(button, Align.topLeft, 0, 1, 0, -index * button.getHeight());
			VertexCanvas canvas = this;
			button.addListener(new ClickListener(){
				public void clicked(InputEvent event, float x, float y){
					editor.selectedCanvas = canvas;
					updateBoxes(gui);
				}
			});

		}
		align.set(Align.topLeft, 0, 1, 0, -index * button.getHeight());
		button.setChecked(selected == this);
	}

	public void updateBoxes(VertexGUI gui){
		gui.field.setText(name);
		gui.box.setSelected(list.material);
		gui.typebox.setSelected(list.type);
	}

	public void delete(){
		button.remove();
		ActorAlign.removeAlign(align);
	}

	public void clear(){
		list.vertices.clear();
	}
}
