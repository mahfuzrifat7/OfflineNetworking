package chatbox;

public class BMessage {

    String text;

    public BMessage(String text) {
        this.text = text + '\n';
    }

    public void cast(String sender, int id) {

        for(int i=0;i<ChatBoxServer.workerThreads.size();i++)
        {
            if (i == id)
                continue;
            if ( ChatBoxServer.workerThreads.get(i).isLoggedIn() )
            {
                ChatBoxServer.workerThreads.get(i).pr.print(sender + ": " + text);
                ChatBoxServer.workerThreads.get(i).pr.flush();
            }
        }

    }
}
