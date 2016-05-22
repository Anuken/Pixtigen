package net.pixelstatic.pixtigen.gui;

import net.pixelstatic.pixtigen.generator.VertexObject;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

public class VertexInput implements InputProcessor{
	static final int alt_key = Keys.CONTROL_LEFT;
	static final int draw_key = Keys.SHIFT_LEFT;
	private VertexEditor editor;

	public VertexInput(VertexEditor gui){
		this.editor = gui;
	}

	@Override
	public boolean keyDown(int keycode){
		if(keycode == draw_key && editor.drawMode && !(dialogOpen())){
			editor.drawing = true;
			editor.selectedCanvas.clear();
			return true;
		}else if(keycode == Keys.R){
			int minvertices = Integer.MAX_VALUE;
			for(VertexCanvas canvas : editor.canvases)
				minvertices = Math.min(canvas.list.vertices.size, minvertices);

			if(minvertices >= 3){
				editor.tree.setVertexObject(new VertexObject(editor.canvases));
				editor.tree.generate();
			}else{
				editor.gui.showInfo("Each polygon must have at least 3 vertices\nfor the tree to generate!");
			}
		}else if(keycode == Keys.T){
			editor.loadObject(EditorState.readObject(Gdx.files.internal("vertexobjects/pinetreepart.vto")));
		}

		return false;
	}

	@Override
	public boolean keyUp(int keycode){
		if(keycode == draw_key) if(editor.drawing){
			editor.finishDrawMode();
			editor.drawing = false;
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
				editor.gui.stage.setKeyboardFocus(null);
			}
			if(editor.drawing && editor.drawMode){
				editor.selectedCanvas.vertices().add(editor.mouseVector());
			}

			Vector2 selected = editor.selectedVertice();
			if(selected != null && !editor.drawing){
				editor.vertice = selected;
			}
			if(selected == null){
				Vector2 mouse = new Vector2(screenX - Gdx.graphics.getWidth()/2 - editor.offsetx, (Gdx.graphics.getHeight() - screenY) - Gdx.graphics.getHeight()/2 - editor.offsety);
				for(VertexCanvas canvas : editor.canvases){
					if(Intersector.isPointInPolygon(canvas.list.vertices, mouse)){
						editor.mouseCanvas = canvas;
						editor.selectedCanvas = canvas;
						editor.gui.updateCanvasInfo();
						break;
					}
				}
				//Intersector.isPointInPolygon(, point)
			}
		}else if(button == Buttons.RIGHT){
			if(editor.selectedCanvas.vertices().size > 3){
				editor.selectedCanvas.vertices().removeValue(editor.selectedVertice(), true);
				editor.vertice = null;
			}
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		if(button == Buttons.LEFT){
			editor.vertice = null;
			editor.mouseCanvas = null;
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
		return editor.gui.stage.getKeyboardFocus() != null;
	}

	@Override
	public boolean scrolled(int amount){
		if(Gdx.input.isKeyPressed(alt_key))
		//	for(VertexCanvas canvas : gui.canvases)
		editor.selectedCanvas.list.scale(amount > 0 ? 0.9f : 1.1f);
		//}
		return false;
	}

}
