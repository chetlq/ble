package com.example.mybattarymonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mybattarymonitor.Handler.NetworkServiceHolder;
import com.example.mybattarymonitor.Handler.User;
import com.example.mybattarymonitor.Handler.User1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BatteryDetectorActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.startScan)
    Button button;
    @BindView(R.id.getData)
    Button getData;
    @BindView(R.id.connectDevice)
    Button connectDevice;

    @BindView(R.id.deviceState)
    TextView deviceStatus;
    @BindView(R.id.batteryLevel)
    TextView batteryLevel;

    @BindView(R.id.deviceAddress)
    TextView deviceAddress;
    @BindView(R.id.deviceName)
    TextView deviceName;
    @BindView(R.id.serviceName)
    TextView serviceName;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    BluetoothDevice bluetoothDevice;
    private boolean mScanning;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothAdapter mBluetoothAdapter;

    @BindView(R.id.connectService)
    Button connectService;

    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private boolean mConnected = false;
    User user = null;
    private BluetoothLEService mBluetoothLEService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLEService = ((BluetoothLEService.LocalBinder) service).getService();
            if (!mBluetoothLEService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLEService.connect(bluetoothDevice.getAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLEService.disconnect();
            mBluetoothLEService = null;
        }
    };


    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLEService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState("connected");
                invalidateOptionsMenu();
            } else if (BluetoothLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState("disconnected");
                //clearUI();
            } else if (BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLEService.getSupportedGattServices());
                if (mNotifyCharacteristic != null) {
                    final int charaProp = mNotifyCharacteristic.getProperties();
//                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                        mBluetoothLEService.readCharacteristic(mNotifyCharacteristic);
//                    }
//                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                        mBluetoothLEService.setCharacteristicNotification(mNotifyCharacteristic, true);
//                    }"psw:1234/14:00/13:00"   psw:"key"/"sinceHour":"sinceMin"/"beforeHour":"beforeMin"
                    if (((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)&& user!=null) {
                        StringBuffer sb = new StringBuffer("psw:").
                                append(user.getKey()).append("/").
                                append(user.getSinceHour()).append("/").
                                append(user.getSinceMin()).append("/").
                                append(user.getBeforeHour()).append("/").
                                append(user.getBeforeMin()).append("/");
                        mBluetoothLEService.writeCharacteristic(mNotifyCharacteristic,sb.toString());
                    }
                    try {
                        Thread.sleep(5000);
                        mBluetoothLEService.disconnect();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            } else if (BluetoothLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLEService.EXTRA_DATA));
            }
        }
    };

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            bluetoothDevice = result.getDevice();
            deviceAddress.setText(bluetoothDevice.getAddress());
            deviceName.setText(bluetoothDevice.getName());
            Log.d(TAG, "onScanResult " + bluetoothDevice.getAddress());
            progressBar.setVisibility(View.INVISIBLE);
            if (bluetoothDevice != null) {
                        progressBar.setVisibility(View.VISIBLE);
                        Intent gattServiceIntent = new Intent(BatteryDetectorActivity.this, BluetoothLEService.class);
                        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG, "Scanning Failed " + errorCode);
            progressBar.setVisibility(View.INVISIBLE);
        }
    };

    private static IntentFilter GattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_detector);
        ButterKnife.bind(this);

        mBluetoothAdapter = BluetoothUtils.getBluetoothAdapter(BatteryDetectorActivity.this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                startScanning(true);
            }
        });

        connectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothDevice != null) {

                    progressBar.setVisibility(View.VISIBLE);
                    Intent gattServiceIntent = new Intent(BatteryDetectorActivity.this, BluetoothLEService.class);
                    bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                    final boolean result = mBluetoothLEService.connect(bluetoothDevice.getAddress());
                    Log.d(TAG, "Connect request result=" + result);
                }
            }
        });

        getData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkServiceHolder.getInstance()
                        .getJSONApi()


                        .getHandler1("getconf")
                        .enqueue(new Callback<User1>() {
                            @Override
                            public void onResponse(Call<User1> call, Response<User1> response) {
                                Optional<User> optionalUser  = response.body().getUser().stream().findFirst();
                                user = optionalUser.get();
                                if(user!=null) button.setEnabled(true);

                              //  textView.append( user.getKey()+">>>\n");
                            }

                            @Override
                            public void onFailure(Call<User1> call, Throwable t) {

                            }
                        });

            }
        });

        connectService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mBluetoothLEService.connect(bluetoothDevice.getAddress());
                Log.d(TAG, "Connect request result>>=" + mBluetoothLEService.getConnected());


