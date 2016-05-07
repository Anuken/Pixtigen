package net.pixelstatic.pixtigen.gui;

import net.pixelstatic.pixtigen.generator.VertexObject;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;

public class VertexInput implements InputProcessor{
	static final int alt_key = Keys.CONTROL_LEFT;
	static final int draw_key = Keys.SHIFT_LEFT;
	private VertexEditor gui;

	public VertexInput(VertexEditor gui){
		this.gui = gui;
	}

	@Override
	public boolean keyDown(int keycode){
		if(keycode == draw_key && gui.drawMode && !(dialogOpen())){
			gui.drawing = true;
			gui.selectedCanvas.clear();
			return true;
		}else if(keycode == Keys.R){
			int minvertices = Integer.MAX_VALUE;
			for(VertexCanvas canvas : gui.canvases)
				minvertices = Math.min(canvas.list.vertices.size, minvertices);

			if(minvertices >= 3){
				gui.tree.setVertexObject(new VertexObject(gui.canvases));
				gui.tree.generate();
			}else{
				gui.gui.showInfo("Each polygon must have at least 3 vertices\nfor the tree to generate!");
			}
		}else if(keycode == Keys.T){
			gui.loadObject(EditorState.readObject(Gdx.files.internal("vertexobjects/pinetreepart.vto")));
		}

		return false;
	}

	@Override
	public boolean keyUp(int keycode){
		if(keycode == draw_key) if(gui.drawing){
			gui.finishDrawMode();
			gui.drawing = false;
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character){
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		if(dialogOpen()) return false;
		if(button == Buttons.LEFT){
			if((Gdx.input.getX() < Gdx.graphics.getWidth() - 130 || Gdx.input.getY() > 30)){
				gui.gui.stage.setKeyboardFocus(null);
			}
			if(gui.drawing && gui.drawMode){
				gui.selectedCanvas.vertices().add(gui.mouseVector());
			}

			Vector2 selected = gui.selectedVertice();
			if(selected != null && !gui.drawing){
				gui.vertice = selected;
			}
		}else if(button == Buttons.RIGHT){
			if(gui.selectedCanvas.vertices().size > 3){
				gui.selectedCanvas.vertices().removeValue(gui.selectedVertice(), true);
				gui.vertice = null;
			}
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		if(button == Buttons.LEFT){
			gui.vertice = null;
		}
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer){
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY){
		return false;
	}
	
	public boolean dialogOpen(){
		return gui.gui.stage.getKeyboardFocus() != null;
	}

	@Override
	public boolean scrolled(int amount){
		if(Gdx.input.isKeyPressed(alt_key))
		//	for(VertexCanvas canvas : gui.canvases)
		gui.selectedCanvas.list.scale(amount > 0 ? 0.9f : 1.1f);
		//}
		return false;
	}

}
