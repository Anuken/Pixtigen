package net.pixelstatic.pixtigen.generator;

import net.pixelstatic.pixtigen.generator.VertexObject.PolygonType;
import net.pixelstatic.pixtigen.util.Value.CrystalValue;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

@SuppressWarnings("unused")
public class TreeGenerator implements Disposable{
	public final int width = 60, height = 80;
	private Pixel[][] materials;
	private Pixmap pixmap;
	private Texture texture;
	private VertexObject object;
	private Array<GeneratorPolygon> polygons;
	private Vector2 lightsource = new Vector2();
	private VertexGenerator vertexgenerator;
	private float scale = 1 / 60f;
	private float canvasScale = 1 / 1000f;
	private boolean autoscale = true;
	private ObjectMap<Material, ObjectMap<Filter, Boolean>> filters = new ObjectMap<Material, ObjectMap<Filter, Boolean>>();
	private float[][] shading;

	private void processPolygons(){
		drawMaterials();

		clearShading();

		Filter[] filters = Filter.values();
		for(Filter filter : filters){
			if(filter.isApplied()) continue;
			for(int x = 0;x < width;x ++){
				for(int y = 0;y < height;y ++){
					int cy = height - 1 - y;
					Pixel pixel = materials[x][y];
					if(pixel.material != null && this.isFilterEnabled(pixel.material, filter)){
						shading[x][y] += filter.apply(this, pixmap, materials, pixel, x, y, cy, width, height);
					}
				}
			}
		}
		float shademin = Float.MAX_VALUE, shademax = Float.MIN_VALUE;
		//normalize colors
		for(int x = 0;x < width;x ++){
			for(int y = 0;y < height;y ++){
				Pixel pixel = materials[x][y];
				if(pixel.material != Material.leaves) continue;
				float f = shading[x][y];
				shademin = Math.min(f, shademin);
				shademax = Math.max(f, shademax);
			}
		}
		
		float normal = (shademax-shademin)/2;

		for(int x = 0;x < width;x ++){
			for(int y = 0;y < height;y ++){
				Pixel pixel = materials[x][y];
				if(pixel.material == null) continue;
				float f = shading[x][y];
				if(isFilterEnabled(pixel.material, Filter.round)) f = Filter.round.change(f);
				if(pixel.material == Material.leaves)f -= normal;
				Color color = brighter(new Color(pixmap.getPixel(x, height - 1 - y)), f);
				pixmap.setColor(color);
				pixmap.drawPixel(x, height - 1 - y);
			}
		}	
		if(this.isFilterEnabled(Material.leaves, Filter.crystallize))
		crystallize();
		
		for(Filter filter : filters){
			if(!filter.isApplied()) continue;
			for(int x = 0;x < width;x ++){
				for(int y = 0;y < height;y ++){
					Pixel pixel = materials[x][y];
					if(pixel.material != null && this.isFilterEnabled(pixel.material, filter))
					filter.apply(this, pixmap, materials, pixel, x, y, height - 1 - y, width, height);
				}
			}
		}
	
		/*
		
		generateShadingPatterns();
		
		addShadows();
		drawOutlines();
		*/
	}

	private void enableDefaultFilters(){
		setFilter(Material.leaves, Filter.noise, true);
		setFilter(Material.leaves, Filter.shadows, true);
		setFilter(Material.leaves, Filter.outline, true);
		setFilter(Material.leaves, Filter.needles, true);
		setFilter(Material.leaves, Filter.light, true);
		setFilter(Material.leaves, Filter.lines, true);
		setFilter(Material.leaves, Filter.round, true);
		setFilter(Material.wood, Filter.outline, true);
		setFilter(Material.wood, Filter.shadows, true);
		setFilter(Material.wood, Filter.bark, true);
	}

	private void clearShading(){
		for(int x = 0;x < width;x ++){
			for(int y = 0;y < height;y ++){
				shading[x][y] = 0;
			}
		}
	}

	private void drawOutlines(){
		for(int x = 0;x < width;x ++){
			for(int cy = 0;cy < height;cy ++){
				int y = height - 1 - cy;
				Pixel pixel = materials[x][y];
				if(pixel.material == null) continue;
				int color = Color.rgba8888(new Color(0, 0, 0, 0.2f));

				if( !same(pixel, x, y - 1)){
					pixmap.drawPixel(x, cy, color);
				}else if( !same(pixel, x, y + 1)){
					pixmap.drawPixel(x, cy, color);
				}else if( !same(pixel, x + 1, y)){
					pixmap.drawPixel(x, cy, color);
				}else if( !same(pixel, x - 1, y)){
					pixmap.drawPixel(x, cy, color);
				}
			}
		}
	}

