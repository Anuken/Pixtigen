package net.pixelstatic.pixtigen.generator;

import net.pixelstatic.pixtigen.gui.VertexCanvas;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class VertexObject{
	public int flag;
	public ObjectMap<String, VertexList> lists = new ObjectMap<String, VertexList>();
	
	public enum PolygonType{
		polygon, line
	}

	public VertexObject(){
		
	}

	public VertexObject(Array<VertexCanvas> canvases){
		for(VertexCanvas canvas : canvases){
			lists.put(canvas.name, new VertexList(canvas.list));
		}
	}
	
	public ObjectMap<String, Polygon> getPolygons(){
		ObjectMap<String, Polygon> polygons = new ObjectMap<String, Polygon>();
		for(String key : this.lists.keys()){
			VertexList list = this.lists.get(key);
			if(list.type == PolygonType.polygon){
				polygons.put(key, toPolygon(list.vertices));
			}
		}
		return polygons;
	}
	
	public Rectangle boundingBox(){
		Rectangle rect = new Rectangle();
		for(VertexList poly : lists.values())
			for(Vector2 vertice : poly.vertices){
				rect.x = Math.min(rect.x, vertice.x);
				rect.width = Math.max(rect.width, rect.x + vertice.x);
				rect.height = Math.max(rect.height, vertice.y);
			}
	
		return rect;
	}
	
	public Polygon toPolygon(Array<Vector2> vertices){
		float[] array = new float[vertices.size*2];
		for(int i =0; i < vertices.size; i ++){
			array[i*2] = vertices.get(i).x;
			array[i*2+1] = vertices.get(i).y;
		}
		return new Polygon(array);
	}
	
	public void alignSides(){
		float min = 0, max = 0;
		
		for(VertexList poly : lists.values())
			for(Vector2 vertice : poly.vertices){
				if(vertice.x < min) min = vertice.x;
				if(vertice.x > max) max = vertice.x;
			}
		
		for(VertexList poly : lists.values())
			for(Vector2 vertice : poly.vertices)
				vertice.x -= (min + max)/2; 
	}
	
	public void alignBottom(){
		float min = 0;
		
		for(VertexList poly : lists.values())
			for(Vector2 vertice : poly.vertices)
				min = Math.min(vertice.y, min);
				//if(Math.abs(vertice.y) > min) min = vertice.y;
		for(VertexList poly : lists.values())
			for(Vector2 vertice : poly.vertices)
				vertice.y -= min; 
	}
	
	public void scl(float amount){
		for(VertexList poly : lists.values())
			for(Vector2 vertice : poly.vertices)
				vertice.scl(amount);
	}

	public void normalize(){
		float max = 1;
		for(VertexList poly : lists.values())
			for(Vector2 vertice : poly.vertices){
				if(Math.abs(vertice.x) > max) max = Math.abs(vertice.x);
				if(Math.abs(vertice.y) > max) max = Math.abs(vertice.y);
			}

		for(VertexList poly : lists.values())
			for(Vector2 vertice : poly.vertices){
				vertice.scl(1f / max);
			}
	}

}
