import java.net.*;
import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class Client
{
    // initialize socket and input output streams
    private Socket socket            = null;
    private DataInputStream  input   = null;
    private DataOutputStream output     = null;
    String route = "S             |          |          |                 |          |      M";
    String bike = "o&o";
    String name = "";

    // constructor to put ip address and port
    public Client(String address, int port, String imie) throws Exception
    {
        // establish a connection
        try
        {
            socket = new Socket(address, port);
            System.out.println("Connected");

            // takes input from terminal
            input  = new DataInputStream(socket.getInputStream());
            name = imie;

            // sends output to the socket
            output    = new DataOutputStream(socket.getOutputStream());
            try {
                output.writeUTF(imie);
            }
            catch (IOException ioio){
                ioio.printStackTrace();
            }
                String zwrot = "";
                zwrot = input.readUTF();

                System.out.println(zwrot);
                if(zwrot.compareTo("Name accepted")!=0){
                    throw new Exception();
                }

        }
        catch(UnknownHostException u)
        {
            System.out.println(u);
        }
        catch(IOException i)
        {
            System.out.println(i);
        }

    }

    protected void run() {
        try {
            System.out.println(route);
            Random random=new Random();
            int pozycja=0;
            String in = input.readUTF();
            System.out.println("Otrzymalem " + in);
            while (pozycja<route.length()-1){
                Thread.sleep(random.nextInt((150-30)+1)+30);
                System.out.print("\r"+".".repeat(pozycja)+bike);
                if(route.charAt(pozycja)=='|')
                    output.writeUTF("ju");
                pozycja++;
            }
            Thread.sleep(random.nextInt((150-30)+1)+30);
            output.writeUTF("meta");
            pozycja= input.readInt();
            System.out.println("\n"+name + " - nr "+pozycja+" na mecie");
            socket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void main(String args[])
    {
        Scanner scanner=new Scanner(System.in);
        System.out.println("Podaj nazwę kolarza:");
        String name = scanner.next();
        Client client = null;
        try {
            client = new Client("127.0.0.1", 59001, name);
            client.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}