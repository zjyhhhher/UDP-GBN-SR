package important;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DelayActionListener implements ActionListener
{
    DatagramSocket clientsocket;
    Timer[] timers;
    GBNhost host;
    public DelayActionListener(DatagramSocket clientsocket,Timer[] timers,GBNhost host)
    {
        this.clientsocket=clientsocket;
        this.timers=timers;
        this.host=host;
    }
    @Override
    public void actionPerformed(ActionEvent e)
    {
        int end=host.nextSeq-1;
        int end_ack=host.base;
        if(end==end_ack)return;
        System.out.println("TIMEOUT!!!");
        System.out.println(host.hostname+" Resend data:"+end_ack+"--"+end);
        for(int i=end_ack;i<=end;i++)
        {
            try
            {
                String resend=host.hostname+":Resend to port"+host.destPort+",Seq="+i;
                byte[] resenddata=resend.getBytes();
                DatagramPacket datagramPacket=new DatagramPacket(resenddata,resenddata.length,host.destAddress,host.destPort);
                host.sendSocket.send(datagramPacket);
            }catch (Exception e1){
                e1.printStackTrace();
            }
            System.out.println(host.hostname+" resend to"+host.destPort+",Seq="+i);
        }
        timers[end_ack].stop();
        timers[end_ack].start();
    }
}
