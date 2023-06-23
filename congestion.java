package important;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.swing.Timer;

public class congestion {
    protected volatile int cwnd = 1;
    protected volatile int ssthresh=10;
    protected volatile int state = 1;
    protected volatile int[] dup;
    protected volatile int remembert = 0;
    protected volatile int nextseq = 1;
    protected volatile int base = 1;

    private volatile Timer[] timers = new Timer[100];
    private Set<Integer> receiverReceivedSet=new HashSet<>();

    protected int data_num;
    protected int timeout;
    protected String hostname;
    protected InetAddress destAddress;
    protected int destPort = 80;
    protected int expectedSeq = 1;

    protected DatagramSocket sendSocket;
    protected DatagramSocket receiveSocket;

    public congestion(int receiveport, int datanum, int time, int ss, String name) throws IOException {
        data_num = datanum;
        ssthresh = ss;
        timeout = time;
        hostname = name;
        sendSocket = new DatagramSocket();
        receiveSocket = new DatagramSocket(receiveport);
        destAddress = InetAddress.getLocalHost();
        dup = new int[datanum + 10];
    }

    public InetAddress getDestAddress() {return destAddress;}
    public int getDestPort() {return destPort;}
    public void setDestAddress(InetAddress addr) {destAddress = addr;}
    public void setDestPort(int dest) {this.destPort = dest;}
    public String getHostname() {return hostname;}
    public int getBase() {return base;}

    protected void sendACK(int seq, InetAddress toaddr, int toport) throws IOException {
        String response = hostname + "response ACK:" + seq;
        byte[] responseData = response.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, toaddr, toport);
        receiveSocket.send(responsePacket);
    }

    public void fast_resend(int ack)throws IOException
    {
        String sendData = hostname + "Send to port" + destPort + ",Seq=" + ack;
        byte[] data = sendData.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(data, data.length, destAddress, destPort);
        sendSocket.send(datagramPacket);
        System.out.println(hostname + " fast_resend to " + destPort + " Seq=" + ack);
        timers[remembert].stop();
        timers[ack]=new Timer(6000, new CDelayActionListener(sendSocket, timers, this, nextseq));
        timers[ack].start();
        remembert=ack;
    }

    public void receiveACK()throws IOException{
        int tag=0;
        while(true){
            if(cwnd<ssthresh)state=1;
            else state=2;
            byte[] bytes=new byte[4096];
            DatagramPacket datagramPacket=new DatagramPacket(bytes,bytes.length);
            sendSocket.receive(datagramPacket);
            String fromserver=new String(datagramPacket.getData(),0,datagramPacket.getLength());
            int ack=Integer.parseInt(fromserver.substring(fromserver.indexOf("ACK:")+4).trim());
            System.out.println(hostname+" get ACK:"+ack);
            if(dup[ack]==0)
            {
                if(state==1)
                {
                    cwnd+=1;
                    System.out.println("SS, cwnd="+cwnd+", ssthresh="+ssthresh);
                }
                else if(state==2)
                {
                    tag++;
                    if(tag==cwnd)
                    {
                        cwnd+=1;
                        System.out.println("CA, cwnd="+cwnd+", ssthresh="+ssthresh);
                        tag=0;
                    }else{System.out.println("CA");}
                }
            }
            dup[ack]++;
            if(dup[ack]==4)
            {
                state=2;
                ssthresh=cwnd/2;
                cwnd=ssthresh;
                fast_resend(ack);
                continue;
            }
            if(ack>base)
            {
                base=ack;
                if(base==nextseq)timers[remembert].stop();
                else
                {
                    timers[remembert].stop();
                    timers[base]=new Timer(6000,new CDelayActionListener(sendSocket,timers,this,base));
                    timers[base].start();
                    remembert=base;
                }
                if(ack==data_num+1)
                {
                    System.out.println("Finish!");
                    return;
                }
            }
        }
    }


    public void sendData() throws IOException {

        while (true)
        {
            while (nextseq < base + cwnd && nextseq <= data_num) {
                if (base == nextseq) {
                    timers[nextseq] = new Timer(6000, new CDelayActionListener(sendSocket, timers, this, nextseq));
                    timers[nextseq].start();
                    remembert = nextseq;
                }
                if (nextseq == 10 && state == 1) {
                    System.out.println(hostname + " Packet Loss! Seq=" + nextseq);
                    nextseq++;
                    continue;
                }
                String sendData = hostname + "Send to port" + destPort + ",Seq=" + nextseq;
                byte[] data = sendData.getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(data, data.length, destAddress, destPort);
                sendSocket.send(datagramPacket);
                System.out.println(hostname + " send to " + destPort + " Seq=" + nextseq);
                dup[nextseq] = 0;
                nextseq++;
                if (nextseq > data_num)
                {
                    nextseq = data_num;
                    return;
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    public void receive()throws IOException
    {
        while(true)
        {
            byte[] receivedData=new byte[4096];
            DatagramPacket datagramPacket=new DatagramPacket(receivedData,receivedData.length);
            receiveSocket.setSoTimeout(5000);
            try {
                receiveSocket.receive(datagramPacket);
            }catch (SocketTimeoutException e){
                System.out.println(hostname+" timed out waiting for the next packet");
                continue;
            }
            String received=new String(receivedData,0,datagramPacket.getLength());

            int seqIndex=received.indexOf("Seq=");
            if(seqIndex==-1)
            {
                System.out.println(hostname+" got the wrong data");
                sendACK(expectedSeq,datagramPacket.getAddress(),datagramPacket.getPort());
            }
            else
            {
                int got=Integer.parseInt(received.substring(seqIndex+4).trim());
                if(got==expectedSeq)
                {
                    System.out.println(hostname+" got the right data, Seq="+expectedSeq);
                    receiverReceivedSet.add(expectedSeq);
                    if(expectedSeq!=7)
                    {
                        while (receiverReceivedSet.contains(expectedSeq))expectedSeq++;
                        sendACK(expectedSeq,datagramPacket.getAddress(),datagramPacket.getPort());
                    }
                    else
                    {
                        System.out.println(hostname+" ACK LOSS :"+(expectedSeq+1));
                        while (receiverReceivedSet.contains(expectedSeq))expectedSeq++;
                    }
                }
                else if(got<expectedSeq)
                {
                    System.out.println(hostname+" hope the Seq="+expectedSeq+" but not receive");
                    //System.out.println(hostname+"未收到预期编号");
                    sendACK(expectedSeq,datagramPacket.getAddress(),datagramPacket.getPort());
                }
                else if(got>expectedSeq)
                {
                    System.out.println(hostname+" hope the Seq="+expectedSeq+" but not receive");
                    //System.out.println(hostname+"未收到预期编号");
                    sendACK(expectedSeq,datagramPacket.getAddress(),datagramPacket.getPort());
                    receiverReceivedSet.add(got);
                }

            }
        }
    }
}
