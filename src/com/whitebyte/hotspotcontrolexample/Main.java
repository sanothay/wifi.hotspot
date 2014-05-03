/*
 * Copyright 2013 WhiteByte (Nick Russler, Ahmet Yueksektepe).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.whitebyte.hotspotcontrolexample;

import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.whitebyte.hotspotclients.R;
import com.whitebyte.wifihotspotutils.ClientScanResult;
import com.whitebyte.wifihotspotutils.FinishScanListener;
import com.whitebyte.wifihotspotutils.WIFI_AP_STATE;
import com.whitebyte.wifihotspotutils.WifiApManager;

public class Main extends Activity {
    TextView textView1;
    WifiApManager wifiApManager;
    private Button btnConfig;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        textView1 = (TextView) findViewById(R.id.textView1);
//        wifiApManager = new WifiApManager(this);

//        WifiConfiguration netConfig = new WifiConfiguration();
//
//        netConfig.SSID = "MyAP";
//        netConfig.preSharedKey = "1234";
//        netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
//        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//        netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//
//        try {
//            
//            wifiApManager.setWifiApConfiguration(netConfig);
//            Method setWifiApMethod = wifiApManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
//            boolean apstatus = (Boolean) setWifiApMethod.invoke(wifiApManager, netConfig, true);
//
//            Method isWifiApEnabledmethod = wifiApManager.getClass().getMethod("isWifiApEnabled");
//            while (!(Boolean) isWifiApEnabledmethod.invoke(wifiApManager)) {
//            }
//            ;
//            Method getWifiApStateMethod = wifiApManager.getClass().getMethod("getWifiApState");
//            WIFI_AP_STATE apstate = WIFI_AP_STATE.valueOf("" + getWifiApStateMethod.invoke(wifiApManager));
//            Method getWifiApConfigurationMethod = wifiApManager.getClass().getMethod("getWifiApConfiguration");
//            netConfig = (WifiConfiguration) getWifiApConfigurationMethod.invoke(wifiApManager);
//            Log.e("CLIENT", "\nSSID:" + netConfig.SSID + "\nPassword:" + netConfig.preSharedKey + "\n");
//
//        } catch (Exception e) {
//            Log.e(this.getClass().toString(), "", e);
//        }
//
//        scan();

        btnConfig = (Button) findViewById(R.id.btnConfigure);
        btnConfig.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                Intent hotspotConfig = new Intent(getApplicationContext(), WifiHotspotActivity.class);
                startActivity(hotspotConfig);
            }
        });
    }

    private void scan() {
        wifiApManager.getClientList(false, new FinishScanListener() {

            @Override
            public void onFinishScan(final ArrayList<ClientScanResult> clients) {

                textView1.setText("WifiApState: " + wifiApManager.getWifiApState() + "\n\n");
                textView1.append("Clients: \n");
                for (ClientScanResult clientScanResult : clients) {
                    textView1.append("####################\n");
                    textView1.append("IpAddr: " + clientScanResult.getIpAddr() + "\n");
                    textView1.append("Device: " + clientScanResult.getDevice() + "\n");
                    textView1.append("HWAddr: " + clientScanResult.getHWAddr() + "\n");
                    textView1.append("isReachable: " + clientScanResult.isReachable() + "\n");
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Get Clients");
        menu.add(0, 1, 0, "Open AP");
        menu.add(0, 2, 0, "Close AP");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case 0:
            scan();
            break;
        case 1:
            wifiApManager.setWifiApEnabled(null, true);
            break;
        case 2:
            wifiApManager.setWifiApEnabled(null, false);
            break;
        }

        return super.onMenuItemSelected(featureId, item);
    }
}