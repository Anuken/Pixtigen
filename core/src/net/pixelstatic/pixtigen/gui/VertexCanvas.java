package net.pixelstatic.pixtigen.gui;

import net.pixelstatic.pixtigen.generator.Material;
import net.pixelstatic.pixtigen.generator.VertexList;
import net.pixelstatic.pixtigen.generator.VertexObject.PolygonType;
import net.pixelstatic.utils.graphics.FilledPolygon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class VertexCanvas{
	int index;
	public static TextureRegion texture;
	public VertexList list;
	public String name;
	public FilledPolygon polygon;

	public VertexCanvas(String name, int index){
		this.index = index;
		this.name = name;
		list = new VertexList(new Array<Vector2>(), PolygonType.polygon, Material.leaves);
		polygon = new FilledPolygon(texture, list.vertices);
	}
	
	public Array<Vector2> vertices(){
		return list.vertices;
	}
	
	public void updateSprite(){
		polygon.setVertices(list.vertices);
		polygon.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
		polygon.sprite().setColor(list.material.color);
	}

	public void clear(){
		list.vertices.clear();
	}
	
	public String toString(){
		return name;
	}
}
