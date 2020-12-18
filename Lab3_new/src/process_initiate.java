import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class process_initiate implements Runnable{
    MSTInterface node;
    ArrayList<String>urls;
    public process_initiate(MSTInterface node, ArrayList<String>urls){
        this.node=node;
        this.urls=urls;
    }
    public void run(){
        try {
            //System.out.println("run");
            node.initiate(urls);

        } catch (RemoteException e) {
            e.printStackTrace();
        }



    }
}
