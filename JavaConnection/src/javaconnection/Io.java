package javaconnection;

import java.io.*;
import java.util.stream.Collectors;

public class Io {

    static void saveStringToFile(String path, String string) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(new File(path));
            os.write(string.getBytes());
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String loadStringFromFile(String path) {
        String string = null;
        try {
            File file = new File(path);
            if (file.exists()) {
                //создаем объект FileReader для объекта File
                FileReader fr = new FileReader(file);
                //создаем BufferedReader с существующего FileReader для построчного считывания
                string = new BufferedReader(fr).lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }
}
