package gitlet;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class CommitObject implements Serializable {

    // Fields
   // id of this Commit Object
    private String id;

    // parent of this Commit Object
    private final String parent;

    // log message of this Commit Object
    private final String message;

    // time stamp
    private String commitDate;

    // blobs
    private HashMap<String, String> blobs;

    // the ids of its children
    private ArrayList<String> children;

    // Constructor
    public CommitObject(String parent, String msg, HashMap<String, String> blobs, String child) {
        this.parent = parent;
        this.message = msg;
        this.blobs = blobs;
        if (child == null) {
            children = new ArrayList<>();
        } else {
            children.add(child);
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        commitDate = dtf.format(LocalDateTime.now());
        String f;
        String p;
        if (blobs.isEmpty()) {
            f = "";
        } else {
            f = blobs.toString();
        }
        if (parent == null) {
            p = "";
        } else {
            p = parent;
        }
        // creates the id
        id = Utils.sha1(message, commitDate, f, p);

    }

    // get the parent
    public String getParent() {
        return parent;
    }
    // get the ID of this commit object
    public String getID() {
        return id;
    }
    // get the corresponding message of this commit object
    public String getMessage() {
        return message;
    }
    // get the timestamp of this commit object
    public String getDate() {
        return commitDate;
    }
    // get the blobs for this commit object
    public HashMap<String, String> getBlobs() {
        return blobs;
    }
    // get the children of this commit
    public ArrayList<String> getChildren() {
        return children; }
    // set the children of this commit
    public void addChild(String s) {
        children.add(s); }

}
