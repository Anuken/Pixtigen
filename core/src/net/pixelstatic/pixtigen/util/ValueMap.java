package net.pixelstatic.pixtigen.util;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Keys;

public class ValueMap{
	private ObjectMap<String, Value<?>> values;
	
	public ValueMap(){
		values = new ObjectMap<String, Value<?>>();
	}
	
	public Value<?> get(String name){
		return values.get(name);
	}
	
	public Object getValue(String name){
		return values.get(name).getValue();
	}
	
	public <T> T get(String name, Class<T> c){
		return c.cast(values.get(name));
	}
	
	public float getFloat(String name){
		return this.get(name).getValue(Float.class);
	}
	
	public void add(String name, Value<?> value){
		values.put(name, value);
	}
	
	public int size(){
		return values.size;
	}
	
	public Keys<String> valueNames(){
		return values.keys();
	}
}
