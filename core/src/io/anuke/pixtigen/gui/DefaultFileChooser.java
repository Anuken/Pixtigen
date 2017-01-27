package io.anuke.pixtigen.gui;

import java.io.File;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pools;

import io.anuke.pixtigen.gui.FileDialog.FileListener;

public class DefaultFileChooser extends FileChooser{

	/** the style */
	private Style style;

	/** the type of file handle created */
	private FileType fileType = FileType.Absolute;

	/** the directories that have been visited previously, for the {@link #backButton} */
	private final Array<FileHandle> fileHistory = new Array<>();

	/** the current directory */
	private FileHandle directory;

	/** @see #pathFieldListener */
	private TextField pathField;

	/** shows the {@link FileHandle#list() children} of current {@link #directory} */
	private List<String> contents;

	/** makes the {@link #contents} scrollable */
	private ScrollPane contentsPane;

	/** @see #backButtonListener */
	private Button backButton;

	/** @see #parentButtonListener */
	private Button parentButton;

	/** @see #chooseButtonListener */
	private Button chooseButton;
	
	private TextField chooseField;

	/** @see #cancelButtonListener */
	private Button cancelButton;
	
	private boolean saving;

	/** if it exists, this open the file at the given {@link FileType#Absolute absolute} path if it is not a folder, {@link #setDirectory(FileHandle) goes into} it otherwise, */
	public final TextFieldListener pathFieldListener = new TextFieldListener() {
		@Override
		public void keyTyped(TextField textField, char key) {
			if(key == '\r' || key == '\n') {
				FileHandle loc = Gdx.files.getFileHandle(textField.getText(), fileType);
				if(isNewFilesChoosable() || loc.exists()) {
					if(loc.isDirectory())
						setDirectory(loc);
					else
						getListener().choose(loc);
					if (getStage() != null)
						getStage().setKeyboardFocus(DefaultFileChooser.this);
				}
			}
		}
	};

