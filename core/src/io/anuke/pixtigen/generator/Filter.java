package io.anuke.pixtigen.generator;

import io.anuke.pixtigen.util.ValueMap;

public class Filter{
	public final FilterType type;
	public final ValueMap values;
	public boolean enabled = true;
	
	public Filter(FilterType type){
		this.type = type;
		values = new ValueMap();
		type.initValues(values);
	}
	
	protected Filter(){
		type = null;
		values = null;
	}
}
