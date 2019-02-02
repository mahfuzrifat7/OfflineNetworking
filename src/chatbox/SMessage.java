package chatbox;

import java.io.PrintWriter;

public class SMessage {

    String command;
    String text;

    public SMessage(String str) {

        String[] x = str.split("##");

        this.command = x[0];
        this.text = x[1];
    }

    public String getCommand() {
        return command;
    }

    public String getText() {
        return text;
    }

    public void show(PrintWriter pr) {

        int i, size = ChatBoxServer.usernames.size();

        String toShow = "Active Clients: ";

        for (i = 0; i < size; i++)
        {
            if (ChatBoxServer.loginStatus.get(i))
            {
                toShow += ChatBoxServer.usernames.get(i);
                break;
            }
        }

        for ( i++; i < size; i++)
        {
            if (ChatBoxServer.loginStatus.get(i))
                toShow += ", " + ChatBoxServer.usernames.get(i);
        }

        pr.println(toShow);
        pr.flush();

    }
}
