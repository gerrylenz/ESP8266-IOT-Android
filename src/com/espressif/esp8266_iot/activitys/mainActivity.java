package com.espressif.esp8266_iot.activitys;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.espressif.esp8266_iot.DataStore.DataStore_Data;
import com.espressif.esp8266_iot.DataStore.DataStore_Name;
import com.espressif.esp8266_iot.DataStore.DatabaseHandler_Data;
import com.espressif.esp8266_iot.DataStore.DatabaseHandler_Name;
import com.espressif.esp8266_iot.GlobalClass;
import com.espressif.esp8266_iot.R;
import com.espressif.esp8266_iot.util.BaseActivity;


import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.List;

import static java.net.InetAddress.*;

public class mainActivity extends BaseActivity implements OnClickListener {

    public static final String PREFS_NAME = "MyPrefsFile";
    static final String TAG = "mainActivity";
    final Context context = this;

    //http://shiki.me/blog/android-button-background-image-pressedhighlighted-and-disabled-states-without-using-multiple-images/
    private Button mBtnScan;
    private Button mBtnSetup;

    private Button SwitchView;

    private static final int MAX_UDP_DATAGRAM_LEN = 1500;

    private MyDatagramReceiver myDatagramReceiver = null;

    public static Integer FRAMELAYOUTS = 1;
    public static Integer TOGGLEBUTTONS = 10;
    public static Integer TEXTVIEWS = 100;
    public static Integer TEXTVIEWS_INFO = 110;
    public static Integer IPTEXTVIEW = 1000;

    private TextView textMessage;

