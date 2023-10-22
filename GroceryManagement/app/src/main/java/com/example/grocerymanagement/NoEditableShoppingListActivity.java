package com.example.grocerymanagement;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import classRecyclerView.CustomAdapter.CustomAdapterNoEditableProduct;
import database.GroveryManagmentDatabase;

/**
 * Class that implements the logic of no_editable_shopping_list GUI.
 */
public class NoEditableShoppingListActivity extends AppCompatActivity {

    /**
     * GUI Toolbar.
     */
    Toolbar toolbar;
    /**
     * GUI Recycler view used to contain CustomAdapterNoEditableProduct object.
     */
    RecyclerView recyclerViewNoEditableProduct;
    /**
     * Adapter used to show products data. This adapter must be set for recycler view GUI.
     */
    private CustomAdapterNoEditableProduct adapterNoEditableProduct;
    /**
     * Name of shopping list.
     */
    private String nameShoppingList;
    /**
     * ID of the shopping list.
     */
    private int idShoppingList;
    /**
     * Local database application.
     */
    private GroveryManagmentDatabase database;
    /**
     * Cursor used to initialize the adapter of recycler view.
     */
    private Cursor productsCursor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_editable_shopping_list);

        database = new GroveryManagmentDatabase(this);

        Intent i = getIntent();
        nameShoppingList = i.getStringExtra(MainActivity.NAME_SHOPPING_LIST_EXTRA);
        idShoppingList = i.getIntExtra(MainActivity.ID_SHOPPING_LIST_EXTRA, -1);

        toolbar = findViewById(R.id.toolbar_no_editable_shopping_list);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Products of " + nameShoppingList);

        recyclerViewNoEditableProduct = findViewById(R.id.recycler_view_no_editable_product);

        ExecutorService setAdapterService = Executors.newSingleThreadExecutor();
        setAdapterService.submit(() -> {
            productsCursor = database.getAllProductsOfAList(idShoppingList);

            if (productsCursor != null) {
                runOnUiThread(() -> {
                    adapterNoEditableProduct = new CustomAdapterNoEditableProduct(this, productsCursor);
                    recyclerViewNoEditableProduct.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                    recyclerViewNoEditableProduct.setAdapter(adapterNoEditableProduct);
                });
            }
        });
        setAdapterService.shutdown();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        productsCursor.close();
    }
}
