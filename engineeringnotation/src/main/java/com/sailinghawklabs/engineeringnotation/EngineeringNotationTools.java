package com.sailinghawklabs.engineeringnotation;

public class EngineeringNotationTools {

    // based on:  http://www.labbookpages.co.uk/software/java/engNotation.html

    private final static int PREFIX_OFFSET = 5;
    private final static String[] PREFIX_ARRAY = {"f", "p", "n", "µ", "m", "", "k", "M", "G", "T"};

    public static class MantissaExponent {
        public MantissaExponent(String mantissaString, String exponentString) {
            this.mantissaString = mantissaString;
            this.exponentString = exponentString;
        }

        public String mantissaString;
        public String exponentString;
    }

    public static String encode(double val, int dp) {
        MantissaExponent result = encodeMantissa(val, dp);
        return result.mantissaString + result.exponentString;
    }

    public static MantissaExponent encodeMantissa(double val, int dp)
    {
        // The function below takes a double value and returns a string formatted in engineering notation.
        // The number of digits after the decimal point is specified by the dp argument. Some examples are listed below.
        //
        //        val	    dp	Output
        //        0.01	    2	10.00m
        //        12456	    1	12.5k
        //        56000000	0	56M
        //        0.0006819	3	681.900μ

        // If the value is zero, then simply return 0 with the correct number of dp
        if (val == 0) return new MantissaExponent(String.format("%." + dp + "f", 0.0), "");

        // If the value is negative, make it positive so the log10 works
        double posVal = (val<0) ? -val : val;
        double log10 = Math.log10(posVal);

        // Determine how many orders of 3 magnitudes the value is
        int count = (int) Math.floor(log10/3);

        // Calculate the index of the prefix symbol
        int index = count + PREFIX_OFFSET;

        // Scale the value into the range 1<=val<1000
        val /= Math.pow(10, count * 3);

        if (index >= 0 && index < PREFIX_ARRAY.length)
        {
            // If a prefix exists use it to create the correct string
            return new MantissaExponent(String.format("%." + dp + "f", val), PREFIX_ARRAY[index]);
            }
        else
        {
            // If no prefix exists just make a string of the form 000e000
            return new MantissaExponent(String.format("%." + dp + "fe%d", val, count * 3), "");
        }
    }


    // The two functions listed below can be used to take a String or a char array
    // containing a number in engineering notation and parse them into a double value.
    // On error a NumberFormatException is thrown.
    // The functions will ignore white space characters before and after the encoded number.
    //
    // Some examples are listed in the table below.
    //
    //    String	Output
    //      1.4k	1400.0
    //     245m7	0.2457
    //    12.56M	12560000.0


    private final static char[] PREFIX_TEST_ARRAY = {'f', 'p', 'n', 'u', 'µ', 'm', 'k', 'K', 'M', 'G', 'T'};
    private final static int[] PREFIX_EXP_ARRAY = {-15, -12, -9, -6, -6, -3, 3, 3, 6, 9, 12};

    public static double parse(String str)
    {
        return parse(str.toCharArray());
    }

    public static double parse(char[] chars) throws NumberFormatException
    {
        int exponent = 0;
        double value = 0;

        boolean gotChar = false;      // Set to true once any non-whitespace, or minus character has been found
        boolean gotMinus = false;     // Set to true once a minus character has been found
        boolean gotDP = false;        // Set to true once a decimal place character has been found
        boolean gotPrefix = false;    // Set to true once a prefix character has been found
        boolean gotDigit = false;     // Set to true once a digit character has been found

        // Search for start of string
        int start = 0;
        while (start < chars.length) {
            if (chars[start] != ' ' && chars[start] != '\t') break;
            start ++;
        }

        if (start == chars.length) throw new NumberFormatException("Empty string");

        // Search for end of string
        int end = chars.length - 1;
        while (end >= 0) {
            if (chars[end] != ' ' && chars[end] != '\t') break;
            end --;
        }

        // Iterate through characters
        CharLoop: for (int c=start ; c<=end ; c++)
        {
            // Check for a minus symbol
            if (chars[c] == '-')
            {
                if (gotChar) throw new NumberFormatException("Can only have minus symbol at the start");
                if (gotMinus) throw new NumberFormatException("Too many minus symbols");
                gotMinus = true;
                continue CharLoop;
            }

            gotChar = true;

            // Check for a numerical digit
            if (chars[c] >= '0' && chars[c] <= '9')
            {
                if (gotPrefix || gotDP) exponent --;
                if (gotPrefix && gotDP) throw new NumberFormatException("Cannot have digits after prefix when number includes decimal point");
                value *= 10;
                value += chars[c] - '0';
                gotDigit = true;
                continue CharLoop;
            }

            // Check for a decimal place
            if (chars[c] == '.')
            {
                if (gotDP) throw new NumberFormatException("Too many decimal points");
                if (gotPrefix) throw new NumberFormatException("Cannot have decimal point after prefix");
                gotDP = true;
                continue CharLoop;
            }

            // Check for a match with a prefix character
            for (int p=0 ; p<PREFIX_TEST_ARRAY.length ; p++)
            {
                if (PREFIX_TEST_ARRAY[p] == chars[c])
                {
                    if (gotPrefix) throw new NumberFormatException("Too many prefixes");
                    exponent += PREFIX_EXP_ARRAY[p];
                    gotPrefix = true;
                    continue CharLoop;
                }
            }

            // All other characters are invalid
            throw new NumberFormatException("Invalid character '" + chars[c] + "'");
        }

        // Check if any digits were found
        if (!gotDigit) throw new NumberFormatException("No digits");

        // Apply negation if required
        if (gotMinus) value *= -1;

        return value * Math.pow(10, exponent);
    }
}
