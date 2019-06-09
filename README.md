# Gitlet
A version control system, similar to Github, that saves and restores backup of files.

Design and implement all the classes, methods, variables and data structures in Java

This project was designed and developed for CS61BL: Data Structures at UC Berkeley in Summer 2018.

## Installations
Clone this repository in your local machine
```
git clone https://github.com/cleomart/Gitlet.git
```
Go to the repository directory and initialize `Gitlet` by compiling and running `gitlet/Main.c`
```
javac gitlet/Main.c
java gitlet/Main init
```
## The Commands
### add
 - ###### Usage 
````
java gitlet/Main add [file name]
````
- ###### Description
```
Adds the file into the staging area, which will be included in the next commit.
```
#### commit 
- ##### Usage
```
java gitlet/Main commit [message]
```
- ##### Description
```
Tracks the files in the staging area. This creates a new commit with commit message.
```
#### rm
- ##### Usage
```
java gitlet/Main rm [file name]
```
- ##### Description
```
Untracks the file -- indicates that the file is not to be included in the next commit.
```

#### log
- ##### Usage
```
java gitlet/Main log
```

- ##### Description
```
Display information for each commit in the commit tree starting from the head commit until the initial commit.
```
#### global-log
- ##### Usage
```
java gitlet/Main global-log
```

- ##### Description
```
Displays the information for each commit ever made.
```
#### find
- ##### Usage
```
java gitlet/Main find [commit message]
```

- ##### Description
```
Prints out the ids of all commits that have the given commit message.
```
#### status
- ##### Usage
```
java gitlet/Main status
```

- ##### Description
```
Displays what branches currently exist, and the branch with * indicated the current branch.
```
#### checkout
- ##### Usage
```
1. java gitlet/Main checkout -- [file name]
2. java gitlet/Main checkout [commit id] -- [file name]
3. java gitlet/Main checkout [branch name]
```

- ##### Description
```
1. Reverts file to the version of the file in the head commit.
2. Takes the version of the file as it exists in the commit with the given id, and puts it in the 
   working directory, overwriting the version of the file that’s already there if there is one.
3. Takes all files in the commit at the head of the given branch, and puts them in the working 
   directory, overwriting the versions of the files that are already there if they exist. Also,
   at the end of this command, the given branch will now be considered the current branch (HEAD).

```
#### branch
- ##### Usage
```
java gitlet/Main branch [branch name]
```

- ##### Description
```
Created a new branch with the given name.
```
#### rm-branch
- ##### Usage
```
java gitlet/Main rm-branch [branch name]
```

- ##### Description
```
Deletes the branch with the given name.
```
#### reset
- ##### Usage
```
java gitlet/Main reset [commit id]
```

- ##### Description
```
Checks out all the files tracked by the given commit. Removes tracked files that are not 
present in the given commit. Moves the current branch’s head pointer and the head pointer
to that commit node.
```
#### merge
- ##### Usage
```
java gitlet/Main merge [branch name]
```

- ##### Description
```
Merges files from the given branch into the current branch
```
## Authors
- Leomart Crisostomo
- Amy Li
- Angela Li
- Shreya Ayyagari
