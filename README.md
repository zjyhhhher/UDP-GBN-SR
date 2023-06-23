# UDP-GBN-SR
基于 UDP 的 GBN 协议，SR 协议以及拥塞控制

本次Project实现了基于 UDP 的 GBN 协议，SR 协议以及拥塞控制三部分，共有 7 个主要的类。

其中 MyHost 是一个抽象类，SRHost 和 GBNHost 分别继承该类并重写了 receive()、sendData()、receiveACK()三个函数以实现选择重传和回退 N 步的具体细节。congestion是一个单独的类，基于 SRHost 和 GBNHost 做了一些改进，实现了拥塞控制。另外DelayActionListener、SDelayActionListener和CDelayActionListener三个类分别实现了GBN，SR 和拥塞控制超时的时候的动作。
