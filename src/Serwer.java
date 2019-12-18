import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Serwer
{
    //initialize socket and input stream 
    private Socket          socket   = null;

    private static boolean [] punkty ={false,false,false,false,false};
    private static AtomicInteger iluKlientow = new AtomicInteger(0);
    private static ArrayList<String> meta=new ArrayList<>();
    private static TreeMap<String,Semaphore> names = new TreeMap<>();

    private static ExecutorService pool;

    public static void main(String args[])
    {
        System.out.println("Serwer oczekuje na zawodnikow");
        pool = Executors.newFixedThreadPool(3);
        try (var listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
    static class Handler implements Runnable {
        private Socket socket;
        private String name;
        final Semaphore mutex;
        DataInputStream in;
        DataOutputStream out;

        protected Handler(Socket socket) {
            this.socket = socket;
            mutex = new Semaphore(1);
        }

        @Override
        public void run() {
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                name = in.readUTF();
                System.out.println("Watek serwera przyjal: " + name);
                synchronized (names) {
                    if (!name.isBlank() && !names.containsKey(name)) {
                        names.put(name, mutex);
                        out.writeUTF("Name accepted");
                        System.out.println("Watek serwera wypisal name accepted");
                    } else {
                        out.writeUTF("Name is used");
                        System.out.println("Watek serwera wypisal name is used");
                        return;
                    }
                }
                iluKlientow.addAndGet(1);
                System.out.println(name+" jest zarejestorwany");
                while (iluKlientow.get() <= 2) {
                    Thread.sleep(5);
                }
                System.out.println(name + " startuje");
                //while (mutex.tryAcquire()) ;
                out.writeUTF("Start!");
                for (int i = 0; i < 5; i++) {
                    in.readUTF();
                    synchronized (punkty) {
                        if (!punkty[i]) {
                            System.out.println(name + " wygrał lotny finisz nr " + i);
                            punkty[i] = true;
                        }
                    }
                }
                in.readUTF();
                synchronized (meta) {
                    if (meta.isEmpty())
                        System.out.println(name + " pierwszy na mecie!");
                    meta.add(name);
                    out.writeInt(meta.size());
                }
            } catch(Exception e)
            {
                e.printStackTrace();
            }
            finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (names) {
                    names.remove(name);
                }
                if (names.size() == 0) {
                    pool.shutdown();
                    System.exit(0);
                }
            }
        }
    }
} 