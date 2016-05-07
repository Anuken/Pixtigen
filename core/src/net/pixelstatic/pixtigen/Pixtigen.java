package net.pixelstatic.pixtigen;

import net.pixelstatic.pixtigen.gui.VertexEditor;
import net.pixelstatic.pixtigen.gui.VertexGUI;
import net.pixelstatic.utils.modules.ModuleController;

public class Pixtigen extends ModuleController<Pixtigen>{

	@Override
	public void init () {
		addModule(VertexEditor.class);
		addModule(VertexGUI.class);
	}
}
