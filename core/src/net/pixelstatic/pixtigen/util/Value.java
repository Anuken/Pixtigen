package net.pixelstatic.pixtigen.util;

import net.pixelstatic.pixtigen.generator.Crystal;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;

public abstract class Value<T>{
	protected T object, defobject;
	
	public Value(T t){
		this.object = t;
		this.defobject = t;
	}
	
	public abstract Actor getActor(Skin skin);
	public abstract void onChange(Actor actor);
	
	public String toString(){
		return object.toString();
	}
	
	public void reset(Actor actor){
		this.object = defobject;
	}
	
	public T getValue(){
		return object;
	}
	
	public <N> N getValue(Class<N> n){
		return n.cast(object);
	}
		
	public static class CrystalValue extends Value<Crystal>{
		
		protected CrystalValue(){super(null);}

		public CrystalValue(Crystal t){
			super(t);
		}

		@Override
		public Actor getActor(Skin skin){
			SelectBox<Crystal> box = new SelectBox<Crystal>(skin);
			box.setItems(Crystal.values());
			box.setSelected(object);
			return box;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onChange(Actor actor){
			object = ((SelectBox<Crystal>)actor).getSelected();
		}
		
		@SuppressWarnings("unchecked")
		public void reset(Actor actor){
			this.object = defobject;
			((SelectBox<Crystal>)actor).setSelected(object);
		}
		
		public CrystalValue clone(){
			return new CrystalValue(object);
		}
	}
	
	public static class FloatValue extends Value<Float>{
		private float min, max;
		
		protected FloatValue(){super(null);}
		
		public FloatValue(float min, float max, float def){
			super(def);
			this.min = min;
			this.max = max;
		}
		
		@Override
		public Actor getActor(Skin skin){
			Slider slider = new Slider(min,max,(max-min)/100f,false,skin);
			slider.setValue(object);
			return slider;
		}

		@Override
		public void onChange(Actor actor){
			object = ((Slider)actor).getValue();
		}
		
		public void reset(Actor actor){
			this.object = defobject;
			((Slider)actor).setValue(object);
		}
		
		public String toString(){
			String string = object.toString();
			return string.length() > 3 ? string.substring(0, 4) : string;
		}
		
		public FloatValue clone(){
			return new FloatValue(min, max, object);
		}
	}
	
	public abstract Value<?> clone();
}
