package net.pixelstatic.pixtigen.generator;



import net.pixelstatic.pixtigen.generator.VertexObject.PolygonType;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class VertexGenerator{
	public float segmentRotation = 0;
	public float segmentCompactness = 0.8f;
	public int segments = 10;
	

	/** Creates a pine tree by stacking segment polygons on top of each other.*
	 * @param part the polygon segment to be stacked.*/
	public void generatePineTree(VertexObject part){
				
		VertexList list = part.lists.get("leafsegment");
		part.lists.remove("leafsegment");
		
		float height = list.height();
		float scl = 1f;
		float seg = 1f/segments;
		float offsety = 0, lastx = 0f, lastrotation = 0;
		float compactness = segmentCompactness;
		float rotatecompactness = 0.7f;
		for(int i = 0; i < segments; i ++){
			VertexList newlist = new VertexList(new Array<Vector2>(), PolygonType.polygon, list.material);
			float rotation = lastrotation*0.9f + segmentRotation;
			Vector2 rotatevector = new Vector2(0,scl*height*compactness*rotatecompactness).rotate(rotation);
			for(Vector2 vector : list.vertices)
				newlist.vertices.add(vector.cpy().scl(scl));
			
			lastx += rotatevector.x;
			newlist.rotate(rotation);
			newlist.translate(lastx, offsety);
			part.lists.put("leafsegment" + i, newlist);
			lastrotation = rotation;
			offsety += rotatevector.y*1f/rotatecompactness;
			
			scl -= seg;
		}
		
		//part.lists.get("trunk").translate(0, 185f);
	
	}
}
