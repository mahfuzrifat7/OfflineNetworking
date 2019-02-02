package chatbox;

import java.io.*;
import java.net.Socket;

public class CMessage {

    String receiver;
    String text;
    String sendFile;

    public CMessage(String str) {

        String[] x = str.split("##");

        if (x.length == 2)
        {
            this.receiver = x[0];
            this.text = x[1];
            this.sendFile = "NONE";
        }
        else if (x.length == 3)
        {
            this.receiver = x[0];
            this.text = x[1];
            this.sendFile = x[2];
        }
    }

    public String send(String sender, Socket socket) {

        if (!ChatBoxServer.usernames.contains(receiver))
            return receiver + " does NOT Exist!";

        if (ChatBoxServer.loginStatus.get(ChatBoxServer.usernames.indexOf(receiver)))
        {
            int id = ChatBoxServer.threadIDs.get(ChatBoxServer.usernames.indexOf(receiver));

            ChatBoxServer.workerThreads.get(id).pr.println(sender + ": " + text);
            ChatBoxServer.workerThreads.get(id).pr.flush();

            if (!sendFile.equals("NONE"))
            {
                //File sending code
                try
                {
                    ChatBoxServer.workerThreads.get(id).pr.println("FILE");
                    ChatBoxServer.workerThreads.get(id).pr.flush();

                    ChatBoxServer.workerThreads.get(id).pr.println(sendFile);
                    ChatBoxServer.workerThreads.get(id).pr.flush();

                    InputStream is = socket.getInputStream();//
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    OutputStream os = ChatBoxServer.workerThreads.get(id).getSocket().getOutputStream();///

                    /*//---
                    FileOutputStream fos = new FileOutputStream("2"+sendFile);//
                    BufferedOutputStream bos = new BufferedOutputStream(fos);//
                    ///---*/

                    String len = br.readLine();//
                    int fileLength = Integer.parseInt(len);//

                    byte[] contents = new byte[10000];//

                    ChatBoxServer.workerThreads.get(id).pr.println(fileLength);
                    ChatBoxServer.workerThreads.get(id).pr.flush();

                    System.out.println("FileLength: "+fileLength);

                    int total = 0;//
                    int bytesRead = 0;

                    while (total != fileLength)//
                    {
                        bytesRead = is.read(contents);
                        total += bytesRead;
                        //os.write(contents, 0, bytesRead);
                        os.write(contents);

                        //bos.write(contents, 0, bytesRead);

                        System.out.println("BytesRead: "+bytesRead);
                        System.out.println("Total: "+total);
                    }
                    //bos.flush();//

                    //System.out.println("Server e File banano hoilo");


                    /*//----
                    File file = new File("2"+sendFile);///
                    FileInputStream fis = new FileInputStream(file);///
                    BufferedInputStream bis = new BufferedInputStream(fis);///
                    byte[] contents2;///
                    long current = 0;///

                    while(current!=fileLength){///
                        int size =10000;
                        if(fileLength-current>=size)
                            current+=size;
                        else {
                            size=(int)(fileLength-current);
                            current=fileLength;
                        }
                        contents2 = new byte[size];///
                        bis.read(contents2, 0, size);///
                        os.write(contents2);///
                    }
                    ///----*/

                    os.flush();
                    System.out.println("File sent succesfully2");

                }
                catch (Exception e)
                {
                    System.err.println("Could not transfer file.");
                }
            }

            //ChatBoxServer.workerThreads.get(id).pr.flush();

            return "Message Sent to " + receiver + "!";
        }

        return receiver + " is NOT Logged in!";

    }

}