    //**********************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Main onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //******************************************************************************************
        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        textMessage = (TextView) findViewById(R.id.messageText);
        textMessage.setOnClickListener(this);

    }

    //**********************************************************************************************
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Main onResume");
        // get action bar
        ActionBar actionBar = getActionBar();
        // Enabling Up / Back navigation
        actionBar.setDisplayHomeAsUpEnabled(false);

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        //Set variable in global/application context
        try {
            globalVariable.set_apSsid(settings.getString("Ssid", ""));
            globalVariable.set_apPassword(settings.getString("Password", ""));
            globalVariable.set_apBssid(settings.getString("Bssid", ""));
            globalVariable.set_isSsidHidden(settings.getBoolean("SsidHidden", false));
            globalVariable.set_isSsidHiddenStr(settings.getString("SsidHiddenStr", ""));
            globalVariable.set_taskResultCountStr(settings.getString("ResultCountStr", ""));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error in onCreate(): Exception: " + e.getMessage());
        }


        boolean isApSsidEmpty = TextUtils.isEmpty(globalVariable.get_apSsid());
        ConnectivityManager connManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) {
            AlertOk();
        } else if (isApSsidEmpty) {
            noApConnect();
        }

        ReadAllModule();

        myDatagramReceiver = new MyDatagramReceiver();
        if (!myDatagramReceiver.isAlive()) {
            myDatagramReceiver.start();
        }


    }

    //**********************************************************************************************
    protected void onPause() {
        Log.d(TAG, "Main onPause");
        //myDatagramReceiver.kill();
        super.onPause();
    }

    //**********************************************************************************************
    @Override
    public void onStop() {
        Log.i(TAG, "Main onStop");
        //myDatagramReceiver.kill();
        super.onStop();
    }

    //**********************************************************************************************
    @Override
    public void onBackPressed() {
        Log.i(TAG, "Main onBackPressed");
        super.onBackPressed();
    }

    //**********************************************************************************************
    @Override
    protected void onDestroy() {
        Log.i(TAG, "Main onDestroy");
        //System.exit(0);
        super.onDestroy();
    }

    //**********************************************************************************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        //getMenuInflater().inflate(R.menu.menu, menu);


        return true;
        //return super.onCreateOptionsMenu(menu);
    }

    //**********************************************************************************************
    public static int convDpToPx(Context context, float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }
    //**********************************************************************************************
    /**
     * On selecting action bar icons
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //When Search action item is clicked
        if (id == R.id.action_link) {
            Intent smartIntent = new Intent(this, smartlinkActivity.class);
            //Start Product Activity
            startActivity(smartIntent);
            return true;
        }
        //When Contact action item is clicked
        else if (id == R.id.action_setup) {
            Intent setupIntent = new Intent(this, setupActivity.class);
            //Start Product Activity
            startActivity(setupIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //**********************************************************************************************
    private void ReadAllModule() {
        //******************************************************************************************
        DatabaseHandler_Data db_data = new DatabaseHandler_Data(context);
        DatabaseHandler_Name db_name = new DatabaseHandler_Name(context);
        //******************************************************************************************
        // Reading all contacts
        Log.i(TAG, "Reading all contacts..");
        List<DataStore_Data> datastore = db_data.getAllDataStores();

        for (DataStore_Data cn : datastore) {

            Log.i(TAG, "ID:" + String.valueOf(cn.getID()));

            DataStore_Name cx = db_name.getData(cn.getID());

            LinearLayout ll = (LinearLayout) findViewById(R.id.SwitchLayout);

            if (ll.findViewById(cn.getID() * FRAMELAYOUTS) == null) {
                //*********************************************************
                // add FrameLayout
                Log.i(TAG, "Create FrameLayout");
                FrameLayout fl = new FrameLayout(this);
                LinearLayout.LayoutParams fl_params = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, 0.15f);
                fl.setId(cn.getID() * FRAMELAYOUTS);
                fl.setLayoutParams(fl_params);
                //*********************************************************
                // add text view
                TextView tv = new TextView(this);
                FrameLayout.LayoutParams tv_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, convDpToPx(this, 50));
                tv.setLayoutParams(tv_params);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                tv.setText(cx.getName());
                //tv.setGravity(Gravity.CENTER);
                tv.setGravity(Gravity.TOP);
                tv.setId(cn.getID() * TEXTVIEWS);
                tv.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Log.e(TAG, "onLongClick ID:" + v.getId());
                        Vibrator vib = (Vibrator) getSystemService(context.VIBRATOR_SERVICE);
                        vib.vibrate(1000);
                        LayoutInflater li = LayoutInflater.from(context);
                        View promptsView = li.inflate(R.layout.delete_station, null);
                        final TextView tv = (TextView) v.findViewById(v.getId());

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        // set prompts.xml to alertdialog builder
                        alertDialogBuilder.setView(promptsView);

                        // set dialog message
                        alertDialogBuilder.setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        int i = tv.getId() / TEXTVIEWS;
                                        DatabaseHandler_Name db_name = new DatabaseHandler_Name(context);
                                        DatabaseHandler_Data db_data = new DatabaseHandler_Data(context);
                                        if (db_name.IsDataInDB(i)) {
                                            db_name.deleteDataStore(new DataStore_Name(i));
                                        }
                                        if (db_data.IsDataInDB(i)) {
                                            db_data.deleteDataStore(new DataStore_Data(i));
                                        }
                                        LinearLayout lp = (LinearLayout) findViewById(R.id.SwitchLayout);
                                        //Now remove them
                                        lp.removeView((View) tv.getParent());
                                    }
                                })
                                .setNegativeButton("Abbruch", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();

                        // show it
                        alertDialog.show();

                        return true;
                    }
                });
                tv.setOnClickListener(this);
                Log.v(TAG, "Add TextView ID:" + tv.getId());
                fl.addView(tv);
                //*********************************************************
                // add text view info
                TextView tvi = new TextView(this);
                FrameLayout.LayoutParams tvi_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, convDpToPx(this, 80) );
                tvi.setLayoutParams(tv_params);
                tvi.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                tvi.setText(cx.getName());
                tvi.setGravity(Gravity.BOTTOM);
                tvi.setId(cn.getID() * TEXTVIEWS_INFO);
                Log.v(TAG, "Add TextView ID:" + tvi.getId());
                fl.addView(tvi);
                //*********************************************************
                // add button
                ToggleButton tb = new ToggleButton(this);
                FrameLayout.LayoutParams tb_params = new FrameLayout.LayoutParams(convDpToPx(this, 100), FrameLayout.LayoutParams.WRAP_CONTENT);
                tb_params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT | Gravity.TOP;
                tb.setLayoutParams(tb_params);
                tb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);

                if (cn.getStatus().equals("0")) {
                    tb.setChecked(false);
                    tb.setText("OFF");
                } else {
                    tb.setChecked(true);
                    tb.setText("ON");
                }

                tb.setTextOn("ON");
                tb.setTextOff("OFF");
                tb.setId(cn.getID() * TOGGLEBUTTONS);
                tb.setOnClickListener(this);
                Log.v(TAG, "Add ToggleButton ID:" + tb.getId());
                fl.addView(tb);
                //*********************************************************
                // add seperator
                TextView ruler = new TextView(this);
                FrameLayout.LayoutParams ruler_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, convDpToPx(this, 2));
                ruler_params.gravity = Gravity.BOTTOM;
                ruler.setLayoutParams(ruler_params);
                ruler.setBackgroundColor(Color.rgb(255, 255, 255));
                fl.addView(ruler);
                //*********************************************************
                // add hidden ipo textview
                TextView ip = new TextView(this);
                FrameLayout.LayoutParams ip_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, convDpToPx(this, 1));
                ip_params.gravity = Gravity.BOTTOM;
                ip.setLayoutParams(ip_params);
                ip.setVisibility(View.GONE);
                ip.setText(cn.getAddress());
                ip.setId(cn.getID() * IPTEXTVIEW);
                fl.addView(ip);
                //*********************************************************
                Log.v(TAG, "Add FrameLayout ID:" + fl.getId());
                ll.addView(fl);
            }
        }
        db_data.close();
        db_name.close();
    }

    //**********************************************************************************************

    /**
     * Launching new activity
     */
    private void LocationFound() {
        Intent i = new Intent(getApplicationContext(), smartlinkActivity.class);
        startActivity(i);
    }

    //**********************************************************************************************
    public void AlertOk() {
        //Put up the Yes/No message box
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Fehler")
                .setMessage("WiFi nicht aktiv!")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Yes button clicked, do something
                    }
                })
                .show();
    }

    //**********************************************************************************************
    public void noApConnect() {
        //Put up the Yes/No message box
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Fehler")
                .setMessage("Mit AP nicht verbunden!")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Yes button clicked, do something
                    }
                })
                .show();
    }

    //**********************************************************************************************
    public boolean onLongClick(View view) {
        Log.e(TAG, "Longpress detected");
        return true;
    }

    //**********************************************************************************************
    @Override
    public void onClick(View v) {
        Log.i(TAG, "Button mit ID : " + String.valueOf(v.getId()) + " Class:" + v.getClass().getSimpleName() + " gedrückt");
        final int HELLO = 0;
        final int ACK = 1;
        final int RESET = 2;
        final int RELAY = 3;
        final int AN = 1;
        final int AUS = 0;

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();

        if (v == mBtnScan) {
            Log.i(TAG, "mBtnScan is clicked, mEdtApSsid = " + globalVariable.get_apSsid() + ", " + " mEdtApPassword = " + globalVariable.get_apPassword());
        } else if (v == mBtnSetup) {
            Log.i(TAG, "mBtnSetup");
            Intent i = new Intent(mainActivity.this, setupActivity.class);
            startActivity(i);
        } else if (v == textMessage) {
            Log.i(TAG, "textMessage");
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            String ipAddress_str = String.format("%d.%d.%d.%d",
                    (ipAddress & 0xff),
                    (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff),
                    (255));
            Log.d(TAG, "Send UDP to :" + ipAddress_str);
            new MyThread(ipAddress_str + ":" + HELLO).start();
            /*
            InetAddress address = null;
            try {
                address = InetAddress.getByName(ipAddress_str);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            */

        } else if (v.getClass().getSimpleName().equals("ToggleButton")) {
            final ToggleButton tb = (ToggleButton) v;
            DatabaseHandler_Data db_data = new DatabaseHandler_Data(this);

            if (db_data != null) {

                DataStore_Data cx = db_data.getData(tb.getId() / TOGGLEBUTTONS);

                if (cx != null) {
                    if (tb.isChecked()) {
                        new MyThread(cx.getAddress() + ":" + RELAY + "," + AN).start();
                    } else {
                        new MyThread(cx.getAddress() + ":" + RELAY + "," + AUS).start();
                    }
                }
            }
            db_data.close();
        } else if (v.getClass().getSimpleName().equals("TextView")) {
            LayoutInflater li = LayoutInflater.from(context);
            View promptsView = li.inflate(R.layout.db_edit_name_dialog, null);

            final TextView tv = (TextView) v.findViewById(v.getId());

            if (tv != null) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
                userInput.setText(tv.getText());

                // set dialog message
                alertDialogBuilder.setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                tv.setText(userInput.getText());
                                DatabaseHandler_Name db_name = new DatabaseHandler_Name(context);
                                db_name.addDataStore(new DataStore_Name(tv.getId() / TEXTVIEWS, tv.getText().toString()));
                                db_name.close();
                            }
                        })
                        .setNegativeButton("Abbruch", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        }


    }

    //**********************************************************************************************
    protected String wifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e("WIFIIP", "Unable to get host address.");
            ipAddressString = null;
        }

        return ipAddressString;
    }
    //**********************************************************************************************

    /**
     * Calculate the broadcast IP we need to send the packet along. If we send it
     * to 255.255.255.255, it never gets sent. I guess this has something to do
     * with the mobile network not wanting to do broadcast.
     */
    private InetAddress getBroadcastAddress() throws IOException {
        Log.i(TAG, "Entree getBroadcastAddress");

        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        DhcpInfo dhcp = wm.getDhcpInfo();

        if (dhcp == null) {
            Log.d(TAG, "Could not get dhcp info");
            return null;
        }

        Log.d(TAG, "ipAddress::" + BigInteger.valueOf(wm.getDhcpInfo().netmask).toString());

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);

        Log.d(TAG, "BroadcastAddress:" + getByAddress(quads).getHostAddress());
        return getByAddress(quads);
    }

    //**********************************************************************************************
    public class MyThread extends Thread {

        private String message;

        //******************************************************************************************
        public MyThread(String value) {
            this.message = value;
        }

        //******************************************************************************************
        @Override
        public void run() {
            udpSend(message);
        }

        //******************************************************************************************
        public void udpSend(String value) {
            String messageStr = value;
            InetAddress to_ip = null;
            DatagramSocket s = null;
            ;
            final GlobalClass globalVariable = (GlobalClass) getApplicationContext();

            try {
                if (s == null) {
                    Log.i(TAG, "Create Socket");
                    s = new DatagramSocket(null);
                    s.setReuseAddress(true);
                    s.setBroadcast(true);
                    s.bind(new InetSocketAddress(globalVariable.get_RemotePort()));
                    Log.i(TAG, "Success");
                }
                //DatagramSocket s = new DatagramSocket(globalVariable.get_TargetPort());
                //local = InetAddress.getByName(getBroadcastAddress().getHostAddress());
                Log.i(TAG, "Send Data:" + messageStr);
                String[] separated = messageStr.split(":");
                to_ip = getByName(separated[0]);
                messageStr = separated[1];
                int msg_length = messageStr.length();
                byte[] message = messageStr.getBytes();
                DatagramPacket p = new DatagramPacket(message, msg_length, to_ip, globalVariable.get_RemotePort());
                s.send(p);
                s.close();
            } catch (SocketException e) {
                e.printStackTrace();
                Log.e(TAG, "Error in udpSend(): SocketException: " + e.getMessage());
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Log.e(TAG, "Error in udpSend(): UnknownHostException: " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error in udpSend(): IOException: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Error in udpSend(): Exception: " + e.getMessage());
            }
        }
    }

    //**********************************************************************************************
//http://stackoverflow.com/questions/16752205/simple-udp-server-for-android-and-get-multi-messages
    private class MyDatagramReceiver extends Thread {
        private boolean bKeepRunning = true;
        private String lastMessage = "";
        private String lastIP = "";
        private String lastFunction = "";
        private String lastPinIO = "";
        private String lastTemp = "";
        private String lastHumi = "";
        private int lastID = 0;
        private Boolean checked = false;
        DatagramSocket socket;
        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();


        @Override
        public void run() {
            Log.i(TAG, "MyDatagramReceiver gestartet");

            final int HELLO = 0;
            final int STATUS = 1;
            final int RESET = 2;
            final int RELAY = 3;
            final int MQTT_CONFIG = 4;
            final int UMWELT = 5;
            final int AN = 1;
            final int AUS = 0;

            final int ID = 0;
            final int Befehl = 1;
            final int LocaleIP = 2;
            final int FUNCTION = 3;
            final int PinIOStatus = 4;
            final int Temp = 4;
            final int Humi = 5;

            String message;
            String delimiter;
            delimiter = "\\,";

            byte[] lmessage = new byte[MAX_UDP_DATAGRAM_LEN];

            try {
                if (socket == null) {
                    socket = new DatagramSocket(null);
                    socket.setReuseAddress(true);
                    socket.setBroadcast(true);
                    socket.bind(new InetSocketAddress(globalVariable.get_TargetPort()));
                }

                DatagramPacket packet = new DatagramPacket(lmessage, lmessage.length);
                DatabaseHandler_Data db_data = new DatabaseHandler_Data(mainActivity.this);
                DatabaseHandler_Name db_name = new DatabaseHandler_Name(mainActivity.this);

                while (bKeepRunning) {
                    socket.receive(packet);
                    String senderIP = packet.getAddress().getHostAddress();
                    message = new String(lmessage, 0, packet.getLength());
                    lastMessage = message;
                    String[] separated = message.split(delimiter);
                    //******************************************************************************************
                    try {
                        switch (Integer.parseInt(separated[Befehl].toString())) {
                            case HELLO:
                                Log.i(TAG, "Hello");
                                // Tabellen Aufbau
                                // KEY_ID, KEY_ADDRESS, KEY_FUNC, KEY_STATUS, KEY_ACTIV
                                Log.d(TAG, "Broadcast:" + separated[ID] + "," + senderIP + "," + separated[FUNCTION] + "," + separated[PinIOStatus] + "," + "1");
                                db_data.addDataStore(new DataStore_Data(Integer.parseInt(separated[ID]), senderIP, separated[FUNCTION], separated[PinIOStatus], 1));
                                if (!db_name.IsDataInDB(Integer.parseInt(separated[ID]))) {
                                    db_name.addDataStore(new DataStore_Name(Integer.parseInt(separated[ID]), senderIP));
                                    db_name.close();
                                }
                                break;
                            case STATUS:
                                Log.i(TAG, "STATUS");
                                lastID = Integer.parseInt(separated[ID]);
                                lastIP = senderIP;
                                lastFunction = separated[FUNCTION];
                                lastPinIO = separated[PinIOStatus];
                                checked = (Integer.parseInt(separated[PinIOStatus]) != 0);
                                final TextView tv_ip = (TextView) findViewById(lastID * IPTEXTVIEW);

                                if (tv_ip != null) {
                                    DataStore_Data db = db_data.getData(lastID);
                                    if (!tv_ip.getText().toString().equals(lastIP)) {
                                        Log.d(TAG, "IP des Moduls " + lastID + " von " + db.getAddress() + " auf " + lastIP + " aktualisiert");
                                        tv_ip.setText(lastIP);
                                        db.setAddress(lastIP);
                                        db_data.updateDataStore(db);
                                        db_data.close();
                                        db_name.close();
                                    }
                                } else {
                                    db_data.addDataStore(new DataStore_Data(lastID, lastIP, lastFunction, lastPinIO, 1));
                                    db_name.addDataStore(new DataStore_Name(lastID, lastIP));
                                    db_data.close();
                                    db_name.close();
                                    runOnUiThread(updateGUI);
                                }

                                runOnUiThread(updateToggleButton);
                                break;
                            case RELAY:
                                Log.i(TAG, "RELAY");
                                lastID = Integer.parseInt(separated[ID]);
                                lastIP = senderIP;
                                lastFunction = separated[FUNCTION];
                                lastPinIO = separated[PinIOStatus];
                                checked = (Integer.parseInt(separated[PinIOStatus]) != 0);
                                runOnUiThread(updateToggleButton);
                                break;
                            case UMWELT:
                                Log.i(TAG, "UMWELT");
                                lastID = Integer.parseInt(separated[ID]);
                                lastIP = senderIP;
                                lastFunction = separated[FUNCTION];
                                lastTemp = separated[Temp];
                                lastHumi = separated[Humi];
                                //checked = (Integer.parseInt(separated[UMWELT]) != 0);
                                runOnUiThread(updateMessageButton);
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error0: " + e.getMessage());
                    }

                    //******************************************************************************************

                    runOnUiThread(updateGUI);
                }

                if (socket != null) {
                    Log.e(TAG, "Socket geschlossen");
                    socket.disconnect();
                    socket.close();
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Log.e(TAG, "Error1: " + e.getMessage());
            }
        }

        public void kill() {
            bKeepRunning = false;
        }

        public String getLastMessage() {
            return lastMessage;
        }

        public Integer getLastID() {
            return lastID;
        }

        public String getLastIP() {
            return lastIP;
        }

        public String getLastPinIO() {
            return lastPinIO;
        }

        public String getLastFunction() {
            return lastFunction;
        }

        public String getLastTemp() {
            return lastTemp;
        }
        public String getLastHumi() {
            return lastHumi;
        }

        @Override
        public void interrupt() {
            try {
                Log.d(TAG, "interrupt");
            } finally {
                super.interrupt();
            }
        }

    }

    //**********************************************************************************************
    private Runnable updateGUI = new Runnable() {
        public void run() {
            if (myDatagramReceiver == null) return;
            textMessage.setText(myDatagramReceiver.getLastMessage());

        }
    };
    private Runnable updateGUIFrames = new Runnable() {
        public void run() {
            if (myDatagramReceiver == null) return;
            ReadAllModule();

        }
    };
    //**********************************************************************************************
    private Runnable updateToggleButton = new Runnable() {
        public void run() {
            if (myDatagramReceiver == null) return;

            int lastID = myDatagramReceiver.getLastID();
            final ToggleButton tb = (ToggleButton) findViewById(lastID * TOGGLEBUTTONS);

            if (tb != null) {
                DatabaseHandler_Data db_data = new DatabaseHandler_Data(mainActivity.this);
                if (tb.isChecked() != myDatagramReceiver.checked) {
                    Log.d(TAG, "Set ToggleButton ID:" + lastID + " to " + myDatagramReceiver.checked);
                    tb.setChecked(myDatagramReceiver.checked);
                    DataStore_Data db = db_data.getData(lastID);
                    db.setStatus(myDatagramReceiver.getLastPinIO());
                    db_data.updateDataStore(db);
                    db_data.close();
                }
            } else
                Log.e(TAG, "ToggleButton ID:" + lastID + " not found");

        }
    };
    //**********************************************************************************************
    private Runnable updateMessageButton = new Runnable() {
        public void run() {
            if (myDatagramReceiver == null) return;

            int lastID = myDatagramReceiver.getLastID();
            final TextView tv = (TextView) findViewById(lastID * TEXTVIEWS_INFO);

            if (tv != null) {
                DatabaseHandler_Data db_data = new DatabaseHandler_Data(mainActivity.this);
                Log.d(TAG, "Set TEXTVIEWS_INFO ID:" + lastID + " to " + "Temp:" + myDatagramReceiver.getLastTemp() + " Humi:" + myDatagramReceiver.getLastHumi());
                tv.setText("Temp:" + myDatagramReceiver.getLastTemp() + " Humi:" + myDatagramReceiver.getLastHumi());
            } else
                Log.e(TAG, "TEXTVIEWS_INFO ID:" + lastID + " not found");
        }
    };
    //**********************************************************************************************

}
