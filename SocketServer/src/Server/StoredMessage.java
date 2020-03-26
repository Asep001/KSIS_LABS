package Server;

public class StoredMessage {
    public int typeOFMessage;
    public int destination;
    public String source;
    public String userWord;


    public StoredMessage(String typeOFMessage,String destination, String source,String userWord){
        this.destination=Integer.parseInt(destination);
        this.source=source;
        this.typeOFMessage=Integer.parseInt(typeOFMessage);
        this.userWord=userWord;
    }

    @Override
    public String toString() {
        return  typeOFMessage +
                " " + destination +
                " " + source +
                " " + userWord;
    }
}
