package com.espressif.esp8266_iot.udp;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.http.util.ExceptionUtils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


/*
 * Linux command to send UDP:
 * #socat - UDP-DATAGRAM:192.168.1.255:11111,broadcast,sp=11111
 *
 * Programm
 * my = new UDPListenerService();
 * my.onStartCommand(getIntent(),0,0);

 */
public class UDPListenerService extends Service {
    private static final String TAG = "UDPListenerService";
    static String UDP_BROADCAST = "UDPBroadcast";

    //Boolean shouldListenForUDPBroadcast = false;
    DatagramSocket socket;

    private void listenAndWaitAndThrowIntent(InetAddress broadcastIP, Integer port) throws Exception {
        byte[] recvBuf = new byte[15000];
        if (socket == null || socket.isClosed()) {
            socket = new DatagramSocket(port, broadcastIP);
            socket.setBroadcast(true);
        }
        //socket.setSoTimeout(1000);
        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
        Log.d(TAG, "Waiting for UDP broadcast");
        socket.receive(packet);

        String senderIP = packet.getAddress().getHostAddress();
        String message = new String(packet.getData()).trim();

        Log.d(TAG, "Got UDB broadcast from " + senderIP + ", message: " + message);

        broadcastIntent(senderIP, message);
        socket.close();
    }

    private void broadcastIntent(String senderIP, String message) {
        /*
        Intent intent = new Intent(UDPListenerService.UDP_BROADCAST);
        intent.putExtra("sender", senderIP);
        intent.putExtra("message", message);
        sendBroadcast(intent);
        */
    }

    Thread UDPBroadcastThread;

    void startListenForUDPBroadcast() {
        UDPBroadcastThread = new Thread(new Runnable() {
            public void run() {
                try {
                    InetAddress broadcastIP = InetAddress.getByName("192.168.178.255"); //172.16.238.42 //192.168.1.255
                    Integer port = 18266;
                    while (shouldRestartSocketListen) {
                        listenAndWaitAndThrowIntent(broadcastIP, port);
                    }
                    //if (!shouldListenForUDPBroadcast) throw new ThreadDeath();
                } catch (Exception e) {
                    Log.e(TAG, "no longer listening for UDP broadcasts cause of error " + e.getMessage());
                }
            }
        });
        UDPBroadcastThread.start();
    }

    private Boolean shouldRestartSocketListen=true;

    void stopListen() {
        Log.e(TAG, "stopListen");
        shouldRestartSocketListen = false;
        socket.close();
    }

    @Override
    public void onCreate() {

    };

    @Override
    public void onDestroy() {
        stopListen();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        shouldRestartSocketListen = true;
        startListenForUDPBroadcast();
        Log.d(TAG, "Service started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}