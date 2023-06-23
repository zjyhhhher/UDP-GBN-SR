package important;

import java.io.IOException;

public class main {
    private static int hostPort1=808;
    private static int hostPort2=809;

    public static void main(String args[])throws IOException
    {
        startGBN();
        //startSR();
        //startcontrol();
    }
    private static void startGBN()throws IOException
    {
        MyHost sender=new GBNhost(hostPort1,5,25,3,"sender");
        MyHost receiver=new GBNhost(hostPort2,5,25,3,"receiver");
        sender.setDestPort(hostPort2);
        receiver.setDestPort(hostPort1);

        new Thread(()->{
            try {
                receiver.receive();
            }catch (IOException e){
                e.printStackTrace();
            }
        }).start();
        new Thread(()->{
            try {
                sender.receiveACK();
            }catch (IOException e){
                e.printStackTrace();
            }
        }).start();
        new Thread(()->{
            try {
                sender.sendData();
            }catch (IOException e){
                e.printStackTrace();
            }
        }).start();
//        new Thread(()->{
//            try {
//                sender.receive();
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//        }).start();
//        new Thread(()->{
//            try {
//                receiver.sendData();
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//        }).start();
//        new Thread(()->{
//            try {
//                receiver.receiveACK();
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//        }).start();
    }
    private static void startSR()throws IOException
    {
        MyHost sender=new SRHost(hostPort1,5,25,3,"sender");
        MyHost receiver=new SRHost(hostPort2,5,25,3,"receiver");
        sender.setDestPort(hostPort2);
        receiver.setDestPort(hostPort1);

        new Thread(()->{
            try {
                receiver.receive();
            }catch (IOException e){
                e.printStackTrace();
            }
        }).start();
        new Thread(()->{
            try {
                sender.sendData();
            }catch (IOException e){
                e.printStackTrace();
            }
        }).start();
        new Thread(()->{
            try {
                sender.receiveACK();
            }catch (IOException e){
                e.printStackTrace();
            }
        }).start();

//        new Thread(()->{
//            try {
//                receiver.sendData();
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//        }).start();
//        new Thread(()->{
//            try {
//                sender.receive();
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//        }).start();
//        new Thread(()->{
//            try {
//                receiver.receiveACK();
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//        }).start();
    }

    public static void startcontrol()throws IOException
    {
        congestion sender=new congestion(hostPort1,25,3,10,"sender");
        congestion receiver=new congestion(hostPort2,25,3,10,"receiver");
        sender.setDestPort(hostPort2);
        receiver.setDestPort(hostPort1);

        new Thread(()->{
            try {
                receiver.receive();
            }catch (IOException e){
                e.printStackTrace();
            }
        }).start();

        new Thread(()->{
            try {
                sender.receiveACK();
            }catch (IOException e){
                e.printStackTrace();
            }
        }).start();

        new Thread(()->{
            try {
                sender.sendData();
            }catch (IOException e){
                e.printStackTrace();
            }
        }).start();

//        new Thread(()->{
//            try {
//                receiver.sendData();
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//        }).start();
//        new Thread(()->{
//            try {
//                sender.receive();
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//        }).start();
//        new Thread(()->{
//            try {
//                receiver.receiveACK();
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//        }).start();
    }
}
