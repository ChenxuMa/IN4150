import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;
public class Main {

    public static void main(String[] args){

        int server_number=0;
        if(args.length>0){
            server_number=Integer.parseInt(args[0]);
        }
        else{
            System.out.println("Please input process id number");
            System.exit(0);
        }
        int num_component=8;
        ArrayList<String> urls= new ArrayList<String>();

        urls.add("rmi://localhost/server0");
        urls.add("rmi://localhost/server1");
        urls.add("rmi://localhost/server2");
        urls.add("rmi://localhost/server3");
        urls.add("rmi://localhost/server4");
        urls.add("rmi://localhost/server5");
        urls.add("rmi://localhost/server6");
        urls.add("rmi://localhost/server7");
        try{
            LocateRegistry.createRegistry(Integer.parseInt(args[1]));
            MSTnodeimpl server= new MSTnodeimpl(server_number, urls, num_component);
            Naming.bind(urls.get(server_number),server);
            System.out.println("Server"+server_number+"ready");
        }catch(Exception e){
            System.err.println("Servers exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
