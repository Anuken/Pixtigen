package io.anuke.pixtigen.gui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class FileDialog extends Dialog{
	private float pheight = 300;
	private DefaultFileChooser file;
	
	public FileDialog(Stage stage, Skin skin, String title,  boolean save, FileListener listener){
		this(stage, title, skin, save, "dialog", listener);
	}
	
	public FileDialog(Stage stage, String title, Skin skin, boolean save, String windowStyleName, FileListener listener){
		super(title, skin, windowStyleName);
		addFileChooser(listener, save);
		show(stage);
		stage.setScrollFocus(file.getContentsPane());
	}
	
	private void addFileChooser(FileListener listener, boolean save){
		file = new DefaultFileChooser(this, getSkin(), save, listener);
		file.setNewFilesChoosable(save);
		this.getContentTable().add(file);
	}
	
	public float getPrefHeight(){
		return pheight;
	}
	
	public static interface FileListener{
		public void choose(FileHandle file);
	}
}
