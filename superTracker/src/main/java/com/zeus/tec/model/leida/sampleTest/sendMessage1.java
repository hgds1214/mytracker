package com.zeus.tec.model.leida.sampleTest;

import com.blankj.utilcode.util.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class sendMessage1 {

    public Socket SendSocket = null;
    //  public EndPoint ServiceAddress = null;
    public int OutTime = 0;
    public byte code = 0x00;
    DatagramPacket datagramPacket;
    DatagramSocket datagramSocket;
    String Server_ip;
    int Server_port;
    // int Local_port;

    public sendMessage1(String server_ip, int server_port, int local_port) throws SocketException {
        datagramSocket = new DatagramSocket(null);
        datagramSocket.setReuseAddress(true);
        datagramSocket.bind(new InetSocketAddress(2223));
        this.Server_port = server_port;
        this.Server_ip = server_ip;
    }

    public int Send(byte[] Message) throws IOException {
        int result = 0;
        datagramPacket = new DatagramPacket(Message, Message.length, InetAddress.getByName(Server_ip), Server_port);
        datagramSocket.send(datagramPacket);
        return result;
    }
}
