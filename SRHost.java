package important;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;

public class SRHost extends MyHost{
    private Set<Integer> senderSentSet=new HashSet<>();
    private Set<Integer> senderReceivedACKSet=new HashSet<>();
    private Set<Integer> receiverReceivedSet=new HashSet<>();
    private volatile Timer[] timers=new Timer[30];

    public SRHost(int REICEIVE_PORT,int WINDOW_SIZE,int DATA_NUMBER,int TIMEOUT,String name)throws IOException
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
            System.out.println(hostname+" got the ACK:"+ack);
            senderReceivedACKSet.add(ack);
            if(ack==base)
            {
                while(senderReceivedACKSet.contains(base))base++;
                int max;
                max=((base + Window_size - 1)<=Data_number)?(base + Window_size - 1):Data_number;
                if(base<=Data_number)System.out.println("\n当前窗口base= " + base + "最大发送到 " + max + " \n");
            }
            timers[ack].stop();
            System.out.println(ack+"的计时器已关闭");
            if(base==Data_number+1)
            {
                System.out.println(hostname + "发送完毕，接收方反馈全部正确接收");
                return;
            }
        }
    }

    @Override
    public void sendData()throws IOException
    {
        while(true)
        {
            while(nextSeq<base+Window_size&&nextSeq<=Data_number&&!senderSentSet.contains(nextSeq))
            {
                timers[nextSeq]=new Timer(5000,new SDelayActionListener(sendSocket,timers,this,nextSeq));
                timers[nextSeq].start();
                System.out.println(nextSeq+"开始计时");
                if(nextSeq%5!=0)
                {
                    String sendData=hostname+"Send to port"+destPort+",Seq="+nextSeq;
                    byte[] data=sendData.getBytes();
                    DatagramPacket datagramPacket=new DatagramPacket(data,data.length,destAddress,destPort);
                    sendSocket.send(datagramPacket);

                    System.out.println(hostname+" send to"+destPort+",Seq="+nextSeq);
                }
                else
                {
                    System.out.println(hostname+" packet loss,Seq="+nextSeq);
                }
                senderSentSet.add(nextSeq);
                nextSeq++;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public void receive()throws IOException
    {
        int rcvBase=1;
        while(true)
        {
            byte[] receivedData=new byte[4096];
            DatagramPacket datagramPacket=new DatagramPacket(receivedData,receivedData.length);
            receiveSocket.setSoTimeout(5000);
            try {
                receiveSocket.receive(datagramPacket);
            }catch (SocketTimeoutException e){
                System.out.println(hostname+"等待下一个分组超时");
                continue;
            }
            String received=new String(receivedData,0,datagramPacket.getLength());

            int seqIndex=received.indexOf("Seq=");
            if(seqIndex==-1)
            {
                System.out.println(hostname+"收到错误数据");
                sendACK(expectedSeq-1,datagramPacket.getAddress(),datagramPacket.getPort());
            }else
            {
                int seq=Integer.parseInt(received.substring(seqIndex+4).trim());
                if(seq>=rcvBase&&seq<=rcvBase+Window_size-1)
                {
                    receiverReceivedSet.add(seq);
                    System.out.println(hostname+"收到一个窗口内的分组Seq= "+seq+"已确认");
                    if(seq%7!=0)
                    {
                        sendACK(seq,datagramPacket.getAddress(),datagramPacket.getPort());
                    }else
                    {
                        System.out.println(hostname+"假装丢失ACK："+seq);
                    }
                    if(seq==rcvBase)
                    {
                        while(receiverReceivedSet.contains(rcvBase))rcvBase++;
                    }
                }else if(seq>=rcvBase-Window_size&&seq<=rcvBase-1)
                {
                    System.out.println(hostname+"收到一个已经确认过的分组，Seq= "+seq+"已经再次确认");
                    sendACK(seq,datagramPacket.getAddress(),datagramPacket.getPort());
                }else
                {
                    System.out.println(hostname+"收到一个不在窗口内的分组，Seq="+seq+"已舍弃");
                }
            }
        }
    }
}
