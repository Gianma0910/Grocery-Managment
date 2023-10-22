package com.example.grocerymanagement;

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

import classRecyclerView.CustomAdapter.CustomAdapterShoppingList;
import classRecyclerView.CustomAnimator.CustomAnimatorSlideInOut;
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
    /** String used to put the id of shopping list as extra for an Intent.*/
    public static final String ID_SHOPPING_LIST_EXTRA = "idShoppingList";

    /** Name of the Bluetooth service.*/
    public static final String myName = "com.example.grocerymanagement";
    /** UUID used to do the Bluetooth connection.*/
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
    /** Integer value that represents the position of the item modified that belongs to recycler view.
     * This integer value is used to update the recycler view.
     */
    private int positionItemModified = 0;

    /** Launcher used to launch the Intent for the activity EditableShoppingList.
     * If the result code is RESULT_OK then upload the home page GUI.*/
    ActivityResultLauncher<Intent> launcherEditShoppingList;

    /** Launcher used to launch the Intent to enable bluetooth.
     * If the result code is RESULT_OK then the bluetooth is enabled.*/
    ActivityResultLauncher<Intent> launcherEnableBluetooth = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    showCustomSnackbar("Bluetooth has been enabled", R.drawable.ic_bluetooth_enable);
                } else {
                    showCustomSnackbar("Failure to enable bluetooth", R.drawable.ic_bluetooth_disable);
                }
            });

    /** Launcher to launch the Intent to enable bluetooth discoverability.
     * If the result code is DURATION_DISCOVERABILITY then the device can be discovered by others.*/
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
        Objects.requireNonNull(getSupportActionBar()).setTitle("Grocery Management");

        recyclerView = findViewById(R.id.recyclerView);

        database = new GroveryManagmentDatabase(this);

        //Background thread used to set adapter of recycler view
        ExecutorService setAdapterService = Executors.newSingleThreadExecutor();
        setAdapterService.submit(() -> {
            Cursor shoppingListCursor = database.getAllShoppingLists();

            if (shoppingListCursor != null) {
                runOnUiThread(() -> {
                    adapter = new CustomAdapterShoppingList(this, shoppingListCursor);

                    adapter.setOnClickListener(new CustomAdapterShoppingList.OnClickListener() {
                        @Override
                        public void onClickCardView(int position, int idShoppingList, String nameShoppingList) {
                            Intent intent = new Intent(MainActivity.this, NoEditableShoppingListActivity.class);
                            intent.putExtra(NAME_SHOPPING_LIST_EXTRA, nameShoppingList);
                            intent.putExtra(ID_SHOPPING_LIST_EXTRA, idShoppingList);
                            startActivity(intent);
                        }

                        @Override
                        public void onDelete(View itemView, int position, int idShoppingList) {
                            AlertDialog.Builder deleteMessageDialog = new AlertDialog.Builder(MainActivity.this);
                            deleteMessageDialog.setTitle("Warning deleting message!");
                            deleteMessageDialog.setMessage("Do you want to delete this list?");
                            deleteMessageDialog.setIcon(R.drawable.ic_delete);
                            deleteMessageDialog.setPositiveButton("Yes", ((dialogInterface, i) -> {
                                ExecutorService deleteShoppingListService = Executors.newSingleThreadExecutor();
                                deleteShoppingListService.submit(() -> {
                                    database.deleteShoppingListById(idShoppingList);
                                    Cursor shoppingListsCursor = database.getAllShoppingLists();

                                    if (shoppingListsCursor != null) {
                                        runOnUiThread(() -> {
                                            adapter.setCursor(shoppingListsCursor);
                                            adapter.notifyItemRemoved(position);
                                        });
                                    }
                                });
                                deleteShoppingListService.shutdown();
                                dialogInterface.cancel();
                            }));
                            deleteMessageDialog.setNegativeButton("No", ((dialogInterface, i) -> dialogInterface.cancel()));
                            deleteMessageDialog.show();
                        }

                        @Override
                        public void onEdit(int position, int idShoppingList, String nameShoppingList) {
                            positionItemModified = position;
                            Intent intent = new Intent(MainActivity.this, ShoppingListActivity.class);
                            intent.putExtra(NAME_SHOPPING_LIST_EXTRA, nameShoppingList);
                            intent.putExtra(ID_SHOPPING_LIST_EXTRA, idShoppingList);
                            launcherEditShoppingList.launch(intent);
                        }
                    });

                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
                    recyclerView.setItemAnimator(new CustomAnimatorSlideInOut());
                });
            }
        });
        setAdapterService.shutdown();

        launcherEditShoppingList = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        ExecutorService setAdapterAfterEditingService = Executors.newSingleThreadExecutor();
                        setAdapterAfterEditingService.submit(() -> {
                           Cursor shoppingListsCursor = database.getAllShoppingLists();

                           if (shoppingListsCursor != null) {
                               runOnUiThread(() -> {
                                   adapter.setCursor(shoppingListsCursor);
                                   adapter.notifyItemChanged(positionItemModified);
                               });
                           }
                        });
                        setAdapterAfterEditingService.shutdown();
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
     * @return True if the command has been executed, false otherwise.
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
            View insertNameLayout = layoutInflater.inflate(R.layout.insert_name_gui, null);

            EditText nameShoppingList = insertNameLayout.findViewById(R.id.edit_text_name);

            AlertDialog.Builder insertNameDialog = new AlertDialog.Builder(MainActivity.this);
            insertNameDialog.setTitle("Add new shopping list");
            insertNameDialog.setIcon(R.drawable.id_add_shopping_list);
            insertNameDialog.setView(insertNameLayout);
            insertNameDialog.setPositiveButton("Submit", (dialogInterface, i) -> {
                String name = nameShoppingList.getText().toString();

                if (name.length() != 0) {
                    ExecutorService insertShoppingListService = Executors.newSingleThreadExecutor();
                    insertShoppingListService.submit(() -> {
                        boolean isAlreadyPresent = database.isShoppingListAlreadyPresentInDb(name);

                        if (!isAlreadyPresent) {
                            database.insertShoppingList(name);
                            Cursor shoppingListsCursor = database.getAllShoppingLists();

                            if (shoppingListsCursor != null) {
                                shoppingListsCursor.moveToLast();
                                runOnUiThread(() -> {
                                    adapter.setCursor(shoppingListsCursor);
                                    adapter.notifyItemInserted(shoppingListsCursor.getPosition());
                                    dialogInterface.cancel();
                                });
                            }
                        } else {
                            runOnUiThread(() -> showCustomSnackbar("Shopping list " + name + " is already present", R.drawable.ic_warning_error));
                        }
                    });
                    insertShoppingListService.shutdown();
                    dialogInterface.cancel();
                } else {
                    showCustomSnackbar("Insert name for shopping list", R.drawable.ic_warning_error);
                }
            });
            insertNameDialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
            insertNameDialog.show();
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
            ExecutorService insertNewNameShoppingListService = Executors.newSingleThreadExecutor();
            insertNewNameShoppingListService.submit(() -> {
                Cursor shoppingListsCursor = database.getAllShoppingLists();

                if (shoppingListsCursor != null) {
                    shoppingListsCursor.moveToPosition(position);
                    String nameShoppingList = shoppingListsCursor.getString(1);

                    runOnUiThread(() -> {
                        LayoutInflater inflater = LayoutInflater.from(this);
                        View insertNameLayout = inflater.inflate(R.layout.insert_name_gui, null);

                        EditText editTextName = insertNameLayout.findViewById(R.id.edit_text_name);
                        editTextName.setText(nameShoppingList);

                        AlertDialog.Builder insertNameDialog = new AlertDialog.Builder(this);
                        insertNameDialog.setTitle("Modify shopping list's name");
                        insertNameDialog.setIcon(R.drawable.ic_edit);
                        insertNameDialog.setView(insertNameLayout);
                        insertNameDialog.setPositiveButton("Submit", ((dialogInterface, i) -> {
                            String newName = editTextName.getText().toString();

                            ExecutorService updateNameShoppingListService = Executors.newSingleThreadExecutor();
                            updateNameShoppingListService.submit(() -> {
                                if (newName.length() != 0) {
                                    boolean isAlreadyPresent = database.isShoppingListAlreadyPresentInDb(newName);

                                    if (!isAlreadyPresent) {
                                        database.updateNameShoppingList(shoppingListsCursor.getInt(0), newName);
                                        Cursor shoppingListsCursor1 = database.getAllShoppingLists();

                                        if (shoppingListsCursor1 != null) {
                                            runOnUiThread(() -> {
                                                adapter.setCursor(shoppingListsCursor1);
                                                adapter.notifyItemChanged(position);
                                            });
                                        }
                                    } else {
                                        runOnUiThread(() -> showCustomSnackbar(newName + " is already present", R.drawable.ic_warning_error));
                                    }
                                } else {
                                    runOnUiThread(() -> showCustomSnackbar("You must insert a new name", R.drawable.ic_warning_error));
                                }
                            });
                            updateNameShoppingListService.shutdown();
                        }));
                        insertNameDialog.setNegativeButton("Cancel", ((dialogInterface, i) -> dialogInterface.cancel()));
                        insertNameDialog.show();

                    });
                }
            });
            insertNewNameShoppingListService.shutdown();
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
     * Private class that implements the Bluetooth "server". The entity that listens for Bluetooth connection request.
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
                    runOnUiThread(() -> {
                        showCustomSnackbar("Bluetooth connection failed", R.drawable.ic_warning_error);
                    });
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
                    runOnUiThread(() -> {
                        showCustomSnackbar("Bluetooth connection failed", R.drawable.ic_warning_error);
                    });
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
                        runOnUiThread(() -> {
                            showCustomSnackbar("Bluetooth connection didn't close properly", R.drawable.ic_warning_error);
                        });
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

                Cursor recipesCursor = database.getAllRecipes();
                List<Recipe> recipes = new ArrayList<>();

                if (recipesCursor != null && recipesCursor.getCount() > 0) {
                    while (recipesCursor.moveToNext()) {
                        Recipe r = new Recipe(recipesCursor.getInt(0), recipesCursor.getString(1), recipesCursor.getString(2));
                        recipes.add(r);
                    }
                    recipesCursor.close();
                }

                String recipesJson = getSerializedRecipes(recipes);
                String encodedRecipesJson = Base64.getEncoder().encodeToString(recipesJson.getBytes(StandardCharsets.UTF_8));

                sendToClientBluetooth(writer, encodedRecipesJson);

                Cursor ingredientsCursor = database.getAllIngredients();
                List<Ingredient> ingredients = new ArrayList<>();

                if (ingredientsCursor != null && ingredientsCursor.getCount() > 0) {
                    while (ingredientsCursor.moveToNext()) {
                        Ingredient i = new Ingredient(ingredientsCursor.getInt(0), ingredientsCursor.getString(1), ingredientsCursor.getString(2), ingredientsCursor.getInt(3));
                        ingredients.add(i);
                    }
                }

                String ingredientsJson = getSerializedIngredients(ingredients);
                String encodedIngredientsJson = Base64.getEncoder().encodeToString(ingredientsJson.getBytes(StandardCharsets.UTF_8));

                sendToClientBluetooth(writer, encodedIngredientsJson);

            } catch (IOException e) {
                runOnUiThread(() -> {
                    showCustomSnackbar("Error on sending shared recipes data", R.drawable.ic_warning_error);
                });
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