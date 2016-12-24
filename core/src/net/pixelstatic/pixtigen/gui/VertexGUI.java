package net.pixelstatic.pixtigen.gui;

import io.anuke.utils.modules.Module;
import io.anuke.utils.scene2D.ColorPicker;
import io.anuke.utils.scene2D.FileDialog;
import net.pixelstatic.pixtigen.Pixtigen;
import net.pixelstatic.pixtigen.generator.Filter;
import net.pixelstatic.pixtigen.generator.FilterType;
import net.pixelstatic.pixtigen.generator.Material;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class VertexGUI extends Module<Pixtigen>{
	VertexEditor editor;
	Skin skin;
	Stage stage;
	Table table;
	Dialog infodialog, addfilterdialog;
	TextButton symmetry, overwrite, add, clear, delete, smooth, resetcolor, addfilter;
	ScrollPane pane;
	List<VertexCanvas> canvaslist;
	SelectBox<Material> box;
	Texture colorbox;
	TextField field;
	FilterDialog editdialog;
	ColorPicker picker;
	Label colorlabel, materiallabel, filterlabel;
	SelectBox<Material> materialbox;
	PolygonSpriteBatch polybatch;
	float uiwidth = 130, uiheight = 30;
	

	public VertexGUI(){
		polybatch = new PolygonSpriteBatch();
		skin = new Skin(Gdx.files.internal("gui/uiskin.json"));
		stage = new Stage();
		stage.setViewport(new ScreenViewport());
		ActorAlign.stage = stage;
	}

	@Override
	public void update(){
		if(Gdx.input.isKeyPressed(Keys.ESCAPE)) Gdx.app.exit();
		updateButtons();
		Gdx.gl.glClearColor(20 / 255f, 33 / 255f, 52 / 255f, 1);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	public void updateButtons(){
		boolean drawMode = editor.drawMode;
		add.setPosition(0, Gdx.graphics.getHeight() - pane.getHeight() - 30);
		pane.setPosition(0, Gdx.graphics.getHeight() - pane.getHeight());
		symmetry.setChecked(editor.symmetry);
		overwrite.setChecked(drawMode);
		overwrite.setText(drawMode ? "Draw Mode" : "Edit Mode");
		clear.setDisabled( !drawMode);
		symmetry.setDisabled( !drawMode);
		delete.setDisabled( !drawMode);
		smooth.setDisabled( !drawMode);
	}

	public void init(){
		setupGUI();
		InputMultiplexer plex = new InputMultiplexer();
		plex.addProcessor(new VertexInput(getModule(VertexEditor.class)));
		plex.addProcessor(stage);
		Gdx.input.setInputProcessor(plex);
	}

	public void setupGUI(){
		table = new Table();
		table.setFillParent(true);
		stage.addActor(table);
		float width = uiwidth, height = uiheight;
		canvaslist = new List<VertexCanvas>(skin);
		canvaslist.getStyle().background = skin.newDrawable("dark");
		canvaslist.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor){
				editor.selectedCanvas = canvaslist.getSelected();
				if(box != null)updateCanvasInfo();
			}
		});
		updateCanvasList();
		
		pane = new ScrollPane(canvaslist);
		pane.setOverscroll(false, false);
		pane.setFadeScrollBars(false);
		table.addActor(pane);
		
		SelectBox<FilterType> filterbox = new SelectBox<FilterType>(skin);

		addfilterdialog = new Dialog("Add Filter", skin){
			public void result(Object object){
				if((Boolean)object){
					editor.tree.getFilters(materialbox.getSelected()).add(new Filter(filterbox.getSelected()));
					updateFilterList(editdialog);
				}
			}
		};
		addfilterdialog.text("Type: ").row();
		filterbox.setItems(FilterType.values());
		addfilterdialog.getContentTable().add(filterbox);

		addfilterdialog.button("    Add    ", true).button("  Cancel  ", false);

		field = new TextField("canvas", skin);
		field.setSize(width, height);
		field.setTouchable(Touchable.disabled);
		field.setTextFieldListener(new TextFieldListener(){
			@Override
			public void keyTyped(TextField textField, char c){
				editor.selectedCanvas.name = field.getText();
			//	editor.selectedCanvas.button.setText(editor.selectedCanvas.name);
			}
		});
		table.top().right();
		table.add(field).size(width, height);

		box = new SelectBox<Material>(skin);
		box.setItems(Material.values());
		box.setSelectedIndex(0);
		box.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor){
				editor.selectedCanvas.list.material = box.getSelected();
			}
		});

		table.row().top().right();
		table.add(box).size(width, height);

		symmetry = new TextButton("Symmetry", skin, "toggle");
		symmetry.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				if(editor.drawMode){
					editor.symmetry = !editor.symmetry;
					if(editor.symmetry) editor.selectedCanvas.clear();
				}
			}
		});

		table.row().top().right();
		table.add(symmetry).size(width, height);

		overwrite = new TextButton("Draw Mode", skin, "toggle");
		overwrite.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				editor.drawMode = !editor.drawMode;
			}
		});

		table.row().top().right();
		table.add(overwrite).size(width, height);

		smooth = new TextButton("Smooth", skin);
		smooth.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				if( !editor.selectedCanvas.list.smooth()) showInfo("Calm down m8");
			}
		});

		table.row().top().right();
		table.add(smooth).size(width, height);

		clear = new TextButton("Clear", skin);
		clear.setSize(field.getWidth(), 30);
		clear.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				if(editor.drawMode) editor.selectedCanvas.clear();
			}
		});

		table.row().top().right();
		table.add(clear).size(width, height);

		delete = new TextButton("Delete Canvas", skin);
		delete.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				/*
				if(editor.drawMode && editor.canvases.size > 1){
					editor.selectedCanvas.delete();
					editor.canvases.removeValue(editor.selectedCanvas, true);
					editor.selectedCanvas = editor.canvases.get(0);
					editor.fixCanvases();
				}
				*/
			}
		});

		table.row().top().right();
		table.add(delete).size(width, height);

		Button exportbutton = new TextButton("Export", skin);
		exportbutton.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				new FileDialog(stage, skin, "Export Image", true, (FileHandle file) -> {
					try{
						editor.exportImage(file.file().getAbsolutePath());
					}catch(Exception e){
						showInfo(e.getMessage());
						e.printStackTrace();
					}
				});
			}
		});

		table.row().top().right();
		table.add(exportbutton).size(width, height);

		Button savebutton = new TextButton("Save", skin);
		savebutton.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				new FileDialog(stage, skin, "Save", true, (FileHandle file) -> {
					try{
						editor.saveState(Gdx.files.absolute(file.file().getAbsolutePath()));
					}catch(Exception e){
						showInfo(e.getMessage());
						e.printStackTrace();
					}
				});
			}
		});

		table.row().top().right();
		table.add(savebutton).size(width, height);

		Button loadbutton = new TextButton("Load", skin);
		loadbutton.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				new FileDialog(stage, skin, "Load", false, (FileHandle file) -> {
					try{
						editor.loadState(Gdx.files.absolute(file.file().getAbsolutePath()));
						((ColorPicker)editdialog.getContentTable().findActor("colorpicker")).setColor(materialbox.getSelected().color);
					}catch(Exception e){
						showInfo(e.getMessage());
						e.printStackTrace();
					}
				});
			}
		});

		table.row().top().right();
		table.add(loadbutton).size(width, height);

		add = new TextButton("New Canvas", skin);
		add.align(Align.topLeft);
		add.setSize(pane.getWidth(), 30);
		add.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				//	addCanvas("canvas" + (editor.canvases.size + 1));
			}
		});
		stage.addActor(add);

		editdialog = new FilterDialog("Filters/Color", skin);

		TextButton closebutton = new TextButton("x", skin);

		closebutton.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				editdialog.hide();
			}
		});

		editdialog.getTitleTable().add(closebutton).height(17);
		//	editdialog.setResizable(true);

		materiallabel = new Label("Material:", skin);

		materialbox = new SelectBox<Material>(skin);
		materialbox.setItems(Material.values());
		materialbox.addListener(new ChangeListener(){
			public void changed(ChangeEvent event, Actor actor){
				((ColorPicker)editdialog.getContentTable().findActor("colorpicker")).setColor(materialbox.getSelected().color);
				updateFilterList(editdialog);
				//	editdialog.getContentTable().clear();

				//FilterType[] filters = FilterType.values();
				//	for(FilterType filter : filters){
				//	((CheckBox)editdialog.getContentTable().findActor(filter.name() + ("check"))).setChecked(editor.tree.isFilterEnabled(materialbox.getSelected(), filter));
				//}
				//editor.tree.setFilter(materialbox.getSelected(), filter, checkbox.isChecked());
			}
		});

		colorlabel = new Label("Color:", skin);

		picker = new ColorPicker(skin);
		picker.setName("colorpicker");
		picker.setColor(materialbox.getSelected().color);
		picker.addListener(new ChangeListener(){
			public void changed(ChangeEvent event, Actor actor){
				materialbox.getSelected().color = picker.getColor();
			}
		});

		resetcolor = new TextButton("Reset Color", skin);
		resetcolor.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				materialbox.getSelected().resetColor();
				picker.setColor(materialbox.getSelected().color);
			}
		});

		filterlabel = new Label("Filters:", skin);

		addfilter = new TextButton("Add Filter", skin);
		addfilter.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				addfilterdialog.show(stage);
			}
		});

		updateFilterList(editdialog);

		TextButton advancedbutton = new TextButton("Filters/Color", skin);
		advancedbutton.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				editdialog.show(stage);
			}
		});
		add(advancedbutton);

		text("Segments: 10").setName("segmenttext");;

		Slider segmentslider = new Slider(1, 40, 1, false, skin);
		segmentslider.setValue(10);
		segmentslider.addListener(new ChangeListener(){
			public void changed(ChangeEvent event, Actor actor){
				int value = (int)segmentslider.getValue();
				((Label)table.findActor("segmenttext")).setText("Segments: " + value);
				editor.tree.getVertexGenerator().segments = value;
			}
		});

		add(segmentslider);
		text("Compactness: 0.8").setName("compacttext");;

		Slider compactslider = new Slider(0.1f, 2f, 0.05f, false, skin);
		compactslider.setValue(0.8f);
		compactslider.addListener(new ChangeListener(){
			public void changed(ChangeEvent event, Actor actor){
				float value = (compactslider.getValue());
				((Label)table.findActor("compacttext")).setText("Compactness: " + (value + "").substring(0, 3));
				editor.tree.getVertexGenerator().segmentCompactness = value;
			}
		});

		add(compactslider);

		text("Rotation: 0").setName("rotationtext");;

		Slider rotationslider = new Slider( -10, 10, 0.1f, false, skin);
		rotationslider.setValue(0f);
		rotationslider.addListener(new ChangeListener(){
			public void changed(ChangeEvent event, Actor actor){
				float value = (rotationslider.getValue());
				((Label)table.findActor("rotationtext")).setText("rotation: " + (value + "").substring(0, 3));
				editor.tree.getVertexGenerator().segmentRotation = value;
			}
		});

		add(rotationslider);

		text("Scale: 1/1000").setName("scaletext");;

		Slider scaleslider = new Slider(1, 3000, 10f, false, skin);
		scaleslider.setValue(1000);
		scaleslider.addListener(new ChangeListener(){
			public void changed(ChangeEvent event, Actor actor){
				float value = (scaleslider.getValue());
				((Label)table.findActor("scaletext")).setText("Scale: 1/" + (int)value);
				editor.tree.setCanvasScale(1f / value);
			}
		});

		add(scaleslider);

		CheckBox scalebox = new CheckBox("Autoscale", skin);
		scalebox.setChecked(true);
		scalebox.addListener(new ChangeListener(){
			public void changed(ChangeEvent event, Actor actor){
				if(editor != null) editor.tree.setAutoScale(scalebox.isChecked());
				scaleslider.setTouchable(scalebox.isChecked() ? Touchable.disabled : Touchable.enabled);
				scaleslider.setColor(scalebox.isChecked() ? Color.GRAY : Color.WHITE);
			}
		});
		scalebox.fire(new ChangeListener.ChangeEvent());

		add(scalebox);

		infodialog = new Dialog("Info", skin, "dialog").text("").button("Ok", true).key(Keys.ENTER, true).key(Keys.ESCAPE, false);

		updateCanvasInfo();
	}

	void updateFilterList(FilterDialog dialog){
		dialog.getContentTable().clear();
		editdialog.getContentTable().top().left().add(materiallabel).align(Align.topLeft).row();
		editdialog.getContentTable().top().left().add(materialbox).height(30).align(Align.topLeft).row();
		editdialog.getContentTable().top().left().add(colorlabel).align(Align.topLeft).row();;
		editdialog.getContentTable().add(picker).row();
		editdialog.getContentTable().add(resetcolor).row();
		editdialog.getContentTable().top().left().add(filterlabel).align(Align.topLeft).row();;
		dialog.setWindowHeight(300 + 30 + 30 * editor.tree.getFilters(materialbox.getSelected()).size);

		for(Filter filter : editor.tree.getFilters(materialbox.getSelected())){
			CheckBox checkbox = new CheckBox(filter.type.getName(), skin);
			checkbox.setChecked(filter.enabled);
			checkbox.setName(filter.type.name() + "filtercheck");
			checkbox.addListener(new ChangeListener(){
				public void changed(ChangeEvent event, Actor actor){
					filter.enabled = checkbox.isChecked();
					//editor.tree.setFilter(materialbox.getSelected(), filter, checkbox.isChecked());
				}
			});
			if(filter.type.alwaysEnabled()){
				checkbox.setDisabled(true);
				checkbox.setChecked(true);
			}
			editdialog.getContentTable().top().left().add(checkbox).align(Align.topLeft);

			TextButton editbutton = new TextButton("Edit", skin);
			editbutton.setName(filter + "filtereditbutton");
			editbutton.addListener(new ClickListener(){
				public void clicked(InputEvent event, float x, float y){
					if(filter.values.size() == 0) return;
					Dialog dialog = new Dialog("Edit Filter", skin){
						public float getPrefWidth(){
							return 250f;
						}

						public float getPrefHeight(){
							return filter.values.size() * 80;
						}
					};
					TextButton editclosebutton = new TextButton("x", skin);
					editclosebutton.addListener(new ClickListener(){
						public void clicked(InputEvent event, float x, float y){
							dialog.hide();
						}
					});

					dialog.getTitleTable().add(editclosebutton).height(17);
					dialog.key(Keys.ENTER, true).key(Keys.ESCAPE, false);
					com.badlogic.gdx.utils.ObjectMap.Keys<String> keys = filter.values.valueNames();

					for(String string : keys){
						net.pixelstatic.pixtigen.util.Value<?> value = filter.values.get(string);
						Label valuelabel = new Label(string, skin);
						dialog.getContentTable().top().left().add(valuelabel).align(Align.topLeft).row();;
						Actor actor = value.getActor(skin);
						actor.addListener(new ChangeListener(){
							public void changed(ChangeEvent event, Actor actor){
								value.onChange(actor);
								valuelabel.setText(string + ": " + value);
							}
						});
						actor.fire(new ChangeListener.ChangeEvent());
						dialog.getContentTable().top().left().add(actor).align(Align.topLeft);
						TextButton resetbutton = new TextButton("Reset", skin);
						resetbutton.addListener(new ClickListener(){
							public void clicked(InputEvent event, float x, float y){
								value.reset(actor);
								actor.fire(new ChangeListener.ChangeEvent());
							}
						});
						dialog.getContentTable().add(resetbutton).row();
					}

					dialog.show(stage);
				}
			});
			if(filter.values.size() == 0){
				editbutton.setDisabled(true);
			}
			editdialog.getContentTable().add(editbutton).width(35);
			
			TextButton removebutton = new TextButton("Remove", skin);
			removebutton.setName(filter + "filterremovebutton");
			removebutton.addListener(new ClickListener(){
				public void clicked(InputEvent event, float x, float y){
					editor.tree.getFilters(materialbox.getSelected()).removeValue(filter, true);
					updateFilterList(dialog);
				}
			});
			
			editdialog.getContentTable().add(removebutton).width(65);
			
		
			editdialog.getContentTable().row();
		}
		editdialog.getContentTable().add(addfilter).align(Align.topLeft).row();
		
	}

	void add(Actor actor){
		table.row().top().right();
		table.add(actor).size(uiwidth, uiheight);
	}

	Label text(String text){
		Label label = new Label(text, skin);
		table.row().top().right();
		table.add(label).align(Align.center);
		return label;
	}
	
	public void updateCanvasInfo(){
		field.setText(editor.selectedCanvas.name);
		box.setSelected(editor.selectedCanvas.list.material);
		canvaslist.setSelected(editor.selectedCanvas);
	}

	public void showInfo(String info){
		((Label)infodialog.getContentTable().getChildren().get(0)).setText(info);
		infodialog.show(stage);
	}
	
	void updateCanvasList(){
		canvaslist.setItems(editor.canvases);
	}

	public void resize(int width, int height){
		stage.getViewport().update(width, height, true);
	}
}
