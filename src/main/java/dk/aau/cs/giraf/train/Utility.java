package dk.aau.cs.giraf.train;

import android.app.Activity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;


public class Utility {

    public static String readRawFileToString(Activity a, int rawResource) {
        String dataString = "";
        InputStream is = a.getResources().openRawResource(rawResource);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            is.close();

            dataString = writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataString;
    }


}
