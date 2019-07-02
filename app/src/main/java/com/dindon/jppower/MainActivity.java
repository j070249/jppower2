package com.dindon.jppower;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.net.wifi.aware.Characteristics;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.dindon.jppower.ble.BleAttribute;
import com.dindon.jppower.ble.BleScan;
import com.dindon.jppower.utils.SendData;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private BleScan bleScan;
    BluetoothClient client;
    private SendData sendData;

    private int mode = 0;

    public int getMode() {
        return mode;
    }

    public void setSendData(SendData sendData) {     //create setter for interface
        this.sendData = sendData;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        bleScan = BleScan.getInstance(this);
        client = new BluetoothClient(getApplicationContext());
        initViews();
        tv_ble_status.setVisibility(View.VISIBLE);
        tv_ble_status.setText("藍牙狀態：尋找裝置中");
        boolean result = bleScan.scan(this, bleScanCallback);
//        Log.d(TAG, "start scan result: " + result);
        fragments.transaction(HomeFragment.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (JPresult != null) {
            client.disconnect(JPresult.getDevice().getAddress());
            client.unnotify(JPresult.getDevice().getAddress(), BleAttribute.UUID_SERVICE, BleAttribute.UUID_CHAR, bleUnnotifyResponse);
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
//        if (JPresult != null) {
//            client.disconnect(JPresult.getDevice().getAddress());
//            client.unnotify(JPresult.getDevice().getAddress(), BleAttribute.UUID_SERVICE, BleAttribute.UUID_CHAR, bleUnnotifyResponse);
//        }
//        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
//        if (JPresult != null) {
//            client.disconnect(JPresult.getDevice().getAddress());
//            client.unnotify(JPresult.getDevice().getAddress(), BleAttribute.UUID_SERVICE, BleAttribute.UUID_CHAR, bleUnnotifyResponse);
//        }
//        finish();
    }

    ScanResult JPresult;

    private ScanCallback bleScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
//            Log.d(TAG, "scan result: " + result.getDevice().getAddress() + " -> " + result.getDevice().getName());
            if (result.getDevice().getName() != null) {
                if (result.getDevice().getName().equals(BleAttribute.DEVICE_NAME)) {
                    JPresult = result;
                    BleConnectOptions options = new BleConnectOptions.Builder()
                            .setConnectRetry(2)
                            .setConnectTimeout(15000)
                            .setServiceDiscoverRetry(2)
                            .setServiceDiscoverTimeout(10000)
                            .build();
                    client.connect(result.getDevice().getAddress(), options, new BleConnectResponse() {
                        @Override
                        public void onResponse(int code, BleGattProfile data) {
//                            for (int i = 0; i < data.getServices().size(); i++) {
//                                Log.d(TAG, "Service result: " + data.getServices().get(i).toString());
//                                for (int j = 0; j < data.getServices().get(i).getCharacters().size(); j++) {
//                                    Log.d(TAG, "  Characte result: " + data.getServices().get(i).getCharacters().get(j).toString());
//                                    for (int k = 0; k < data.getServices().get(i).getCharacters().get(j).getDescriptors().size(); k++) {
//                                        Log.d(TAG, "    Descriptor result: " + data.getServices().get(i).getCharacters().get(j).getDescriptors().get(k).toString());
//                                    }
//                                }
//                            }
                            if (code == Constants.REQUEST_SUCCESS) {
//                                client.read(result.getDevice().getAddress(), BleAttribute.UUID_SERVICE, BleAttribute.UUID_CHAR, bleReadResponse);
                                client.notify(result.getDevice().getAddress(), BleAttribute.UUID_SERVICE, BleAttribute.UUID_CHAR, bleNotifyResponse);
//                                client.readDescriptor(result.getDevice().getAddress(), BleAttribute.UUID_SERVICE, BleAttribute.UUID_CHAR, BleAttribute.UUID_DES, bleReadResponse);
//                                client.writeDescriptor(result.getDevice().getAddress(), BleAttribute.UUID_SERVICE, BleAttribute.UUID_CHAR, BleAttribute.UUID_DES, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, bleWriteResponse);
//                                client.readDescriptor(result.getDevice().getAddress(), BleAttribute.UUID_SERVICE, BleAttribute.UUID_CHAR, BleAttribute.UUID_DES, bleReadResponse);
                                Toast.makeText(MainActivity.this, "connect success", Toast.LENGTH_SHORT).show();
                                tv_ble_status.setVisibility(View.GONE);
                                bleScan.stopScan(bleScanCallback);
                                client.registerConnectStatusListener(result.getDevice().getAddress(), new BleConnectStatusListener() {
                                    @Override
                                    public void onConnectStatusChanged(String mac, int status) {
                                        Log.d(TAG, "status " + mac + "/" + status);
                                        if (status == Constants.STATUS_CONNECTED) {
                                            tv_ble_status.setVisibility(View.GONE);
                                        } else if (status == Constants.STATUS_DISCONNECTED) {
//                                            client.disconnect(JPresult.getDevice().getAddress());
//                                            client.unnotify(JPresult.getDevice().getAddress(), BleAttribute.UUID_SERVICE, BleAttribute.UUID_CHAR, bleUnnotifyResponse);
                                            tv_ble_status.setVisibility(View.VISIBLE);
                                            tv_ble_status.setText("藍牙狀態：尋找裝置中");
                                            bleScan.scan(MainActivity.this, bleScanCallback);
                                        } else {
//                                            client.disconnect(JPresult.getDevice().getAddress());
//                                            client.unnotify(JPresult.getDevice().getAddress(), BleAttribute.UUID_SERVICE, BleAttribute.UUID_CHAR, bleUnnotifyResponse);
                                            tv_ble_status.setVisibility(View.VISIBLE);
                                            tv_ble_status.setText("藍牙狀態：尋找裝置中");
                                            bleScan.scan(MainActivity.this, bleScanCallback);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private BleReadResponse bleReadResponse = new BleReadResponse() {
        @Override
        public void onResponse(int code, byte[] data) {
//            Log.d(TAG, "code : " + code + "/bleReadResponse result: " + Arrays.toString(data));

        }
    };

    private BleNotifyResponse bleNotifyResponse = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {
            String s = new String(value);
//            Log.d(TAG, "bleNotifyResponse result: " + s);
//            Toast.makeText(MainActivity.this, "bleNotifyResponse result: " + Arrays.toString(value), Toast.LENGTH_SHORT).show();
            if (s.equals("N"))
                mode = 0;
            if (s.equals("S1"))
                mode = 1;
            if (s.equals("S2"))
                mode = 2;
            if (s.equals("S3"))
                mode = 3;
            if (s.equals("S4"))
                mode = 4;
            if (s.equals("S5"))
                mode = 5;
            if (s.equals("S6"))
                mode = 6;
            if (s.equals("S7"))
                mode = 7;
            if (s.equals("S1_1"))
                mode = 8;
            if (s.equals("S1_2"))
                mode = 9;
            if (s.equals("S1_3"))
                mode = 10;
            if (s.equals("S1_4"))
                mode = 11;
            if (s.equals("S1_2_2"))
                mode = 16;
            if (s.equals("S1_3_1"))
                mode = 17;
            if (s.equals("S1_3_2"))
                mode = 18;
            sendData.onDataListener(mode);
        }

        @Override
        public void onResponse(int code) {

        }
    };

    private BleUnnotifyResponse bleUnnotifyResponse = new BleUnnotifyResponse() {

        @Override
        public void onResponse(int code) {

        }
    };

    private BleWriteResponse bleWriteResponse = new BleWriteResponse() {
        @Override
        public void onResponse(int code) {
//            Log.d(TAG, "bleWriteResponse result: " + code);
        }
    };

    private void initViews() {
    }

    /* --- Fragment --- */

    public Fragments fragments = new Fragments();

    public class Fragments {

        Map<String, Fragment> fragmentMap = new HashMap<>();
        Fragment fragment;

        Fragments() {
            fragmentMap.put(QA_Fragment.class.getSimpleName(), new QA_Fragment());
            fragmentMap.put(HomeFragment.class.getSimpleName(), new HomeFragment());
        }

        public Fragment getFragment(Class c) {
            return fragmentMap.get(c.getSimpleName());
        }

        public Fragment getFragment(String c) {
            return fragmentMap.get(c);
        }

        public void transaction(Class c) {
            fragment = fragmentMap.get(c.getSimpleName());
            getFragmentManager().beginTransaction().replace(R.id.cl_main, fragment, fragment.getClass().getSimpleName())
                    .commit();
        }

        public void transaction(String c) {
            fragment = fragmentMap.get(c);
            getFragmentManager().beginTransaction().replace(R.id.cl_main, fragment, fragment.getClass().getSimpleName())
                    .commit();
        }
    }

    @BindView(R.id.tv_ble_status)
    TextView tv_ble_status;
}
