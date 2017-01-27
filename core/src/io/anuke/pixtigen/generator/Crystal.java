package io.anuke.pixtigen.generator;

public enum Crystal{
	RANDOM, SQUARE, HEXAGONAL, OCTAGONAL, TRIANGULAR;
	
	public String toString(){
		String name = name();
		return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
	}
}
