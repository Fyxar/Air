package com.dev.air.font;

import com.dev.air.font.impl.CustomFontRenderer;

import java.awt.*;

public class Fonts {

    public static CustomFontRenderer robotoRegular18, sfUI18, productSans18, poppins18;

    static {
        robotoRegular18 = new CustomFontRenderer("Roboto-Regular", 18, Font.PLAIN, true, true);
        sfUI18 = new CustomFontRenderer("sfuiregular", 18, Font.PLAIN, true, true);
        productSans18 = new CustomFontRenderer("Product Sans Regular", 18, Font.PLAIN, true, true);
        poppins18 = new CustomFontRenderer("Poppins-Regular", 18, Font.PLAIN, true, true);
    }

}
