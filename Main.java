// Author: Ivan Vasilev
import java.util.*;

// Main class containing the program entry point
public class Main{
    // main() method
    public static void main(String[] args){


        // Create a scanner to read input
        Scanner sc = new Scanner(System.in);
        // Read the number of commands to process
        int numberOfCommands = sc.nextInt();
        sc.nextLine();
        // Create a HashMap to store nodes by their ID parent and node itself
        HashMap<Integer, Node> nodes = new HashMap<>();
        // Create a list to store all nodes
        ArrayList<Node> listNodes = new ArrayList<>();
        // Create the root directory with ID 0 and name "."
        // root has not a parent
        Directory root = new Directory(0, ".", -1);
        // Add root directory to the nodes map
        nodes.put(0, root);
        // Process each command
        for(int i = 0; i < numberOfCommands; i++){
            // Split the command line into parts
            String[] command = sc.nextLine().split(" ");
            // Handle FILE command
            if (command[0].equals("FILE")){
                // Parse file properties from command
                int id = Integer.parseInt(command[1]);
                String type = command[2];
                String owner = command[3];
                String group = command[4];
                double size = Double.parseDouble(command[5]);
                String name = command[6].split("\\.")[0];
                String typeOfFile = command[6].split("\\.")[1];
                // Get or create file properties that might be similar with other file
                FileProperties properties = FilePropertiesFactory.getProperties(type, owner, group, typeOfFile);
                // Create new file with parsed properties
                File file = new File (size, properties, id, name);
                // Set parent ID for file
                file.parent = id;
                // Add file to nodes list
                listNodes.add(file);
                // Add file to parent directory's children
                ((Directory) nodes.get(id)).children.add(file);
            }
            // Handle DIR command
            else if (command[0].equals("DIR")){
                // Handle directory with parent ID
                if (command.length == 4) {
                    int id = Integer.parseInt(command[1]);
                    int parentId = Integer.parseInt(command[2]);
                    String name = command[3];
                    // Create new directory
                    Directory dir = new Directory(id, name, parentId);
                    // Add directory to parent's children
                    ((Directory) nodes.get(parentId)).children.add(dir);
                    // Store directory in nodes map
                    nodes.put(id, dir);
                    // Add directory to nodes list
                    listNodes.add(dir);
                }
                // Handle root-level directory
                else {
                    int id = Integer.parseInt(command[1]);
                    int parentId = 0;
                    String name = command[2];
                    // Create new directory with root as parent
                    Directory dir = new Directory(id, name, 0);
                    // Add directory to root's children
                    ((Directory) nodes.get(parentId)).children.add(dir);
                    // Store directory in nodes map
                    nodes.put(id, dir);
                    // Add directory to nodes list
                    listNodes.add(dir);
                }
            }
        }
        // Create visitor to calculate total size
        SizeVisitor  visitor = new SizeVisitor ();
        // Start visiting from root for other nodes
        root.accept(visitor);
        // Print total size
        if (visitor.getTotalSize() % 1 == 0){ // if number without floating point
            System.out.printf(Locale.ENGLISH, "total: %dKB\n", (int)visitor.getTotalSize());
        }else{ //if number with floating point
            System.out.println("total: " + String.format(Locale.ENGLISH, "%.2f", visitor.getTotalSize()).replaceAll("0*$", "").replaceAll("\\.$", "") + "KB");
        }
        // draw the tree structure... Usage of Iterator pattern
        root.createIterator(root, nodes, listNodes);

    }
}

// Visitor class to calculate total size of files
class SizeVisitor  implements Visitor{
    //contains total size of the files in the tree
    private double totalSize = 0;
    // Visit a file and add its size to total
    @Override
    public void visit(File file){
        totalSize += file.getSize();
    }
    // Visit a directory and recursively visit all children
    @Override
    public void visit(Directory directory){
        //for each child apply visitor
        for (Node child : directory.children){
            child.accept(this);
        }
    }
    // Get the total size of all files
    public double getTotalSize(){
        return totalSize;
    }
}

// Base class for all nodes in the file system
class Node{
    protected int id;         // Unique identifier for the node
    protected String name;    // Name of the node
    protected int parent;     // ID of parent node
    // Constructor for Node
    public Node(int id, String name){
        this.id = id;
        this.name = name;
        parent = 0; // initially parent is 0
    }
    // Accept method for visitor pattern
    public void accept(Visitor visitor){}
}

