
import java.util.*;
import java.util.Random;

public class Cluster {


    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static final String[] ANSI_ARR = {ANSI_RED, ANSI_GREEN, ANSI_YELLOW, ANSI_BLUE, ANSI_PURPLE, ANSI_CYAN};

    int w = 0;
    int h = 0;

    int[][] matrix = null;

    public Cluster (int[][] matrix) {
        this.matrix = matrix;
        h = matrix.length;
        w = matrix[0].length;
    }

    public static int[][] genClusterMatrix (int h, int w, float percentFilled) {
        int[][] clusterMatrix = new int[h+2][w+2];
        Random rand = new Random();
        
        for (int i = 2; i < h; i++) {
            for (int j = 2; j < w; j++) {
                float randomNum = rand.nextFloat(1);
                if (randomNum <= percentFilled) {
                    clusterMatrix[i][j] = 1;

                    int north = clusterMatrix[i-1][j];
                    int south = clusterMatrix[i+1][j];
                    int west = clusterMatrix[i][j-1];
                    int east = clusterMatrix[i][j+1];

                    if (north == 0) {clusterMatrix[i-1][j] = 2;}
                    if (south == 0) {clusterMatrix[i+1][j] = 2;}
                    if (west == 0) {clusterMatrix[i][j-1] = 2;}
                    if (east == 0) {clusterMatrix[i][j+1] = 2;}

                }
            }
        }

        return clusterMatrix;
    }

    public static void printCluster (int[][] cluster) {
        for(int[] i : cluster) {
            for (int j : i) {
                if (j == 2) {System.out.print(ANSI_RED + j + ANSI_RESET);}
                else if (j == 1) {System.out.print(ANSI_WHITE + "0" + j + ANSI_RESET);}
                else if (j != 0) {
                    if (j < 10) {
                        System.out.print(ANSI_ARR[j % ANSI_ARR.length] + "0" + j + ANSI_RESET);
                    } else {
                        System.out.print(ANSI_ARR[j % ANSI_ARR.length] + j + ANSI_RESET);
                    }
                } else {System.out.print(ANSI_BLACK + j + j + ANSI_RESET);}
            }
            System.out.print("\n");
        }
    }

    public Tuple createClusters(int[][] clusterMatrix) {

        HashMap<Integer, ClusterGroup> hs = new HashMap<Integer, ClusterGroup>();
        boolean noFirstClusterExists = true;
        int numGroups = 2;

        int searchAreaWidth = 1;
        for (int i = 1; i < h; i++) {
            for (int j = 1; j < w; j++) {
                int currentBlock = clusterMatrix[i][j];
                if (currentBlock > 1) {

                    if (noFirstClusterExists) { //first block only
                        numGroups ++;
                        clusterMatrix[i][j] = numGroups;
                        currentBlock = numGroups;
                        ClusterGroup cg = new ClusterGroup(numGroups);
                        cg.addMember(i,j);
                        hs.put(numGroups, cg);
                        noFirstClusterExists = false;
                        System.out.println("STARTING");
                    }

                    System.out.println(ANSI_GREEN + "current block: " + currentBlock + ", " + i + ", " + j + ANSI_RESET);

                    //continue conditions:
                    if (currentBlock < 2) {
                         continue;
                    } else if (currentBlock != 2) {
                        ClusterGroup currentGroup = hs.get(currentBlock);
                        if (currentGroup.size() >= 12) {
                            System.out.println(ANSI_CYAN + "**SKIPPING** group size >= 12 current block: " + currentBlock + ", " + i + ", " + j + ANSI_RESET);
                            continue;
                        }
                    }

                    int iiStart = 0;
                    if (i - searchAreaWidth > 0) {
                        iiStart = i - searchAreaWidth;
                    }

                    int iiEnd = i + searchAreaWidth;
                    if (iiEnd > h - 1) {
                        iiStart = i + searchAreaWidth;
                    }

                    int jjStart = 0;
                    if (j - searchAreaWidth > 0) {
                        jjStart = j - searchAreaWidth;
                    }

                    int jjEnd = j + searchAreaWidth;
                    if (jjEnd > w - 1) {
                        jjStart = j + searchAreaWidth;
                    }
                    for (int ii = iiStart; (ii <= iiEnd); ii++) {
                        for (int jj = jjStart; jj <= jjEnd; jj++) {
                            int currentAreaBlock = clusterMatrix[ii][jj];
                            System.out.println(ANSI_YELLOW + "looking at:  " + currentAreaBlock + ", " + ii + ", " + jj  + ANSI_RESET);

                            if (currentBlock != 2 && currentAreaBlock == currentBlock) {
                                continue;
                            } else if (currentAreaBlock < 2) {
                                continue;
                            } else if (currentBlock == 2) {
                                System.out.println(ANSI_RED + "looking at:  " + currentAreaBlock + ", " + ii + ", " + jj  + ANSI_RESET);

                                BFS bfs = new BFS();

                                List<int[]> path = bfs.findPath(clusterMatrix, i, j, ii, jj, currentBlock);

                                if (path.size() <= 12 && path.size() > 0) {
                                    numGroups ++;
                                    clusterMatrix[i][j] = numGroups;
                                    clusterMatrix[ii][jj] = numGroups;
                                    currentBlock = numGroups;

                                    ClusterGroup cg = new ClusterGroup(numGroups);
                                    cg.addMember(i,j);
                                    cg.addMember(ii,jj);

                                    for (int[] block : path) {
                                        int blockX = block[0];
                                        int blockY = block[1];

                                        System.out.println(ANSI_PURPLE + "looking at:  " + clusterMatrix[blockX][blockY] + ", " + blockX + ", " + blockY  + ANSI_RESET);

                                        cg.addMember(blockX, blockY);
                                        clusterMatrix[blockX][blockY] = currentBlock;
                                    }
                                    hs.put(numGroups, cg);
                                }

                            } else {
                                System.out.println(ANSI_RED + "looking at:  " + currentAreaBlock + ", " + ii + ", " + jj  + ANSI_RESET);

                                ClusterGroup currentGroup = hs.get(currentBlock);
                                if (currentGroup.size() < 12) {

                                    BFS bfs = new BFS();

                                    List<int[]> path = bfs.findPath(clusterMatrix, i, j, ii, jj, currentBlock);

                                    System.out.println(path.size());

                                    if (path.size() + currentGroup.size() <= 12) {
                                        System.out.println("x");
                                        for (int[] block : path) {
                                            
                                            int blockX = block[0];
                                            int blockY = block[1];

                                            System.out.println(ANSI_BLUE + "looking at:  " + clusterMatrix[blockX][blockY] + ", " + blockX + ", " + blockY  + ANSI_RESET);

                                            currentGroup.addMember(blockX, blockY);
                                            clusterMatrix[blockX][blockY] = currentBlock;
                                        }
                                    }

                                    
                                }
                            }

                        }
                        
                    }

                    
                }


            }
        }

        return new Tuple(clusterMatrix, hs);
    }

