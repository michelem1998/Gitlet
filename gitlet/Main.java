package gitlet;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;


public class Main {

    // A Commit Tree that stores commits
    private static CommitTree commitTree;

    //Checks if a gitlet directory already exists
    private static boolean isGitletInitialized() {
        File checkDir = new File(System.getProperty("user.dir") + "/.gitlet");
        if (checkDir.exists()) {
            recoverCommitTree();
            return true;
        }
        return false;
    }

    // Returns the operands of a command in an array of Strings
    private static String[] getOperands(String[] args) {
        String[] copy = new String[args.length - 1];
        System.arraycopy(args, 1, copy, 0, copy.length);
        return copy;
    }

    // recovers Commit Tree that has been serialized
    private static void recoverCommitTree() {
        File main = new File(".gitlet/commitTree");
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(main));
            commitTree = (CommitTree) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException e) {
            return;
        }
    }

    // serialize a Commit Tree Object
    private static void serialize(CommitTree tree) {
        File main = new File(".gitlet/commitTree");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(main));
            out.writeObject(tree);
            out.close();
        } catch (IOException e) {
            return;
        }
    }

    // checks if arg is valid
    private static boolean isArgsValid(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            return false;
        }
        return true;
    }


    public static void main(String... args) {

        // checks failure cases first
        if (args.length == 0) {
            System.out.println("Please enter a command");
        } else if (args[0].equals("init")) {
            if (!isArgsValid(args, 1)) {
                return;
            }
            if (isGitletInitialized()) {
                System.out.println("A gitlet version-control system "
                        + "already exists in the current directory.");
            } else {
                commitTree = new CommitTree();
                File directory = new File(".gitlet");
                directory.mkdir();
                serialize(commitTree);
            }
        } else {
            if (!isGitletInitialized()) {
                System.out.println("Not in an initialized gitlet directory.");
                return;
            }
            // performs a command
            switch (args[0]) {
                case "add":
                    addHelper(args);
                    break;
                case "commit":
                    commitHelper(args);
                    break;
                case "rm":
                    rmHelper(args);
                    break;
                case "log":
                    logHelper(args);
                    break;
                case "global-log":
                    globalLogHelper(args);
                    break;
                case "find":
                    findHelper(args);
                    break;
                case "status":
                    statusHelper(args);
                    break;
                case "checkout":
                    checkoutHelper(args);
                    break;
                case "branch":
                    branchHelper(args);
                    break;
                case "rm-branch":
                    rmBranchHelper(args);
                    break;
                case "reset":
                    resetHelper(args);
                    break;
                case "merge":
                    mergeHelper(args);
                    break;
                default:
                    System.out.println("No command with that name exists.");
            }
        }
    }

    // COMMANDS HELPER
    private static void addHelper(String[] args) {
        if (!isArgsValid(args, 2)) {
            return;
        }
        commitTree.add(args[1]);
        serialize(commitTree);
    }

    private static void commitHelper(String[] args) {
        if (!isArgsValid(args, 2)) {
            return;
        }
        commitTree.commit(args[1]);
        serialize(commitTree);
    }

    private static void rmHelper(String[] args) {
        if (!isArgsValid(args, 2)) {
            return;
        }
        String[] files = getOperands(args);
        for (String file : files) {
            commitTree.rm(file);
        }
        serialize(commitTree);
    }

    private static void logHelper(String[] args) {
        if (!isArgsValid(args, 1)) {
            return;
        }
        commitTree.log();
    }

    private static void globalLogHelper(String[] args) {
        if (!isArgsValid(args, 1)) {
            return;
        }
        commitTree.globalLog();
    }

    private static void findHelper(String[] args) {
        if (!isArgsValid(args, 2)) {
            return;
        }
        commitTree.find(args[1]);
    }

    private static void statusHelper(String[] args) {
        if (!isArgsValid(args, 1)) {
            return;
        }
        commitTree.status();
    }

    private static void checkoutHelper(String[] args) {
        if (args.length > 4) {
            System.out.println("Incorrect operands.");
            return;
        }
        commitTree.checkout(getOperands(args));
        serialize(commitTree);
    }

    private static void branchHelper(String[] args) {
        if (!isArgsValid(args, 2)) {
            return;
        }
        commitTree.branch(args[1]);
        serialize(commitTree);
    }

    private static void rmBranchHelper(String[] args) {
        if (!isArgsValid(args, 2)) {
            return;
        }
        commitTree.rmBranch(args[1]);
        serialize(commitTree);
    }

    private static void resetHelper(String[] args) {
        if (!isArgsValid(args, 2)) {
            return;
        }
        commitTree.reset(args[1]);
        serialize(commitTree);
    }

    private static void mergeHelper(String[] args) {
        if (!isArgsValid(args, 2)) {
            return;
        }
        commitTree.merge(args[1]);
        serialize(commitTree);
    }
}
