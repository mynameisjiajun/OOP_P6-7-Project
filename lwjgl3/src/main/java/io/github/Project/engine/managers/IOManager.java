package io.github.Project.engine.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Manages input/output operations.
 * Handles file reading and writing.
 */
public class IOManager {

    /**
     * Creates a new IOManager.
     */
    public IOManager() {
        // No input processor registration (SRP-compliant)
    }

    /**
     * Reads a file from the assets directory.
     * @param filePath The file path relative to assets
     * @return File contents as String
     */
    public String readInternalFile(String filePath) {
        FileHandle file = Gdx.files.internal(filePath);
        return file.readString();
    }

    /**
     * Writes data to a local file.
     * @param filePath The file path relative to local storage
     * @param content The content to write
     */
    public void writeLocalFile(String filePath, String content) {
        FileHandle file = Gdx.files.local(filePath);
        file.writeString(content, false);
    }

    /**
     * Checks if a file exists in internal storage.
     * @param filePath The file path
     * @return true if exists, false otherwise
     */
    public boolean fileExists(String filePath) {
        return Gdx.files.internal(filePath).exists();
    }
}