    public int[][] mergeClusters(int[][] clusterMatrix, HashMap<Integer, Cluster.ClusterGroup> hs) {
        for (int searchAreaWidth = 1; searchAreaWidth < 12; searchAreaWidth++) {
            for (int i = 1; i < h; i++) {
                for (int j = 1; j < w; j++) {
                    int currentBlock = clusterMatrix[i][j];
                    if (currentBlock > 1) {
                        int currentSize = hs.get(currentBlock).size();
                        if (currentSize < 12) {
                            //System.out.println(ANSI_GREEN + "current block: " + currentBlock + ", " + i + ", " + j + ANSI_RESET);
                            //System.out.println(ANSI_CYAN + "**SKIPPING** group size >= 12 current block: " + currentBlock + ", " + i + ", " + j + ANSI_RESET);
                            

                            int iiStart = 0;
                            if (i - searchAreaWidth > 0) {
                                iiStart = i - searchAreaWidth;
                            }

                            int iiEnd = i + searchAreaWidth;
                            if (iiEnd > h - 1) {
                                iiEnd = h - 1;
                            }

                            int jjStart = 0;
                            if (j - searchAreaWidth > 0) {
                                jjStart = j - searchAreaWidth;
                            }

                            int jjEnd = j + searchAreaWidth;
                            if (jjEnd > w - 1) {
                                jjEnd = w - 1;
                            }
                            //System.out.println("jj: " + jjEnd + " ii: " + iiEnd);
                            for (int ii = iiStart; (ii <= iiEnd); ii++) {
                                for (int jj = jjStart; jj <= jjEnd; jj++) {

                                    if (j == jj && i == ii) {continue;}

                                    int currentAreaBlock = clusterMatrix[ii][jj];
                                    if (currentAreaBlock > 1) {
                                        int currentAreaSize = hs.get(currentAreaBlock).size();
                                        if (currentAreaSize + currentSize <= 12) {
                                            //System.out.println(ANSI_YELLOW + "looking at:  " + currentAreaBlock + ", " + ii + ", " + jj  + ANSI_RESET);

                                            BFS bfs = new BFS();
                                            List<int[]> path = bfs.findPath(clusterMatrix, i, j, ii, jj, currentBlock);

                                            if (path.size() + currentAreaSize + currentSize <= 12 && path.size() > 0) {
                                                
                                                System.out.println("combining... to size: " + (path.size() + currentAreaSize + currentSize));
                                                System.out.println(ANSI_GREEN + "current block: " + currentBlock + ", " + i + ", " + j + ANSI_RESET);
                                                ClusterGroup currentGroup = hs.get(currentBlock);
                                                for (int[] block : path) {
                                                    
                                                    int blockX = block[0];
                                                    int blockY = block[1];
        
                                                    System.out.println(ANSI_BLUE + "looking at:  " + clusterMatrix[blockX][blockY] + ", " + blockX + ", " + blockY  + ANSI_RESET);

                                                    currentGroup.addMember(blockX, blockY);
                                                    clusterMatrix[blockX][blockY] = currentBlock;
                                                }

                                                ClusterGroup otherGroup = hs.get(currentAreaBlock);

                                                for (String str : otherGroup.members) {
                                                    System.out.println(str);
                                                    String[] spltStr = str.split(",");
                                                    clusterMatrix[Integer.parseInt(spltStr[0])][Integer.parseInt(spltStr[1])] = currentBlock;
                                                }

                                                currentGroup.Union(otherGroup);
                                                hs.remove(otherGroup);
                                                hs.put(currentBlock, currentGroup);

                                            }

                                        }
                                    }

                                }
                            }


                        }
                    }
                }
            }
        }
        

        return clusterMatrix;
    }

