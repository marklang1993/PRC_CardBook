package com.swws.marklang.prc_cardbook.utility;

public class Size {

    public int Width;
    public int Height;

    public Size(int width, int height) {
        Width = width;
        Height = height;
    }

    @Override
    public String toString() {
        return "Size: " + Width + ", " + Height;
    }
}
