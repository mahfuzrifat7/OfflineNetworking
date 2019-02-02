package chatbox;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;


public class ChatBoxServer
{
    public static int workerThreadCount = 0;
    static ArrayList<String> usernames;
    static ArrayList<String> passwords;
    static ArrayList<String> types;
    static ArrayList<Boolean> loginStatus;
    static ArrayList<Integer> threadIDs;
    static ArrayList<WorkerThread> workerThreads;

    public static void main(String args[])
    {
        int id = 0;
        handleFile();
        workerThreads = new ArrayList<>();

        try
        {
            ServerSocket ss = new ServerSocket(5555);
            System.out.println("Server has been started successfully.");
            System.out.println("Waiting for Client");

            while(true)
            {
                Socket s = ss.accept();		//TCP Connection
                WorkerThread wt = new WorkerThread(s, id);
                workerThreads.add(wt);
                Thread t = new Thread(wt);
                t.start();
                workerThreadCount++;
                System.out.println("Client [" + id + "] is now connected. No. of worker threads = " + workerThreadCount);
                id++;
            }
        }
        catch(Exception e)
        {
            System.err.println("Problem in ServerSocket operation. Exiting main.");
        }
    }

    public static void handleFile() {

        File file;
        Scanner sc = null;
        int i=0;
        String temp;
        file = new File("E:\\PDFs & Study Materials\\Academics\\L1 T2\\CSE 108\\OfflineNetworking\\Experimental\\src\\chatbox\\info.txt");

        usernames = new ArrayList<>();
        passwords = new ArrayList<>();
        types = new ArrayList<>();
        loginStatus = new ArrayList<>();
        threadIDs = new ArrayList<>();

        try {
            sc = new Scanner(file);
        }
        catch (FileNotFoundException fnf)
        {
            System.out.println(fnf);
        }

        while (sc.hasNext())
        {
            try {
                temp = sc.next();
                usernames.add(i, temp);

                temp = sc.next();
                passwords.add(i, temp);

                temp = sc.next();
                types.add(i, temp);

                loginStatus.add(i, false);
                i++;

                threadIDs.add(-1);
            }
            catch (NullPointerException npe)
            {
                npe.printStackTrace();
            }
        }

    }
}

class WorkerThread implements Runnable
{
    private Socket socket;
    private InputStream is;
    private OutputStream os;

    BufferedReader br;
    PrintWriter pr;

    private int id;
    private String username;
    private boolean isLoggedIn;

    public WorkerThread(Socket s, int id)
    {
        this.socket = s;

        try
        {
            this.is = this.socket.getInputStream();
            this.os = this.socket.getOutputStream();
        }
        catch(Exception e)
        {
            System.err.println("Sorry. Cannot manage client [" + id + "] properly.");
        }

        this.id = id;
        this.isLoggedIn = false;
    }

    public boolean isLoggedIn()
    {
        return isLoggedIn;
    }
    public Socket getSocket() { return socket; }

    public void run()
    {
        br = new BufferedReader(new InputStreamReader(this.is));
        pr = new PrintWriter(this.os);

        pr.println("Your id is: " + this.id);
        pr.flush();

        String str;

        while(true)
        {
            try
            {
                if( (str = br.readLine()) != null )
                {
                    if (str.charAt(1) == 'L')
                    {
                        LMessage l = new LMessage(str.substring(3));

                        String info = l.loginInfo();

                        if(info.equals("Login Successful!"))
                        {
                            username = l.getUsername();
                            ChatBoxServer.threadIDs.set(ChatBoxServer.usernames.indexOf(username), id);
                            isLoggedIn = true;
                        }

                        pr.println(info);
                        pr.flush();
                    }
                    else if (str.charAt(1) == 'S')
                    {
                        if(isLoggedIn)
                        {
                            SMessage s = new SMessage(str.substring(3));

                            if (s.getCommand().equals("show"))
                            {
                                s.show(pr);

                                System.out.println(username + ": " + s.getText());
                            }
                            else if (s.getCommand().equals("logout"))
                            {
                                ///System.out.println(username + " [" + id + "] says: logout. Worker thread will terminate now.");

                                System.out.println(username + ": " + s.getText());
                                ChatBoxServer.loginStatus.set(ChatBoxServer.usernames.indexOf(username), false);
                                isLoggedIn = false;

                                pr.println("Log Out");
                                pr.flush();

                                break; // terminate the loop; it will terminate the thread also
                            }
                        }
                        else
                        {
                            pr.println("You are NOT Logged in!");
                            pr.flush();
                        }
                    }
                    else if (str.charAt(1) == 'B')
                    {
                        if(isLoggedIn)
                        {
                            if (ChatBoxServer.types.get(ChatBoxServer.usernames.indexOf(username)).equals("admin"))
                            {
                                BMessage b = new BMessage(str.substring(3));
                                b.cast(username, id);

                                pr.println("Message has been Broadcast Successfully!");
                                pr.flush();
                            }
                            else
                            {
                                pr.println("You are NOT an ADMIN!");
                                pr.flush();
                            }
                        }
                        else
                        {
                            pr.println("You are NOT Logged in!");
                            pr.flush();
                        }
                    }
                    else if (str.charAt(1) == 'C')
                    {
                        if(isLoggedIn)
                        {
                            CMessage c = new CMessage(str.substring(3));
                            pr.println(c.send(username, socket));
                            pr.flush();
                        }
                        else
                        {
                            pr.println("You are NOT Logged in!");
                            pr.flush();
                        }
                    }
                    /*else
                    {
                        System.out.println("[" + id + "] says: " + str);
                        pr.println("Got it. You sent \"" + str + "\"");
                        pr.flush();
                    }*/
                }
                else
                {
                    System.out.println("[" + id + "] terminated connection. Worker thread will terminate now.");
                    break;
                }
            }
            catch(Exception e)
            {
                System.err.println("Problem in communicating with the client [" + id + "]. Terminating worker thread.");
                break;
            }
        }

        try
        {
            this.is.close();
            this.os.close();
            this.socket.close();
        }
        catch(Exception e)
        {

        }

        ChatBoxServer.workerThreadCount--;
        System.out.println("Client [" + id + "] is now terminating. No. of worker threads = "
                + ChatBoxServer.workerThreadCount);
        try
        {
            ChatBoxServer.loginStatus.set(ChatBoxServer.usernames.indexOf(username), false);
        }
        catch (Exception e)
        {
            //IndexOutOfBound
        }
    }
}