//                if (mNotifyCharacteristic != null) {
//                    final int charaProp = mNotifyCharacteristic.getProperties();
////                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
////                        mBluetoothLEService.readCharacteristic(mNotifyCharacteristic);
////                    }
////                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
////                        mBluetoothLEService.setCharacteristicNotification(mNotifyCharacteristic, true);
////                    }"psw:1234/14:00/13:00"   psw:"key"/"sinceHour":"sinceMin"/"beforeHour":"beforeMin"
//                    if (((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)&& user!=null) {
//                        StringBuffer sb = new StringBuffer("psw:").
//                                append(user.getKey()).append("/").
//                                append(user.getSinceHour()).append("/").
//                                append(user.getSinceMin()).append("/").
//                                append(user.getBeforeHour()).append("/").
//                                append(user.getBeforeMin()).append("/");
//                        mBluetoothLEService.writeCharacteristic(mNotifyCharacteristic,sb.toString());
//                    }
//                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.REQUEST_LOCATION_ENABLE_CODE);
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Your devices that don't support BLE", Toast.LENGTH_LONG).show();
            finish();
        }
        if (!mBluetoothAdapter.enable()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, Constants.REQUEST_BLUETOOTH_ENABLE_CODE);
        }
        registerReceiver(mGattUpdateReceiver, GattUpdateIntentFilter());
        if (mBluetoothLEService != null) {
            final boolean result = mBluetoothLEService.connect(bluetoothDevice.getAddress());
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLEService = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_BLUETOOTH_ENABLE_CODE && resultCode == RESULT_CANCELED) {
            finish();
        }
    }


    private void startScanning(final boolean enable) {
        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        Handler mHandler = new Handler();
        if (enable) {
            List<ScanFilter> scanFilters = new ArrayList<>();
            final ScanSettings settings = new ScanSettings.Builder().build();
            ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(SampleGattAttributes.TIME_SERVICE)).build();//UUID_BATTERY_SERVICE)).build();
            scanFilters.add(scanFilter);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    progressBar.setVisibility(View.INVISIBLE);
                    bluetoothLeScanner.stopScan(scanCallback);
//                    if (bluetoothDevice != null) {
//                        progressBar.setVisibility(View.VISIBLE);
//                        Intent gattServiceIntent = new Intent(BatteryDetectorActivity.this, BluetoothLEService.class);
//                        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
//                    }

                }
            }, Constants.SCAN_PERIOD);
            mScanning = true;
            bluetoothLeScanner.startScan(scanFilters, settings, scanCallback);
        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }


    private void updateConnectionState(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceStatus.setText(status);
                if (mNotifyCharacteristic != null)
                {
                    final int charaProp = mNotifyCharacteristic.getProperties();
//                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                        mBluetoothLEService.readCharacteristic(mNotifyCharacteristic);
//                    }
//                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                        mBluetoothLEService.setCharacteristicNotification(mNotifyCharacteristic, true);
//                    }"psw:1234/14:00/13:00"   psw:"key"/"sinceHour":"sinceMin"/"beforeHour":"beforeMin"
                    if (((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)&& user!=null) {
                        StringBuffer sb = new StringBuffer("psw:").
                                append(user.getKey()).append("/").
                                append(user.getSinceHour()).append("/").
                                append(user.getSinceMin()).append("/").
                                append(user.getBeforeHour()).append("/").
                                append(user.getBeforeMin()).append("/");
                        mBluetoothLEService.writeCharacteristic(mNotifyCharacteristic,sb.toString());
                    }
                }

            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            batteryLevel.setText(data);
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        String uuid = null;
        String serviceString = "unknown service";
        String charaString = "unknown characteristic";

        for (BluetoothGattService gattService : gattServices) {

            uuid = gattService.getUuid().toString();

            serviceString = SampleGattAttributes.lookup(uuid);

            if (serviceString != null) {
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();

                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    int a = gattCharacteristic.getWriteType();
                    HashMap<String, String> currentCharaData = new HashMap<String, String>();
                    uuid = gattCharacteristic.getUuid().toString();
                    charaString = SampleGattAttributes.lookup(uuid);
                    if (charaString != null) {
                        serviceName.setText(charaString);
                    }
                    if ( gattCharacteristic.getUuid().equals(UUID.fromString(SampleGattAttributes.LOCAL_TIME_INFO))) {
                        mNotifyCharacteristic = gattCharacteristic;
                        return;
                    }
                }
            }
        }
    }
}
