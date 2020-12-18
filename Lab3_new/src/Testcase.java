import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Testcase {
    private Testcase() {};
    public static void main(String[] args) throws FileNotFoundException, RemoteException {
        //int case_number = 0;
        ArrayList<String> urls = new ArrayList<String>();
        urls.add("rmi://localhost/server0");
        urls.add("rmi://localhost/server1");
        urls.add("rmi://localhost/server2");
        urls.add("rmi://localhost/server3");
        urls.add("rmi://localhost/server4");
        urls.add("rmi://localhost/server5");
        urls.add("rmi://localhost/server6");
        urls.add("rmi://localhost/server7");
        int case_number = 0;
        if (args.length > 0) {
            case_number = Integer.parseInt(args[0]);
        }
        if (case_number == 0) {
            testcase0(urls);
        } else {
            testcase1(urls);
        }
    }
    public static void testcase0(ArrayList<String>urls) throws FileNotFoundException, RemoteException {
        int mat[][] = parseGraph("E:\\IDEA\\Lab3_new\\inputs\\graph3");

        // Create and register nodes with lookup
        List<MSTInterface> nodes_test0=new ArrayList<MSTInterface>();
        for(int i=0;i<mat.length;i++){
            try {
                nodes_test0.add((MSTInterface) Naming.lookup(urls.get(i)));
            }catch (RemoteException | NotBoundException | MalformedURLException e) {
                e.printStackTrace();
            }
        }
        try{
            new Thread(new process_initiate(nodes_test0.get(0),urls)).start();

            Thread.sleep(20);
            new Thread(new process_initiate(nodes_test0.get(1),urls)).start();
            new Thread(new process_initiate(nodes_test0.get(2),urls)).start();
            new Thread(new process_initiate(nodes_test0.get(3),urls)).start();
            new Thread(new process_initiate(nodes_test0.get(4),urls)).start();
            new Thread(new process_initiate(nodes_test0.get(5),urls)).start();
            new Thread(new process_initiate(nodes_test0.get(6),urls)).start();
            new Thread(new process_initiate(nodes_test0.get(7),urls)).start();
            Thread.sleep(10000);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Create links
        for(int i=0;i<mat.length;i++){
            for(int j=0;j<mat.length;j++){
                if(mat[i][j]!=0){
                    try {
                        Edge edge = new Edge(mat[i][j],nodes_test0.get(i),nodes_test0.get(j));
                        nodes_test0.get(i).addlink(edge);

                        //create_edges(mat[i][j], nodes_test0.get(i).getID(), nodes_test0.get(j).getID(), nodes_test0.get(i), nodes_test0.get(j));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // wake up nodes
        nodes_test0.get(0).wakeup();
       /*
        try {
            for (MSTInterface n : nodes_test0) n.wakeup();
        } catch (Exception e) {
            System.out.println("Exception @wakeup");
            //System.out.println("Wakeup from tasecase");
            System.exit(1);
        }

        */


    }
    public static void testcase1(ArrayList<String>urls) throws FileNotFoundException {
        int mat[][] = parseGraph("E:\\IDEA\\Lab3_new\\inputs\\graph4");

        // Create and register nodes with lookup
        List<MSTInterface> nodes_test1= new ArrayList<MSTInterface>();
        for(int i=0;i<mat.length;i++){
            try {
                nodes_test1.add((MSTInterface) Naming.lookup(urls.get(i)));
            }catch (RemoteException | NotBoundException | MalformedURLException e) {
                e.printStackTrace();
            }
        }
        try{
            new Thread(new process_initiate(nodes_test1.get(0),urls)).start();
            Thread.sleep(20);
            new Thread(new process_initiate(nodes_test1.get(1),urls)).start();
            new Thread(new process_initiate(nodes_test1.get(2),urls)).start();
            new Thread(new process_initiate(nodes_test1.get(3),urls)).start();
            new Thread(new process_initiate(nodes_test1.get(4),urls)).start();
            new Thread(new process_initiate(nodes_test1.get(5),urls)).start();
            new Thread(new process_initiate(nodes_test1.get(6),urls)).start();
            new Thread(new process_initiate(nodes_test1.get(7),urls)).start();
            Thread.sleep(10000);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }


        // Create links
        for(int i=0;i<mat.length;i++){
            for(int j=0;j<mat.length;j++){
                if(mat[i][j]!=0){
                    //create_edges(mat[i][j],nodes_test1.get(i),nodes_test1.get(j));
                    try {
                        Edge edge = new Edge(mat[i][j],nodes_test1.get(i),nodes_test1.get(j));
                        nodes_test1.get(i).addlink(edge);
                        //create_edges(mat[i][j],nodes_test1.get(i).getID(),nodes_test1.get(j).getID(),nodes_test1.get(i),nodes_test1.get(j));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // wake up nodes
        try {
            for (MSTInterface n : nodes_test1) n.wakeup();
        } catch (Exception e) {
            System.out.println("Exception @wakeup");
            System.exit(1);
        }
    }
    //int weight, MSTInterface node1, MSTInterface node2
    /*
    public static void create_edges(int weight, int node1_ip, int node2_ip, MSTInterface node1, MSTInterface node2){
        Edge edge=new Edge(weight,node1_ip,node2_ip);
        try {
            node1.addlink(edge);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
   /*
        try {
            node2.addlink(edge);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    */


    private static int[][] parseGraph(String filename) throws FileNotFoundException {

        // Find matrix dimension
        int n = new Scanner(new File(filename)).nextLine().split(" ").length;
        int[][] matrix = new int[n][n];

        // Parse integers into array
        Scanner s = new Scanner(new File(filename));
        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) {
                matrix[i][j] = s.nextInt();
            }
        }
        return matrix;
    }
}
