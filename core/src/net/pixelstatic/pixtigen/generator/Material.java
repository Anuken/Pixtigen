package net.pixelstatic.pixtigen.generator;

import com.badlogic.gdx.graphics.Color;
import com.jhlabs.image.CellularFilter;

public enum Material{
	wood(Color.BROWN.cpy().sub(0.1f, 0.1f, 0.1f, 0f), -1), 
	leaves(new Color(0.26f, 0.72f, 0.0f, 1f), -1);
	
	private Color originalColor;
	public Color color;
	public int type = CellularFilter.TRIANGULAR;

	private Material(Color color){
		this.color = color;
		this.originalColor = color.cpy();
	}

	private Material(Color color, int type){
		this(color);
		this.type = type;
	}
	
	public void resetColor(){
		color = originalColor.cpy();
	}

	public Color getColor(){
		return color;
	}
}
