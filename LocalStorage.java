package main;

import java.io.*;
import java.util.Properties;

public class LocalStorage {

    private String fileName;

    public LocalStorage(String fileName) {
        this.fileName = fileName;
    }

    public void saveData(String key, int value) {
        Properties properties = new Properties();

        try (OutputStream outputStream = new FileOutputStream(fileName)) {
            properties.setProperty(key, String.valueOf(value));
            properties.store(outputStream, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int loadData(String key) {
        Properties properties = new Properties();

        try (InputStream inputStream = new FileInputStream(fileName)) {
            properties.load(inputStream);
            String value = properties.getProperty(key);
            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }
    
    public void resetLocalStorage() {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete(); // Delete the file if it exists
            System.out.println("Local storage reset successfully.");
        } 
        saveData("Score", 0);
    }
    
    public boolean localStorageExists() {
        File file = new File(fileName);
        return file.exists();
    }
}