    public static void main(String[] args) {
        int[][] arr = {{1,2,3},
                        {1,2,3}};
        System.out.println(arr.length);
        

        int[][] clusterMatrix = Cluster.genClusterMatrix(12, 14, 0.3f);

        Cluster cluster = new Cluster(clusterMatrix);

        Tuple tuple = cluster.createClusters(clusterMatrix);

        System.out.println("------------");
        Cluster.printCluster(tuple.a);

        int[][] mergedCluster = cluster.mergeClusters(tuple.a, tuple.b);

        Cluster.printCluster(mergedCluster);
    }

    public class ClusterGroup {
        HashSet<String> members = new HashSet<String>();
        int id;

        public ClusterGroup(int id) {
            this.id = id;
        }

        public void addMember(int x, int y) {
            String element = x + "," + y;
            members.add(element);
        }

        public int size() {
            return members.size();
        }

        public void Union(ClusterGroup cg) {
            members.addAll(cg.members);
        }

    }

    public class Tuple {
        public final int[][] a;
        public final HashMap<Integer, ClusterGroup> b;
    
        public Tuple(int[][] a, HashMap<Integer, ClusterGroup> b) {
            this.a = a;
            this.b = b;
        }
    }
     
    class BFS
    {

        // A queue node used in BFS
        class Node
        {
            // (x, y) represents coordinates of a cell in the matrix
            int x, y;
        
            // maintain a parent node for printing the final path
            Node parent;
        
            public Node(int x, int y, Node parent)
            {
                this.x = x;
                this.y = y;
                this.parent = parent;
            }
        
            @Override
            public String toString() {
                return "(" + x + ", " + y + ')';
            }
        }

        // Below arrays detail all four possible movements from a cell
        private static int[] row = { -1, 0, 0, 1 };
        private static int[] col = { 0, -1, 1, 0 };
     
        // The function returns false if (x, y) is not a valid position
        private static boolean isValid(int x, int y, int[][] matrix, int group) {
            return ((x >= 0 && x < matrix.length) && (y >= 0 && y < matrix[0].length)   &&  ((matrix[x][y] == 0 || matrix[x][y] == group) || matrix[x][y] == 2));
        }
     
        // Utility function to find path from source to destination
        private static void findPath(Node node, List<int[]> path)
        {
            if (node != null) {
                findPath(node.parent, path);
                int[] xy = {node.x, node.y};
                path.add(xy);
            }
        }

        public BFS(){}
     
        // Find the shortest route in a matrix from source cell (x, y) to
        // destination cell (N-1, N-1)
        public List<int[]> findPath(int[][] matrix, int x, int y, int x2, int y2, int group)
        {
    
    
    
            // list to store shortest path
            List<int[]> path = new ArrayList<>();
     
            // base case
            if (matrix == null || matrix.length == 0) {
                return path;
            }
     
            // create a queue and enqueue the first node
            Queue<Node> q = new ArrayDeque<>();
            Node src = new Node(x, y, null);
            q.add(src);
     
            // set to check if the matrix cell is visited before or not
            Set<String> visited = new HashSet<>();
     
            String key = src.x + "," + src.y;
            visited.add(key);
     
            // loop till queue is empty
            while (!q.isEmpty())
            {
                // dequeue front node and process it
                Node curr = q.poll();
                int i = curr.x, j = curr.y;
     
                // return if the destination is found
                if (i == x2 && j == y2) {
                    findPath(curr, path);
                    return path;
                }
     
                // check all four possible movements from the current cell
                // and recur for each valid movement
                for (int k = 0; k < row.length; k++)
                {
                    // get next position coordinates using the value of the current cell
                    x = i + row[k];
                    y = j + col[k];
     
                    // check if it is possible to go to the next position
                    // from the current position
                    if (isValid(x, y, matrix, group))
                    {
                        // construct the next cell node
                        Node next = new Node(x, y, curr);
     
                        key = next.x + "," + next.y;
     
                        // if it isn't visited yet
                        if (!visited.contains(key))
                        {
                            // enqueue it and mark it as visited
                            q.add(next);
                            visited.add(key);
                        }
                    }
                }
            }
     
            // we reach here if the path is not possible
            return path;
        }
    }

}