	private void addShadows(){
		for(int x = 0;x < width;x ++){
			for(int cy = 0;cy < height;cy ++){
				int y = height - 1 - cy;
				if(y == 0) continue;
				int offsetx = x - 2;
				int offsety = y + 3;

				GeneratorPolygon poly = getPixelPolygon(offsetx, offsety);
				GeneratorPolygon other = getPixelPolygon(x, y);
				if(poly == null) continue;

				if( !(poly.above(other))) continue;
				//	float scl = 1f -poly.center.dst(project(offsetx - width / 2), project(offsety)) / (poly.height());

				pixmap.setColor(new Color(0, 0, 0, 0.3f));
				pixmap.drawPixel(x, cy);

			}
		}
	}

	private void drawMaterials(){
		for(int x = 0;x < width;x ++){
			for(int y = 0;y < height;y ++){
				if(materials[x][y] != null && materials[x][y].material != null) pixmap.drawPixel(x, height - 1 - y, Color.rgba8888(materials[x][y].material.getColor()));
			}
		}
	}

	//@SuppressWarnings("unused")
	private void crystallize(){
		int[] colors = new int[width * height];
		//store color array
		for(int x = 0;x < width;x ++){
			for(int y = 0;y < height;y ++){
				if(materials[x][height - 1 -y].material != Material.leaves) continue;
				colors[y * width + x] = /*toARGB*/(pixmap.getPixel(x, y));
			}
		}
		float scale = Filter.crystallize.valueMap(Material.leaves).getFloat("scale");
		int type = Filter.crystallize.valueMap(Material.leaves).get("type", CrystalValue.class).getValue().ordinal();
		//crystallization...
		for(int x = 0;x < width;x ++){
			for(int cy = 0;cy < height;cy ++){
				int y = height - 1 - cy;
				Pixel pixel = materials[x][height - 1 - y];
				if(pixel.material != Material.leaves) continue;

				//if(pixel.material.type == -1) continue;
				int color = Patterns.leafPattern(x, y, width, height, colors, type, scale);
				Color cc = new Color(color);
				
				//cc.a = 1f;
				/*
				float mscl = 0.8f;
				if(cc.a < 0.5f){
					cc.a = 0f;
				}else{
					cc.a = 1f;
					cc.set(pixel.material.getColor().cpy().mul(mscl, mscl, mscl, 1f));
				}
				*/
				pixmap.setColor(cc);
				/*if(new Color(pixmap.getPixel(x, y)).a > 0.001f) */pixmap.drawPixel(x, y);
			}
		}
	}

	private void generateShadingPatterns(){
		//add brightness, leaf patterns
		for(int x = 0;x < width;x ++){
			for(int cy = 0;cy < height;cy ++){
				int y = height - 1 - cy;
				Pixel pixel = materials[x][y];
				if(pixel.polygon == null) continue;
				if(pixel.polygon.material() == Material.leaves){
					generateLeafPattern(x, y, cy, pixel);
				}else if(pixel.polygon.material() == Material.wood){
					generateLogPattern(x, y, cy, pixel);
				}
			}
		}
	}

	private void generateLogPattern(int x, int y, int cy, Pixel pixel){
		Color color = new Color(pixmap.getPixel(x, cy));

		float m = 0f;
		m = (Patterns.nMod(x, y, 0.2f));
		float dist = pixel.polygon.distance(project(x - width / 2), project(y));
		m += dist * 10f / (pixel.polygon.dimensions() * 3.4f);
		if(m > 1.4f) m /= 2f;
		m = round(m, 0.4f);
		m += 1f;
		color.mul(m, m, m, 1f);

		pixmap.drawPixel(x, cy, Color.rgba8888(color));
	}

	private void generateLeafPattern(int x, int y, int cy, Pixel pixel){
		float round = 0.1f;
		float gscl = -0.1f;

		float selflightscale = 0.4f; //default is 0.7f
		float globallightscale = 0.8f; //default is 0.6f;

		float dist = pixel.polygon.height() / 3f + gscl + selflightscale * (((1.5f) - pixel.polygon.lightVertice.dst(project(x - width / 2), project(y))));
		float lightdist = gscl + globallightscale * ((1f - lightsource.dst(project(x - width / 2), project(y))));

		float scl = Patterns.leaves(x + (int)(pixel.polygon.center.x / scale), y + (int)(pixel.polygon.center.y / scale)) + dist + lightdist + Patterns.noise(x, y, 4f, 0.2f) + Patterns.mod(x, y, 0.07f);
		pixmap.drawPixel(x, cy, Color.rgba8888(brighter(new Color(pixmap.getPixel(x, cy)), round * (int)(((scl * 0.5f) / round)))));

	}

