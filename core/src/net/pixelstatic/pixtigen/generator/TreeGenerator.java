package net.pixelstatic.pixtigen.generator;

import net.pixelstatic.pixtigen.generator.VertexObject.PolygonType;
import net.pixelstatic.pixtigen.util.ValueMap;

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
	private ObjectMap<Material, Array<Filter>> filters = new ObjectMap<>();
	private Array<Filter> globalfilters = new Array<Filter>();
	private float[][] shading;

	private void processPolygons(){
		drawMaterials();
		clearShading();

		for(int x = 0;x < width;x ++){
			for(int y = 0;y < height;y ++){
				int cy = height - 1 - y;
				Pixel pixel = materials[x][y];
				if(pixel.material != null) 
				for(Filter filter : filters.get(pixel.material)){
					if(!filter.type.isApplied() && (filter.enabled || filter.type.alwaysEnabled())){
						shading[x][y] += filter.type.apply(filter, this, pixmap, materials, pixel, x, y, cy, width, height);
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

		float normal = (shademax - shademin) / 2;

		for(int x = 0;x < width;x ++){
			for(int y = 0;y < height;y ++){
				Pixel pixel = materials[x][y];
				if(pixel.material == null) continue;
				float f = shading[x][y];
				for( Filter filter : filters.get(pixel.material)){
					filter.type.values = filter.values;
					f = filter.type.change(f);
				}
				if(pixel.material == Material.leaves) f -= normal;
				Color color = brighter(new Color(pixmap.getPixel(x, height - 1 - y)), f);
				pixmap.setColor(color);
				pixmap.drawPixel(x, height - 1 - y);
			}
		}
		
		crystallize();

		for(int x = 0;x < width;x ++){
			for(int y = 0;y < height;y ++){
				Pixel pixel = materials[x][y];
				if(pixel.material != null) for(Filter filter : filters.get(pixel.material)){
					if(filter.type.isApplied() && (filter.enabled || filter.type.alwaysEnabled())){
						filter.type.apply(filter, this, pixmap, materials, pixel, x, y, height - 1 - y, width, height);
					}
				}
			}
		}
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

	private void crystallize(){
		int[] colors = new int[width * height];
		
		ObjectMap<Material, ValueMap> enabled = new ObjectMap<>();
		for(Material material : Material.values()){
			for(Filter filter : filters.get(material)){
				if(filter.type == FilterType.crystallize){
					enabled.put(material, filter.values);
					break;
				}
			}
		
		}
		
		//store color array
		for(int x = 0;x < width;x ++){
			for(int y = 0;y < height;y ++){
				if(materials[x][height - 1 - y].material != Material.leaves) continue;
				colors[y * width + x] = /*toARGB*/(pixmap.getPixel(x, y));
			}
		}
		
		//crystallization...
		for(int x = 0;x < width;x ++){
			for(int cy = 0;cy < height;cy ++){
				int y = height - 1 - cy;
				Pixel pixel = materials[x][height - 1 - y];
				if(pixel.material == null) continue;
				
				ValueMap map = enabled.get(pixel.material);
				if(map == null) continue;
				int color = Patterns.leafPattern(x, y, width, height, colors, ((Crystal)map.getValue("type")).ordinal(), map.getFloat("scale"));
				Color cc = new Color(color);

				pixmap.setColor(cc);
				pixmap.drawPixel(x, y);
			}
		}
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

		for(Material material : Material.values())
			filters.put(material, new Array<Filter>());
		addDefaultFilters();

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

	public Vector2 lightSource(){
		return lightsource;
	}

	public Pixmap getPixmap(){
		return pixmap;
	}

	private void addDefaultFilters(){
		globalfilters.add(new Filter(FilterType.shading));
		addFilter(Material.leaves, FilterType.noise);
		addFilter(Material.leaves, FilterType.shadows);
		addFilter(Material.leaves, FilterType.outline);
		addFilter(Material.leaves, FilterType.needles);
		addFilter(Material.leaves, FilterType.light);
		addFilter(Material.leaves, FilterType.lines);
		addFilter(Material.leaves, FilterType.round);
		
		addFilter(Material.wood, FilterType.outline);
		addFilter(Material.wood, FilterType.shadows);
		addFilter(Material.wood, FilterType.bark);
	}

	public void addFilter(Material material, FilterType type){
		filters.get(material).add(new Filter(type));
	}

	public Array<Filter> getFilters(Material material){
		return filters.get(material);
	}
	
	public ObjectMap<Material, Array<Filter>> getAllFilters(){
		return filters;
	}
	
	public void setAllFilters(ObjectMap<Material, Array<Filter>> filters){
		this.filters = filters;
	}
	
	public boolean isFilterEnabled(Material material, FilterType type){
		return false;//filters.get(material)
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
		globalfilters.get(0).type.change(color, a);
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
