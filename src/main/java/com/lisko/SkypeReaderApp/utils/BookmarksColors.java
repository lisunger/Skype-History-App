package com.lisko.SkypeReaderApp.utils;

public enum BookmarksColors {
	
	RED("#d32f2f"),
	ORANGE("#ff8f0f"),
	YELLOW("#fbc02d"),
	GREEN("#9acd32"),
	BLUE("#00aff0"),
	PINK("#eb7bc0");
	
	private final String rgb;
	
	private BookmarksColors(String rgb) {
        this.rgb = rgb;
    }
	
	public String getRgb() {
		return this.rgb;
	}
	
}
