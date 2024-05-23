package com.zeus.tec.ui.leida.util;

public class TryParse {

    public static int tryparse (String input,int out ){

        try {
            int parsedValue = Integer.parseInt(input);

            return parsedValue;
            //System.out.println("Parsed value: " + parsedValue);
        } catch (NumberFormatException e) {
            return -1;
            //System.out.println("Invalid input. Using default value: " + defaultValue);
        }

    }

    public static float tryparse (String input ){

        try {
            float parsedValue = Float.parseFloat(input);

            return parsedValue;
            //System.out.println("Parsed value: " + parsedValue);
        } catch (NumberFormatException e) {
            return -1;
            //System.out.println("Invalid input. Using default value: " + defaultValue);
        }

    }


}
