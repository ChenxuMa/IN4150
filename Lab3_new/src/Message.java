import java.io.Serializable;

enum Type {initiate, test, accept, reject, report, changeroot, connect}


public class Message implements Serializable{
    Type type;
    int LN;
    int FN;
    SN SN_state;
    double weight;
    public Message(Type type, int LN, int FN, SN SN_state, double weight){
        this.type=type;
        this.LN=LN;
        this.FN=FN;
        this.SN_state=SN_state;
        this.weight=weight;
    }



}
