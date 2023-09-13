package com.example.groverymanagment;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import classRecyclerView.CustomAdapterDiscoveryDevices;

/**
 * Class that implements the connect_to_other_device GUI, where you search for new device to connect with Bluetooth.
 */
public class ConnectToDeviceActivity extends AppCompatActivity {

    private static final String TAG = "ConnectToDeviceActivity";
    /**
     * String value that represents the name for the extra String to put for an Intent.
     */
    public static final String RECIPES_EXTRA = "RecipesReceived";
    /**
     * String value that represents the name for the extra String to put for an Intent.
     */
    public static final String INGREDIENTS_EXTRA = "IngredientsReceived";
    /**
     * String that representes the name for the extra Strng to put for an Intent.
     */
    public static final String NAME_DEVICE_CONNECTED_TO_EXTRA = "NameDeviceConnectedTo";
    /**
     * UUID object used to connect with Bluetooth "server". This UUID must be the same of Bluetooth "server"'s UUID.
     */
    private final UUID myUUID = UUID.fromString("d364b420-8d71-11e3-baa8-0800200c9a66");

    /**
     * GUI layout.
     */
    RelativeLayout relativeLayout;
    /**
     * GUI Button used to discover other devices to do Bluetooth connection.
     */
    Button buttonDiscoveryDevices;
    /**
     * GUI RecyclerView that contains CustomAdapterDiscoveryDevices object.
     */
    RecyclerView recyclerViewDevices;
    /**
     * GUI Toolbar.
     */
    Toolbar toolbar;

    /**
     * Bluetooth adapter object.
     */
    private BluetoothAdapter bluetoothAdapter;

    /**
     * Local list of BluetoothDevice object. It contains all the discovered devices.
     */
    private List<BluetoothDevice> devicesDiscovered = new ArrayList<>();
    /**
     * Local list of String. It contains all the name of the discovered devices.
     */
    private List<String> nameDevicesDiscovered = new ArrayList<>();
    /**
     * IntentFilter object used to handle BluetoothDevice.ACTION_FOUND and BluetoothAdapter.ACTION_DISCOVERY_STARTED Intent
     */
    private IntentFilter intentFilter = new IntentFilter();
    /**
     * Adapter used to show discovery devices data. This adapter must be set for the recycler view GUI.
     */
    private CustomAdapterDiscoveryDevices adapterDiscoveryDevices;

    /**
     * Integer that represents the duration discoverability and it's set to limit the time when the device can be discovered by other devices.
     */
    private final int DURATION_DISCOVERABILITY = 300;

