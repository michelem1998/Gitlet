package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

public class StagingArea implements Serializable {

    // holds all the blobs with key as file names and value as id
    private HashMap<String, String> stage;
    // directory of blobs
    private String directory = ".gitlet/blobs";

    // constructor
    public StagingArea() {
        stage = new HashMap<String, String>();
        File path = new File(directory);
        // creates the directory of blobs
        path.mkdirs();
    }

    // Clear all the blobs in the list
    public void clear() {
        stage = new HashMap<String, String>();
    }

    //Adds a Blob into the stage list
    public void add(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        byte[] content = Utils.readContents(file);
        String id = Utils.sha1(content);
        File blob = new File(directory + "/" + id);

        if (stage.isEmpty() || !stage.containsKey(filename)
                || !stage.get(filename).equals(id)) {
            stage.put(filename, id);
            Utils.writeContents(blob, content);
        }
    }

    // returns all the blobs in the staging area
    public HashMap<String, String> getBlobs() {
        return stage;
    }

    // Removes Blob b from the stage list
    public HashMap<String, String> remove(String filename) {
        stage.remove(filename);
        return stage;
    }
}
