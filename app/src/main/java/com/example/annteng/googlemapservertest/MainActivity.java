package com.example.annteng.googlemapservertest;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private String APP_TAG = "map_server";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onRegisterButtonClick(View view)
    {
        Log.d(APP_TAG, "onRegisterButtonClick");
        initializeRegistrationListener();
        registerService(1771, this.getApplicationContext());

        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
        info = (TextView)findViewById(R.id.server_info_text);

        msg = (TextView)findViewById(R.id.server_msg_text);
        if (msg != null)
            msg.setText(getIpAddress());
        //message = "";

    }

    private NsdManager mNsdManager;
    private DiscoveryListener mDiscoveryListener;
    private ResolveListener mResolveListener;
    private RegistrationListener mRegistrationListener;
    String mServiceName;

    public void registerService(int port, Context context) {
        // Create the NsdServiceInfo object, and populate it.
        Log.d(APP_TAG, "registerService port = " + port);
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.setServiceName("AnnChat");
        serviceInfo.setServiceType("_http._tcp.");
        serviceInfo.setPort(port);

        mNsdManager = (NsdManager)context.getSystemService(Context.NSD_SERVICE);
        //mNsdManager = context.getSystemService(Context.NSD_SERVICE);


        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

   // private String mServiceName;

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {


            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.

                mServiceName = NsdServiceInfo.getServiceName();
                Log.d(APP_TAG, "onServiceRegistered mServiceName = " + mServiceName);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed!  Put debugging code here to determine why.
                Log.d(APP_TAG, "onRegistrationFailed ");
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered.  This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                Log.d(APP_TAG, "onServiceUnregistered ");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed.  Put debugging code here to determine why.
                Log.d(APP_TAG, "onUnregistrationFailed ");
            }
        };
    }

    TextView info, infoip, msg;
    String message = "";
    ServerSocket serverSocket;


    private class SocketServerThread extends Thread {

        static final int SocketServerPORT = 8080;
        int count = 0;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        info.setText("I'm waiting here: "
                                + serverSocket.getLocalPort());
                    }
                });


                Socket socket = serverSocket.accept();
                InputStream inputStream = socket.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

                while (true) {

                    count++;
                   // message += "#" + count + " from " + socket.getInetAddress()
                   //         + ":" + socket.getPort() + "\n";

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           // message += "1";
                            msg.setText(message);
                        }
                    });

                 /*   SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(
                            socket, count);
                    socketServerReplyThread.run();*/


                  /*  SocketServerRecvThread socketServerRecvThread = new SocketServerRecvThread(socket);
                    socketServerRecvThread.run();
                    */
                    String line = null;
                    while ((line = in.readLine()) != null) {
                       // Log.d(APP_TAG, "while true LOL");
                        Log.d(APP_TAG, line);
                        message += line.toString();
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                msg.setText(message);
                            }
                        });

                        /*handler.post(new Runnable() {
                            @Override
                            public void run() {
                                // DO WHATEVER YOU WANT TO THE FRONT END
                                // THIS IS WHERE YOU CAN BE CREATIVE
                            }
                        });*/
                    }
                    //break;

                /*   byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1){
                        Log.d(APP_TAG, "inputstream.read");
                        message = buffer.toString();
                    }

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            msg.setText(message);
                        }
                    });

*/
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Log.d(APP_TAG, "try end");
        }


    }

    private class SocketServerReplyThread extends Thread {

        private Socket hostThreadSocket;
        int cnt;

        SocketServerReplyThread(Socket socket, int c) {
            hostThreadSocket = socket;
            cnt = c;
        }

        @Override
        public void run() {
            OutputStream outputStream;
            String msgReply = "Hello from Android, you are #" + cnt;

            try {
                outputStream = hostThreadSocket.getOutputStream();
                PrintStream printStream = new PrintStream(outputStream);
                printStream.print(msgReply);
                printStream.close();

                message += "replayed: " + msgReply + "\n";

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        msg.setText(message);
                        Log.d(APP_TAG, "set here1");
                    }
                });

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                message += "Something wrong! " + e.toString() + "\n";
            }

            MainActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    msg.setText(message);
                    Log.d(APP_TAG, "set here2");
                }
            });
        }


    }

    private class SocketServerRecvThread extends Thread {

            private Socket hostThreadSocket;
            int cnt;

            SocketServerRecvThread(Socket socket) {
                hostThreadSocket = socket;
              //  cnt = c;
            }

            @Override
            public void run() {
               // OutputStream outputStream;
               // String msgReply = "Hello from Android, you are #" + cnt;
                InputStream inputStream;
                String msgIn;

                try {
                   /* outputStream = hostThreadSocket.getOutputStream();
                    PrintStream printStream = new PrintStream(outputStream);
                    printStream.print(msgReply);
                    printStream.close();

                    message += "replayed: " + msgReply + "\n";*/
                    byte[] buffer = new byte[1024];
                    ByteArrayOutputStream byteArrayOutputStream =
                            new ByteArrayOutputStream(1024);
                    int bytesRead;
                    Log.d(APP_TAG, "before read data");
                    inputStream = hostThreadSocket.getInputStream();

                    while ((bytesRead = inputStream.read(buffer)) != -1){
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                        message += byteArrayOutputStream.toString("UTF-8");
                        Log.d(APP_TAG, "asdasd");
                    }

                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            msg.setText(message);
                            Log.d(APP_TAG, "set here1");
                        }
                    });

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    message += "Something wrong 2! " + e.toString() + "\n";
                }

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        msg.setText(message);
                        Log.d(APP_TAG, "set here2");
                    }
                });
            }
    }


    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }
}
