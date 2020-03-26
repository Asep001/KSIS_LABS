package Server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedList;

class Story {

    final int GLOBAL_MESSAGE = 0;

    final int COUNT_OF_SAVE_MESSAGES = 100;

    private LinkedList<StoredMessage> story = new LinkedList<>();

    public void addStoryMessageHelper(StoredMessage message) {
        if (story.size() > COUNT_OF_SAVE_MESSAGES) {
            story.removeFirst();
        }
        story.add(message);
    }

    public void printStoryHelper(BufferedWriter writer,String source,String destination){
        int destinationId = Integer.parseInt(destination.trim());
        if(story.size() > 0) {
            try {
                for (StoredMessage item : story) {
                    if (destination.equals("0")&& item.destination == destinationId ){
                        writer.write(item+"\n");
                    }
                    else {
                        if (item.source.equals(source) && item.destination == destinationId ||
                                item.source.equals(destination) && item.destination == Integer.parseInt(source.trim())) {
                            writer.write(item + "\n");
                        }
                    }
                }
                writer.flush();
            } catch (IOException ignored) {}
        }
    }

    public void delStoryHelper(int idUserNumber){
        if(story.size() > 0) {
            story.removeIf(item -> (item.source.equals(Integer.toString(idUserNumber))&&item.destination!=GLOBAL_MESSAGE)
                    || item.destination == idUserNumber);
            for (StoredMessage item : story){
                if (Integer.parseInt(item.source)>idUserNumber||item.destination>idUserNumber){
                    item.source=Integer.toString(Integer.parseInt(item.source)-1);
                    if (item.destination!=0) item.destination--;
                }
            }
        }
    }
}