    /**
     * Launcher used to launch the Intent to enable Bluetooth, only in case the Bluetooth is disabled.
     */
    ActivityResultLauncher<Intent> launcherEnableBluetooth = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    showCustomSnackbar("Bluetooth has been enable", R.drawable.ic_bluetooth_enable);
                } else {
                    showCustomSnackbar("Failure to enable bluetooth", R.drawable.ic_bluetooth_disable);
                }
            });

    /**
     * Launcher used to launch the Intent to set the device discoverability.
     */
    ActivityResultLauncher<Intent> launcherEnableDiscoverability = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == DURATION_DISCOVERABILITY) {
                    showCustomSnackbar("This device can be connected with others devices", R.drawable.ic_success);
                } else {
                    showCustomSnackbar("This device can't connect with others devices", R.drawable.ic_warning_error);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_to_others_device);

        relativeLayout = findViewById(R.id.relative_layout);

        this.toolbar = findViewById(R.id.toolbar_connect_to_device);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Search devices");

        this.adapterDiscoveryDevices = new CustomAdapterDiscoveryDevices(this);

        this.buttonDiscoveryDevices = findViewById(R.id.button_discovery_peers);
        this.recyclerViewDevices = findViewById(R.id.recycler_view_devices);
        this.recyclerViewDevices.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        this.recyclerViewDevices.setAdapter(adapterDiscoveryDevices);

        if (checkAndRequestPermissions()) {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            this.bluetoothAdapter = bluetoothManager.getAdapter();

            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                launcherEnableBluetooth.launch(enableBluetoothIntent);
            }

            /**
             * Intent object used to launch BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE.
             */
            Intent enableDiscoverabilityIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            enableDiscoverabilityIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DURATION_DISCOVERABILITY);
            launcherEnableDiscoverability.launch(enableDiscoverabilityIntent);

            this.intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            this.intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            registerReceiver(receiver, intentFilter);

            this.buttonDiscoveryDevices.setOnClickListener(view -> {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    if (bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();
                    }
                    bluetoothAdapter.startDiscovery();
                }
            });

            this.adapterDiscoveryDevices.setOnClickListener((position, device, nameDevice) -> {
                showCustomSnackbar("Connecting to: " + nameDevice, R.drawable.ic_bluetooth_connect);
                ConnectThread connectThread = new ConnectThread(device, nameDevice);
                connectThread.start();
            });
        }
    }

    /**
     * Private method used to check if all the Bluetooth permissions have been granted by the user.
     * @return True if all the Bluetooth permissions have been granted, false otherwise.
     */
    private boolean checkAndRequestPermissions() {
        int permissionBluetoothConnect = ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_CONNECT);
        int permissionBluetoothScan = ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_SCAN);
        int permissionBluetoothAdvertise = ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_ADVERTISE);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionBluetoothConnect != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        if (permissionBluetoothAdvertise != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_ADVERTISE);
        }
        if (permissionBluetoothScan != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            String[] permissions = new String[listPermissionsNeeded.size()];
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(permissions), 1);
            return false;
        }
        return true;
    }

    /**
     * Private class BroadcastReceiver created to handle BluetoothDevice.ACTION_FOUND and BluetoothAdapter.ACTION_DISCOVERY_STARTED.
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        /**
         * Method used to check which Intent has been received by BroadcastReceiver.
         * @param context Context object where the BroadcastReceiver has been created.
         * @param intent Intent object used to check which Intent has been received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                showCustomSnackbar("Discovery started", R.drawable.ic_bluetooth_searching);
            } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    BluetoothDevice deviceDiscovered = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    devicesDiscovered.add(deviceDiscovered);
                    nameDevicesDiscovered.add(deviceDiscovered.getName());
                    adapterDiscoveryDevices.setDiscoveredDevices(devicesDiscovered);
                    adapterDiscoveryDevices.setNameDiscoveredDevices(nameDevicesDiscovered);
                }
            }
        }
    };

    /**
     * Private class that implements the logic of Bluetooth "client". The entity that request a Bluetooth connection.
     */
    private class ConnectThread extends Thread {
        /**
         * BluetoothSocket class used to connect with the "server".
         */
        private BluetoothSocket bluetoothSocket;
        /**
         * String value that represents the name of the Bluetooth "server".
         */
        private String nameDeviceConnectedTo;

        /**
         * ConnectedThread constructor.
         * @param device BluetoothDevice object that represents the Bluetooth "server".
         * @param nameDevice String value that represents the name of the Bluetooth "server".
         */
        public ConnectThread(BluetoothDevice device, String nameDevice) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                try {
                    this.bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
                    this.nameDeviceConnectedTo = nameDevice;
                } catch (IOException e) {
                    Log.e(TAG, "Socket's create method failed ", e);
                }
            }
        }

        @Override
        public void run() {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter.cancelDiscovery();
                try {
                    this.bluetoothSocket.connect();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's connect method failed", e);
                    try {
                        this.bluetoothSocket.close();
                    } catch (IOException ex) {
                        Log.e(TAG, "Socket's close method failed ", ex);
                    }
                }
            }

            handleConnectedSocket(bluetoothSocket);
        }

        /**
         * Private method used to handle the socket connection.
         * This method received the serialized objects from Bluetooth "server" and then it opens shared_recipes_activity GUI,
         * implemented by SharedRecipesActivity.class, where the user can see all the recipes shared by the Bluetooth "server".
         * @param socket BluetoothSocket object that has been used to connect with the server.
         */
        private void handleConnectedSocket(BluetoothSocket socket) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String recipesReceived = receiveFromServerBluetooth(reader);
                String ingredientsReceived = receiveFromServerBluetooth(reader);

                Intent i = new Intent(ConnectToDeviceActivity.this, SharedRecipesActivity.class);
                i.putExtra(RECIPES_EXTRA, recipesReceived);
                i.putExtra(INGREDIENTS_EXTRA, ingredientsReceived);
                i.putExtra(NAME_DEVICE_CONNECTED_TO_EXTRA, this.nameDeviceConnectedTo);
                startActivity(i);

            } catch (IOException e) {
                Log.e(TAG, "Error in creating buffered reader: ", e);
            }
        }

        /**
         * Private method used to receive the serialized objects from Bluetooth "server".
         * @param reader BufferedReader object used to read the Json String received.
         * @return String value that representd the Json String received.
         * @throws IOException Occurs only when there are I/O errors while reading the Json String.
         */
        private String receiveFromServerBluetooth(BufferedReader reader) throws IOException {
            String encodedJson = reader.readLine();

            return new String(Base64.getDecoder().decode(encodedJson.getBytes(StandardCharsets.UTF_8)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            Map<String, Integer> perms = new HashMap<>();
            // Initialize the map with both permissions
            perms.put(Manifest.permission.BLUETOOTH_CONNECT, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.BLUETOOTH_ADVERTISE, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.BLUETOOTH_SCAN, PackageManager.PERMISSION_GRANTED);
            // Fill with actual results from user
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for both permissions
                if (Objects.requireNonNull(perms.get(Manifest.permission.BLUETOOTH_CONNECT)) == PackageManager.PERMISSION_GRANTED
                        && Objects.requireNonNull(perms.get(Manifest.permission.BLUETOOTH_ADVERTISE)) == PackageManager.PERMISSION_GRANTED
                        && Objects.requireNonNull(perms.get(Manifest.permission.BLUETOOTH_SCAN)) == PackageManager.PERMISSION_GRANTED) {
                    showCustomSnackbar("All permessions are granted", R.drawable.ic_success);
                    // process the normal flow
                    //else any one or both the permissions are not granted
                } else {
                    showCustomSnackbar("Some permissions are not granted. Ask again", R.drawable.ic_warning_error);
                    //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                    // shouldShowRequestPermissionRationale will return true
                    //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_SCAN) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_CONNECT)
                            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_ADVERTISE)) {
                        showDialogOK(
                                (dialog, which) -> {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            checkAndRequestPermissions();
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            finish();
                                            break;
                                    }
                                });
                    }
                    //permission is denied (and never ask again is  checked)
                    //shouldShowRequestPermissionRationale will return false
                    else {
                        showCustomSnackbar("Go to settings and enable permissions", R.drawable.ic_warning_error);
                        finish();
                    }
                }
            }
        }

    }

    private void showDialogOK(DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(this)
                .setMessage("All the bluetooth permissions and location permission must be granted")
                .setPositiveButton("OK", onClickListener)
                .setNegativeButton("Cancel", onClickListener)
                .create()
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    /**
     * Private method used to show a custom snackbar used to notify the user about error or successful operation.
     * @param message String value that represents the message used to custom the snackbar.
     * @param icon Integer value that represents the icon used to custom the snackbar.
     */
    private void showCustomSnackbar(String message, int icon) {
        Snackbar snackbar = Snackbar.make(relativeLayout, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();

        TextView snackbarTextView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        snackbarTextView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);

        snackbar.show();
    }
}
