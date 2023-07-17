package com.zzowo.bluetooth1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    String myDeviceName = "";
    EditText deviceName;
    Button setDeviceName;

    Button GetDevBtn, ConnectBtn, SendBtn;
    EditText InoutTxt;
    TextView ResultTxt;
    TextView devicesTxt;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    BluetoothSocket bluetoothSocket;
    IntentFilter intentFilter;

    InputStream inputStream;
    OutputStream outputStream;
    RxThread rxThread;

    String RxData = "";

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // test
        deviceName = findViewById(R.id.device_name);
        setDeviceName = findViewById(R.id.btn0);

        GetDevBtn = findViewById(R.id.btn1);
        ConnectBtn = findViewById(R.id.btn2);
        SendBtn = findViewById(R.id.btn3);
        InoutTxt = findViewById(R.id.txt1);
        ResultTxt = findViewById(R.id.txt2);
        devicesTxt = findViewById(R.id.devices);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        rxThread = new RxThread();

        registerReceiver(Btreceiver, intentFilter);
        ConnectBtn.setEnabled(false);

        initPermission();

        setDeviceName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDeviceName = deviceName.getText().toString();
                deviceName.setText("");
                setDeviceName.setText(myDeviceName);
            }
        });

        GetDevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
                String allDev = "All Devices:\n ";
                for (BluetoothDevice dev : devices) {
                    allDev += (dev.getName() + "\n    " + dev.getAddress() + "\n");
                    if (dev.getName().equals(myDeviceName)) {
                        bluetoothDevice = dev;
                        bluetoothAdapter.cancelDiscovery();
                        Toast.makeText(MainActivity.this, "Detected your device", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                devicesTxt.setText("Result \n" + allDev);
            }
        });

        ConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                    bluetoothSocket.connect();
                    inputStream = bluetoothSocket.getInputStream();
                    outputStream = bluetoothSocket.getOutputStream();
                    rxThread.start();
                    ConnectBtn.setText("Connected: " + myDeviceName);
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {

                }
            }
        });

        SendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    outputStream.write((InoutTxt.getText() + "\r\n").getBytes());
                } catch (Exception e) {

                }
            }
        });
    }

    private void initPermission() {
        int permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    1
            );
        } else if (permission2 != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_LOCATION,
                    1
            );
        }
    }

    class RxThread extends Thread {
        public Boolean isRunning;
        byte[] rx;
        RxThread() {
            isRunning = true;
            rx = new byte[10];
        }

        @Override
        public void run() {
            while (isRunning) {
                try {
                    if (inputStream.available() > 2) {
                        inputStream.read(rx);
                        RxData = new String(rx);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!RxData.equals("")) {
                                ResultTxt.setText(RxData);
                            }
                        }
                    });
                    Thread.sleep(10);
                } catch (Exception e) {

                }
            }
        }
    }

    BroadcastReceiver Btreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ConnectBtn.setEnabled(true);
                        }
                    });
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    rxThread.isRunning = false;
                    break;

            }
        }
    };
}