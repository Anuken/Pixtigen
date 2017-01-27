package io.anuke.pixtigen;

import io.anuke.pixtigen.gui.VertexEditor;
import io.anuke.pixtigen.gui.VertexGUI;
import io.anuke.ucore.modules.ModuleController;

public class Pixtigen extends ModuleController<Pixtigen>{
	
	@Override
	public void init () {
		addModule(VertexEditor.class);
		addModule(VertexGUI.class);
	}
}
