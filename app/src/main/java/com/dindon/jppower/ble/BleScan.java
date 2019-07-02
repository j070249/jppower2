package com.dindon.jppower.ble;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by JimmyTai on 2017/11/19.
 */

public class BleScan {

    private static final int REQUEST_BLE_ENABLE = 1001;
    private static final int REQUEST_GPS = 1002;
    private static final int PERMISSION_REQUEST_CODE = 1003;

    private static BleScan bleScan;
    private BluetoothAdapter bluetoothAdapter;

    public static BleScan getInstance(Activity activity) {
        if (bleScan == null) {
            bleScan = new BleScan();
        }
        bleScan.checkPermission(activity);
        return bleScan;
    }

    private synchronized boolean checkPermission(Activity activity) {
        try {
            LocationManager locationManager = (LocationManager) (activity.getSystemService(Context.LOCATION_SERVICE));
            assert locationManager != null;
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                LocationServiceDialog gpsDialog = LocationServiceDialog.newInstance();
                gpsDialog.show(activity.getFragmentManager(), LocationServiceDialog.class.getSimpleName());
                gpsDialog.setCancelable(false);
                return false;
            }
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                    .PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_CODE);
                return false;
            }
            BluetoothManager bt_manager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            assert bt_manager != null;
            bluetoothAdapter = bt_manager.getAdapter();
            // 檢查設備是否支援藍牙
            if (bluetoothAdapter == null) {
                Toast.makeText(activity, "此設備不支援藍芽", Toast.LENGTH_SHORT).show();
                activity.finish();
                return false;
            }

            // 檢查設備是否支援BLE
            if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(activity, "此設備不支援BLE", Toast.LENGTH_SHORT).show();
                activity.finish();
                return false;
            }
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, REQUEST_BLE_ENABLE);
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    static void gpsDialogResult(Activity activity, boolean isGoToSetting) {
        if (isGoToSetting) activity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_GPS);
        else activity.finish();
    }

    private AtomicBoolean isScanning = new AtomicBoolean(false);

    public boolean isScanning() {
        return isScanning.get();
    }

    public synchronized boolean scan(Activity activity, ScanCallback scanCallback) {
        if (isScanning()) {
            return false;
        }
        if (!checkPermission(activity))
            return false;
        ScanSettings settings = new ScanSettings.Builder().build();
        List<ScanFilter> filters = new ArrayList<>();
        if (bluetoothAdapter == null)
            return false;
        bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
        isScanning.set(true);
        return true;
    }

    public synchronized boolean scan(Activity activity, List<ScanFilter> scanFilters, ScanCallback scanCallback) {
        if (isScanning()) {
            return false;
        }
        if (!checkPermission(activity))
            return false;
        ScanSettings settings = new ScanSettings.Builder().build();
        if (bluetoothAdapter == null)
            return false;
        bluetoothAdapter.getBluetoothLeScanner().startScan(scanFilters, settings, scanCallback);
        isScanning.set(true);
        return true;
    }

    public synchronized boolean scan(Activity activity, ScanSettings settings, List<ScanFilter> scanFilters, ScanCallback scanCallback) {
        if (isScanning()) {
            return false;
        }
        if (!checkPermission(activity))
            return false;
        if (bluetoothAdapter == null)
            return false;
        bluetoothAdapter.getBluetoothLeScanner().startScan(scanFilters, settings, scanCallback);
        isScanning.set(true);
        return true;
    }

    public void stopScan(ScanCallback scanCallback) {
        try {
            bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
        isScanning.set(false);
    }

    private Handler scanHandler = new Handler();
    private ScanRunnable scanRunnable;

    private class ScanRunnable implements Runnable {

        private Activity activity;
        private ScanCallback scanCallback;

        public ScanRunnable(Activity activity, ScanCallback scanCallback) {
            this.activity = activity;
            this.scanCallback = scanCallback;
        }

        @Override
        public void run() {
            stopScan(scanCallback);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!isScanning.get())
                return;
            scan(activity, scanCallback);
            scanRunnable = new ScanRunnable(activity, scanCallback);
            scanHandler.postDelayed(scanRunnable, 5 * 1000);
        }
    }
}
