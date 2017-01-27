package io.anuke.pixtigen.gui;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class FilterDialog extends Dialog{
	private float wheight = 720f;

	public FilterDialog(String title, Skin skin){
		super(title, skin);
	}
	
	public FilterDialog(String title, Skin skin, String s){
		super(title, skin, s);
	}
	
	public void setWindowHeight(float height){
		if(!MathUtils.isEqual(wheight, height))
		setY(getY() +( wheight - height));
		wheight = height;
	}
	
	public float getPrefWidth(){
		return 240f;
	}
	
	public float getHeight(){
		return wheight;
	}

	public float getPrefHeight(){
		return wheight;
	}

}
