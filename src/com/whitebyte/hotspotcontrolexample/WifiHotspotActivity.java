package com.whitebyte.hotspotcontrolexample;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.whitebyte.hotspotclients.R;
import com.whitebyte.wifihotspotutils.WIFI_AP_STATE;
import com.whitebyte.wifihotspotutils.WifiApManager;

public class WifiHotspotActivity extends Activity {

    private Button btnHotspotOnOff;
    private WifiApManager wifiApManager;
    private String ssid;
    private String keyPair;

    public enum WifiHotspotState {
        ON, OFF
    }

    private WifiHotspotState wifiHotspotState = WifiHotspotState.OFF;
    private TextView txvHotspotSsid;
    private TextView txvHotspotKey;
    
    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_hotspot);

        btnHotspotOnOff = (Button) findViewById(R.id.btnHotspotOnOff);
        btnHotspotOnOff.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (wifiHotspotState == WifiHotspotState.OFF) {
                    new TurnWifiHotspotOnOffTask("FINGI"+ssid, keyPair, WifiHotspotState.ON).execute();   
                } else {
                    new TurnWifiHotspotOnOffTask(null, null, WifiHotspotState.OFF).execute();
                }
            }
        });

        wifiApManager = new WifiApManager(this);
        
        ssid = getSsid();
        keyPair = getKeypair();
        
        txvHotspotSsid = (TextView) findViewById(R.id.textViewHotspotSsid);
        txvHotspotKey = (TextView) findViewById(R.id.textViewHotspotKey);
        
        txvHotspotSsid.append(ssid);
        txvHotspotKey.append(keyPair);
        
    }

    private String getSsid () {
        String ssid = "";
        Random r = new Random();
        
        for (int i = 0; i < 4; i++) {
            ssid += ""+r.nextInt(10);
        }

        return ssid;
    }
    
    private String getKeypair () {
        String keyPair = "";
        Random r = new Random();
        
        for (int i = 0; i < 8; i++) {
            keyPair += ""+r.nextInt(10);
        }

        return keyPair;
    }
    
    private class TurnWifiHotspotOnOffTask extends AsyncTask<Void, Void, HashMap<String, String>> {

        private ProgressDialog progressDialog;
        private WifiHotspotState state;
        private String ssid;
        private String keyPair;

        public TurnWifiHotspotOnOffTask(String ssid, String keyPair, WifiHotspotState state) {
            this.state = state;
            this.ssid = ssid;
            this.keyPair = keyPair;
        }

        @Override
        protected void onPostExecute(HashMap<String, String> result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            progressDialog = null;
            
            if (result != null) {
                boolean success = Boolean.valueOf(result.get("STATUS"));
                String extraInfo = result.get("EXTR_INFO");
                
                if (!success) {
                    extraInfo += " Please try again";
                    Toast.makeText(getApplicationContext(), extraInfo, Toast.LENGTH_LONG).show();
                } else {
                    wifiHotspotState = state;
                    btnHotspotOnOff.setText((state == WifiHotspotState.OFF) ? getString(R.string.lbl_hotspot_off) : getString(R.string.lbl_hotspot_on));
                }
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(WifiHotspotActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage((state == WifiHotspotState.ON) ? "Turning Wifi Hotspot on, please wait..." : "Turning Wifi Hotspot off, please wait...");
        
            progressDialog.show();
        }

        @Override
        protected HashMap<String, String> doInBackground(Void... params) {
            
            HashMap<String, String> result = new HashMap<String, String>();

            try {
                
                if (state == WifiHotspotState.ON) {
                    WifiConfiguration netConfig = new WifiConfiguration();
                    netConfig.SSID = ssid;
                    netConfig.preSharedKey = keyPair;
                    netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                    
                    wifiApManager.setWifiApConfiguration(netConfig);
                    Method setWifiApMethod = wifiApManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                    boolean apstatus = (Boolean) setWifiApMethod.invoke(wifiApManager, netConfig, true);

                    Method isWifiApEnabledmethod = wifiApManager.getClass().getMethod("isWifiApEnabled");
                    while (!(Boolean) isWifiApEnabledmethod.invoke(wifiApManager)) {
                        SystemClock.sleep(500);
                    };
                    
                    Method getWifiApStateMethod = wifiApManager.getClass().getMethod("getWifiApState");
                    WIFI_AP_STATE apstate = WIFI_AP_STATE.valueOf("" + getWifiApStateMethod.invoke(wifiApManager));
                    Method getWifiApConfigurationMethod = wifiApManager.getClass().getMethod("getWifiApConfiguration");
                    netConfig = (WifiConfiguration) getWifiApConfigurationMethod.invoke(wifiApManager);
                    
                    Log.e("CLIENT", "\nSSID:" + netConfig.SSID + "\nPassword:" + netConfig.preSharedKey + "\n");
                } else {
                    Method setWifiApMethod = wifiApManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                    boolean apstatus = (Boolean) setWifiApMethod.invoke(wifiApManager, null, false);
                }
                
                result.put("STATUS", Boolean.toString(true));
                result.put("EXTR_INFO", "Wifi Hotspot state was successfully toggled.");
                
            } catch (NoSuchMethodException e) {
                result.put("STATUS", Boolean.toString(false));
                result.put("EXTR_INFO", "NoSuchMethodException.");
            } catch (IllegalAccessException e) {
                result.put("STATUS", Boolean.toString(false));
                result.put("EXTR_INFO", "IllegalAccessException.");
            } catch (IllegalArgumentException e) {
                result.put("STATUS", Boolean.toString(false));
                result.put("EXTR_INFO", "IllegalArgumentException.");
            } catch (InvocationTargetException e) {
                result.put("STATUS", Boolean.toString(false));
                result.put("EXTR_INFO", "InvocationTargetException.");
            }

            return result;
        }

    }

}
