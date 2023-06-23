package important;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public abstract class MyHost {
    protected final int Window_size;
    protected final int Data_number;
    protected final int Timeout;
    protected String hostname;

    protected volatile int nextSeq=1;
    protected volatile int base=1;//线程安全
    protected InetAddress destAddress;
    protected int destPort=80;

    protected volatile int expectedSeq=1;

    protected DatagramSocket sendSocket;
    protected DatagramSocket receiveSocket;

    public MyHost(int receive_port,int window_size,int data_number,int timeout,String name)throws IOException {
        Window_size = window_size;
        Data_number=data_number;
        Timeout=timeout;
        hostname=name;

        sendSocket=new DatagramSocket();
        receiveSocket=new DatagramSocket(receive_port);
        destAddress=InetAddress.getLocalHost();

    }

    public InetAddress getDestAddress(){return destAddress;}
    public int getDestPort(){return destPort;}
    public void setDestAddress(InetAddress addr){destAddress=addr;}
    public void setDestPort(int dest){this.destPort=dest;}

    public abstract void sendData()throws IOException;
   // public abstract void timeOut()throws IOException;
    public abstract void receive()throws IOException;
    public abstract void receiveACK()throws IOException;
    protected void sendACK(int seq,InetAddress toaddr,int toport)throws IOException
    {
        String response=hostname+"response ACK:"+seq;
        byte[] responseData=response.getBytes();
        DatagramPacket responsePacket=new DatagramPacket(responseData,responseData.length,toaddr,toport);
        receiveSocket.send(responsePacket);
    }

    public String getHostname(){return hostname;}
    public int getBase(){return base;}
}
