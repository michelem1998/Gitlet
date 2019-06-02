package gitlet;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class CommitTree implements Serializable {
    // Map that stores the name of the branch and the last commit in the branch
    Map<String, CommitObject> branches = new HashMap<>();
    // Name of the current branch we're working on
    String currentBranchName;
    // CommitObject that points to the head
    CommitObject head;
    // The "root" of our CommitTree
    CommitObject initialCommit;
    // Staging area
    StagingArea stageArea;
    // ArrayList that stores the names of removed files
    ArrayList<String> removedFiles = new ArrayList<>();
    // ArrayList that stores the names of untracked files
    // (untracked files are files in the staging area)
    ArrayList<String> untrackedFiles = new ArrayList<>();
    // Map that stores the ID of the commit and the CommitObject
    Map<String, CommitObject> iDCommits = new HashMap<>();
    // Stored files that are not staged since it's the same file as the current CommitObject file
    ArrayList<String> nonStagedFiles = new ArrayList<>();

    // Constructor
    public CommitTree() {
        initialCommit = new CommitObject(null, "initial commit", new HashMap<>(), null);
        iDCommits.put(initialCommit.getID(), initialCommit);
        branches.put("master", initialCommit);
        currentBranchName = "master";
        head = initialCommit;
        stageArea = new StagingArea();
    }

    // Creates a new commit, tracks the saved files
    public void commit(String message) {
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message");
            return;
        }
        if (stageArea.getBlobs().isEmpty() && nonStagedFiles.isEmpty()
                && untrackedFiles.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        HashMap<String, String> parentBlobs = head.getBlobs();
        HashMap<String, String> newCommitBlobs = new HashMap<>();
        // copy over the parent blobs
        if (parentBlobs != null) {
            newCommitBlobs.putAll(parentBlobs);
        }
        // replace any of the parent blobs with the blobs found in the staging area
        for (String fileName : stageArea.getBlobs().keySet()) {
            // don't include the file if it is marked as untracked
            if (!untrackedFiles.contains(fileName)) {
                if (newCommitBlobs.containsKey(fileName)) {
                    newCommitBlobs.replace(fileName, stageArea.getBlobs().get(fileName));
                } else { // if the parent does not contain the staged blob
                    // (aka added a new file), add it
                    newCommitBlobs.put(fileName, stageArea.getBlobs().get(fileName));
                }
            } else {
                untrackedFiles.remove(fileName);
                newCommitBlobs.remove(fileName);
            }
        }
        // create new commit with head as parent, and has the same blobs as the parent
        CommitObject newCommit = new CommitObject(head.getID(), message, newCommitBlobs, null);
        //set newCommit as the child of the head
        head.addChild(newCommit.getID());
        // maps newCommit in idCommits
        iDCommits.put(newCommit.getID(), newCommit);

        // clear staging area
        stageArea.clear();
        removedFiles.clear();
        untrackedFiles.clear();
        nonStagedFiles.clear();
        head = newCommit;
        branches.put(currentBranchName, newCommit);
    }

    // Adds the file to the staging area
    public void add(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        byte[] content = Utils.readContents(file);
        String id = Utils.sha1(content);
        boolean isAdd = true;
        for (String blobID : head.getBlobs().values()) {
            if (blobID.equals(id)) {
                isAdd = false;
                nonStagedFiles.add(fileName);
            }
        }
        if (isAdd) {
            stageArea.add(fileName);
        }
        untrackedFiles.remove(fileName);
        removedFiles.remove(fileName);
    }

    // Untrack file, indicating it is not to be included in
    // next commit even if currently tracked
    public void rm(String fileName) {
        if (!stageArea.getBlobs().containsKey(fileName)
                && !head.getBlobs().containsKey(fileName)
                && !nonStagedFiles.contains(fileName)) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (head.getBlobs().containsKey(fileName)) {
            File fileDelete = new File(fileName);
            Utils.restrictedDelete(fileDelete);
            untrackedFiles.add(fileName);
            removedFiles.add(fileName);
        }
        if (stageArea.getBlobs().containsKey(fileName)) {
            stageArea.remove(fileName);
        }

    }

    // Prints out all the information from each commit,
    // starting from the head all the way to initial
    public void log() {
        CommitObject curr = head;
        while (curr != null) {
            System.out.println("===");
            System.out.println("Commit " + curr.getID());
            System.out.println(curr.getDate());
            System.out.println(curr.getMessage() + "\n");
            curr = iDCommits.get(curr.getParent());
        }
    }

    // Prints out all the information from every commit (order does not matter)
    public void globalLog() {
        for (CommitObject c : iDCommits.values()) {
            System.out.println("===");
            System.out.println("Commit " + c.getID());
            System.out.println(c.getDate());
            System.out.println(c.getMessage() + "\n");
        }
    }

    // Prints out all IDs of commits that have the given message
    public void find(String message) {
        boolean found = false;
        for (CommitObject c : iDCommits.values()) {
            if (c.getMessage().equals(message)) {
                found = true;
                System.out.println(c.getID());
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    // Prints out the status of the gitlet
    public void status() {
        System.out.println("=== Branches ===");
        ArrayList<String> sortedBranches = new ArrayList<>();
        for (String key : branches.keySet()) {
            sortedBranches.add(key);
        }
        sortedBranches.sort(Comparator.naturalOrder());
        for (String key : sortedBranches) {
            if (key.equals(currentBranchName)) {
                System.out.println("*" + currentBranchName);
            } else {
                System.out.println(key);
            }
        }
        System.out.println("\n=== Staged Files ===");
        ArrayList<String> sortedStage = new ArrayList<>();
        for (String key : stageArea.getBlobs().keySet()) {
            sortedStage.add(key);
        }
        sortedStage.sort(Comparator.naturalOrder());
        for (String key : sortedStage) {
            System.out.println(key);
        }
        System.out.println("\n=== Removed Files ===");
        for (String file : removedFiles) {
            System.out.println(file);
        }
        // last two sections are optional, can leave blank
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        System.out.println("\n=== Untracked Files ===");
    }

    /*
     * 3 possible uses:
     *  1. takes version of file in the head and overwrites the version of the file in
     *     the working directory
     *  2. takes version of the file in the commit with given id and overwrites the version
     *     of the file in the working directory
     *  3. takes all files in the head of given branch and overwrites them in the working
     *     directory
     */
    public void checkout(String[] operands) {
        String fileName = null;
        String commitID;
        String branchName;
        if (operands.length >= 2) { // CASE 1: checkout -- [file name]
            if (operands.length == 2) {
                if (!operands[0].equals("--")) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                fileName = operands[1];
                if (!head.getBlobs().containsKey(fileName)) {
                    System.out.println("File does not exist in that commit.");
                    return;
                }
                writeContentHelper(fileName, head.getBlobs().get(fileName));
            } else if (operands.length == 3) { // CASE 2: checkout [commID] -- [filename]
                if (!operands[1].equals("--")) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                commitID = shortIDtoLongID(operands[0]);
                fileName = operands[2];
                if (!iDCommits.containsKey(commitID)) {
                    System.out.println("No commit with that id exists.");
                    return;
                } else if (!iDCommits.get(commitID).getBlobs().containsKey(fileName)) {
                    System.out.println("File does not exist in that commit.");
                    return;
                }
                CommitObject curr = iDCommits.get(commitID);
                writeContentHelper(fileName, curr.getBlobs().get(fileName));
            }
        } else if (operands.length == 1) { // CASE 3: checkout [branch name]
            branchName = operands[0];
            CommitObject givenBranchHead = branches.get(branchName);
            CommitObject currBranchHead = branches.get(currentBranchName);
            if (!branches.containsKey(branchName)) {
                System.out.println("No such branch exists.");
                return;
            } else if (branchName.equals(currentBranchName)) {
                System.out.println("No need to checkout the current branch.");
                return;
            } // TO-DO: else if (check for untracked branch in current branch)
            if (checkUntrackedFiles(branches.get(branchName))) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it or add it first.");
                return;
            }
            for (String file : givenBranchHead.getBlobs().keySet()) {
                writeContentHelper(file, givenBranchHead.getBlobs().get(file));
            }
            for (String blob : currBranchHead.getBlobs().keySet()) {
                if (!givenBranchHead.getBlobs().containsKey(blob)) {
                    rm(blob);
                }
            }
            stageArea.clear();
            head = givenBranchHead;
            currentBranchName = branchName;
            removedFiles.clear();
            stageArea.clear();
        }
    }

    // Creates a new branch that points to the head, but does not switch to this branch
    public void branch(String name) {
        if (branches.containsKey(name)) {
            System.out.println("A branch with that name already exists.");
            return;
        } else {
            branches.put(name, head);
        }
    }

    // Deletes branch, but not the commits that were in the branch or anything else
    public void rmBranch(String name) {
        if (!branches.containsKey(name)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (name.equals(currentBranchName)) {
            System.out.println("Cannot remove the current branch");
            return;
        } else {
            branches.remove(name);
        }
    }

    // Checks out all files tracked by given commit, removing tracked files not present
    public void reset(String id) {
        String commitID = shortIDtoLongID(id);
        if (!iDCommits.containsKey(commitID)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        CommitObject givenCommit = iDCommits.get(commitID);

        if (checkUntrackedFiles(givenCommit)) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it or add it first.");
        }
        //for (String files : givenCommit.getBlobs().keySet()) {
        //checks out all the files tracked by the given commit.
        for (String givenFiles : givenCommit.getBlobs().keySet()) {
            String[] input = new String[]{commitID, "--", givenFiles};
            checkout(input);
        }
        // Moves the current branch head pointer and
        // the head pointer to that commit node.
        head = givenCommit;
        branches.replace(currentBranchName, givenCommit);
        // The staging area is cleared.
        stageArea.clear();
        removedFiles.clear();
        untrackedFiles.clear();
    }

    // Check if merging fails
    private boolean mergeFailureCases(String givenBranchName) {
        if (!stageArea.getBlobs().isEmpty() || !nonStagedFiles.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return true;
        }
        if (!branches.containsKey(givenBranchName)) {
            System.out.println("A branch with that name does not exist.");
            return true;
        }
        if (currentBranchName.equals(givenBranchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return true;
        }
        if (checkUntrackedFiles(branches.get(givenBranchName))) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it or add it first.");
            return true;
        }
        CommitObject givenBranch = branches.get(givenBranchName);
        CommitObject currBranch = branches.get(currentBranchName);
        CommitObject givenSplitPoint = givenBranch;
        CommitObject currSplitPoint = currBranch;
        while (currSplitPoint != null) {
            if (currSplitPoint.equals(givenBranch)) {
                System.out.println("Given branch is an ancestor of the current branch.");
                return true;
            }
            currSplitPoint = iDCommits.get(currSplitPoint.getParent());
        }
        while (givenSplitPoint != null) {
            if (givenSplitPoint.equals(currBranch)) {
                currentBranchName = givenBranchName;
                System.out.println("Current branch fast-forwarded.");
                return true;
            }
            givenSplitPoint = iDCommits.get(givenSplitPoint.getParent());
        }
        return false;
    }

    // Find split point of current and given branch
    private CommitObject findSplitPoint(String givenBranchName) {
        CommitObject splitPoint = null;
        // make a list of all ancestors of last commit in given branch
        ArrayList<String> givenAncestors = new ArrayList<>();
        CommitObject given = branches.get(givenBranchName);
        while (given.getParent() != null) {
            givenAncestors.add(given.getParent());
            given = iDCommits.get(given.getParent());
        }
        // make a list of all ancestors of last commit in current branch
        ArrayList<String> currAncestors = new ArrayList<>();
        CommitObject curr = branches.get(currentBranchName);
        while (curr.getParent() != null) {
            currAncestors.add(curr.getParent());
            curr = iDCommits.get(curr.getParent());
        }
        // using the larger ancestor list, find the first common ancestor
        if (currAncestors.size() > givenAncestors.size()) {
            for (String ancestor : givenAncestors) {
                if (currAncestors.contains(ancestor)) {
                    splitPoint = iDCommits.get(ancestor);
                    break;
                }
            }
        } else {
            for (String ancestor : currAncestors) {
                if (givenAncestors.contains(ancestor)) {
                    splitPoint = iDCommits.get(ancestor);
                    break;
                }
            }
        }
        return splitPoint;
    }

    // Merge files from given branch into the current branch
    public void merge(String givenBranchName) {
        if (mergeFailureCases(givenBranchName)) {
            return;
        }
        CommitObject splitPoint = findSplitPoint(givenBranchName);
        //CommitObject splitPoint = iDtoCommit(givenBranchName);
        CommitObject currBranch = branches.get(currentBranchName);
        CommitObject givenBranch = branches.get(givenBranchName);
        int i  = 0; //this int is to check with the conflict will help.
        HashMap<String, String> blobsGiven = branches.get(givenBranchName).getBlobs();
        HashMap<String, String> blobsCurr = branches.get(currentBranchName).getBlobs();
        HashMap<String, String> blobsSplitPoint = splitPoint.getBlobs();
        for (String nameOfSplit: blobsSplitPoint.keySet()) {
            String idOfSplit = blobsSplitPoint.get(nameOfSplit);
            // Case 1 split have and only modified in the given branch;
            if (blobsCurr.containsKey(nameOfSplit)
                    && blobsCurr.containsValue(idOfSplit)) {
                if (blobsGiven.containsKey(nameOfSplit)
                        && !blobsGiven.get(nameOfSplit).equals(idOfSplit)) {
                    // the following steps are creating a new file containing the content of
                    // the modified file in the given branch and stage it
                    String[] inp = {branches.get(givenBranchName).getID(), "--", nameOfSplit};
                    checkout(inp);
                    stageArea.add(nameOfSplit);
                    continue;
                } else {
                    // case 5: file is absent in the given branch
                    rm(nameOfSplit);
                    removedFiles.clear();
                    continue;
                }
            }
            // Merge Conflict: file present in the split point
            if (blobsCurr.containsKey(nameOfSplit) && blobsGiven.containsKey(nameOfSplit)) {
                if (!blobsCurr.containsValue(idOfSplit) && !blobsGiven.containsValue(idOfSplit)
                        && !blobsCurr.get(nameOfSplit).equals(blobsGiven.get(nameOfSplit))) {
                    String givenID = blobsGiven.get(nameOfSplit);
                    String currID = blobsCurr.get(nameOfSplit);
                    mergeRewrite(nameOfSplit, givenID, currID);
                    i = 1;
                } else if (!blobsCurr.containsValue(idOfSplit)
                        && blobsGiven.containsValue(idOfSplit)) {
                    /*String givenID = blobsGiven.get(nameOfSplit);
                    mergeRewrite(nameOfSplit, givenID, null);*/
                    String currID = blobsCurr.get(nameOfSplit);
                    mergeRewrite(nameOfSplit, null, currID);
                    i = 1;
                } else if (blobsCurr.containsValue(idOfSplit)
                        && !blobsGiven.containsValue(idOfSplit)) {
                    /*String currID = blobsCurr.get(nameOfSplit);
                    mergeRewrite(nameOfSplit, null, currID);*/
                    String givenID = blobsGiven.get(nameOfSplit);
                    mergeRewrite(nameOfSplit, givenID, null);
                }
            }
        }
        for (String file: blobsGiven.keySet()) {
            String givenId = blobsGiven.get(file);
            //Merge Conflict: file not present in the split point
            if (!blobsSplitPoint.containsKey(file) && blobsCurr.containsKey(file)
                    && !blobsCurr.containsValue(givenId)) {
                String currId = blobsCurr.get(file);
                mergeRewrite(file, givenId, currId);
                i = 1;
            }
            // case 4
            if (!blobsSplitPoint.containsKey(file) && !blobsCurr.containsKey(file)) {
                String[] inp = {branches.get(givenBranchName).getID(), "--", file};
                checkout(inp);
                stageArea.add(file);
            }
        }
        if (i == 0 && ((!splitPoint.getID().equals(currBranch.getID())
                || !splitPoint.getID().equals(givenBranch.getID())))) {
            commit("Merged " + currentBranchName + " with " + givenBranchName + ".");
        } else {
            System.out.println("Encountered a merge conflict.");
        }
    }

    // if merging has a conflict, rewrite the contents of the conflicted file
    public void mergeRewrite(String fileName, String givenID, String currID) {
        File rewrite = new File(fileName);
        File givenFile = new File(".gitlet/blobs/" + givenID);
        File currFile = new File(".gitlet/blobs/" + currID);
        byte[] contentOfGiven = "".getBytes();
        if (givenID != null) {
            contentOfGiven = Utils.readContents(givenFile);
        }
        byte[] contentOfCurr = "".getBytes();
        if (currID != null) {
            contentOfCurr = Utils.readContents(currFile);
        }
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        try {
            content.write("<<<<<<< HEAD\n".getBytes());
            if (currID != null) {
                content.write(contentOfCurr);
            }
            content.write("=======\n".getBytes());
            if (givenID != null) {
                content.write(contentOfGiven);
            }
            content.write(">>>>>>>\n".getBytes());
        } catch (IOException e) {
            return;
        }
        byte[] contentOfRewrite = content.toByteArray();
        Utils.writeContents(rewrite, contentOfRewrite);
    }

    // converts the short version of an id to the corresponding whole id
    public String shortIDtoLongID(String id) {
        for (String longID : iDCommits.keySet()) {
            if (longID.substring(0, id.length()).equals(id)) {
                return longID;
            }
        }
        return null;
    }


    // returns true if there are untracked files in the directory
    private boolean checkUntrackedFiles(CommitObject curr) {
        File dir = new File(System.getProperty("user.dir"));
        for (File f : dir.listFiles()) {
            if (!f.getName().equals(".gitlet")
                    && untrackedFiles.contains(f.getName())
                    && curr.getBlobs().containsKey(f.getName())) {
                return true;
            }
        }
        return false;
    }

    private void writeContentHelper(String file, String id) {
        File rewrite = new File(file);
        File older = new File(".gitlet/blobs/" + id);
        byte[] bytes = Utils.readContents(older);
        Utils.writeContents(rewrite, bytes);


    }
}
