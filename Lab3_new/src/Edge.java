import java.io.Serializable;
import java.rmi.RemoteException;

enum SE {
    dont_know, in_MST, not_in_MST
}
public class Edge implements Serializable {
    //MSTInterface node1;
    int weight;
    SE SE_state;
    int LN;
    int id;
    int node1_ip, node2_ip;//MSTnodeInterface node1,node2
    MSTInterface node1,node2;
    public Edge(int weight, MSTInterface node1_ip, MSTInterface node2_ip){
        this.weight=weight;
        this.node1=node1_ip;
        this.node2=node2_ip;
        this.SE_state=SE.dont_know;
    }
    public int getweight(){
        return this.weight;
    }
    public MSTInterface get_destination_id(int id) {
        int localID=-1;
        try {
            localID = node1.getID();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (localID == id) {
                //node2_ip
                return node2;
        } else {
            //node1_ip
                return node1;
        }
    }
}
