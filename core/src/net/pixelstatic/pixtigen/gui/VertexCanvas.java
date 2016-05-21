package net.pixelstatic.pixtigen.gui;

import net.pixelstatic.pixtigen.generator.Material;
import net.pixelstatic.pixtigen.generator.VertexList;
import net.pixelstatic.pixtigen.generator.VertexObject.PolygonType;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class VertexCanvas{
	int index;
	public VertexList list;
	public String name;
	boolean symmetry;

	public VertexCanvas(String name, int index){
		this.index = index;
		this.name = name;
		list = new VertexList(new Array<Vector2>(), PolygonType.polygon, Material.leaves);
	}
	
	public Array<Vector2> vertices(){
		return list.vertices;
	}

	public void clear(){
		list.vertices.clear();
	}
	
	public String toString(){
		return name;
	}
}
