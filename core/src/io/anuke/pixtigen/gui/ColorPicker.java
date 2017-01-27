package io.anuke.pixtigen.gui;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.NumberUtils;

import io.anuke.ucore.graphics.Hue;

public class ColorPicker extends Widget{
	private int[] colors = new int[]{Batch.C1, Batch.C2, Batch.C3, Batch.C4};
	private Skin skin;
	private Style style;
	private Sprite saturationbar, brightnessbar;
	private float hue, saturation, brightness;
	private float offset1 = 00, offset2 = 30, offset3 = 60, xoffset = 10, barwidth = 100;
	private int selected;
	private float[] hsbvals = new float[3];

	public ColorPicker(Skin skin){
		this.skin = skin;
		style = skin.get(Style.class);
		setTouchable(Touchable.enabled);
		addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
				float rx = x - getX() - xoffset, ry = y;
				boolean d = (rx >= 0 && rx <= barwidth);
				if(d){
					if(ry > offset1 && ry < offset1 + 20){
						selected = 0;
					}else if(ry > offset2 && ry < offset2 + 20){
						selected = 1;
					}else if(ry > offset3 && ry < offset3 + 20){
						selected = 2;
					}else{
						d = false;
					}
					if(d) touchDragged(event, x, y, pointer);
				}
				return d;
			}

			public void touchDragged(InputEvent event, float x, float y, int pointer){
				float rx = x - getX() - xoffset;
				if(selected == 0){
					brightness = scl(rx);
				}else if(selected == 1){
					saturation = scl(rx);
				}else if(selected == 2){
					hue = scl(rx);
				}
				fire(new ChangeListener.ChangeEvent());
			}
		});
		saturationbar = new Sprite(skin.getAtlas().findRegion("white"));

		brightnessbar = new Sprite(skin.getAtlas().findRegion("white"));
		
		brightnessbar.getVertices()[SpriteBatch.C1] = Color.BLACK.toFloatBits();
		brightnessbar.getVertices()[SpriteBatch.C2] = Color.BLACK.toFloatBits();
		saturationbar.getVertices()[SpriteBatch.C1] = Color.GRAY.toFloatBits();
		saturationbar.getVertices()[SpriteBatch.C2] = Color.GRAY.toFloatBits();
		
	}

	private float scl(float f){
		float a = f / barwidth;
		if(a < 0) a = 0;
		if(a > 1) a = 1f;
		return a;
	}

	@Override
	public void draw(Batch batch, float parentAlpha){
		if(Gdx.input.isKeyJustPressed(Keys.U)) System.out.println(getColor().r + " " + getColor().g + " " + getColor().b);
		float boxs = 30;
		float border = 1f;
		
		style.border.draw(batch, getX()-border+xoffset, getY()-border + offset1,barwidth+ border*2,20+ border*2);
		style.border.draw(batch, getX()-border+xoffset, getY()-border + offset2,barwidth+ border*2,20+ border*2);
		style.border.draw(batch, getX()-border+xoffset, getY()-border + offset3,barwidth+ border*2,20+ border*2);
		style.border.draw(batch, getPrefWidth() / 2 - boxs / 2-border, getY() + offset3 + 30-border, boxs+border*2, boxs+border*2);
		
		setAlpha(saturationbar, parentAlpha);
		saturationbar.setBounds(xoffset + getX(), getY() + offset2, barwidth, 20);
		saturationbar.draw(batch);
		
		setAlpha(brightnessbar, parentAlpha);
		brightnessbar.setBounds(xoffset + getX(), getY() + offset1, barwidth, 20);
		brightnessbar.draw(batch);

		style.colorbox.draw(batch, xoffset + getX(), getY() + offset3, barwidth, 20);

		TextureRegion region = skin.getAtlas().findRegion("default-slider-knob");
		int width = region.getRegionWidth() + 4, height = region.getRegionHeight() + 8;
		batch.draw(region, xoffset + getX() + barwidth * hue - width / 2, getY() + offset3 - 2, width, height);
		batch.draw(region, xoffset + getX() + barwidth * saturation - width / 2, getY() + offset2 - 2, width, height);
		batch.draw(region, xoffset + getX() + barwidth * brightness - width / 2, getY() + offset1 - 2, width, height);

		
		Color color = getColor();
		color.a = batch.getColor().a;
		batch.setColor(color);
		batch.draw(skin.getAtlas().findRegion("white"), getPrefWidth() / 2 - boxs / 2, getY() + offset3 + 30, boxs, boxs);
		batch.setColor(Color.WHITE);
		
		updateHue();
		validate();
	}

	private void setAlpha(Sprite sprite, float a){
		for(int colorvertice : colors){
			int intBits = NumberUtils.floatToIntColor(sprite.getVertices()[colorvertice]);
			int alphaBits = (int)(255 * a) << 24;
			intBits = intBits & 0x00FFFFFF;
			intBits = intBits | alphaBits;
			float color = NumberUtils.intToFloatColor(intBits);
			sprite.getVertices()[colorvertice] = color;
		}
	}

	public Color getColor(){
		return Hue.fromHSB(hue, saturation, brightness);
	}
	
	public void setColor(Color color){
		java.awt.Color.RGBtoHSB((int)(color.r*255f),(int)(color.g*255f), (int)(color.b*255f), hsbvals);
		hue = hsbvals[0];
		saturation = hsbvals[1];
		brightness = hsbvals[2];
	}

	private void updateHue(){
		Color brightcolor = Hue.fromHSB(hue, saturation, 0.5f);
		Color satcolor = Hue.fromHSB(hue, 1f, brightness);

		brightnessbar.getVertices()[SpriteBatch.C3] = brightcolor.toFloatBits();
		brightnessbar.getVertices()[SpriteBatch.C4] = brightcolor.toFloatBits();
		saturationbar.getVertices()[SpriteBatch.C3] = satcolor.toFloatBits();
		saturationbar.getVertices()[SpriteBatch.C4] = satcolor.toFloatBits();
	}

	@Override
	public float getPrefWidth(){
		return 120;
	}

	@Override
	public float getPrefHeight(){
		return 120;
	}
	
	public static class Style{
		public Drawable colorbox;
		public Drawable border;
	}
}