// Directory class representing a folder in the file system
class Directory extends Node{
    ArrayList<Node> children = new ArrayList<>();  // List of child nodes
    // Constructor for Directory
    public Directory(int id, String name, int parentID){
        super(id, name);
        this.parent = parentID; // set the parent ID
    }
    // Create and start tree iteration
    public void createIterator(Directory dir, HashMap<Integer, Node> nodes, ArrayList<Node> nodesList){
        new IterationInDepth(this).drawTreeIt(dir, nodes, nodesList); // create instance of Iterator class
    }
    // Accept method for visitor pattern
    @Override
    public void accept(Visitor visitor){
        visitor.visit(this);
    }
}

// File class representing a file in the file system
class File extends Node{
    private final double size; // Size of the file as a double
    private final FileProperties properties;  // File properties (permissions, owner, group)
    // Constructor for File
    public File(double size, FileProperties properties, int id, String name){
        super(id, name);
        this.size = size;
        this.properties = properties;// set the common properties Flyweight pattern
    }
    // Get file size
    public double getSize(){
        return size;
    }
    // Get file properties
    public FileProperties getProperties(){
        return properties;
    }
    // Accept method for visitor pattern
    @Override
    public void accept(Visitor visitor){
        visitor.visit(this);
    }
}

// Class representing file properties (permissions, owner, group) Flyweight pattern
class FileProperties{
    String PermissionType;  // Type of permissions
    String owner;          // File owner
    String group;          // File group
    String typeOfFile;
    // Constructor for FileProperties
    public FileProperties(String type, String owner, String group, String typeOfFile){
        this.PermissionType = type;
        this.owner = owner;
        this.group = group;
        this.typeOfFile = typeOfFile;
    }
}

// Factory class for creating and managing file properties Flyweight pattern
class FilePropertiesFactory{
    // store different type of file properties
    private static ArrayList<FileProperties> filesProperties = new ArrayList<>();
    // Get existing properties or create new ones
    static FileProperties getProperties(String type, String owner, String group, String typeOfFile){
        // if file properties already exist  -> create a new one, otherwise use existed properties
        if (filesProperties.stream().noneMatch(s -> s.PermissionType.equals(type) && s.owner.equals(owner) && s.group.equals(group) && s.typeOfFile.equals(typeOfFile))){
            //crate new properties
            FileProperties fp = new FileProperties(type, owner, group, typeOfFile);
            // add to properties array
            filesProperties.add(fp);
            return fp;
        }
        // otherwise use existed properties
        return filesProperties.stream().filter(s -> s.PermissionType.equals(type) && s.owner.equals(owner) && s.group.equals(group) && s.typeOfFile.equals(typeOfFile)).findFirst().orElse(null);
    }
}

// Iterator class for depth-first traversal of the file system tree
//implements Iterator interface ... Iterator pattern
class IterationInDepth implements IteratorForTree{
    public Stack<Node> stack = new Stack<>();  // Stack for DFS
    ArrayList<Node> treeAsArray;              // Array representation of tree
    Node dir;                                 // Current directory -> is a root
    HashMap<Integer, Node> nodes;             // Map of all nodes (Parent id, and node)
    // Constructor
    public IterationInDepth(Directory dir){ // initially set the root into the stack
        this.dir = dir;
        stack.push(dir);
    }

