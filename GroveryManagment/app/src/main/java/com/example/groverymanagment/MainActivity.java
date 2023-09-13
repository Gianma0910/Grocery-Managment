package com.example.groverymanagment;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import classRecyclerView.CustomAdapterShoppingList;
import database.GroveryManagmentDatabase;
import utilityClassDatabase.Ingredient;
import utilityClassDatabase.Recipe;

/**
 * Main class that implements the logic of home page application (activity main).
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    /** String used to put the name of shopping list as extra for an Intent.*/
    public static final String NAME_SHOPPING_LIST_EXTRA = "nameShoppingList";

    /** Name of the Bluetooth service.*/
    public static final String myName = "com.exmaple.groverymanagment";
    /** UUID used to do the Blueutooth connection.*/
    private final UUID myUUID = UUID.fromString("d364b420-8d71-11e3-baa8-0800200c9a66");
    /** Integer that represents the durability of bluetooth discoverability.*/
    public final int DURATION_DISCOVERABILITY = 300;

    /** GUI button to add a new shopping list.*/
    Button buttonAddNewShoppingList;
    /** GUI Toolbar.*/
    Toolbar toolbar;
    /** GUI layout.*/
    RelativeLayout relativeLayout;
    /** Adapter used to show shopping lists data. This adapter must be set for the recycler view of the GUI.*/
    private CustomAdapterShoppingList adapter;
    /** Recycler view of the GUI, used to contain CustomAdapterShoppingList object.*/
    private RecyclerView recyclerView;
    /** Local database of the application.*/
    private GroveryManagmentDatabase database;

    /** Bluetooth adapter*/
    private BluetoothAdapter bluetoothAdapter;

    /** Launcher used to launch the Intent to enable bluetooth.*/
    ActivityResultLauncher<Intent> launcherEnableBluetooth = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    showCustomSnackbar("Bluetooth has been enabled", R.drawable.ic_bluetooth_enable);
                } else {
                    showCustomSnackbar("Failure to enable bluetooth", R.drawable.ic_bluetooth_disable);
                }
            });

    /** Launcher to launch an Intent to enable bluetooth discoverability.*/
    ActivityResultLauncher<Intent> launcherEnableDiscoverability = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == DURATION_DISCOVERABILITY) {
                    showCustomSnackbar("This device can be discovered by others", R.drawable.ic_success);
                } else {
                    showCustomSnackbar("Failure to enable device's discoverability", R.drawable.ic_warning_error);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        relativeLayout = findViewById(R.id.relative_layout);
        buttonAddNewShoppingList = findViewById(R.id.button_add_new_shopping_list);
        toolbar = findViewById(R.id.toolbar_homepage);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Grovery Management");

        recyclerView = findViewById(R.id.recyclerView);

        database = new GroveryManagmentDatabase(this);
        adapter = new CustomAdapterShoppingList(getApplication());

        //Background thread used to set adapter of recycler view
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            Cursor cur = database.getAllShoppingLists();

            runOnUiThread(() -> {
                if (cur != null) {
                    adapter.setCursor(cur);
                }
            });
        });
        service.shutdown();

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));

        adapter.setOnClickListener(new CustomAdapterShoppingList.OnClickListener() {
            @Override
            public void onClickCardView(int position, String nameShoppingList) {
                Intent intent = new Intent(MainActivity.this, NoEditableShoppingListActivity.class);
                intent.putExtra(NAME_SHOPPING_LIST_EXTRA, nameShoppingList);
                startActivity(intent);
            }

            @Override
            public void onDelete(int position, int idShoppingList) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Are you sure you want to delete this product?");
                builder.setIcon(R.drawable.ic_delete);
                builder.setPositiveButton("Yes", ((dialogInterface, i) -> {
                    ExecutorService service = Executors.newSingleThreadExecutor();
                    service.submit(() -> {
                        database.deleteShoppingListById(idShoppingList);
                        Cursor cur = database.getAllShoppingLists();

                        runOnUiThread(() -> {
                            if (cur != null) {
                                adapter.setCursor(cur);
                            }
                        });
                    });
                    service.shutdown();
                    dialogInterface.cancel();
                }));
                builder.setNegativeButton("No", ((dialogInterface, i) -> dialogInterface.cancel()));
                builder.show();
            }

            @Override
            public void onEdit(int position, String nameShoppingList) {
                runOnUiThread(() -> {
                    Intent intent = new Intent(MainActivity.this, ShoppingListActivity.class);
                    intent.putExtra(NAME_SHOPPING_LIST_EXTRA, nameShoppingList);
                    startActivity(intent);
                });
            }
        });

        buttonAddNewShoppingList.setOnClickListener(this);
    }

    /**
     * Create the options menu for the toolbar.
     * @param menu Object menu used to inflate the layout menu for toolbar.
     * @return True if the menu has been inflated, false otherwise.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_homepage, menu);

        return true;
    }

    /**
     * Method used to check which option has been selected.
     * @param item MenuItem object used to check which option has been selected.
     * @return True
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.my_recipes) {
            Intent i = new Intent(this, MyRecipeActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.connect_with_others_devices) {
            Intent i = new Intent(this, ConnectToDeviceActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.share_your_recipes) {
            if (checkAndRequestPermissions()) {
                BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
                this.bluetoothAdapter = bluetoothManager.getAdapter();

                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    launcherEnableBluetooth.launch(enableBluetoothIntent);
                }

                Intent enableDiscoverabilityIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                enableDiscoverabilityIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DURATION_DISCOVERABILITY);
                launcherEnableDiscoverability.launch(enableDiscoverabilityIntent);

                AcceptThread acceptThread = new AcceptThread();
                acceptThread.start();
            }

        }

        return true;
    }

    /**
     * Method used to check which view item has been selected. The view must be set a OnClickListener.
     * @param view View object used to check which item has been selected.
     */
    @Override
    public void onClick(View view) {
        if (view == buttonAddNewShoppingList) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View view2 = layoutInflater.inflate(R.layout.insert_name_gui, null);

            EditText nameShoppingList = view2.findViewById(R.id.edit_text_name);

            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
            builder1.setTitle("Add new shopping list");
            builder1.setIcon(R.drawable.id_add_shopping_list);
            builder1.setView(view2);
            builder1.setPositiveButton("Submit", (dialogInterface, i) -> {
                String name = nameShoppingList.getText().toString();

                if (name.length() != 0) {
                    ExecutorService service = Executors.newSingleThreadExecutor();
                    service.submit(() -> {
                        database.insertShoppingList(name);
                        Cursor cur = database.getAllShoppingLists();

                        runOnUiThread(() -> {
                            if (cur != null) {
                                adapter.setCursor(cur);
                            }
                            dialogInterface.cancel();
                        });
                    });
                    service.shutdown();

                    Intent intent = new Intent(this, ShoppingListActivity.class);
                    intent.putExtra(NAME_SHOPPING_LIST_EXTRA, name);
                    startActivity(intent);
                    dialogInterface.cancel();
                } else {
                    showCustomSnackbar("Insert name for shopping list", R.drawable.ic_warning_error);
                }
            });
            builder1.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
            builder1.show();
        }
    }

    /**
     * Method used to check which item of the context menu has been selected.
     * @param item MenuItem object used to check which item of the context menu has been selected.
     * @return True.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = ((CustomAdapterShoppingList) Objects.requireNonNull(recyclerView.getAdapter())).getPosition();

        if (item.getItemId() == 1) {
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.submit(() -> {
                Cursor cur = database.getAllShoppingLists();

                if (cur != null) {
                    cur.moveToPosition(position);
                    String nameShoppingList = cur.getString(1);

                    runOnUiThread(() -> {
                        LayoutInflater inflater = LayoutInflater.from(this);
                        View v = inflater.inflate(R.layout.insert_name_gui, null);

                        EditText editTextName = v.findViewById(R.id.edit_text_name);
                        editTextName.setText(nameShoppingList);

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Modify name shopping list");
                        builder.setIcon(R.drawable.ic_edit);
                        builder.setView(v);
                        builder.setPositiveButton("Submit", ((dialogInterface, i) -> {
                            String newName = editTextName.getText().toString();

                            ExecutorService service1 = Executors.newSingleThreadExecutor();
                            service1.submit(() -> {
                                if (newName.length() != 0) {
                                    boolean isAlreadyPresent = database.isShoppingListAlreadyPresentInDb(newName);

                                    if (!isAlreadyPresent) {
                                        database.updateNameShoppingList(cur.getInt(0), newName);
                                        Cursor cur1 = database.getAllShoppingLists();

                                        if (cur1 != null) {
                                            runOnUiThread(() -> adapter.setCursor(cur1));
                                        }
                                    } else {
                                        runOnUiThread(() -> showCustomSnackbar(newName + " is already present", R.drawable.ic_warning_error));
                                    }
                                } else {
                                    runOnUiThread(() -> showCustomSnackbar("You must insert a new name", R.drawable.ic_warning_error));
                                }
                            });
                            service1.shutdown();
                        }));
                        builder.setNegativeButton("Cancel", ((dialogInterface, i) -> dialogInterface.cancel()));
                        builder.show();

                    });
                }
            });
            service.shutdown();
        }
        return true;
    }

    /**
     * Private method used to check if all the Bluetooth permissions has been granted.
     * @return True if all the Bluetooth permission has been granted, false otherwise.
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
                    showCustomSnackbar("All permissions are granted", R.drawable.ic_success);
                    // process the normal flow
                    //else any one or both the permissions are not granted
                } else {
                    showCustomSnackbar("Some permissions are not granted. Ask again", R.drawable.ic_warning_error);
                    //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
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

    /**
     * Private method that show an AlertDialog in case not all the Bluetooth permissions has been granted.
     * @param onClickListener OnClickListener object.
     */
    private void showDialogOK(DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(this)
                .setMessage("All the bluetooth permissions and location permission must be granted")
                .setPositiveButton("OK", onClickListener)
                .setNegativeButton("Cancel", onClickListener)
                .create()
                .show();
    }

    /**
     * Private class that implements the Bluetooth "server". The entity that initiates the Bluetooth connection.
     */
    private class AcceptThread extends Thread {
        /** Bluetooth server socket object to accept Bluetooth request connection.*/
        private BluetoothServerSocket serverSocket;

        /**
         * AcceptThread constructor.
         */
        public AcceptThread() {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                BluetoothServerSocket temp = null;
                try {
                    temp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(myName, myUUID);
                } catch (IOException e) {
                    Log.e(TAG, "Socket's listen method failed: ", e);
                }
                this.serverSocket = temp;
            }
        }

        @Override
        public void run() {
            BluetoothSocket socket;

            while (true) {
                try {
                    Log.d(TAG, "Server is running...");
                    socket = this.serverSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept method failed: ", e);
                    break;
                }

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothAdapter.cancelDiscovery();
                }

                if (socket != null) {
                    handleConnectedSocket(socket);
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "ServerSocket's close method failed: ", e);
                        break;
                    }
                    break;
                }
            }
        }

        /**
         * Private method used to handle the Bluetooth connection.
         * This method serialize the objects to send via Gson library. This method sends the data to Bluetooth "client".
         * @param socket BluetoothSocket object that has been created by method accept of BluetoothServerSocket.
         */
        private void handleConnectedSocket(BluetoothSocket socket) {
            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                Cursor cur = database.getAllRecipes();
                List<Recipe> recipes = new ArrayList<>();

                if (cur != null && cur.getCount() > 0) {
                    while (cur.moveToNext()) {
                        Recipe r = new Recipe(cur.getInt(0), cur.getString(1), cur.getString(2));
                        recipes.add(r);
                    }
                    cur.close();
                }

                String recipesJson = getSerializedRecipes(recipes);
                String encodedRecipesJson = Base64.getEncoder().encodeToString(recipesJson.getBytes(StandardCharsets.UTF_8));

                sendToClientBluetooth(writer, encodedRecipesJson);

                Cursor cur1 = database.getAllIngredients();
                List<Ingredient> ingredients = new ArrayList<>();

                if (cur1 != null && cur1.getCount() > 0) {
                    while (cur1.moveToNext()) {
                        Ingredient i = new Ingredient(cur1.getInt(0), cur1.getString(1), cur1.getString(2), cur1.getInt(3));
                        ingredients.add(i);
                    }
                }

                String ingredientsJson = getSerializedIngredients(ingredients);
                String encodedIngredientsJson = Base64.getEncoder().encodeToString(ingredientsJson.getBytes(StandardCharsets.UTF_8));

                sendToClientBluetooth(writer, encodedIngredientsJson);

            } catch (IOException e) {
                Log.e(TAG, "Error in creating buffered writer ", e);
            }
        }

        /**
         * Method used to send the data by using BufferedWriter.
         * @param writer BufferedWriter object used to send data.
         * @param encodedString String value that represents the objects serialization to send.
         * @throws IOException Occurs only when there will be I/O error while sending data.
         */
        private void sendToClientBluetooth(BufferedWriter writer, String encodedString) throws IOException {
            writer.write(encodedString);
            writer.newLine();
            writer.flush();
        }

        /**
         * Private method used to get the serialization of a list of Recipe objects.
         * @param recipes List of Recipe objects.
         * @return Json String of list of Recipe objects.
         */
        private String getSerializedRecipes(List<Recipe> recipes) {
            Gson gson = new GsonBuilder().create();
            Type recipeListType = new TypeToken<List<Recipe>>() {}.getType();

            return gson.toJson(recipes, recipeListType);
        }

        /**
         * Private method used to get the serialization of a list of Ingredient objects.
         * @param ingredients List of Ingredient objects.
         * @return Json String of list of Ingredient objects.
         */
        private String getSerializedIngredients(List<Ingredient> ingredients) {
            Gson gson = new GsonBuilder().create();
            Type ingredientListType = new TypeToken<List<Ingredient>>() {}.getType();

            return gson.toJson(ingredients, ingredientListType);
        }
    }

    /**
     * Private method used to show a custom snackbar to notify the user about error or successful operation.
     * @param message String value that represents the message to custom the snackbar.
     * @param icon Integer value that represents the icon to custom the snackbar.
     */
    private void showCustomSnackbar(String message, int icon) {
        Snackbar snackbar = Snackbar.make(relativeLayout, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();

        TextView snackbarText = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        snackbarText.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        snackbar.show();
    }
}