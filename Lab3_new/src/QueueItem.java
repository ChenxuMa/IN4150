public class QueueItem {
    int id1;
    int id2;
    Edge edge1;
    Message message1;
    public QueueItem(int id1,int id2, Message message){
        this.id1 = id1;
        this.id2 = id2;
        this.message1=message;
    }
}
