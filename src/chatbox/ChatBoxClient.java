package chatbox;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatBoxClient
{
    private static Socket s = null;
    private static BufferedReader br = null;
    private static PrintWriter pr = null;

    private static boolean isLoggedIn;

    public static void main(String args[])
    {
        try
        {
            s = new Socket("localhost", 5555);

            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            pr = new PrintWriter(s.getOutputStream());

            isLoggedIn = false;
        }
        catch(Exception e)
        {
            System.err.println("Problem in connecting with the server. Exiting main.");
            System.exit(1);
        }

        Scanner input = new Scanner(System.in);
        String  strRecv = null;

        try
        {
            strRecv = br.readLine();

            if(strRecv != null)
            {
                System.out.println("Server says: " + strRecv);
            }
            else
            {
                System.err.println("Error in reading from the socket. Exiting main.");
                cleanUp();
                System.exit(0);
            }
        }
        catch(Exception e)
        {
            System.err.println("Error in reading from the socket. Exiting main.");
            cleanUp();
            System.exit(0);
        }

        //sendMessage
        Thread sendMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {

                String strSend = null;

                while (true) {

                    // read the message to deliver.
                    try
                    {
                        strSend = input.nextLine();
                    }
                    catch(Exception e)
                    {
                        continue;
                    }

                    try
                    {
                        pr.println(strSend);
                        pr.flush();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    try
                    {
                        if(strSend.substring(0, 11).equals("#S#logout##") && isLoggedIn)
                        {
                            System.out.println("Client wishes to terminate the connection. Exiting main.");
                            isLoggedIn = false;
                            break;
                        }
                    }
                    catch (Exception e)
                    {
                        //Out of Bound Exception
                    }

                    if (strSend.charAt(1) == 'C' && strSend.substring(3).split("##").length == 3)
                    {

                        String[] x = strSend.substring(3).split("##");
                        String fileName = x[2];
                        ///System.out.println(fileName);

                        try
                        {
                            File file = new File(fileName);
                            FileInputStream fis = new FileInputStream(file);
                            BufferedInputStream bis = new BufferedInputStream(fis);
                            OutputStream os = s.getOutputStream();

                            byte[] contents;
                            long filelength = file.length();

                            pr.println(String.valueOf(filelength));
                            pr.flush();

                            long current = 0;

                            while (current != filelength)
                            {
                                int size = 10000;

                                if (filelength - current >= size)
                                    current += size;
                                else
                                {
                                    size = (int) (filelength - current);
                                    current = filelength;
                                }

                                contents = new byte[size];
                                bis.read(contents, 0, size);
                                os.write(contents);

                            }

                            os.flush();
                            System.out.println("File sent successfully!");

                        }
                        catch (Exception e)
                        {
                            System.err.println("Could not transfer file.");
                        }

                    }

                }

            }
        });

        //readMessage
        Thread readMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {

                String msg = null;

                while (true)
                {
                    try
                    {
                        // read the message sent to this client
                        msg = br.readLine();

                        if (msg.equals("Login Successful!"))
                            isLoggedIn = true;

                        if (msg.equals("Log Out"))
                            break;


                        if(msg.equals("FILE"))
                        {
                            try
                            {
                                ///System.out.println("Here");

                                String fileName = "1" + br.readLine();    ///-------
                                String strRecv2 = br.readLine();					//These two lines are used to determine
                                int filesize=Integer.parseInt(strRecv2);		//the size of the receiving file
                                byte[] contents = new byte[10000];

                                FileOutputStream fos = new FileOutputStream(fileName);
                                BufferedOutputStream bos = new BufferedOutputStream(fos);
                                InputStream is = s.getInputStream();

                                int bytesRead = 0;
                                int total=0;			//how many bytes read

                                while(total!=filesize)	//loop is continued until received byte=totalfilesize
                                {
                                    bytesRead=is.read(contents);
                                    total+=bytesRead;
                                    bos.write(contents, 0, bytesRead);

                                    ///System.out.println("BytesRead: "+bytesRead);
                                    ///System.out.println("Total: "+total);
                                }
                                bos.flush();
                                ///System.out.println("Here2");
                            }
                            catch(Exception e)
                            {
                                System.err.println("Could not transfer file.");
                            }

                        }


                        else if (msg != null)
                        {
                            System.out.println(msg);
                        }
                        else
                        {
                            System.err.println("Error in reading from the socket. Exiting main.");
                            break;
                        }

                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                }

                cleanUp();
            }
        });

        sendMessage.start();
        readMessage.start();
    }

    private static void cleanUp()
    {
        try
        {
            br.close();
            pr.close();
            s.close();
        }
        catch(Exception e)
        {

        }
    }
}