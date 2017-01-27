package io.anuke.pixtigen.generator;

import com.badlogic.gdx.math.*;

public class GeneratorPolygon{
	public String name;
	public VertexList list;
	public Polygon polygon;
	public Rectangle boundingbox;
	//public static Vector2 lightsource = new Vector2( -0.4f, 1.5f);
	public Vector2 center;
	public Vector2 lightVertice;
	public Roughness roughness = Roughness.smooth;
	public float layer, diagonal;

	public enum Roughness{
		smooth;
	}

	public GeneratorPolygon(String name, VertexList list, Polygon polygon){
		this.list = list;
		this.polygon = polygon;
		this.name = name;
		calculateBoundingBox();
		calculateCenter();
		this.layer = center.y;
		if(material() == Material.wood) layer = 0;
	}

	private void calculateBoundingBox(){
		this.boundingbox = polygon.getBoundingRectangle();
		diagonal = (float)Math.sqrt(boundingbox.height * boundingbox.height + boundingbox.width * boundingbox.width);
	}

	private void calculateCenter(){
		/*
		Vector2 vector = null;
		for(int i = 0; i < polygon.getVertices().length; i += 2){
			float x = polygon.getVertices()[i];
			float y = polygon.getVertices()[i+1];
			if(vector == null){
				vector = new Vector2(x,y);
			}else{
				vector.add(x,y);
			}
		}
		vector.scl(1f/(polygon.getVertices().length/2f));
		*/
		center = new Vector2();
		center = boundingbox.getCenter(center);

		//Vector2 ray = new Vector2(center);
		//float step = 0.02f;
		/*
		for(int i = 0;i < 20;i ++){
			ray.x -= step;
			ray.y += step;
			if( !polygon.contains(ray)){
				lightVertice = ray;
				return;
			}
		}
		*/
		lightVertice = new Vector2(boundingbox.x, top());
	}
	
	static Vector2 point = new Vector2();
	
	public float distance(float x, float y){
		point.set(x,y);
		float minDist = Float.MAX_VALUE;
		float ignoration = dimensions()/20f;
		for(int i =0; i < list.vertices.size; i ++){
			Vector2 vector = list.vertices.get(i);
			Vector2 next = (i == list.vertices.size - 1) ? list.vertices.get(0) : list.vertices.get(i+1);
			if(MathUtils.isEqual(vector.y, 0, ignoration) || MathUtils.isEqual(next.y, 0, ignoration)) continue;
			float dist = Intersector.distanceSegmentPoint(vector, next, point);
			minDist = Math.min(dist,minDist);
		}
		
		return minDist;
	}

	public Material material(){
		return list.material;
	}

	public boolean above(GeneratorPolygon other){
		return other == null || layer > other.layer;
	}
	
	public float dimensions(){
		return Math.max(boundingbox.width, boundingbox.height);
	}

	public float height(){
		return boundingbox.height;
	}

	public float top(){
		return boundingbox.getHeight() + boundingbox.y;
	}

	public float bottom(){
		return boundingbox.y;
	}
}