	private void loadPolygons(){
		object.alignBottom();
		object.alignSides();
		vertexgenerator.generatePineTree(object);
		if(autoscale){
			object.normalize();
		}else{
			object.scl(canvasScale);
		}

		Rectangle rect = object.boundingBox();
		lightsource.set(rect.x, rect.height);

		ObjectMap<String, Polygon> rawpolygons = object.getPolygons();

		polygons = new Array<GeneratorPolygon>();

		for(String key : rawpolygons.keys())
			polygons.add(new GeneratorPolygon(key, object.lists.get(key), rawpolygons.get(key)));

		for(int x = 0;x < width;x ++){
			for(int y = 0;y < height;y ++){
				float rx = project(x - width / 2), ry = project(y);
				for(GeneratorPolygon poly : polygons){
					if(poly.list.type == PolygonType.line) continue;
					if(poly.polygon.contains(rx, ry)) set(x, y, poly.list.material, poly);
				}
			}
		}
	}

	public TreeGenerator(){
		this(null);
	}

	public TreeGenerator(VertexObject object){
		this.object = object;
		materials = new Pixel[width][height];
		pixmap = new Pixmap(width, height, Format.RGBA8888);
		texture = new Texture(pixmap);
		shading = new float[width][height];
		vertexgenerator = new VertexGenerator();
		for(Material material : Material.values()){
			filters.put(material, new ObjectMap<Filter, Boolean>());
			for(Filter filter : Filter.values()){
				this.filters.get(material).put(filter, false);
			}
		}
		enableDefaultFilters();
	}

	/** Resets the internal pixmap and generates the tree using the {@link VertexObject} provided. **/
	public void generate(){
		if(object == null){
			throw new RuntimeException("Set a vertex object before calling generate()!");
		}

		long starttime = System.currentTimeMillis();
		Pixmap.setBlending(Blending.None);
		for(int x = 0;x < width;x ++){
			for(int y = 0;y < height;y ++){
				materials[x][y] = new Pixel();
				pixmap.drawPixel(x, y, Color.rgba8888(Color.CLEAR));
			}
		}
		Pixmap.setBlending(Blending.SourceOver);

		print("Loading polygons...");
		loadPolygons();

		print("Procesing polygons...");
		processPolygons();

		print("Done generating.");
		texture.draw(pixmap, 0, 0);

		long endtime = System.currentTimeMillis();
		print("Time taken: " + (endtime - starttime) + " ms.");

	}

	public void setVertexObject(VertexObject object){
		this.object = object;
	}

	public Texture getTexture(){
		return texture;
	}

	public VertexGenerator getVertexGenerator(){
		return vertexgenerator;
	}

	public void setAutoScale(boolean autoscale){
		this.autoscale = autoscale;
	}

	public void setCanvasScale(float scale){
		this.canvasScale = scale;
	}

	public void setFilter(Material material, Filter filter, boolean enabled){
		filters.get(material).put(filter, enabled);
	}

	public boolean isFilterEnabled(Material material, Filter filter){
		return filter.alwaysEnabled() || filters.get(material).get(filter);
	}

	public Vector2 lightSource(){
		return lightsource;
	}
	
	public Pixmap getPixmap(){
		return pixmap;
	}
	
	public ObjectMap<Material, ObjectMap<Filter, Boolean>> getFilters(){
		return filters;
	}

	/**Returns an integer projected to polygon coordinates.**/
	public float project(int i){
		return i * scale;
	}

	private void set(int x, int y, Material material, GeneratorPolygon polygon){
		if(x < 0 || y < 0 || x >= width || y >= height) return;
		if( !(materials[x][y].material == null || materials[x][y].material.ordinal() < material.ordinal() || polygon.above(getPixelPolygon(x, y)))) return;
		materials[x][y].material = material;
		materials[x][y].polygon = polygon;
	}

	private GeneratorPolygon getPixelPolygon(int x, int y){
		if(x < 0 || y < 0 || x >= width || y >= height) return null;
		return materials[x][y].polygon;
	}

	private float round(float a, float b){
		return b * (int)(a / b);
	}

	private boolean same(Pixel pixel, int x, int y){
		if(x < 0 || y < 0 || x >= width || y >= height) return true;
		return !pixel.polygon.above(materials[x][y].polygon);
	}

	private Color brighter(Color color, float a){
		Filter.shading.change(color, a);
		return color;
	}

	void print(Object o){
		System.out.println(o);
	}

	static class Pixel{
		Material material;
		GeneratorPolygon polygon;
	}

	@Override
	public void dispose(){
		texture.dispose();
		pixmap.dispose();
	}

}
