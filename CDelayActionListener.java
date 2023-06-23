package important;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class CDelayActionListener implements ActionListener
{
    DatagramSocket clientsocket;
    Timer[] timers;
    congestion host;
    int tag;
    public CDelayActionListener(DatagramSocket clientsocket,Timer[] timers,congestion host,int TAG)
    {
        this.clientsocket=clientsocket;
        this.timers=timers;
        this.host=host;
        this.tag=TAG;
    }
    @Override
    public void actionPerformed(ActionEvent e)
    {
        System.out.println("TIMEOUT!!!");
        System.out.println(host.hostname+" Resend data:"+tag);
        host.state=1;
        host.ssthresh=host.cwnd/2;
        host.cwnd=1;
        host.dup[tag]=0;
        try
        {
            String resend=host.hostname+":Resend to port"+host.destPort+",Seq="+tag;
            byte[] resenddata=resend.getBytes();
            DatagramPacket datagramPacket=new DatagramPacket(resenddata,resenddata.length,host.destAddress,host.destPort);
            host.sendSocket.send(datagramPacket);
        }catch (Exception e1){
            e1.printStackTrace();
        }
        timers[tag].stop();
        timers[tag].start();

    }
}
