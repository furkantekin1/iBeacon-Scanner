package com.furkotek.ibeacon;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;

public class BeaconInfo extends AppCompatActivity implements BeaconConsumer, RangeNotifier {

    private final String TAG = "BeaconInfo";
    public static final String EXTRA = "UUID";

    private TextView txtDeviceName, txt_rssi, txt_mac, txt_uuid, txt_major, txt_minor, txt_tx;

    private BeaconManager beaconManager;
    private BluetoothAdapter bluetoothAdapter;

    void init(){
        txtDeviceName = findViewById(R.id.txt_device_name);
        txt_rssi = findViewById(R.id.txt_rssi);
        txt_mac = findViewById(R.id.txt_mac);
        txt_uuid = findViewById(R.id.txt_uuid);
        txt_major = findViewById(R.id.txt_major);
        txt_minor = findViewById(R.id.txt_minor);
        txt_tx = findViewById(R.id.txt_tx);
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_info);
        init();
        scan();
    }

    public void scan() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter.isEnabled()){
            beaconManager = BeaconManager.getInstanceForApplication(BeaconInfo.this);
            beaconManager.getBeaconParsers().add(new BeaconParser().
                    setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
            beaconManager.bind(this);
        }
        else {
            finish();
        }



    }

    @Override
    public void onBeaconServiceConnect() {
        ArrayList<Identifier> identifiers = new ArrayList<Identifier>();
        identifiers.add(null);
        Region region = new Region("AllBeaconsRegion", identifiers);
        try {
            beaconManager.startRangingBeaconsInRegion(region);
            Toast.makeText(this, getString(R.string.getting_information), Toast.LENGTH_LONG).show();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        beaconManager.addRangeNotifier(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

        for (int i = 0; i < ((ArrayList) beacons).size(); i++) {
            Beacon bc = (Beacon) ((ArrayList) beacons).get(i);
            String uuid = bc.getId1().toString();
            if (getIntent().getStringExtra(EXTRA).equals(uuid)) {
                txtDeviceName.setText(bc.getBluetoothName());
                txt_rssi.setText("RSSI: " + bc.getRssi());
                txt_uuid.setText("UUID: " + uuid);
                txt_major.setText("MAJOR: " + bc.getId2() + "");
                txt_minor.setText("MINOR: " + bc.getId3() + "");
                txt_tx.setText("TX POWER: " + bc.getTxPower());
                txt_mac.setText("MAC: " + bc.getBluetoothAddress());
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if(beaconManager != null)
            beaconManager.unbind(this);
    }
}
