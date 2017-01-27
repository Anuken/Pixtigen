package io.anuke.pixtigen.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

public class ActorAlign{
	private static Array<ActorAlign> aligns = new Array<ActorAlign>();
	public static Stage stage;
	private float wscl, hscl;
	private float x, y;
	private int align;
	public Actor actor;

	public ActorAlign(Actor actor, int align, float wscl, float hscl, float xoffset, float yoffset){
		set(align, wscl, hscl, xoffset, yoffset);
		this.actor = actor;
		stage.addActor(actor);
		aligns.add(this);
	}

	public void set(int align, float wscl, float hscl, float xoffset, float yoffset){
		this.wscl = wscl;
		this.hscl = hscl;
		this.x = xoffset;
		this.y = yoffset;
		this.align = align;

	}

	public void update(){
		actor.setPosition(Gdx.graphics.getWidth() * wscl + x, Gdx.graphics.getHeight() * hscl + y, align);
	}
	

	public static void removeAlign(ActorAlign align){
		aligns.removeValue(align, true);
	}
	
	public static void removeAlign(Actor actor){
		for(ActorAlign align : aligns){
			if(align.actor == actor){
				aligns.removeValue(align, true);
				return;
			}
		}
	}

	public static void updateAll(){
		for(ActorAlign align : aligns){
			align.update();
		}
	}
}