import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

enum SN {
    FOUND, FIND, SLEEPING
}

public class MSTnodeimpl extends UnicastRemoteObject implements MSTInterface{
    public int id;
    public SN SN_state;
    //public SE SE_state;
    public int LN;
    public int FN;
    public ArrayList<String> urls;
    public double best_weight; // weight of current candidate MOE
    public int find_count;                  // number of report messages expected

    public List<Edge> edges;
    public Edge best_edge;  // local direction of candidate MOE
    private Edge in_branch; // edge towards core (sense of direction)  //private
    private Edge test_edge; // edge checked whether other end in same fragment //private
    public int node_number;
    public BlockingQueue<QueueItem> rxQueue;
    public Queue<QueueItem> queue;
    public List<MSTInterface> target_node=new ArrayList<>();

    public MSTnodeimpl(int id, ArrayList<String> urls, int node_number) throws RemoteException {
        this.urls=urls;
        this.id = id;
        this.SN_state = SN.SLEEPING;
        this.LN = 0;
        this.FN = -1;
        this.best_weight =  Integer.MAX_VALUE;
        this.find_count = 0;
        this.edges = new ArrayList<Edge>();
        this.best_edge = null;
        this.in_branch = null;
        this.test_edge = null;
        this.rxQueue = new LinkedBlockingQueue<QueueItem>();  // MESSAGE RECEIVING FIFO
        this.queue = new LinkedList<QueueItem>();
        this.node_number=node_number;

    }



