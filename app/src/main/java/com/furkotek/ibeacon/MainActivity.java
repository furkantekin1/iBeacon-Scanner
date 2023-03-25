package com.furkotek.ibeacon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private final int REQUEST_ENABLE_BT = 10;
    private final int LOCATION_PERMISSION_REQUEST = 100;
    private final String TAG = "MainActivity";

    private Button btnTara;
    private ListView lv;
    private ProgressBar progressBar;

    private BluetoothAdapter btAdapter;
    private BluetoothLeScanner mLEScanner;
    private ArrayList<BeaconItem> beaconsList;
    private CustomListViewAdapter listViewAdapter;
    private AlertDialog.Builder permAlert;

    private final String[] locationPermission = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION};
    private final ArrayList filters = new ArrayList<>();
    private final ScanSettings settings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build();


    void init() {
        lv = findViewById(R.id.lvBeacon);
        btnTara = findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBar2);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        beaconsList = new ArrayList<BeaconItem>();
        listViewAdapter = new CustomListViewAdapter(MainActivity.this, beaconsList);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), BeaconInfo.class);
                i.putExtra(BeaconInfo.EXTRA, beaconsList.get(position).getUuid());
                startActivity(i);
            }
        });
        btnTara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btAdapter != null) {
                    if (!btAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    } else {
                        beaconsList.clear();
                        if(mLEScanner == null)
                            mLEScanner = btAdapter.getBluetoothLeScanner();
                        mLEScanner.startScan(filters, settings, mScanCallback);
                        progressBar.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mLEScanner.stopScan(mScanCallback);
                                progressBar.setVisibility(View.GONE);
                            }
                        }, 3000);
                    }
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.bt_not_found), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        checkAppPermissions();

    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i(TAG, "callbackType " + callbackType);
            byte[] scanRecord = result.getScanRecord().getBytes();
            findBeaconPattern(scanRecord);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i(TAG, "ScanResult - Results" + sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "Scan Failed Error Code: " + errorCode);
        }
    };


    private void findBeaconPattern(byte[] scanRecord) {
        int startByte = 2;
        boolean patternFound = false;
        while (startByte <= 5) {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                patternFound = true;
                break;
            }
            startByte++;
        }

        if (patternFound) {
            //Convert to hex String
            byte[] uuidBytes = new byte[16];
            System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
            String hexString = Utils.byteArrayToHexString(uuidBytes);

            //UUID detection
            String uuid = hexString.substring(0, 8) + "-" +
                    hexString.substring(8, 12) + "-" +
                    hexString.substring(12, 16) + "-" +
                    hexString.substring(16, 20) + "-" +
                    hexString.substring(20, 32);

            // major
            final int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

            // minor
            final int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);
            final int power = (scanRecord[startByte + 24] & 0xff) * 0x100 + (scanRecord[startByte + 24] & 0xff);

            BeaconItem beacon = new BeaconItem(uuid, major, minor);
            if (beaconsList.size() == 0) {
                beaconsList.add(beacon);
                lv.setAdapter(listViewAdapter);
            } else {
                if (!isExistBeaconInArray(beacon)) {
                    beaconsList.add(beacon);
                    lv.setAdapter(listViewAdapter);
                }
            }
        }
    }

    public boolean isExistBeaconInArray(BeaconItem item) {
        for (int i = 0; i < beaconsList.size(); i++) {
            if (((beaconsList.get(i).getMajor() == item.getMajor()) &&
                    (beaconsList.get(i).getMinor() == item.getMinor()) &&
                    (beaconsList.get(i).getUuid().equals(item.getUuid())))) {
                return true;
            }
        }
        return false;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, getString(R.string.bt_on_try_again), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.bt_cant_on), Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void checkAppPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Daha önceden izin kabul edilmemiş
                showRationaleDialog();
            } else {
                //Uygulama ilk defa açılmış
                ActivityCompat.requestPermissions(this, locationPermission, LOCATION_PERMISSION_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showRationaleDialog();
            }
            return;
        }
    }

    public void showRationaleDialog(){
        if (permAlert == null)
            permAlert = new AlertDialog.Builder(this);
        permAlert.setTitle(getString(R.string.perm_dialog_header));
        permAlert.setMessage(getString(R.string.perm_dialog_text));
        permAlert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        permAlert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION))
                    ActivityCompat.requestPermissions(MainActivity.this, locationPermission, LOCATION_PERMISSION_REQUEST);
                else {
                    Utils.openAppSettings(MainActivity.this);
                    finish();
                }

            }
        });
        permAlert.show();

    }

}
