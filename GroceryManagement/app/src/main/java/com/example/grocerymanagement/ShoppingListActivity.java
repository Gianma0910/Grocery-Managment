package com.example.grocerymanagement;

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

import classRecyclerView.CustomAdapter.CustomAdapterProduct;
import classRecyclerView.CustomAnimator.CustomAnimatorSlideInOut;
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
     * String that represents the query to append to the URL. In this way in the JSON response there will be only the product name.
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
    /**
     * ID of shopping list.
     */
    private int idShoppingList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);

        database = new GroveryManagmentDatabase(this);

        relativeLayout = findViewById(R.id.relative_layout);
        buttonAddNewProduct = findViewById(R.id.button_add_new_product);
        toolbarShoppingList = findViewById(R.id.toolbar_shopping_list);
        setSupportActionBar(toolbarShoppingList);

        Intent i = getIntent();
        nameShoppingList = i.getStringExtra(MainActivity.NAME_SHOPPING_LIST_EXTRA);
        idShoppingList = i.getIntExtra(MainActivity.ID_SHOPPING_LIST_EXTRA, -1);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Products of " + nameShoppingList);

        buttonAddNewProduct.setOnClickListener(this);

        /**
         * GUI RecyclerView that contains a CustomAdapterProduct object.
         */
        RecyclerView recyclerViewProduct = findViewById(R.id.recycler_view_product);

        ExecutorService setAdapterService = Executors.newSingleThreadExecutor();
        setAdapterService.submit(() -> {
            Cursor productsCursor = database.getAllProductsOfAList(idShoppingList);

            if (productsCursor != null) {
                runOnUiThread(() -> {
                    adapterProduct = new CustomAdapterProduct(this, productsCursor);

                    adapterProduct.setOnClickListener(new CustomAdapterProduct.OnClickListener() {
                        @Override
                        public void onDelete(int position, int idProduct) {
                            AlertDialog.Builder deleteProductDialog = new AlertDialog.Builder(ShoppingListActivity.this);
                            deleteProductDialog.setTitle("Warning deleting message!");
                            deleteProductDialog.setMessage("Do you want to delete this product?");
                            deleteProductDialog.setIcon(R.drawable.ic_delete);
                            deleteProductDialog.setPositiveButton("Yes", (dialogInterface, i) -> {
                                ExecutorService deleteProductService = Executors.newSingleThreadExecutor();
                                deleteProductService.submit(() -> {
                                    database.deleteProductById(idProduct);
                                    Cursor productsCursor = database.getAllProductsOfAList(idShoppingList);

                                    if (productsCursor != null) {
                                        runOnUiThread(() -> {
                                            adapterProduct.setCursor(productsCursor);
                                            adapterProduct.notifyItemRemoved(position);
                                            dialogInterface.cancel();
                                        });
                                    }
                                });
                                deleteProductService.shutdown();
                            });
                            deleteProductDialog.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel());
                            deleteProductDialog.show();
                        }

                        @Override
                        public void onEdit(int position, int idProduct, String nameProduct, int amountProduct) {
                            LayoutInflater inflater = LayoutInflater.from(ShoppingListActivity.this);
                            View insertDataLayout = inflater.inflate(R.layout.insert_product_data, null);

                            EditText editTextNameProduct = insertDataLayout.findViewById(R.id.edit_text_name_product);
                            EditText editTextAmountProduct = insertDataLayout.findViewById(R.id.edit_text_amount_product);

                            editTextNameProduct.setText(nameProduct);
                            editTextAmountProduct.setText(String.valueOf(amountProduct));

                            AlertDialog.Builder modifyDataDialog = new AlertDialog.Builder(ShoppingListActivity.this);
                            modifyDataDialog.setTitle("Modify product's data");
                            modifyDataDialog.setIcon(R.drawable.ic_edit);
                            modifyDataDialog.setView(insertDataLayout);
                            modifyDataDialog.setPositiveButton("Submit", (dialogInterface, i) -> {
                                ExecutorService modifyProductDataService = Executors.newSingleThreadExecutor();
                                modifyProductDataService.submit(() -> {
                                    String newName = editTextNameProduct.getText().toString();
                                    int newAmount;

                                    if (editTextAmountProduct.getText().toString().length() == 0) {
                                        newAmount = 0;
                                    } else {
                                        newAmount = Integer.parseInt(editTextAmountProduct.getText().toString());
                                    }

                                    if (newName.length() != 0 && newAmount != 0) {
                                        boolean productIsPresent = database.isProductInsideShoppingList(newName, idShoppingList);

                                        if ((newName.equals(nameProduct) && newAmount != amountProduct) || !productIsPresent) {
                                            database.updateProductData(idProduct, newName, newAmount);
                                            Cursor productsCursor = database.getAllProductsOfAList(idShoppingList);

                                            runOnUiThread(() -> {
                                                if (productsCursor != null) {
                                                    adapterProduct.setCursor(productsCursor);
                                                    adapterProduct.notifyItemChanged(position);
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
                                modifyProductDataService.shutdown();
                            });
                            modifyDataDialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
                            modifyDataDialog.show();
                        }

                        @Override
                        public void onChangeAlreadyTakenProduct(int position, int idProduct, int isProductAlreadyTaken) {
                            ExecutorService changeFlagService = Executors.newSingleThreadExecutor();
                            if (isProductAlreadyTaken == 0) {
                                changeFlagService.submit(() -> {
                                    database.setProductAsTaken(idProduct);
                                    Cursor productsCursor = database.getAllProductsOfAList(idShoppingList);

                                    runOnUiThread(() -> {
                                        if (productsCursor != null) {
                                            adapterProduct.setCursor(productsCursor);
                                            adapterProduct.notifyItemChanged(position);
                                        }
                                    });
                                });
                            } else {
                                changeFlagService.submit(() -> {
                                    database.setProductAsNoTaken(idProduct);
                                    Cursor products = database.getAllProductsOfAList(idShoppingList);

                                    runOnUiThread(() -> {
                                        if (products != null) {
                                            adapterProduct.setCursor(products);
                                            adapterProduct.notifyItemChanged(position);
                                        }
                                    });
                                });
                            }
                            changeFlagService.shutdown();
                        }
                    });

                    recyclerViewProduct.setAdapter(adapterProduct);
                    recyclerViewProduct.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
                    recyclerViewProduct.setItemAnimator(new CustomAnimatorSlideInOut());
                });
            }
        });
        setAdapterService.shutdown();

    }

    /**
     * Method used to check which item has been clicked. The items must set OnClickListener.
     * @param view View object used to check which item has been clicked.
     */
    @Override
    public void onClick(View view) {
        if (view == buttonAddNewProduct) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View insertDataLayout = layoutInflater.inflate(R.layout.insert_product_data, null);

            EditText editTextNameProduct = insertDataLayout.findViewById(R.id.edit_text_name_product);
            EditText editTextAmountProduct = insertDataLayout.findViewById(R.id.edit_text_amount_product);

            AlertDialog.Builder insertDataDialog = new AlertDialog.Builder(this);
            insertDataDialog.setTitle("Add new product to the shopping list");
            insertDataDialog.setIcon(R.drawable.ic_add_ingredient);
            insertDataDialog.setView(insertDataLayout);
            insertDataDialog.setPositiveButton("Submit", (dialogInterface, i) -> {
                String nameProduct = editTextNameProduct.getText().toString();
                int amountProduct;

                if (editTextAmountProduct.getText().toString().length() == 0) {
                    amountProduct = 0;
                } else {
                    amountProduct = Integer.parseInt(editTextAmountProduct.getText().toString());
                }

                if (nameProduct.length() != 0 && amountProduct != 0) {
                    ExecutorService insertNewProductService = Executors.newSingleThreadExecutor();
                    insertNewProductService.submit(() -> {
                        boolean productIsPresent = database.isProductInsideShoppingList(nameProduct, idShoppingList);

                        if (!productIsPresent) {
                            database.insertProduct(nameProduct, amountProduct,idShoppingList);
                            Cursor productsCursor = database.getAllProductsOfAList(idShoppingList);

                            if (productsCursor != null) {
                                productsCursor.moveToLast();
                                runOnUiThread(() -> {
                                    adapterProduct.setCursor(productsCursor);
                                    adapterProduct.notifyItemInserted(productsCursor.getPosition());
                                    dialogInterface.cancel();
                                });
                            }
                        } else {
                            runOnUiThread(() -> showCustomSnackbar("Product " + nameProduct + " is already present in this list", R.drawable.ic_warning_error));
                        }
                    });
                    insertNewProductService.shutdown();
                } else {
                    showCustomSnackbar("You have to insert name and amount of the new product", R.drawable.ic_warning_error);
                }
            });
            insertDataDialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
            insertDataDialog.show();
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
        scanOptions.setPrompt("Scan the bar code of a product");
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
                    ExecutorService scanBarcodeService = Executors.newSingleThreadExecutor();
                    scanBarcodeService.submit(() -> {
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
                            connection.setRequestProperty("User-agent", "GroceryManagement");

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

                                boolean productIsPresent = database.isProductInsideShoppingList(nameProductScanned, idShoppingList);

                                String finalNameProductScanned = nameProductScanned;
                                runOnUiThread(() -> {
                                    if (!productIsPresent) {
                                        LayoutInflater layoutInflater = LayoutInflater.from(this);
                                        View insertDataLayout = layoutInflater.inflate(R.layout.insert_product_data, null);

                                        EditText editTextNameProduct = insertDataLayout.findViewById(R.id.edit_text_name_product);
                                        EditText editTextAmountProduct = insertDataLayout.findViewById(R.id.edit_text_amount_product);

                                        editTextNameProduct.setText(finalNameProductScanned);
                                        editTextNameProduct.setEnabled(false);

                                        AlertDialog.Builder insertDataDialog = new AlertDialog.Builder(this);
                                        insertDataDialog.setIcon(R.drawable.ic_add_ingredient);
                                        insertDataDialog.setTitle("Add new product to the shopping list");
                                        insertDataDialog.setView(insertDataLayout);
                                        insertDataDialog.setPositiveButton("Submit", ((dialogInterface, i) -> {
                                            String nameProduct = editTextNameProduct.getText().toString();
                                            int amountProduct;

                                            if (editTextAmountProduct.getText().toString().length() == 0) {
                                                amountProduct = 0;
                                            } else {
                                                amountProduct = Integer.parseInt(editTextAmountProduct.getText().toString());
                                            }

                                            if (amountProduct != 0) {
                                                ExecutorService insertNewProductService = Executors.newSingleThreadExecutor();
                                                insertNewProductService.submit(() -> {
                                                    database.insertProduct(nameProduct, amountProduct, idShoppingList);
                                                    database.setProductAsTaken(nameProduct, idShoppingList);
                                                    Cursor productsCursor = database.getAllProductsOfAList(idShoppingList);

                                                    if (productsCursor != null) {
                                                        productsCursor.moveToLast();
                                                        runOnUiThread(() -> {
                                                            adapterProduct.setCursor(productsCursor);
                                                            adapterProduct.notifyItemInserted(productsCursor.getPosition());
                                                            dialogInterface.cancel();
                                                        });
                                                    }
                                                });
                                                insertNewProductService.shutdown();
                                            } else {
                                                showCustomSnackbar("Invalid value for the product amount!", R.drawable.ic_warning_error);
                                            }
                                        }));
                                        insertDataDialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
                                        insertDataDialog.show();
                                    } else {
                                        ExecutorService changeFlagService = Executors.newSingleThreadExecutor();
                                        changeFlagService.submit(() -> {
                                            database.setProductAsTaken(finalNameProductScanned, idShoppingList);
                                            Cursor productsCursor = database.getAllProductsOfAList(idShoppingList);
                                            int positionItemModified = -1;

                                            if (productsCursor != null && productsCursor.getCount() > 0) {
                                                while (productsCursor.moveToNext()) {
                                                    if (productsCursor.getString(1).equals(finalNameProductScanned)) {
                                                        positionItemModified = productsCursor.getPosition();
                                                        break;
                                                    }
                                                }

                                                int finalPositionItemModified = positionItemModified;
                                                runOnUiThread(() -> {
                                                    adapterProduct.setCursor(productsCursor);
                                                    adapterProduct.notifyItemChanged(finalPositionItemModified);
                                                });
                                            }
                                        });
                                        changeFlagService.shutdown();
                                    }
                                });
                            } else {
                                showCustomSnackbar("There was an error while scanning the product", R.drawable.ic_warning_error);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    scanBarcodeService.shutdown();
                }
            });

    @Override
    public void onBackPressed() {
        ExecutorService closeActivityService = Executors.newSingleThreadExecutor();
        closeActivityService.submit(() -> {
           Cursor productsCursor = database.getAllProductsOfAList(idShoppingList);

           if (productsCursor != null && productsCursor.getCount() > 0) {
                boolean flagAllProductsAlreadyTaken = isAllProductsAlreadyTaken(productsCursor);
                if (flagAllProductsAlreadyTaken) {
                    database.setCompletedStatusShoppingList(idShoppingList);

                    runOnUiThread(() -> {
                        setResult(RESULT_OK);
                        finish();
                    });
                } else {
                    database.setNotCompletedStatusShoppingList(idShoppingList);

                    runOnUiThread(() -> {
                        AlertDialog.Builder warningDialog = new AlertDialog.Builder(this);
                        warningDialog.setTitle("Warning!");
                        warningDialog.setMessage("Are you sure to close? There are some products not taken");
                        warningDialog.setIcon(R.drawable.ic_warning_error);
                        warningDialog.setPositiveButton("Yes!", ((dialogInterface, i) -> {
                            setResult(RESULT_OK);
                            finish();
                        }));
                        warningDialog.setNegativeButton("No", ((dialogInterface, i) -> dialogInterface.cancel()));
                        warningDialog.show();
                    });
                }
           } else {
               database.setEmptyStatusShoppingList(idShoppingList);

                runOnUiThread(() -> {
                    AlertDialog.Builder warningDialog = new AlertDialog.Builder(this);
                    warningDialog.setTitle("Warning!");
                    warningDialog.setMessage("Are you sure to close? Your list is empty!");
                    warningDialog.setIcon(R.drawable.ic_warning_error);
                    warningDialog.setPositiveButton("Yes!", ((dialogInterface, i) -> {
                        setResult(RESULT_OK);
                        finish();
                    }));
                    warningDialog.setNegativeButton("No", ((dialogInterface, i) -> dialogInterface.cancel()));
                    warningDialog.show();
                });
           }
        });
        closeActivityService.shutdown();
    }

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

    /**
     * Method used to check if all the products of the list have already been taken/bought.
     * @param cur Cursor object that represents all the products of the shopping list.
     * @return True if all the products have already been taken/bought, false otherwise.
     */
    private boolean isAllProductsAlreadyTaken(Cursor cur) {
        while (cur.moveToNext()) {
            int alreadyTaken = cur.getInt(3);
            if (alreadyTaken == 0) {
                return false;
            }
        }
        return true;
    }
}
