package com.example.notyet.utilities;

import java.text.DecimalFormat;

public class CustomNumberFormatter {
    public static final DecimalFormat format100 = new DecimalFormat("###");
    public static final DecimalFormat format10 = new DecimalFormat("##.#");
    public static final DecimalFormat format1 = new DecimalFormat("#.##");
    public static final DecimalFormat format0 = new DecimalFormat(".###");

    // Yes I know this is REALLY ugly. If you see a better way to do it, please, please let me know...
    // But do check that it gives the same results first... ".0" is ugly.
    //This will truncate the numbers to 3 places. If they go very high or very low, this will not work. This is currently by design.
    public static String formatToThreeCharacters(float input)
    {
        String retVal = "";
        boolean negate = false;
        if(input < 0)
        {
            input = Math.abs(input);
            negate = true;
        }

        if(input > 100) {
            retVal = format100.format(input);
        } else if(input > 10) {
            retVal = format10.format(input);
        } else if (input >= 1){
            retVal = format1.format(input);
        } else if (input > 0.0005) {
            retVal = format0.format(input);
        } else {
            retVal = "0";
        }
        if(negate) {
            retVal = "-" + retVal;
        }

        return retVal;
    }
}
