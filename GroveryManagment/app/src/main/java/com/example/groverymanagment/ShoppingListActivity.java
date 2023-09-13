package com.example.groverymanagment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import classRecyclerView.CustomAdapterProduct;
import database.GroveryManagmentDatabase;

/**
 * Class that implements the logic of shopping_list GUI.
 */
public class ShoppingListActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * URL used to connect with openfoodfacts API.
     */
    private final String urlOpenFoodFacts = "https://world.openfoodfacts.org/api/v0/product/";
    /**
     * String that represents the query to append to the URL.
     */
    private static final String queryUrlOpenFoodFacts = "?fields=product_name";

    /**
     * GUI Button used to add a new product into the shopping list.
     */
    Button buttonAddNewProduct;
    /**
     * GUI Toolbar.
     */
    Toolbar toolbarShoppingList;
    /**
     * GUI Layout.
     */
    RelativeLayout relativeLayout;

    /**
     * Adapter used to show products data. This adapter must be set for recycler view GUI.
     */
    private CustomAdapterProduct adapterProduct;
    /**
     * Local database application.
     */
    private GroveryManagmentDatabase database;
    /**
     * Name of shopping list.
     */
    private String nameShoppingList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);

        relativeLayout = findViewById(R.id.relative_layout);
        buttonAddNewProduct = findViewById(R.id.button_add_new_product);
        toolbarShoppingList = findViewById(R.id.toolbar_shopping_list);
        setSupportActionBar(toolbarShoppingList);

        Intent i = getIntent();
        nameShoppingList = i.getStringExtra(MainActivity.NAME_SHOPPING_LIST_EXTRA);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Products of " + nameShoppingList);

        buttonAddNewProduct.setOnClickListener(this);

        /**
         * GUI RecyclerView that contains a CustomAdapterProduct object.
         */
        RecyclerView recyclerViewProduct = findViewById(R.id.recycler_view_product);
        adapterProduct = new CustomAdapterProduct(this);

        database = new GroveryManagmentDatabase(this);

        recyclerViewProduct.setAdapter(adapterProduct);

        adapterProduct.setOnClickListener(new CustomAdapterProduct.OnClickListener() {
            @Override
            public void onDelete(int position, int idProduct) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingListActivity.this);
                builder.setTitle("Are you sure you want to delete this product?");
                builder.setIcon(R.drawable.ic_delete);
                builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                    ExecutorService service = Executors.newSingleThreadExecutor();
                    service.submit(() -> {
                        database.deleteProductById(idProduct);
                        Cursor cur = database.getAllProductsOfAList(nameShoppingList);

                        runOnUiThread(() -> {
                            if (cur != null) {
                                adapterProduct.setCursor(cur);
                                dialogInterface.cancel();
                            }
                        });
                    });
                    service.shutdown();
                });
                builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel());
                builder.show();
            }

            @Override
            public void onEdit(int position, int idProduct, String nameProduct, int amountProduct) {
                LayoutInflater inflater = LayoutInflater.from(ShoppingListActivity.this);
                View v = inflater.inflate(R.layout.insert_product_data, null);

                EditText editTextNameProduct = v.findViewById(R.id.edit_text_name_product);
                EditText editTextAmountProduct = v.findViewById(R.id.edit_text_amount_product);

                editTextNameProduct.setText(nameProduct);
                editTextAmountProduct.setText(String.valueOf(amountProduct));

                AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingListActivity.this);
                builder.setTitle("Modify product's data");
                builder.setIcon(R.drawable.ic_edit);
                builder.setView(v);
                builder.setPositiveButton("Submit", (dialogInterface, i) -> {
                    ExecutorService service = Executors.newSingleThreadExecutor();
                    service.submit(() -> {
                        String newName = editTextNameProduct.getText().toString();
                        int newAmount;

                        if (editTextAmountProduct.getText().toString().length() == 0) {
                            newAmount = 0;
                        } else {
                            newAmount = Integer.parseInt(editTextAmountProduct.getText().toString());
                        }

                        if (newName.length() != 0 && newAmount != 0) {
                            boolean productIsPresent = database.isProductInsideShoppingList(newName, nameShoppingList);

                            if ((newName.equals(nameProduct) && newAmount != amountProduct) || !productIsPresent) {
                                database.updateProductData(idProduct, newName, newAmount);
                                Cursor cur = database.getAllProductsOfAList(nameShoppingList);

                                runOnUiThread(() -> {
                                    if (cur != null) {
                                        adapterProduct.setCursor(cur);
                                        dialogInterface.cancel();
                                    } else {
                                        showCustomSnackbar("Error in updating the product", R.drawable.ic_warning_error);
                                    }
                                });
                            } else {
                                runOnUiThread(() -> {
                                    editTextNameProduct.setText("");
                                    showCustomSnackbar("Product " + newName + " is already present in this list", R.drawable.ic_warning_error);
                                });
                            }
                        } else {
                            runOnUiThread(() -> showCustomSnackbar("You have to insert name and amount to edit a product", R.drawable.ic_warning_error));
                        }
                    });
                    service.shutdown();
                });
                builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
                builder.show();
            }
        });

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            Cursor cur = database.getAllProductsOfAList(nameShoppingList);

            runOnUiThread(() -> {
                if (cur != null) {
                    adapterProduct.setCursor(cur);
                }
            });
        });
        service.shutdown();

        recyclerViewProduct.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }

    /**
     * Method used to check which item has been clicked. The items must set OnClickListener.
     * @param view View object used to check which item has been clicked.
     */
    @Override
    public void onClick(View view) {
        if (view == buttonAddNewProduct) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View v = layoutInflater.inflate(R.layout.insert_product_data, null);

            EditText editTextNameProduct = v.findViewById(R.id.edit_text_name_product);
            EditText editTextAmountProduct = v.findViewById(R.id.edit_text_amount_product);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add new product to the shopping list");
            builder.setIcon(R.drawable.ic_add_ingredient);
            builder.setView(v);
            builder.setPositiveButton("Submit", (dialogInterface, i) -> {
                String nameProduct = editTextNameProduct.getText().toString();
                int amountProduct;

                if (editTextAmountProduct.getText().toString().length() == 0) {
                    amountProduct = 0;
                } else {
                    amountProduct = Integer.parseInt(editTextAmountProduct.getText().toString());
                }

                if (nameProduct.length() != 0 && amountProduct != 0) {
                    ExecutorService service = Executors.newSingleThreadExecutor();
                    service.submit(() -> {
                        boolean productIsPresent = database.isProductInsideShoppingList(nameProduct, nameShoppingList);

                        if (!productIsPresent) {
                            database.insertProduct(nameProduct, amountProduct, nameShoppingList);
                            Cursor cur = database.getAllProductsOfAList(nameShoppingList);

                            runOnUiThread(() -> {
                                if (cur != null) {
                                    adapterProduct.setCursor(cur);
                                }
                                dialogInterface.cancel();
                            });
                        } else {
                            runOnUiThread(() -> showCustomSnackbar("Product " + nameProduct + " is already present in this list", R.drawable.ic_warning_error));
                        }
                    });
                    service.shutdown();
                } else {
                    showCustomSnackbar("You have to insert name and amount of the new product", R.drawable.ic_warning_error);
                }
            });
            builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
            builder.show();
        }
    }

    /**
     * Method used to create options menu for toolbar.
     * @param menu Menu object used to store the layout inflated.
     * @return True.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shopping_list, menu);

        return true;
    }

    /**
     * Method used to check which menu option has been selected.
     * @param item MenuItem object used to check which menu option has been selected.
     * @return True.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.scan_barcode) {
            scanBarcode();
        }

        return true;
    }

    /**
     * Method used to set scanOptions object and to start the camera in scan mode.
     */
    private void scanBarcode() {
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setPrompt("Scan the barcode of a product");
        scanOptions.setBeepEnabled(false);
        scanOptions.setOrientationLocked(true);
        scanOptions.setCaptureActivity(CaptureAct.class);
        barCodeLauncher.launch(scanOptions);
    }

    /**
     * Launcher used to launch the camera in scan mode. After the camera has finished to scan the barcode
     * it will start the process of insertion and selection of the new product into the shopping list.
     */
    ActivityResultLauncher<ScanOptions> barCodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                //getContents contains the element that has been scanned by the camera.
                if (result.getContents() != null) {
                    ExecutorService service = Executors.newSingleThreadExecutor();
                    service.submit(() -> {
                        String barcode = result.getContents();
                        //Process of built of openfoodfact API's URL.
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(urlOpenFoodFacts).append(barcode).append(queryUrlOpenFoodFacts);

                        String nameProductScanned = "";

                        try {
                            //Initiate a GET HttpURL connection with the API to obtain the product data,
                            //in particular the name of it.
                            URL url = new URL(stringBuilder.toString());
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setDoInput(true);
                            connection.setRequestMethod("GET");
                            connection.setRequestProperty("User-agent", "GroveryManagment");

                            int responseCodeConnection = connection.getResponseCode();
                            //if the GET response is HTTP_OK so we obtain the product data from the API.
                            //The format of API result is a Json String
                            if (responseCodeConnection == HttpURLConnection.HTTP_OK) {
                                //Initialization of JsonReader with the connection inputStream, where there is the GET HTTP result.
                                JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(connection.getInputStream())));
                                reader.setLenient(true);
                                reader.beginObject();
                                String nameAttribute;

                                //Process of retrieving of product name.
                                while (reader.hasNext()) {
                                    nameAttribute = reader.nextName();
                                    if (nameAttribute.equals("product")) {
                                        reader.beginObject();
                                        while (reader.hasNext()) {
                                            if (reader.nextName().equals("product_name")) {
                                                nameProductScanned = reader.nextString();
                                            }
                                        }
                                        reader.endObject();
                                    } else {
                                        reader.skipValue();
                                    }
                                }
                                reader.endObject();
                                reader.close();

                                connection.disconnect();

                                boolean productIsPresent = database.isProductInsideShoppingList(nameProductScanned, nameShoppingList);

                                String finalNameProductScanned = nameProductScanned;
                                runOnUiThread(() -> {
                                    if (!productIsPresent) {
                                        LayoutInflater layoutInflater = LayoutInflater.from(this);
                                        View view = layoutInflater.inflate(R.layout.insert_product_data, null);

                                        EditText editTextNameProduct = view.findViewById(R.id.edit_text_name_product);
                                        EditText editTextAmountProduct = view.findViewById(R.id.edit_text_amount_product);

                                        editTextNameProduct.setText(finalNameProductScanned);
                                        editTextNameProduct.setEnabled(false);

                                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                        builder.setTitle("Insert data for new product");
                                        builder.setView(view);
                                        builder.setPositiveButton("Submit", ((dialogInterface, i) -> {
                                            String nameProduct = editTextNameProduct.getText().toString();
                                            int amountProduct = Integer.parseInt(editTextAmountProduct.getText().toString());
                                            ExecutorService service1 = Executors.newSingleThreadExecutor();
                                            service1.submit(() -> {
                                                database.insertProduct(nameProduct, amountProduct, nameShoppingList);
                                                database.setProductAsTaken(nameProduct, nameShoppingList);
                                                Cursor cur = database.getAllProductsOfAList(nameShoppingList);

                                                runOnUiThread(() -> {
                                                    if (cur != null) {
                                                        adapterProduct.setCursor(cur);
                                                    }

                                                    dialogInterface.cancel();
                                                });
                                            });
                                            service1.shutdown();
                                        }));
                                        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
                                        builder.show();
                                    } else {
                                        ExecutorService service1 = Executors.newSingleThreadExecutor();
                                        service1.submit(() -> {
                                            database.setProductAsTaken(finalNameProductScanned, nameShoppingList);

                                            runOnUiThread(() -> {
                                                Cursor cur = database.getAllProductsOfAList(nameShoppingList);

                                                if (cur != null) {
                                                    adapterProduct.setCursor(cur);
                                                }
                                            });
                                        });
                                        service1.shutdown();
                                    }
                                });
                            } else {
                                showCustomSnackbar("There was an error while scanning the product", R.drawable.ic_warning_error);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    service.shutdown();
                }
            });

    /**
     * Method used to show custom snackbar, used to notify the user about error and successful operation.
     * @param message String that represents the message used to custom the snackbar.
     * @param icon Integer that represents the icon used to custom the snackbar.
     */
    private void showCustomSnackbar(String message, int icon) {
        Snackbar snackbar = Snackbar.make(relativeLayout, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();

        TextView snackbarTextView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        snackbarTextView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);

        snackbar.show();
    }
}