    // Check if there are more nodes to visit
    @Override
    public boolean hasNextNode() {
        return !stack.isEmpty();
    }
    // Get the next node in the tree
    @Override
    public Node getNextNode() {
        if (hasNextNode()) { // if node exists -> return the node, otherwise return null
            Node iterator = stack.pop(); // take element from the stack
            if (iterator instanceof Directory ) { // if the element is Directory put all children in the stack
                List<Node> reversedChildren = new ArrayList<>(((Directory) iterator).children);
                Collections.reverse(reversedChildren);
                stack.addAll(reversedChildren);
            }
            return iterator; // return the node
        }
        return null;
    }
    // Generate enclosure string for tree visualization
    public String enclosure(Node node, HashMap<Integer, Node> nodes){
        //check if the node the last among children
        ArrayList<Boolean> indexOfEnclosure = new ArrayList<>();
        // if parent is 0 -> no indents
        if (node.parent == 0){
            return "";
        }
        // repressively check if the node the last among children
        Node additionalParent = node;
        // while parent is not 0 and parent exists in nodes
        while (additionalParent.parent != 0 && nodes.containsKey(additionalParent.parent)){
            additionalParent = nodes.get(additionalParent.parent); // get the parent of the node
            // get index of the node
            int index = ((Directory) nodes.get(additionalParent.parent)).children.indexOf(additionalParent);
            // is the index is last -> it is the last
            if (index == ((Directory) nodes.get(additionalParent.parent)).children.size() - 1){
                indexOfEnclosure.add(true);
            } else { // otherwise -> not the last
                indexOfEnclosure.add(false);
            }
        }
        //return string
        StringBuilder answer = new StringBuilder();
        //go throw the reversed array
        for (int i = indexOfEnclosure.size() - 1; i >= 0; i--) {
            if (indexOfEnclosure.get(i)){ // if the last
                answer.append("    ");
            } else { // if not the last
                answer.append("│   ");
            }
        }
        // returns indents
        return answer.toString();
    }

    // Draw the tree structure
    public void drawTreeIt(Directory dir, HashMap<Integer, Node> nodes, ArrayList<Node> nodesList){
        this.treeAsArray = nodesList; // array of nodes
        this.nodes = nodes; // (Parent Id + Node)
        while (hasNextNode()){ // while we have a node in the tree
            Node currentNode = getNextNode(); // get the node
            if (currentNode == dir){ // if the node is root -> print the dot
                System.out.println(".");
            }else if (currentNode instanceof File && last(currentNode, nodes)){ // if node is a file, and it is last among the children in directory
                if (((File) currentNode).getSize() % 1 == 0){ // if size without floating points
                    System.out.println(enclosure(currentNode, nodes) + "└── " + currentNode.name + "." + ((File) currentNode).getProperties().typeOfFile + " (" + (int)(((File) currentNode).getSize()) + "KB)");
                }else{ // if file has a floating point
                    System.out.println(enclosure(currentNode, nodes) + "└── " + currentNode.name + "." +  ((File) currentNode).getProperties().typeOfFile +  " (" + ((File) currentNode).getSize() + "KB)");
                }
                // if node is a file, and it is not the last among the children in directory
            }else if (currentNode instanceof File && !last(currentNode, nodes)){
                if (((File) currentNode).getSize() % 1 == 0){// if size without floating points
                    System.out.println(enclosure(currentNode, nodes) + "├── " + currentNode.name + "." +  ((File) currentNode).getProperties().typeOfFile +  " (" + (int)(((File) currentNode).getSize()) + "KB)");
                }else{// if file has a floating point
                    System.out.println(enclosure(currentNode, nodes) + "├── " + currentNode.name + "." +  ((File) currentNode).getProperties().typeOfFile + " (" + ((File) currentNode).getSize() + "KB)");
                }
                // if node is directory check only is it a last or not
            } else if (currentNode instanceof Directory){
                String dop = last(currentNode, nodes) ? "└── " : "├── ";
                System.out.println( enclosure(currentNode, nodes) + dop + currentNode.name);
            }
        }
    }
    // Check if a node is the last child of its parent
    public static boolean last(Node node, HashMap<Integer, Node> tree) {
        Node parent = tree.get(node.parent); // get the parent of the child
        if (parent instanceof File) return false; // if node is a file -> it cannot be the last
        Directory parentDir = (Directory) parent; // parent as a directory
        int nodeIndex = parentDir.children.indexOf(node); // get the index of the child
        if (nodeIndex == -1) return false;
        return nodeIndex == parentDir.children.size() - 1; // is the child is last -> return true, else return false
    }
}

// Interface for tree iteration...

/**
 * Interface for Flyweight pattern
 */
interface IteratorForTree {
    Node getNextNode(); // get the next node
    boolean hasNextNode(); // check is there exist node or not
}

/**
 * Interface Visitor for Visitor pattern
 */
interface Visitor{
    void visit(File file);
    void visit(Directory dir);
}

