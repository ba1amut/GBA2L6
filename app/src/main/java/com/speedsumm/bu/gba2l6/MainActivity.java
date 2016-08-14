package com.speedsumm.bu.gba2l6;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String FILE_NAME = "SMSBank.txt";
    Cursor cursor;
    TextView tvLog;
    BluetoothAdapter bluetoothAdapter;
    ArrayList<BluetoothDevice> btDevices;
    BroadcastReceiver mBroadcastReceiver;
    public ArrayList<String> needPermitionsList;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvLog = (TextView) findViewById(R.id.tvLog);
        btDevices = new ArrayList<BluetoothDevice>();
        mBroadcastReceiver = new BluetoothReceiver();
        needPermitionsList = new ArrayList<>();

        permitionNeed(needPermitionsList, Manifest.permission.READ_SMS);
        permitionNeed(needPermitionsList, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (needPermitionsList.size() > 0) {
            ActivityCompat.requestPermissions(this, needPermitionsList.toArray(new String[needPermitionsList.size()]), REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }
    }


    private void permitionNeed(ArrayList<String> needPermitions, String checkPermission) {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, checkPermission) != PackageManager.PERMISSION_GRANTED) {
            needPermitions.add(checkPermission);
        }

    }

    public void OnClick(View view) {
        switch (view.getId()) {
            case R.id.btnSMSREader:

                Uri uri = Uri.parse("content://sms");
                cursor = getContentResolver().query(uri, null, null, null, null);
                File path = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "SMS");
                path.mkdir();
                tvLog.append("Создаем каталог " + path + "\n\n");
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(path, FILE_NAME));
                    tvLog.append("Создаем файл " + FILE_NAME + "\n\n");
                    assert cursor != null;
                    tvLog.append("Записываем данные \n\n");
                    while (cursor.moveToNext()) {
                        Log.d("...", cursor.getString(cursor.getColumnIndex("address")) + "\n"
                                + cursor.getString(cursor.getColumnIndex("body")));
                        fileOutputStream.write((cursor.getString(cursor.getColumnIndex("address")) + "," + cursor.getString(cursor.getColumnIndex("body")) + "\n").getBytes());
                    }
                    fileOutputStream.close();
                    tvLog.append("Сохраняем файл " + FILE_NAME + "\n\n");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btnBluetoothListener:

                btDevices.clear();
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                }
                boolean result = bluetoothAdapter.startDiscovery();
                Log.d("Результат сканирования", String.valueOf(result));
                tvLog.setText("");
        }
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mBroadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiver, filter);
        super.onStart();
    }

    public class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("...", "Обнаружено устройство" + btDevice.getName());
                btDevices.add(btDevice);
                tvLog.append(btDevice.getName() + "," + btDevice.getType() + "\n");
            }
        }
    }
}
