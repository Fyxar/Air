package com.dev.air.font;

import com.dev.air.font.impl.CustomFontRenderer;

import java.awt.*;

public class Fonts {

    public static CustomFontRenderer robotoRegular9, robotoRegular18, smallPixel18, verdana18;

    static {
        robotoRegular9 = new CustomFontRenderer("Roboto-Regular", 9, Font.PLAIN, true, true);
        robotoRegular18 = new CustomFontRenderer("Roboto-Regular", 18, Font.PLAIN, true, true);

        smallPixel18 = new CustomFontRenderer("SmallPixel", 18, Font.PLAIN, true, true);
        verdana18 = new CustomFontRenderer("Verdana", 18, Font.PLAIN, true, true);
    }

}
