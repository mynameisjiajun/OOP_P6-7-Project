package io.github.Project.engine.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;


public class IOManager {

    public IOManager() {
        
    }
     //Reads from assets folder (read-only)
    public String readInternalFile(String filePath) {
        FileHandle file = Gdx.files.internal(filePath);
        return file.readString();
    }
    //Local storage for save files and settings
    public void writeLocalFile(String filePath, String content) {
        FileHandle file = Gdx.files.local(filePath);
        file.writeString(content, false);
    }
    public String readLocalFile(String filePath) {
        FileHandle file = Gdx.files.local(filePath);
        return file.exists() ? file.readString() : null;
    }

    public boolean localFileExists(String filePath) {
        return Gdx.files.local(filePath).exists();
    }


   //check internal assets for existence
    public boolean internalfileExists(String filePath) {
        return Gdx.files.internal(filePath).exists();
    }
}

