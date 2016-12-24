package net.pixelstatic.pixtigen;

import io.anuke.utils.modules.ModuleController;
import net.pixelstatic.pixtigen.gui.VertexEditor;
import net.pixelstatic.pixtigen.gui.VertexGUI;

public class Pixtigen extends ModuleController<Pixtigen>{
	
	@Override
	public void init () {
		addModule(VertexEditor.class);
		addModule(VertexGUI.class);
	}
}
