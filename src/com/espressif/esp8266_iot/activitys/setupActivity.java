package com.espressif.esp8266_iot.activitys;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.espressif.esp8266_iot.GlobalClass;
import com.espressif.esp8266_iot.R;
import com.espressif.esp8266_iot.util.BaseActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.prefs.Preferences;

import static java.net.InetAddress.getByName;

public class setupActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "setupActivity";
    public static final String PREFS_NAME = "MyPrefsFile";

    public static TextView mTvApSsid;

    private EditText mEdtApPassword;
    private Switch mSwitchIsSsidHidden;
    private EspWifiAdminSimple mWifiAdmin;
    private Spinner mSpinnerTaskCount;
    private EditText mEdtMqttIp;
    private EditText mEdtMqttPort;
    private EditText mEdtMqttUser;
    private EditText mEdtMqttPass;
    private Switch mMqttSSL;

    private EditText mEdtUpgradeIp;
    private EditText mEdtUpgradePort;

    private EditText mEdtImpulsLength;

    Button mBtnSave;

    //**********************************************************************************************
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Setup onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_activity);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        // get action bar
        ActionBar actionBar = getActionBar();
        // Enabling Up / Back navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setDisplayShowTitleEnabled(false);

        mWifiAdmin = new EspWifiAdminSimple(this);

        mTvApSsid = (TextView) findViewById(R.id.tvApSssidConnected);

        mEdtApPassword = (EditText) findViewById(R.id.edtApPassword);
        mSwitchIsSsidHidden = (Switch) findViewById(R.id.switchIsSsidHidden);
        //mSpinnerTaskCount = (Spinner) findViewById(R.id.spinnerTaskResultCount);

        mEdtMqttIp = (EditText) findViewById(R.id.edt_mqtt_ip);
        mEdtMqttPort = (EditText) findViewById(R.id.edt_mqtt_port);
        mEdtMqttUser = (EditText) findViewById(R.id.edt_mqtt_user);
        mEdtMqttPass = (EditText) findViewById(R.id.edt_mqtt_pass);
        mMqttSSL = (Switch) findViewById(R.id.sw_mqtt_ssl);

        mEdtUpgradeIp = (EditText) findViewById(R.id.edt_Upgrade_ip);
        mEdtUpgradePort = (EditText) findViewById(R.id.edt_Upgrade_port);

        mEdtImpulsLength = (EditText) findViewById(R.id.edt_Impuls_length);

        mBtnSave = (net.shikii.widgets.SAutoBgButton) findViewById(R.id.btnSave);
        mBtnSave.setOnClickListener(this);


    }

    //**********************************************************************************************
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Setup onResume");
        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();

        loadVariablen();
        initSpinner();

        // display the connected ap's ssid
        globalVariable.set_apSsid(mWifiAdmin.getWifiConnectedSsid());

        if (globalVariable.get_apSsid() != null) {
            mTvApSsid.setText(globalVariable.get_apSsid());
        } else {
            mTvApSsid.setText("");
        }
        if (globalVariable.get_apPassword() != null) {
            mEdtApPassword.setText(globalVariable.get_apPassword());
        } else {
            mEdtApPassword.setText("");
        }
        globalVariable.set_apBssid(mWifiAdmin.getWifiConnectedBssid());

        globalVariable.set_isSsidHidden(globalVariable.get_isSsidHidden());
        globalVariable.set_isSsidHiddenStr("NO");

        if (globalVariable.get_isSsidHidden()) {
            globalVariable.set_isSsidHiddenStr("YES");
        }


        mSwitchIsSsidHidden.setChecked(globalVariable.get_isSsidHidden());

        mEdtMqttIp.setText(globalVariable.get_mqttIp());
        mEdtMqttPort.setText(globalVariable.get_mqttPort());
        mEdtMqttUser.setText(globalVariable.get_mqttUser());
        mEdtMqttPass.setText(globalVariable.get_mqttPass());
        mMqttSSL.setChecked(globalVariable.get_mqttSSL());

        mEdtUpgradeIp.setText(globalVariable.get_upgradeIp());
        mEdtUpgradePort.setText(globalVariable.get_upgradePort());

        mEdtImpulsLength.setText(globalVariable.get_ImpulsLengthStr());

    }

    //**********************************************************************************************
    @Override
    protected void onStop() {
        saveVariablen();
        super.onStop();

    }

    //**********************************************************************************************
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //**********************************************************************************************
    @Override
    public void onBackPressed() {
        Log.d(TAG, "Setup onBackPressed");
        saveVariablen();
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
        else if (id == R.id.action_link) {
            Intent smartlinkIntent = new Intent(this, smartlinkActivity.class);
            //Start Product Activity
            startActivity(smartlinkIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //**********************************************************************************************
    private Integer Bool2Int(Boolean value) {
        return value ? 1 : 0;
    }

    //**********************************************************************************************
    private String Bool2Str(Boolean value) {
        int b = value ? 1 : 0;
        return String.valueOf(b);
    }

    //**********************************************************************************************
    @Override
    public void onClick(View v) {
        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        switch (v.getId()) {
            case R.id.btnSave:
                Log.d(TAG, "mBtnSave is clicked");
                saveVariablen();
                udpSend("255.255.255.255:4," + globalVariable.get_mqttIp() + ";" + globalVariable.get_mqttPort() + ";" + globalVariable.get_mqttUser() + ";" + globalVariable.get_mqttPass() + ";" + Bool2Str(globalVariable.get_mqttSSL()) + ";" + globalVariable.get_upgradeIp() + ";" + globalVariable.get_upgradePort());
                //finish();
                super.onBackPressed();
                break;
        }
    }
    //**********************************************************************************************
    private void initSpinner() {
        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        mSpinnerTaskCount = (Spinner) findViewById(R.id.spinnerTaskResultCount);
        int[] spinnerItemsInt = getResources().getIntArray(R.array.taskResultCount);
        int length = spinnerItemsInt.length;
        Integer[] spinnerItemsInteger = new Integer[length];
        for (int i = 0; i < length; i++) {
            spinnerItemsInteger[i] = spinnerItemsInt[i];
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_list_item_1, spinnerItemsInteger);
        mSpinnerTaskCount.setAdapter(adapter);
        try {
            mSpinnerTaskCount.setSelection(Integer.parseInt(globalVariable.get_taskResultCountStr()));
        }catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Error in initSpinner(): Exception: " + e.getMessage());
                mSpinnerTaskCount.setSelection(0);
            }
        }

    //**********************************************************************************************
    public void loadVariablen() {
        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        if (prefs.contains("Ssid"))
            globalVariable.set_apSsid(prefs.getString("Ssid", ""));
        if (prefs.contains("Password"))
            globalVariable.set_apPassword(prefs.getString("Password", ""));
        if (prefs.contains("Bssid"))
            globalVariable.set_apBssid(prefs.getString("Bssid", ""));
        if (prefs.contains("SsidHidden"))
            globalVariable.set_isSsidHidden(prefs.getBoolean("SsidHidden", false));
        if (prefs.contains("SsidHiddenStr"))
            globalVariable.set_isSsidHiddenStr(prefs.getString("SsidHiddenStr", ""));
        if (prefs.contains("ResultCountStr"))
            globalVariable.set_taskResultCountStr(prefs.getString("ResultCountStr", "NO"));

        if (globalVariable.get_isSsidHidden()) {
            globalVariable.set_isSsidHiddenStr("YES");
        }
        if (prefs.contains("ResultCount"))
            globalVariable.set_taskResultCount(prefs.getInt("ResultCount", 1));
        if (prefs.contains("mqttIp"))
            globalVariable.set_mqttIp(prefs.getString("mqttIp", ""));
        if (prefs.contains("mqttPort"))
            globalVariable.set_mqttPort(prefs.getString("mqttPort", ""));
        if (prefs.contains("mqttUser"))
            globalVariable.set_mqttUser(prefs.getString("mqttUser", ""));
        if (prefs.contains("mqttPass"))
            globalVariable.set_mqttPass(prefs.getString("mqttPass", ""));
        if (prefs.contains("mqttSSL"))
            globalVariable.set_mqttSSL(prefs.getBoolean("mqttSSL", false));

        if (prefs.contains("upgradeIp"))
            globalVariable.set_upgradeIp(prefs.getString("upgradeIp", ""));
        if (prefs.contains("upgradePort"))
            globalVariable.set_upgradePort(prefs.getString("upgradePort", ""));

        if (prefs.contains("ImpulsLength"))
            globalVariable.set_ImpulsLengthStr(prefs.getString("ImpulsLength", ""));
    }

    //**********************************************************************************************
    public void saveVariablen() {
        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();

        globalVariable.set_apSsid(mTvApSsid.getText().toString());
        globalVariable.set_apPassword(mEdtApPassword.getText().toString());
        globalVariable.set_apBssid(mWifiAdmin.getWifiConnectedBssid());
        globalVariable.set_isSsidHidden(mSwitchIsSsidHidden.isChecked());
        globalVariable.set_isSsidHiddenStr("NO");
        globalVariable.set_taskResultCountStr(Integer.toString(mSpinnerTaskCount.getSelectedItemPosition()));
        if (globalVariable.get_isSsidHidden()) {
            globalVariable.set_isSsidHiddenStr("YES");
        }
        globalVariable.set_taskResultCount(mSpinnerTaskCount.getSelectedItemPosition());

        globalVariable.set_mqttIp(mEdtMqttIp.getText().toString());
        globalVariable.set_mqttPort(mEdtMqttPort.getText().toString());
        globalVariable.set_mqttUser(mEdtMqttUser.getText().toString());
        globalVariable.set_mqttPass(mEdtMqttPass.getText().toString());
        globalVariable.set_mqttSSL(mMqttSSL.isChecked());

        globalVariable.set_upgradeIp(mEdtUpgradeIp.getText().toString());
        globalVariable.set_upgradePort(mEdtUpgradePort.getText().toString());

        globalVariable.set_ImpulsLengthStr(mEdtImpulsLength.getText().toString());

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();

        editor.putString("Ssid", globalVariable.get_apSsid());
        editor.putString("Password", globalVariable.get_apPassword());
        editor.putString("Bssid", globalVariable.get_apBssid());
        editor.putBoolean("SsidHidden", globalVariable.get_isSsidHidden());
        editor.putString("SsidHiddenStr", globalVariable.get_isSsidHiddenStr());
        editor.putInt("ResultCount", globalVariable.get_taskResultCount());
        editor.putString("ResultCountStr", globalVariable.get_taskResultCountStr());

        editor.putString("mqttIp", globalVariable.get_mqttIp());
        editor.putString("mqttPort", globalVariable.get_mqttPort());
        editor.putString("mqttUser", globalVariable.get_mqttUser());
        editor.putString("mqttPass", globalVariable.get_mqttPass());
        editor.putBoolean("mqttSSL", globalVariable.get_mqttSSL());

        editor.putString("upgradeIp", globalVariable.get_upgradeIp());
        editor.putString("upgradePort", globalVariable.get_upgradePort());

        editor.putString("ImpulsLength", globalVariable.get_ImpulsLengthStr());


        // Commit the edits!
        editor.commit();
    }

    //******************************************************************************************
    void udpSend(String value) {
        String messageStr = value;
        InetAddress to_ip = null;
        DatagramSocket s = null;
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
