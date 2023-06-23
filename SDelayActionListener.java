package important;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class SDelayActionListener implements ActionListener
{
    DatagramSocket clientsocket;
    Timer[] timers;
    SRHost host;
    int tag;
    public SDelayActionListener(DatagramSocket clientsocket,Timer[] timers,SRHost host,int tag)
    {
        this.clientsocket=clientsocket;
        this.timers=timers;
        this.host=host;
        this.tag=tag;
    }
    @Override
    public void actionPerformed(ActionEvent e)
    {
        System.out.println("TIMEOUT!!!");
        System.out.println(host.hostname+" Resend data:"+tag);
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
            //System.out.println(host.hostname+" resend to"+host.destPort+",Seq="+tag);
    }
}