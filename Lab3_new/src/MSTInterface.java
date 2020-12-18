import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;


public interface MSTInterface extends Remote{
    public void wakeup() throws RemoteException;
    public void send_message(Edge edge, Message message) throws RemoteException, InterruptedException;
    public void receive_message(Edge edge, Message message) throws RemoteException,InterruptedException;

    public void addlink(Edge edge) throws RemoteException;
    public int getID() throws RemoteException;
    public void initiate(ArrayList<String> urls) throws RemoteException;
}
