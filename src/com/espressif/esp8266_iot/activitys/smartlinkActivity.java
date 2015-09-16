package com.espressif.esp8266_iot.activitys;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.espressif.esp8266_iot.DataStore.DataStore_Data;
import com.espressif.esp8266_iot.DataStore.DataStore_Name;
import com.espressif.esp8266_iot.DataStore.DatabaseHandler_Data;
import com.espressif.esp8266_iot.DataStore.DatabaseHandler_Name;
import com.espressif.esp8266_iot.EsptouchTask;
import com.espressif.esp8266_iot.GlobalClass;
import com.espressif.esp8266_iot.IEsptouchResult;
import com.espressif.esp8266_iot.IEsptouchTask;
import com.espressif.esp8266_iot.task.__IEsptouchTask;
import com.espressif.esp8266_iot.R;
import com.espressif.esp8266_iot.util.BaseActivity;
import com.espressif.esp8266_iot.util.CRC16CCITT;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import static java.net.InetAddress.getByName;

public class smartlinkActivity extends BaseActivity implements OnClickListener {

    public static final String PREFS_NAME = "MyPrefsFile";
    static final String TAG = "smartlinkActivity";

    //http://shiki.me/blog/android-button-background-image-pressedhighlighted-and-disabled-states-without-using-multiple-images/
    private Button mBtnUpdate;
    private TextView LogView;

    //**********************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "smartlink onCreate");
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.main_smartlink);

        // get action bar
        ActionBar actionBar = getActionBar();
        // Enabling Up / Back navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setDisplayShowTitleEnabled(false);

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();

        mBtnUpdate = (net.shikii.widgets.SAutoBgButton) findViewById(R.id.btnUpdate);
        mBtnUpdate.setOnClickListener(this);

        LogView = (TextView) findViewById(R.id.LogView);
    }

    //**********************************************************************************************
    @Override
    protected void onResume() {
        Log.d(TAG, "smartlink onResume");
        // get action bar
        ActionBar actionBar = getActionBar();
        // Enabling Up / Back navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();

        boolean isApSsidEmpty = TextUtils.isEmpty(globalVariable.get_apSsid());
        ConnectivityManager connManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) {
            AlertOk();
        }
        if (isApSsidEmpty) {
            noApConnect();
        }

        mBtnUpdate.setEnabled(!isApSsidEmpty);

        super.onResume();

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
    @Override
    protected void onDestroy() {
        Log.d(TAG, "Smartlink onDestroy");
        super.onDestroy();
    }

    //**********************************************************************************************
    @Override
    public void onBackPressed() {
        Log.d(TAG, "Smartlink onBackPressed");
        finish();
        super.onBackPressed();
    }
    //**********************************************************************************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
        //return super.onCreateOptionsMenu(menu);
    }
    //**********************************************************************************************
    /**
     * On selecting action bar icons
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //When Search action item is clicked
        if (id == R.id.action_main) {
            Intent mainIntent = new Intent(this, mainActivity.class);
            //Start Product Activity
            startActivity(mainIntent);
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
    @Override
    public void onClick(View v) {
        Log.d(TAG, "Button mit ID : " + String.valueOf(v.getId()) + " gedrückt");
        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        if (v == mBtnUpdate) {
            Log.d(TAG, "Scan Button pressed");
            LogView.setText("Touchtask started");
            new EsptouchAsyncTask3().execute(globalVariable.get_apSsid(), globalVariable.get_apBssid(), globalVariable.get_apPassword(), globalVariable.get_isSsidHiddenStr(), globalVariable.get_taskResultCountStr());
        }
    }

    //**********************************************************************************************
    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {

        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();
        private ProgressDialog mProgressDialog;
        private IEsptouchTask mEsptouchTask;

        //******************************************************************************************
        @Override
        protected void onPreExecute() {
            LogView.setText("Please wait ...");
            mProgressDialog = new ProgressDialog(smartlinkActivity.this);
            mProgressDialog.setMessage("Bitte warten...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);

            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (__IEsptouchTask.DEBUG) {
                            Log.d(TAG, "Abgebrochen");
                            LogView.setText("User break");
                        }
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                    }
                }
            });

            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Abbruch", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            mProgressDialog.show();
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        }

        //******************************************************************************************
        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            final GlobalClass globalVariable = (GlobalClass) getApplicationContext();

            synchronized (mLock) {
                globalVariable.set_apSsid(params[0]);
                globalVariable.set_apBssid(params[1]);
                globalVariable.set_apPassword(params[2]);
                globalVariable.set_isSsidHiddenStr(params[3]);
                globalVariable.set_isSsidHidden(false);
                if (globalVariable.get_isSsidHidden().equals("YES")) {
                    globalVariable.set_isSsidHidden(true);
                }
                taskResultCount = Integer.parseInt(globalVariable.get_taskResultCountStr());
                mEsptouchTask = new EsptouchTask(globalVariable.get_apSsid(), globalVariable.get_apBssid(), globalVariable.get_apPassword(),
                        globalVariable.get_isSsidHidden(), smartlinkActivity.this);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        //******************************************************************************************
        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Fertig");
            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                int count = 0;
                // max results to be displayed, if it is more than maxDisplayCount,
                // just show the count of redundant ones
                final int maxDisplayCount = 5;
                // the task received some results including cancelled while
                // executing before receiving enough results
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    //******************************************************************************************
                    DatabaseHandler_Data db_data = new DatabaseHandler_Data(smartlinkActivity.this);
                    DatabaseHandler_Name db_name = new DatabaseHandler_Name(smartlinkActivity.this);

                    for (IEsptouchResult resultInList : result) {
                        sb.append("Erfolgreich"
                                + "\n"
                                + "bssid = "
                                + resultInList.getBssid()
                                + "\n"
                                + "InetAddress = "
                                + resultInList.getInetAddress().getHostAddress()
                                + "\n");
                        //******************************************************************************************
                        // Inserting Module to Datastore
                        Log.d(TAG, "Inserting to Database ..");
                        LogView.setText("Inserting to Database ..");

                        db_data.addDataStore(new DataStore_Data(CRC16CCITT.CRC16(resultInList.getBssid().getBytes(), resultInList.getBssid().length()), "NULL", resultInList.getInetAddress().getHostAddress(), "NULL", "NULL", 1));
                        db_name.addDataStore(new DataStore_Name(CRC16CCITT.CRC16(resultInList.getBssid().getBytes(), resultInList.getBssid().length()), resultInList.getBssid()));

                        count++;
                        if (count >= maxDisplayCount) {
                            break;
                        }
                    }

                    db_data.close();

                    if (count < result.size()) {
                        sb.append("\nthere's " + (result.size() - count)
                                + " more result(s) without showing\n");
                    }
                    LogView.setText(sb.toString());
                    mProgressDialog.setMessage(sb.toString());
                } else {
                    mProgressDialog.setMessage("Fehler");
                }
            }
        }
    }

}