	/** {@link Listener#choose(FileHandle) chooses} the {@link List#getSelection() selected} file in from the {@link #contents} */
	public final ClickListener chooseButtonListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			if(saving && chooseField.getText().equals("")){
				return;
			}else if(saving){
				getListener().choose(Gdx.files.getFileHandle(directory.path() + "/" + chooseField.getText(), fileType));
				return;
			}
			Selection<String> selection = contents.getSelection();
			if(!selection.getMultiple()) {
				FileHandle selected = currentlySelected();
				if(!isDirectoriesChoosable() && selected.isDirectory())
					setDirectory(selected);
				else
					getListener().choose(selected);
			} else {
				@SuppressWarnings("unchecked")
				Array<FileHandle> files = Pools.obtain(Array.class);
				files.clear();
				for(String fileName : selection)
					files.add(directory.child(fileName));
				getListener().choose(files);
				Pools.free(files);
			}
		}
	};

	/** goes into the currently marked folder */
	public final ClickListener openButtonListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			FileHandle child = currentlySelected();
			if(child.isDirectory())
				setDirectory(child);
		}
	};

	/** @see Listener#cancel() */
	public final ClickListener cancelButtonListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			getListener().cancel();
		}
	};

	/** goes back to the {@link #fileHistory previous} {@link #directory} */
	public final ClickListener backButtonListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			if(fileHistory.size > 1) {
				fileHistory.removeIndex(fileHistory.size - 1);
				setDirectory(directory = fileHistory.peek(), false);
			}
		}
	};

	/** {@link #setDirectory(FileHandle) sets} {@link #directory} to its {@link FileHandle#parent() parent} */
	public final ClickListener parentButtonListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			setDirectory(directory.parent());
		}
	};

	/** {@link Button#setDisabled(boolean) enables/disables} {@link #chooseButton} and {@link #openButton} */
	public final ChangeListener contentsListener = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			chooseButton.setDisabled(isDirectoriesChoosable());
		}
	};

	/** key controls of {@link #contents} */
	public final InputListener keyControlsListener = new InputListener() {
		@Override
		public boolean keyTyped(InputEvent event, char c) {
			if(event.isHandled())
				return true;

			if((getStage() == null || getStage().getKeyboardFocus() != pathField) && (c == '\r' || c == '\n')) {
				if(currentlySelected().isDirectory())
					openButtonListener.clicked(null, 0, 0); // fake event
				else
					chooseButtonListener.clicked(null, 0, 0); // fake event
				return true;
			}

			int keyCode = event.getKeyCode();

			if(keyCode == Keys.DEL) {
				backButtonListener.clicked(null, 0, 0); // fake event
				return true;
			} else if(keyCode == Keys.LEFT) {
				parentButtonListener.clicked(null, 0, 0); // fake event
				return true;
			}

			int direction;
			if(keyCode == Keys.UP)
				direction = -1;
			else if(keyCode == Keys.DOWN)
				direction = 1;
			else
				return false;

			int newIndex = contents.getSelectedIndex() + direction;
			newIndex = MathUtils.clamp(newIndex, 0, contents.getItems().size - 1);
			contents.setSelectedIndex(newIndex);
			return true;
		}
	};
	
	/** The ui skin file needs an entry for this class; see https://bitbucket.org/dermetfan/libgdx-utils/issue/3 */
	public DefaultFileChooser(Dialog window, Skin skin, boolean saving, FileListener listener) {
		this(skin.get(Style.class), saving, new Listener(){
			@Override
			public void choose(FileHandle file){
				listener.choose(file);
				window.hide();
			}

			@Override
			public void choose(Array<FileHandle> files){

			}

			@Override
			public void cancel(){
				window.hide();
			}
		});
		setSkin(skin);
	}

	public DefaultFileChooser(Style style, boolean saving, Listener listener) {
		super(listener);
		this.saving = saving;
		this.style = style;
		buildWidgets();
		setDirectory(Gdx.files.absolute(Gdx.files.getExternalStoragePath()));
		build();
		refresh();
		contentsPane.setOverscroll(false, false);
		List<String> list = getContents();
		list.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				if(this.getTapCount() == 2) {
					setDirectory(currentlySelected());
				}
			}
		});
	}
	
	public void setFileType(FileType fileType) {
		this.fileType = fileType;
		fileHistory.clear();
		setDirectory(Gdx.files.getFileHandle("", fileType));
	}

	/** Override this if you want to adjust all the Widgets. Be careful though! */
	protected void buildWidgets() {
		addListener(keyControlsListener);

		(pathField = new TextField("", style.pathFieldStyle)).setTextFieldListener(pathFieldListener);
		contents = new List<String>(style.contentsStyle){
			public float getPrefHeight(){
				return Math.max(super.getPrefHeight(), 200f);
			}
		};
		contents.addListener(contentsListener);
		
		(chooseField = new TextField("", style.pathFieldStyle)).setTextFieldFilter(new TextFieldFilter(){
			@Override
			public boolean acceptChar(TextField textField, char c){
				return c != ' ';
			}
		});;
		
		(chooseButton = new TextButton( saving ? "save" : "open", style.buttonStyle)).addListener(chooseButtonListener);
		(cancelButton = new TextButton("cancel", style.buttonStyle)).addListener(cancelButtonListener);
		(backButton = new TextButton("back", style.buttonStyle)).addListener(backButtonListener);
		(parentButton = new TextButton("up", style.buttonStyle)).addListener(parentButtonListener);

		contentsPane = style.contentsPaneStyle == null ? new ScrollPane(contents) : new ScrollPane(contents, style.contentsPaneStyle);
		contentsPane.setFadeScrollBars(false);
		setBackground(style.background);
	}

	/** Override this if you want to adjust the {@link Table layout}. Clears this {@link DefaultFileChooser}'s children and adds {@link #backButton}, {@link #pathField}, {@link #parentButton}, {@link #contentsPane}, {@link #chooseButton}, {@link #cancelButton} and {@link #openButton} if {@link #isDirectoriesChoosable()} is true. */
	@Override
	protected void build() {
		clearChildren();
		Style style = getStyle();
		add(pathField).fill().space(style.space);
		add(backButton).fill().space(style.space);
		add(parentButton).fill().space(style.space).row();
		add(contentsPane).colspan(3).expand().fill().space(style.space).row();
		if(saving) add(chooseField).fill().space(style.space);
		add(chooseButton).fill().colspan(saving ? 1 : 2).space(style.space);
		add(cancelButton).fill().space(style.space);
	}

	/** refreshes the {@link #contents} */
	public void refresh() {
		scan(directory);
	}

	/** populates {@link #contents} with the children of {@link #directory} */
	protected void scan(FileHandle dir) {
		try {
			FileHandle[] files = dir.list(handlingFileFilter);
			String[] names = new String[files.length];
			for(int i = 0; i < files.length; i++) {
				String name = files[i].name();
				if(files[i].isDirectory())
					name += File.separator;
				names[i] = name;
			}
			contents.setItems(names);
		} catch(GdxRuntimeException ignore) {
			Gdx.app.error("DefaultFileChooser", " cannot read " + dir);
		}
	}

	/** @return the file currently selected in {@link #contents} */
	public FileHandle currentlySelected() {
		String selected = contents.getSelected();
		return selected == null ? directory : directory.child(selected);
	}

	/** set {@link #directory} and adds it to {@link #fileHistory}
	 *  @see #setDirectory(FileHandle, boolean) */
	public void setDirectory(FileHandle dir) {
		setDirectory(dir, true);
	}

	/** sets {@link #directory} and updates all things that need to be updated */
	public void setDirectory(FileHandle dir, boolean addToHistory) {
		FileHandle loc = dir.isDirectory() ? dir : dir.parent();
		if(addToHistory)
			fileHistory.add(loc);
		scan(directory = loc);
		pathField.setText(loc.path());
		pathField.setCursorPosition(pathField.getText().length());
	}

	/** @return the {@link #backButton} */
	public Button getBackButton() {
		return backButton;
	}

	/** @param backButton the {@link #backButton} to set */
	public void setBackButton(Button backButton) {
		this.backButton.removeListener(backButtonListener);
		backButton.addListener(backButtonListener);
		getCell(this.backButton).setActor(this.backButton = backButton);
	}

	/** @return the {@link #cancelButton} */
	public Button getCancelButton() {
		return cancelButton;
	}

	/** @param cancelButton the {@link #cancelButton} to set */
	public void setCancelButton(Button cancelButton) {
		this.cancelButton.removeListener(cancelButtonListener);
		cancelButton.addListener(cancelButtonListener);
		getCell(this.cancelButton).setActor(this.cancelButton = cancelButton);
	}

	/** @return the {@link #chooseButton} */
	public Button getChooseButton() {
		return chooseButton;
	}

	/** @param chooseButton the {@link #chooseButton} to set */
	public void setChooseButton(Button chooseButton) {
		this.chooseButton.removeListener(chooseButtonListener);
		chooseButton.addListener(chooseButtonListener);
		getCell(this.chooseButton).setActor(this.chooseButton = chooseButton);
	}

	/** @return the {@link #contents} */
	public List<String> getContents() {
		return contents;
	}

	/** @param contents the {@link #contents} to set */
	public void setContents(List<String> contents) {
		this.contents.removeListener(contentsListener);
		contents.addListener(contentsListener);
		contentsPane.setWidget(contents);
	}

	/** @return the {@link #contentsPane} */
	public ScrollPane getContentsPane() {
		return contentsPane;
	}

	/** @param contentsPane the {@link #contentsPane} to set */
	public void setContentsPane(ScrollPane contentsPane) {
		contentsPane.setWidget(contents);
		getCell(this.contentsPane).setActor(this.contentsPane = contentsPane);
	}

	/** @return the {@link #directory} */
	public FileHandle getDirectory() {
		return directory;
	}

	/** @return the {@link #fileHistory} */
	public Array<FileHandle> getFileHistory() {
		return fileHistory;
	}

	/** @param fileHistory the {@link #fileHistory} to set */
	public void setFileHistory(Array<FileHandle> fileHistory) {
		this.fileHistory.clear();
		this.fileHistory.addAll(fileHistory);
	}

	/** @return the {@link #parentButton} */
	public Button getParentButton() {
		return parentButton;
	}

	/** @param parentButton the {@link #parentButton} to set */
	public void setParentButton(Button parentButton) {
		this.parentButton.removeListener(parentButtonListener);
		parentButton.addListener(parentButtonListener);
		getCell(this.parentButton).setActor(this.parentButton = parentButton);
	}

	/** @return the {@link #pathField} */
	public TextField getPathField() {
		return pathField;
	}

	/** @param pathField the {@link #pathField} to set */
	public void setPathField(TextField pathField) {
		this.pathField.setTextFieldListener(null);
		pathField.setTextFieldListener(pathFieldListener);
		getCell(this.pathField).setActor(this.pathField = pathField);
	}

	/** {@link #build() builds} if necessary */
	@Override
	public void setDirectoriesChoosable(boolean directoriesChoosable) {
		if(isDirectoriesChoosable() != directoriesChoosable) {
			super.setDirectoriesChoosable(directoriesChoosable);
			build();
		}
	}

	/** @return the {@link #style} */
	public Style getStyle() {
		return style;
	}

	/** @param style the {@link #style} to set and use for all widgets */
	public void setStyle(Style style) {
		this.style = style;
		setBackground(style.background);
		backButton.setStyle(style.buttonStyle);
		cancelButton.setStyle(style.buttonStyle);
		chooseButton.setStyle(style.buttonStyle);
		contents.setStyle(style.contentsStyle);
		contentsPane.setStyle(style.contentsPaneStyle);
		parentButton.setStyle(style.buttonStyle);
		pathField.setStyle(style.pathFieldStyle);
	}

	/** defines styles for the widgets of a {@link DefaultFileChooser}
	 *  @author dermetfan */
	public static class Style {

		/** the style of {@link #pathField} */
		public TextFieldStyle pathFieldStyle;

		/** the style of {@link #contents} */
		public ListStyle contentsStyle;

		/** the styles of the buttons */
		public TextButtonStyle buttonStyle;
		//public ButtonStyle chooseButtonStyle, openButtonStyle, cancelButtonStyle, backButtonStyle, parentButtonStyle;

		/** the spacing between the Widgets */
		public float space;

		/** optional */
		public ScrollPaneStyle contentsPaneStyle;

		/** optional */
		public Drawable background;
		

		public Style() {}

		public Style(Style style) {
			set(style);
		}

		public Style(TextFieldStyle pathFieldStyle, ListStyle contentsStyle, TextButtonStyle buttonStyle, Drawable background) {
			this.pathFieldStyle = pathFieldStyle;
			this.contentsStyle = contentsStyle;
			this.buttonStyle = buttonStyle;
			this.background = background;
		}

		/** @param style the {@link Style} to set this instance to (giving all fields the same value) */
		public void set(Style style) {
			pathFieldStyle = style.pathFieldStyle;
			contentsStyle = style.contentsStyle;
			buttonStyle = style.buttonStyle;
			contentsPaneStyle = style.contentsPaneStyle;
			background = style.background;
			space = style.space;
		}
	}
}
