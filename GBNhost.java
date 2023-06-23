package important;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import javax.swing.Timer;

public class GBNhost extends MyHost {
    private volatile Timer[] timers=new Timer[30];
    protected volatile int rememberseq=1;

    public GBNhost(int REICEIVE_PORT,int WINDOW_SIZE,int DATA_NUMBER,int TIMEOUT,String name)throws IOException
    {
        super(REICEIVE_PORT,WINDOW_SIZE,DATA_NUMBER,TIMEOUT,name);
    }
    @Override
    public void receiveACK()throws IOException
    {
        while (true)
        {
            byte[] bytes=new byte[4096];
            DatagramPacket datagramPacket=new DatagramPacket(bytes,bytes.length);
            sendSocket.receive(datagramPacket);
            String fromserver=new String(datagramPacket.getData(),0,datagramPacket.getLength());
            int ack=Integer.parseInt(fromserver.substring(fromserver.indexOf("ACK:")+4).trim());
            System.out.println(hostname+" get ACK:"+ack);
            if(ack==Data_number)
            {
                System.out.println(hostname+" finish ");
                return;
            }
            if(ack>=base)
            {
                base=ack+1;
                if(base==nextSeq)timers[rememberseq].stop();
                else
                {
                    timers[rememberseq].stop();
                    timers[base]=new Timer(5000, new DelayActionListener(sendSocket, timers, this));
                    rememberseq=base;
                    timers[base].start();
                }
            }
        }
    }

    @Override
    public void sendData()throws IOException
    {
        while(true)
        {
            while(nextSeq<base+Window_size&&nextSeq<=Data_number)
            {
                if(base==nextSeq)
                {
                    timers[nextSeq] = new Timer(5000, new DelayActionListener(sendSocket, timers, this));
                    timers[nextSeq].start();
                    rememberseq = nextSeq;
                }
                if(nextSeq%5==0)
                {
                    System.out.println(hostname+" Packet Loss! Seq="+nextSeq);
                    nextSeq++;
                    continue;
                }
                String sendData=hostname+"Send to port"+destPort+",Seq="+nextSeq;
                byte[] data=sendData.getBytes();
                DatagramPacket datagramPacket=new DatagramPacket(data,data.length,destAddress,destPort);
                sendSocket.send(datagramPacket);
                System.out.println(hostname+" send to "+destPort+" Seq="+nextSeq);
                nextSeq++;
                if(nextSeq>Data_number)
                {
                    nextSeq=Data_number;
                    return;
                }
                try {
                    Thread.sleep(300);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
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
                sendACK(expectedSeq-1,datagramPacket.getAddress(),datagramPacket.getPort());
            }
            else
            {
                if(Integer.parseInt(received.substring(seqIndex+4).trim())==expectedSeq)
                {
                    if(expectedSeq%7!=0)
                        sendACK(expectedSeq,datagramPacket.getAddress(),datagramPacket.getPort());
                    else
                        System.out.println(hostname+" ACK LOSS :"+expectedSeq);
                    System.out.println(hostname+" got the right data, Seq="+expectedSeq);
                    expectedSeq++;
                }
                else
                {
                    System.out.println(hostname+" hope the Seq="+expectedSeq+" but not receive");
                    //System.out.println(hostname+"未收到预期编号");
                    sendACK(expectedSeq-1,datagramPacket.getAddress(),datagramPacket.getPort());
                }
            }
        }
    }

}
