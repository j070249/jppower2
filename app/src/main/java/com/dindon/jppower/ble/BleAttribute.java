package com.dindon.jppower.ble;
import java.util.UUID;

/**
 * Created by JimmyTai on 2017/5/22.
 */

public class BleAttribute {
    public static final String DEVICE_NAME = "JPModel";
    public static final UUID UUID_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_DES = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

}