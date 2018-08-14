package com.sailinghawklabs.lambdaking.tlines;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TransLineFile {
    public static List<TransmissionLine> read(InputStream inputStream){
        List<TransmissionLine> resultList = new ArrayList<TransmissionLine>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");

                row[0] = removeUTF8BOM(row[0].trim());
                double value = Double.parseDouble(row[0].trim());
                resultList.add(new TransmissionLine(value, row[1].trim()));
            }
        }
        catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: " + ex);
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: "+e);
            }
        }
        return resultList;
    }

    public static String removeUTF8BOM(String s) {
        if (s.startsWith("\uFEFF")) {
            s = s.substring(1);
        }
        return s;
    }
}
