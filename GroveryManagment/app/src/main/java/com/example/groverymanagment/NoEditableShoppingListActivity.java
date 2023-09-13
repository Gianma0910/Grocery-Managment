package com.example.groverymanagment;

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

import classRecyclerView.CustomAdapterNoEditableProduct;
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
     * Local database application.
     */
    private GroveryManagmentDatabase database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_editable_shopping_list);

        database = new GroveryManagmentDatabase(this);

        Intent i = getIntent();
        nameShoppingList = i.getStringExtra(MainActivity.NAME_SHOPPING_LIST_EXTRA);

        toolbar = findViewById(R.id.toolbar_no_editable_shopping_list);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Products of " + nameShoppingList);

        adapterNoEditableProduct = new CustomAdapterNoEditableProduct(this);
        recyclerViewNoEditableProduct = findViewById(R.id.recycler_view_no_editable_product);

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            Cursor cur = database.getAllProductsOfAList(nameShoppingList);

            if (cur != null) {
                runOnUiThread(() -> {
                    adapterNoEditableProduct.setCursor(cur);
                    recyclerViewNoEditableProduct.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                    recyclerViewNoEditableProduct.setAdapter(adapterNoEditableProduct);
                });
            }
        });
        service.shutdown();
    }
}