    public int getID(){
        return id;
    }
    public synchronized void wakeup(){
        if(this.SN_state!=SN.SLEEPING) return;
        int temp = Integer.MAX_VALUE;
        Edge templink = null;

        for (Edge edge:edges){
            if(edge.getweight()<temp){
                //edge.SE_state=SE.in_MST;
                temp=edge.weight;
                templink=edge;
            }
        }
        this.LN=0;
        this.SN_state=SN.FOUND;  //FOUND
        System.out.println("awake");
        this.find_count=0;
        templink.SE_state=SE.in_MST;
        send_message(templink, new Message(Type.connect, this.LN, this.FN, this.SN_state, best_weight));

    }
    public void addlink(Edge edge){
        this.edges.add(edge);
    }
    public void execute(Edge edge, Message message) throws RemoteException {
        switch(message.type){
            case connect:
                receive_connect(edge, message);
                break;

            case initiate:
                receive_initiate(edge,message);
                break;

            case test:
                receive_test(edge,message);
                break;

            case accept:
                receive_accept(edge, message);
                break;

            case reject:
                receive_reject(edge, message);
                break;

            case report:
                receive_report(edge, message);
                break;

            case changeroot:
                receive_change_root();
                break;
            default:
                break;

        }
    }
    public synchronized void receive_connect(Edge edge, Message message) throws RemoteException {
        System.out.println("Connecting to " + edge.get_destination_id(this.id));
        if(this.SN_state==SN.SLEEPING)wakeup();
        if(message.LN<this.LN){
            System.out.println("Node "+id+" absorb link "+edge.getweight());
            edge.SE_state=SE.in_MST;
            send_message(edge, new Message(Type.initiate, this.LN, this.FN, this.SN_state, best_weight));
            if(this.SN_state==SN.FIND){
                this.find_count=this.find_count+1;
            }
        }
        else
            if(edge.SE_state==SE.dont_know){
                queue.add(new QueueItem(edge.node1.getID(),edge.node2.getID(),message));
            }
            else {
                send_message(edge, new Message(Type.initiate, this.LN + 1, edge.getweight(), SN.FIND, best_weight));
               // this.in_branch=edge;//make sure in_branch has been modified
                System.out.println("send initiate on"+edge.weight);
                System.out.println("Node"+id+" merge link"+edge.weight);
            }
    }
    public synchronized void send_message(Edge edge, Message message){
        if(message==null){
            System.out.println("message is null");
        }
        if(edge==null){
            System.out.println("Edge is null");
        }
        System.out.println("Sending"+ message.type+"message to"+ edge.get_destination_id(this.id));
        try {
            //edge.get_destination_id(this.id)
            System.out.println(edge.get_destination_id(this.id));
            int destid =edge.get_destination_id(this.id).getID();
            System.out.println(destid);
            target_node.get(destid).receive_message(edge,message);
            //edge.get_destination_id(this.id).receive_message(edge,message);
            //target_node
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public synchronized void receive_initiate(Edge edge, Message message){
        //System.out.println("Initial message received from "+edge.get_destination_id(this.id));
        System.out.println(message.SN_state);
        this.LN= message.LN;
        this.FN= message.FN;

        this.SN_state=message.SN_state;
        //System.out.println(this.SN_state);
        this.in_branch=edge;
        this.best_edge=null;
        this.best_weight=Integer.MAX_VALUE;
        /*
        for(Edge adjacent_edge:edges){
            if(adjacent_edge.SE_state==SE.dont_know&&adjacent_edge.weight==message.weight){
                adjacent_edge.SE_state=SE.in_MST;
            }
        }

         */
        /*
        System.out.println("----------------------");
        System.out.println(edge.SE_state);
        System.out.println("----------------------");
        System.out.println(this.in_branch.getweight());
        for(Edge adjacent_edge:edges){
            System.out.println(adjacent_edge.getweight());
        }
        System.out.println("$$$$$$$$$$$$$$$$$$");

         */
        for(Edge adjacent_edge:edges){
            if(adjacent_edge.getweight()!=this.in_branch.getweight()&&adjacent_edge.SE_state==SE.in_MST){
                send_message(adjacent_edge, new Message(Type.initiate, this.LN, this.FN, this.SN_state,best_weight));
                if(this.SN_state==SN.FIND){
                    this.find_count=this.find_count+1;
                }
            }
        }
        if(this.SN_state==SN.FIND){
            test();
        }
    }
    public synchronized void test(){
        System.out.println("Testing");
       // best_weight=Integer.MAX_VALUE;
        int minimum_weight=Integer.MAX_VALUE;
        Edge candidate = null;
        /*
        for(Edge adjacent_edge:edges){
            System.out.println(adjacent_edge.id+"and"+adjacent_edge.getweight()+"and"+adjacent_edge.weight+"and"+adjacent_edge.node1_ip+"and"+adjacent_edge.node2_ip);
        }


         */

        /*
        for(Edge adjacent_edge:edges){
            System.out.println(adjacent_edge.SE_state);
        }

         */




        //System.out.println(edges);


        /*
        for(Edge adjacent_edge:edges){
            if(adjacent_edge.SE_state==SE.dont_know){
                System.out.println(adjacent_edge.id+"state is dont know");
            }
        }
        */

        for(Edge adjacent_edge:edges){
            if(adjacent_edge.SE_state==SE.dont_know && adjacent_edge.getweight()<minimum_weight){
                    //System.out.println("determining");
                    minimum_weight=adjacent_edge.getweight();
                    candidate=adjacent_edge;
                    //this.test_edge.weight=minimum_weight;
                }
            }
        //System.out.println(candidate);

        //this.test_edge=candidate;

        if(candidate==null){
            test_edge = null;
            report();
        }
        else{
            test_edge = candidate;
            //this.best_weight=this.test_edge.getweight();
            send_message(this.test_edge, new Message(Type.test, this.LN, this.FN, this.SN_state,this.best_weight));
            //System.out.println("Sending message");
        }
        //if(test_edge==null){
        //    System.out.println("test edge is null");
        //}
    }
    public synchronized void receive_test(Edge edge, Message message) throws RemoteException {
        System.out.println("Received test from"+edge.get_destination_id(this.id));
        /*
        System.out.println(message.FN+" "+this.FN);
        System.out.println(message.LN+" "+this.LN);
        System.out.println(edge.SE_state);

         */
        //System.out.println(test_edge+" "+test_edge.weight+" "+edge.weight);

        if(edge==null){
            System.out.println("edge is null");
        }
        if(test_edge==null){
            System.out.println("test edge is null");
        }
        System.out.println(message.FN+" "+message.LN+" ");
        if(this.SN_state==SN.SLEEPING){
            wakeup();
        }
        if(message.LN>this.LN){
            System.out.println("append!");
            queue.add(new QueueItem(edge.node1.getID(),edge.node2.getID(), message));
        }
        else if(message.FN!=this.FN){
               // System.out.println("a");
                send_message(edge, new Message(Type.accept, this.LN, this.FN, this.SN_state,best_weight));
        } else{
            if(edge.SE_state==SE.dont_know){
                //System.out.println("b");
                edge.SE_state=SE.not_in_MST;
                //send_message(edge,new Message(Type.reject, this.LN, this.FN, this.SN_state,best_weight));
            }
                //&&edge.getweight()!=this.test_edge.getweight()
                //if(test_edge==null){
                //    System.out.println("test edge is null");
                //}
                //edge!=null&&edge.getweight()!=test_edge.getweight()&&test_edge!=null
            if(test_edge==null){
                System.out.println("yes it is null");
            }
            if(test_edge != null || test_edge.getweight() != edge.getweight()){
                //System.out.println("c");
                System.out.println("sending rejectection");
                send_message(edge,new Message(Type.reject, this.LN, this.FN, this.SN_state,best_weight));

            }else{
                test();
            }
        }

    }
    public synchronized void receive_accept(Edge edge, Message message){
        System.out.println("Received acceptance from "+edge.get_destination_id(this.id));
        /*
        System.out.println("-------in receive acceptance-------");
        System.out.println("edge weight="+edge.getweight());
        System.out.println("its best weight is "+this.best_weight);

         */
        this.test_edge=null;
        //if(this.in_branch!=null){
            if(edge.getweight()<this.best_weight){
                this.best_weight=edge.getweight();
                this.best_edge=edge;
            }
            System.out.println("best edge is "+best_edge.id+", and its weight is"+best_edge.getweight());
            report();
        //}

    }
    public synchronized void receive_reject(Edge edge, Message message){
        int i=0;
        System.out.println("Rejection received from "+edge.get_destination_id(this.id));
        System.out.println(edge.SE_state+"edge for rejection");
        /*
        for(Edge adjacent_edge:edges){
            System.out.println(adjacent_edge.id+"and"+adjacent_edge.getweight()+"and"+adjacent_edge.weight+"and"+adjacent_edge.node1_ip+"and"+adjacent_edge.node2_ip);
        }

         */


        /*
        for(Edge adjacent_edge:edges){

            System.out.println(adjacent_edge.SE_state);
            i++;
            System.out.println(i);
        }

         */
        //int j=0;
        for(Edge adjacent_edge:edges){
            if(adjacent_edge.SE_state==SE.dont_know){
                adjacent_edge.SE_state=SE.not_in_MST;
                edge.SE_state=adjacent_edge.SE_state;
            }
        }

        /*
        for(Edge adjacent_edge:edges){

            System.out.println(adjacent_edge.SE_state);
            j++;
            System.out.println(i);
        }

         */
        /*
        if(edge.SE_state==SE.dont_know){

            edge.SE_state=SE.not_in_MST;
        }

         */
        //System.out.println("**********************");
        //System.out.println(edge.SE_state);
        /*
        for(Edge adjacent_edge:edges){
            System.out.println(adjacent_edge.SE_state);
        }

         */
        test();
    }
    public synchronized void report(){
        //System.out.println("Report to " +this.in_branch.get_destination_id(this.id));
        if(this.test_edge==null){
            System.out.println("Test edge is null");
        }
        if((this.find_count==0&&this.test_edge==null)){
            this.SN_state=SN.FOUND;
            send_message(in_branch, new Message(Type.report, this.LN, this.FN, this.SN_state,best_weight));
        }
    }

    public synchronized void receive_report(Edge edge, Message message) throws RemoteException {
        System.out.println("Received a report from " + edge.get_destination_id(this.id));
        //int edgeweight=edge.weight;
        //int in_branch_weight=in_branch.weight;
        /*
        System.out.println("edge weight="+edge.getweight());
        System.out.println("in_branch is"+in_branch.getweight());
        System.out.println("message weight="+message.weight);
        System.out.println(this.SN_state);
        System.out.println(best_weight);

         */

        if(edge.getweight()!=in_branch.getweight()){
            assert(find_count>0);
            find_count=find_count-1;
            if(message.weight<best_weight){
                this.best_weight=message.weight;
                this.best_edge=edge;
            }
            report();
        }
        else{
            if(this.SN_state==SN.FIND){
                queue.add(new QueueItem(edge.node1.getID(),edge.node2.getID(),message));
            }
            else{
                if(message.weight>this.best_weight){
                    change_root();
                }
                else{
                    if(message.weight==Integer.MAX_VALUE&&best_weight==Integer.MAX_VALUE){
                        System.out.println("Halt");
                        System.exit(0);
                    }
                }
            }
        }
    }
    public synchronized void change_root(){
        System.out.println("changing root");
        /*
        if(best_edge==null){
            System.out.println("Best edge is null");
        }

         */
        /*
        if(best_edge==null){
            System.out.println("Best edge is"+best_edge.id+", and its weight is "+best_edge.getweight());
        }

         */

        if(this.best_edge.SE_state==SE.in_MST){
            send_message(best_edge, new Message(Type.changeroot, LN, FN, SN_state,best_weight));
        }
        else{
            this.best_edge.SE_state=SE.in_MST;
            send_message(best_edge, new Message(Type.connect, LN, FN, SN_state,best_weight));

        }


    }
    public void receive_change_root(){
        change_root();
    }
    public void receive_message(Edge edge, Message message){
        System.out.println("A "+ message.type+"  message from " +edge.get_destination_id(this.id) +"has been received");
        try {
            System.out.println(edge.get_destination_id(this.id).getID());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        int size= queue.size();
        try {
            this.rxQueue.put(new QueueItem(edge.node1.getID(),edge.node2.getID(), message));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void executeMessage(Edge edge, Message message) throws RemoteException {
        //System.out.println("Executing");
        if(SN_state==SN.SLEEPING){
            wakeup();
        }
        execute(edge, message);
        check_queue();
    }
    public void check_queue() throws RemoteException {
        System.out.println("checking");
        int size=queue.size();
        //System.out.println(size);
        if(size!=0){
            for(int i=0;i<size;i++){
                QueueItem item=queue.remove();
                execute(weightToEdge(item.id1,item.id2),item.message1);
            }
        }
    }
    public void initiate(ArrayList<String> urls) {
        int j=0;
        for(int i=0;i<node_number;i++){
            j=this.createLookup(urls,j);
        }

        System.out.println("total ip number"+j);
        QueueItem rx = null;
        while (true) {
        //for (int i = 0; i < 5; i++) {
            try {
                rx = rxQueue.poll(7, TimeUnit.SECONDS);
                if (rx == null) {
                    System.out.println(this);
                    if (this.SN_state == SN.FIND) {
                        System.out.println("findmoe");
                        test();
                    }
                    continue;
                }

                    Edge dst = weightToEdge(rx.id1,rx.id2);
                    //System.out.println(rx.message1);
                    executeMessage(dst, rx.message1);

                    //System.out.println(rx.edge1);
                    //System.out.println("Initiated");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //}
    public String toString() {
        String string =  id + " " + SN_state + " fragmentID=" + FN + " fragmentLevel=" + LN ;
        return string;
    }
    public int createLookup(ArrayList<String>urls,int j){

        for(int i=0;i<node_number;i++){
            try {
                this.target_node.add((MSTInterface)Naming.lookup(urls.get(i)));
            } catch (NotBoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        for(int i=0;i<node_number;i++){

            j++;

        }
        return j;

        /*
        for(int i=0;i<node_number;i++){
            System.out.println(target_node.get(i));
        }
        */

        /*
        try{
            for(int i=0;i<this.node_number;i++){
                this.target_node.add((MSTInterface) Naming.lookup(urls.get(i)));

            }
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            e.printStackTrace();
        }
        */

    }

    public Edge weightToEdge(int id1,int id2){
        Edge dst = null;
        for (Edge e:this.edges){
            try {
                if((e.node1.getID() == id1&&e.node2.getID()==id2)||(e.node1.getID() == id2&&e.node2.getID()==id1)){
                    dst = e;
                }
            } catch (RemoteException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        //System.out.println(id1+" "+id2+" "+this.id+"xxxx");
        //System.out.println(id1+" "+id2+" "+dst.weight);
        return dst;
    }

}
