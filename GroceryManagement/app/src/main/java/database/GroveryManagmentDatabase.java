package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;

import utilityClassDatabase.Ingredient;

/**
 * Local database of the application. It's used to store data about:
 * Shopping list, Product of every shopping list, Recipe and Ingredient of every recipe.
 */
public class GroveryManagmentDatabase extends SQLiteOpenHelper {

    //database data
    /** Name of the database.*/
    private static final String DATABASE_NAME = "Database";
    /** Version of the database used to do the various upgrade.*/
    private static final int VERSION_DATABASE = 1;
    private static final String TAG = "GroveryManagmentDatabase";

    //name of tables
    /** Name of the table to store shopping lists data.*/
    private static final String SHOPPING_LIST_TABLE_NAME = "ShoppingLists";
    /** Name of the table to store recipes data.*/
    private static final String RECIPE_TABLE_NAME = "Recipes";
    /** Name of the table to store product data.*/
    private static final String PRODUCT_TABLE_NAME = "Products";
    /** Name of the table to store ingredients data.*/
    private static final String INGREDIENT_TABLE_NAME = "Ingredients";

    //column shopping lists table
    /** First column of shopping list table. It stores the ID that is the primary key (type integer).
     * The value for this column is inserted automatically by the system.*/
    private static final String ID_SHOPPING_LIST_COLUMN = "IdShoppingList";
    /** Second column of the shopping list table. It store the list's name and it's unique (type TEXT/String). */
    private static final String NAME_SHOPPING_LIST_COLUMN = "NameShoppingList";
    /** Third column of the shopping list table. It store an integer value that it can be -1, 0 or 1.
     * -1 = shopping list is empty, 0 = shopping list isn't empty but not all the products have already been bought, 1 = all products have already been bought. */
    public static final String STATUS_SHOPPING_LIST = "StatusShoppingList";

    //column products table
    /** First column of the product table. It stores the ID that is the primary key (type integer).
     * The value for this column is inserted automatically by the system.*/
    private static final String ID_PRODUCT_COLUMN = "IdProduct";
    /** Second column of the product table. It stores the product's name (type TEXT/String.)*/
    private static final String NAME_PRODUCT_COLUMN = "NameProduct";
    /** Third column of the product table. It stores the product's amount (type integer).*/
    private static final String AMOUNT_PRODUCT_COLUMN = "AmountProduct";
    /** Fourth column of the product table. It stores an integer value that it can be only 0 or 1.
     * This column specify if the product was already taken/bought by the user. If the value is 1 then the product was taken, 0 otherwise.
     */
    private static final String ALREADY_TAKEN_COLUMN = "AlreadyTaken";
    /** Fifth column of the product table. It stores the shopping list's ID, and indicates which list the product was inserted into.
     * So this column stores a foreign key.*/
    private static final String ID_SHOPPING_LIST_FK_COLUMN = "IdShoppingList";

    //column recipe table
    /** First column of the recipe table. It stores the ID that is the primary key (type integer).
     * The value for this column is inserted automatically by the system.*/
    private static final String ID_RECIPE_COLUMN = "IdRecipe";
    /** Second column of the recipe table. It stores the recipe's name and it's unique (type TEXT/String).*/
    private static final String NAME_RECIPE_COLUMN = "NameRecipe";
    /** Third column of the recipe table. It stores the recipe's description (type Text/String).*/
    private static final String DESCRIPTION_RECIPE_COLUMN = "DescriptionRecipe";
    /** Fourth column of the recipe table. It stores a byte array that represents the bitmap image of the recipe (type blob/byte array).*/
    private static final String IMAGE_RECIPE_COLUMN = "ImageRecipe";

    //column ingredient table
    /** First column of the ingredient table. It stores the ID that is the primary key (type integer).
     * The value for this column is inserted automatically by the system.*/
    private static final String ID_INGREDIENT_COLUMN = "IdIngredient";
    /** Second column of the ingredient table. It stores the ingredient's name (type TEXT/String).*/
    private static final String NAME_INGREDIENT_COLUMN = "NameIngredient";
    /** Third column of the ingredient table. It stores the ingredient's dose (type TEXT/String).*/
    private static final String DOSE_INGREDIENT_COLUMN = "DoseIngredient";
    /** Fourth column of the ingredient table. It stores the recipe's ID, and indicates which recipe the ingredient was inserted into.
     * So this column stores a foreign key.*/
    private static final String ID_RECIPE_FK_COLUMN = "IdRecipe";

    /** SQLite database used to do the insert, select, upgrade, delete, create, drop operations*/
    private SQLiteDatabase db;
    /** Stream used to convert the bitmap image into byte array. So the bitmap can be insert into the database*/
    private ByteArrayOutputStream baos;
    /** Byte array that stores the converted bitmap image */
    private byte[] imageInByte;

    /**
     * Database constructor.
     * @param context Context where the database construct is called.
     */
    public GroveryManagmentDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION_DATABASE);
    }

    /**
     * Method used to create database tables.
     * @param db SQLiteDatabase to pass to begin the built.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        //Execution of the query that create shopping list table
        db.execSQL("CREATE TABLE " + SHOPPING_LIST_TABLE_NAME
                + "(" + ID_SHOPPING_LIST_COLUMN + " INTEGER, "
                + NAME_SHOPPING_LIST_COLUMN + " TEXT, "
                + STATUS_SHOPPING_LIST + " INTEGER, "
                + "PRIMARY KEY (" + ID_SHOPPING_LIST_COLUMN + "), "
                + "UNIQUE (" + NAME_SHOPPING_LIST_COLUMN + "))");

        //Execution of the query that create product table
        db.execSQL("CREATE TABLE " + PRODUCT_TABLE_NAME
                + "(" + ID_PRODUCT_COLUMN + " INTEGER, "
                + NAME_PRODUCT_COLUMN + " TEXT, "
                + AMOUNT_PRODUCT_COLUMN + " INTEGER, "
                + ALREADY_TAKEN_COLUMN + " INTEGER, "
                + ID_SHOPPING_LIST_FK_COLUMN + " INTEGER, "
                + "PRIMARY KEY (" + ID_PRODUCT_COLUMN + "), "
                + "FOREIGN KEY (" + ID_SHOPPING_LIST_FK_COLUMN + ") REFERENCES " + SHOPPING_LIST_TABLE_NAME + "(" + ID_SHOPPING_LIST_COLUMN + "))");

        //Execution of the query that create recipe table
        db.execSQL("CREATE TABLE " + RECIPE_TABLE_NAME
                + "(" + ID_RECIPE_COLUMN + " INTEGER, "
                + NAME_RECIPE_COLUMN + " TEXT, "
                + DESCRIPTION_RECIPE_COLUMN + " TEXT, "
                + IMAGE_RECIPE_COLUMN + " BLOB, "
                + "PRIMARY KEY (" + ID_RECIPE_COLUMN + "), "
                + "UNIQUE (" + NAME_RECIPE_COLUMN + "))");

        //Execution of the query that creare ingredient table
        db.execSQL("CREATE TABLE " + INGREDIENT_TABLE_NAME
                + "(" + ID_INGREDIENT_COLUMN + " INTEGER, "
                + NAME_INGREDIENT_COLUMN + " TEXT, "
                + DOSE_INGREDIENT_COLUMN + " INTEGER, "
                + ID_RECIPE_FK_COLUMN + " INTEGER, "
                + "PRIMARY KEY (" + ID_INGREDIENT_COLUMN + "), "
                + "FOREIGN KEY (" + ID_RECIPE_FK_COLUMN + ") REFERENCES "
                + RECIPE_TABLE_NAME + "(" + ID_RECIPE_COLUMN + "))");
    }


    /**
     * Method used to enable foreign key constraint. In this way the couple of tables:
     * ShoppingLists-Products and Recipes-Ingredients, are linked by the foreign key.
     * @param db SQLiteDatabase to pass to enable the constraint.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Method used to upgrade the the database table. The upgrade is done only when the
     * integer newVersion is greater than the integer oldVersion.
     * @param db SQLiteDatabase to pass to do the upgrade.
     * @param oldVersion Integer value that represents the old version of the database.
     * @param newVersion Integer value that represents the new versione of the database.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_LIST_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RECIPE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + INGREDIENT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PRODUCT_TABLE_NAME);

        onCreate(db);
    }

    /**
     * Method used to get all the shopping lists that has been stored into the database.
     * The database is opened only in read mode.
     * @return A Cursor object that contains the value of shopping list table columns.
     */
    public Cursor getAllShoppingLists() {
        this.db = getReadableDatabase();

        String query = "SELECT * FROM " + SHOPPING_LIST_TABLE_NAME;

        return db.rawQuery(query, null);
    }

    /**
     * Method used to insert shopping list data into the table.
     * The database is opened in read/write mode.
     * @param nameShoppingList String value that represents the name of the shopping list.
     */
    public void insertShoppingList(String nameShoppingList) {
        this.db = getWritableDatabase();

        this.db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(NAME_SHOPPING_LIST_COLUMN, nameShoppingList);
            cv.put(STATUS_SHOPPING_LIST, -1);

            this.db.insert(SHOPPING_LIST_TABLE_NAME, null, cv);
            this.db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "insertShoppingList: error while inserting shopping list into database");
        } finally {
            this.db.endTransaction();
        }
    }

    /**
     * Method used to set the status of the shopping list as "empty", so the list hasn't products inside.
     * @param idShoppingList Integer value that represents the ID of the specified shopping list.
     */
    public void setEmptyStatusShoppingList(int idShoppingList) {
        this.db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(STATUS_SHOPPING_LIST, -1);

        this.db.update(SHOPPING_LIST_TABLE_NAME, cv, ID_SHOPPING_LIST_COLUMN + "=" + idShoppingList, null);
    }

    /**
     * Method used to set the status of the shopping list as "completed", so the products of the list have already been bought.
     * @param idShoppingList Integer value that represents the id of the specified shopping list.
     */
    public void setCompletedStatusShoppingList(int idShoppingList){
        this.db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(STATUS_SHOPPING_LIST, 1);

        this.db.update(SHOPPING_LIST_TABLE_NAME, cv, ID_SHOPPING_LIST_COLUMN + "=" + idShoppingList, null);
    }

    /**
     * Method used to set the status of the shopping list as "to complete", so not all the products have already been bought.
     * @param idShoppingList Integer value that represents the id of the specified shopping list.
     */
    public void setNotCompletedStatusShoppingList(int idShoppingList) {
        this.db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(STATUS_SHOPPING_LIST, 0);

        this.db.update(SHOPPING_LIST_TABLE_NAME, cv, ID_SHOPPING_LIST_COLUMN + "=" + idShoppingList, null);
    }

    /**
     * Method used to delete a shopping list specified by its ID.
     * The database is opened in read/write mode.
     * @param id Integer value that represents the ID of the shopping list to delete.
     */
    public void deleteShoppingListById(int id) {
        this.db = getWritableDatabase();

        this.db.delete(PRODUCT_TABLE_NAME, ID_SHOPPING_LIST_FK_COLUMN + "=" + id, null);
        this.db.delete(SHOPPING_LIST_TABLE_NAME, ID_SHOPPING_LIST_COLUMN + "=" + id, null);
    }

    /**
     * Method used to get all the products stored in a specific shopping list.
     * The database is opened only in read mode.
     * @param idShoppingList Integer value that specify the id of shopping list to get its products.
     * @return A Cursor object that contains the value of product table columns.
     */
    public Cursor getAllProductsOfAList(int idShoppingList) {
        this.db = getReadableDatabase();

        String query = "SELECT * FROM " + PRODUCT_TABLE_NAME
                + " INNER JOIN " + SHOPPING_LIST_TABLE_NAME
                + " ON " + PRODUCT_TABLE_NAME + "." + ID_SHOPPING_LIST_FK_COLUMN + "=" + SHOPPING_LIST_TABLE_NAME + "." + ID_SHOPPING_LIST_COLUMN
                + " WHERE " + PRODUCT_TABLE_NAME + "." + ID_SHOPPING_LIST_FK_COLUMN + "=" + idShoppingList + "";

        return db.rawQuery(query, null);
    }

    /**
     * Method used to insert product data into the table.
     * The database is opened in read/write mode.
     * @param nameProduct String value that represents the name of the product.
     * @param amountProduct Integer value that represents the amount of the product
     * @param idShoppingList Integer value that represents the id of the shopping list which the product has been inserted into.
     */
    public void insertProduct(String nameProduct, int amountProduct, int idShoppingList) {
        this.db = getWritableDatabase();

        this.db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(NAME_PRODUCT_COLUMN, nameProduct);
            cv.put(AMOUNT_PRODUCT_COLUMN, amountProduct);
            cv.put(ALREADY_TAKEN_COLUMN, 0);
            cv.put(ID_SHOPPING_LIST_FK_COLUMN, idShoppingList);

            this.db.insert(PRODUCT_TABLE_NAME, null, cv);
            this.db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "insertProduct: error while inserting product into database");
        } finally {
            this.db.endTransaction();
        }
    }

    /**
     * Method used to delete a product specified by its ID.
     * The database is opened in read/write mode.
     * @param idProduct Integer value that represents the ID of the product to delete.
     */
    public void deleteProductById(int idProduct) {
        this.db = getWritableDatabase();

        this.db.delete(PRODUCT_TABLE_NAME, ID_PRODUCT_COLUMN + "=" + idProduct, null);
    }

    /**
     * Method used to update the product data specified by its ID.
     * The database is opened in read/write mode.
     * @param idProduct Integer value that represents the ID of the product to update.
     * @param nameProduct String value that represents the new name of the product.
     * @param amountProduct Integer value that represents the new amount of the product.
     */
    public void updateProductData(int idProduct, String nameProduct, int amountProduct) {
        this.db = getWritableDatabase();

        String oldNameProduct = getNameProductById(idProduct);

        ContentValues cv = new ContentValues();
        cv.put(NAME_PRODUCT_COLUMN, nameProduct);
        cv.put(AMOUNT_PRODUCT_COLUMN, amountProduct);

        if (!nameProduct.equals(oldNameProduct)) {
            cv.put(ALREADY_TAKEN_COLUMN, 0);
        }

        this.db.update(PRODUCT_TABLE_NAME, cv, ID_PRODUCT_COLUMN + "=?", new String[]{String.valueOf(idProduct)});
    }

    /**
     * Method used to get all the recipes that has been stored into the database.
     * The database is opened only in read mode.
     * @return A Cursor object that contains the value of recipe column.
     */
    public Cursor getAllRecipes() {
        this.db = getReadableDatabase();

        String query = "SELECT * FROM " + RECIPE_TABLE_NAME;

        return db.rawQuery(query, null);
    }

    /**
     * Method used to get the image of the recipe specified by its ID.
     * @param idRecipe Integer value that represents the ID of recipe to use to get the image.
     * @return Bitmap object that represents the image of recipe.
     */
    public Bitmap getImageRecipeById(int idRecipe) {
        this.db = getReadableDatabase();

        String query = "SELECT * FROM " + RECIPE_TABLE_NAME + " WHERE " + ID_RECIPE_COLUMN + "=" + idRecipe;
        Cursor cur = db.rawQuery(query, null);

        cur.moveToFirst();
        byte[] imageToDecode = cur.getBlob(3);
        cur.close();

        if (imageToDecode == null) {
            return null;
        }else{
            return BitmapFactory.decodeByteArray(imageToDecode, 0, imageToDecode.length);
        }
    }

    /**
     * Method used to insert the recipe data into the table.
     * The database is opened in read/write mode.
     * @param nameRecipe String value that represents the name of the recipe.
     * @param descriptionRecipe String value that represents the description of the recipe.
     * @param imageToStore Bitmap object that represents the image of the recipe.
     */
    public void insertRecipe(String nameRecipe, String descriptionRecipe, Bitmap imageToStore) {
        this.db = getWritableDatabase();

        this.db.beginTransaction();
        try {
            baos = new ByteArrayOutputStream();
            imageToStore.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            imageInByte = baos.toByteArray();

            ContentValues cv = new ContentValues();
            cv.put(NAME_RECIPE_COLUMN, nameRecipe);
            cv.put(DESCRIPTION_RECIPE_COLUMN, descriptionRecipe);
            cv.put(IMAGE_RECIPE_COLUMN, imageInByte);

            this.db.insert(RECIPE_TABLE_NAME, null, cv);
            this.db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while inserting recipe's data into database");
        } finally {
            this.db.endTransaction();
        }
    }

    /**
     * Method used to insert data of a recipe that has benn shared by another user using Bluetooth.
     * The database is opened in read/write mode.
     * @param nameRecipe String value that represents the name of the shared recipe.
     * @param descriptionRecipe String value that represents the description of the shared recipe.
     */
    public void insertSharedRecipe(String nameRecipe, String descriptionRecipe) {
        this.db = getWritableDatabase();

        this.db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(NAME_RECIPE_COLUMN, nameRecipe);
            cv.put(DESCRIPTION_RECIPE_COLUMN, descriptionRecipe);

            this.db.insert(RECIPE_TABLE_NAME, IMAGE_RECIPE_COLUMN, cv);
            this.db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while inserting recipe's data into database");
        } finally {
            this.db.endTransaction();
        }
    }

    /**
     * Method used to insert ingredient data into the table.
     * The database is opened in read/write mode.
     * @param nameIngredient String value that represents the name of the ingredient.
     * @param doseIngredient String value that represents the dose of the ingredient.
     * @param nameRecipe String value that represents the name of the recipe which the ingredient has been inserted.
     */
    public void insertIngredient(String nameIngredient, String doseIngredient, String nameRecipe) {
        this.db = getWritableDatabase();

        this.db.beginTransaction();
        try {
            int id = getIdRecipeByName(nameRecipe);

            ContentValues cv = new ContentValues();
            cv.put(NAME_INGREDIENT_COLUMN, nameIngredient);
            cv.put(DOSE_INGREDIENT_COLUMN, doseIngredient);
            cv.put(ID_RECIPE_FK_COLUMN, id);

            this.db.insert(INGREDIENT_TABLE_NAME, null, cv);
            this.db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.db.endTransaction();
        }
    }

    /**
     * Private method used to get the ID recipe by its name.
     * @param nameRecipe String value that represents the name of the recipe to get the ID.
     * @return An integer value that represents the ID recipe.
     */
    private int getIdRecipeByName(String nameRecipe) {
        String query = "SELECT " + ID_RECIPE_COLUMN + " FROM " + RECIPE_TABLE_NAME
                + " WHERE " + NAME_RECIPE_COLUMN + "='" + nameRecipe + "'";

        Cursor cur = this.db.rawQuery(query, null);
        cur.moveToFirst();

        int idRecipe = cur.getInt(0);
        cur.close();

        return idRecipe;
    }

    /**
     * Private method used to get the name of the product by its ID.
     * @param id Integer value that represents the ID of the product to get the name.
     * @return A String value that represents the name of the product.
     */
    private String getNameProductById(int id) {
        String query = "SELECT " + NAME_PRODUCT_COLUMN + " FROM " + PRODUCT_TABLE_NAME
                + " WHERE " + ID_PRODUCT_COLUMN + "=" + id;

        Cursor cur = this.db.rawQuery(query, null);
        cur.moveToFirst();

        String nameProduct = cur.getString(1);
        cur.close();

        return nameProduct;
    }

    /**
     * Method used to get all the ingredients that has been stored into a specific recipe.
     * The database is opened only in read mode.
     * @param idRecipe Integer value that represents the ID of the recipe to get all its ingredients.
     * @return A Cursor object that contains value of the ingredient table column.
     */
    public Cursor getAllIngredientsOfARecipe(int idRecipe) {
        this.db = getReadableDatabase();

        String query = "SELECT * FROM " + INGREDIENT_TABLE_NAME + " WHERE " + ID_RECIPE_FK_COLUMN + "=" + idRecipe;

        return this.db.rawQuery(query, null);
    }

    /**
     * Method used to get all the ingredients that has been stored into the database.
     * The database is opened only in read mode.
     * @return A Cursor object that contains value of the ingredient table column.
     */
    public Cursor getAllIngredients() {
        this.db = getReadableDatabase();

        String query = "SELECT * FROM " + INGREDIENT_TABLE_NAME;

        return this.db.rawQuery(query, null);
    }

    /**
     * Method used to update the data of a specific recipe.
     * The database is opened in read/write mode.
     * @param idRecipe Integer value that represents the ID of the recipe to update.
     * @param nameRecipe String value that represents the new name of the recipe.
     * @param descriptionRecipe String value that represents the new description of the recipe.
     * @param imageToStore Bitmap object that represents the new image of the recipe.
     */
    public void updateRecipeData(int idRecipe, String nameRecipe, String descriptionRecipe, Bitmap imageToStore) {
        this.db = getWritableDatabase();

        baos = new ByteArrayOutputStream();
        imageToStore.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        imageInByte = baos.toByteArray();

        ContentValues cv = new ContentValues();
        cv.put(NAME_RECIPE_COLUMN, nameRecipe);
        cv.put(DESCRIPTION_RECIPE_COLUMN, descriptionRecipe);
        cv.put(IMAGE_RECIPE_COLUMN, imageInByte);

        this.db.update(RECIPE_TABLE_NAME, cv, ID_RECIPE_COLUMN + "=" + idRecipe, null);
    }

    /**
     * Method used to update the data of an ingredient that belongs to a specific recipe.
     * The database is opened in read/write mode.
     * @param idRecipe Integer value that represents the ID of the recipe which the ingredient belongs.
     * @param ingredient Ingredient object used to update the value in the table.
     */
    public void updateIngredientOfARecipe(int idRecipe, Ingredient ingredient) {
        this.db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(NAME_INGREDIENT_COLUMN, ingredient.getNameIngredient());
        cv.put(DOSE_INGREDIENT_COLUMN, ingredient.getDoseIngredient());

        this.db.update(INGREDIENT_TABLE_NAME, cv, ID_RECIPE_FK_COLUMN + "=" + idRecipe + " AND " + ID_INGREDIENT_COLUMN + "=" + ingredient.getIdIngredient(), null);
    }

    /**
     *  Method used to delete a recipe by its ID.
     *  The database is opened in read/write mode.
     * @param idRecipe Integer value that represents the ID of the recipe to delete.
     */
    public void deleteRecipeById(int idRecipe) {
        this.db = getWritableDatabase();

        this.db.delete(INGREDIENT_TABLE_NAME, ID_RECIPE_FK_COLUMN + "=" + idRecipe, null);
        this.db.delete(RECIPE_TABLE_NAME, ID_RECIPE_COLUMN + "=" + idRecipe, null);
    }

    /**
     *  Method used to delete an ingredient by its ID.
     *  The database is opened in read/write mode.
     * @param idIngredient Integer value that represents the ID of the ingredient to delete.
     */
    public void deleteIngredientById(int idIngredient) {
        this.db = getWritableDatabase();

        this.db.delete(INGREDIENT_TABLE_NAME, ID_INGREDIENT_COLUMN + "=" + idIngredient, null);
    }

    /**
     * Method used to check if a product is already present inside a specific shopping list.
     * The database is opened only in read mode.
     * @param nameProduct String value that represents the name of the product to check if it's present inside the shopping list.
     * @param idShoppingList Integer value that represents the id of the shopping list.
     * @return True if the product is already present inside the shopping list, false otherwise.
     */
    public boolean isProductInsideShoppingList(String nameProduct, int idShoppingList) {
        this.db = getReadableDatabase();

        String query = "SELECT * FROM " + PRODUCT_TABLE_NAME
                + " WHERE " + NAME_PRODUCT_COLUMN + "='" + nameProduct + "' AND "
                + ID_SHOPPING_LIST_FK_COLUMN + "=" + idShoppingList;

        Cursor cur = this.db.rawQuery(query, null);

        int sizeCursor = cur.getCount();
        cur.close();

        return sizeCursor != 0;
    }

    /**
     * Method used to check if an ingredient is already present inside a specific recipe.
     * The database is opened only in read mode.
     * @param nameIngredient String value that represents the name of the ingredient to check if it's present inside the recipe.
     * @param nameRecipe String value that represents the name of the recipe.
     * @return True if the ingredient is already present inside the recipe, false otherwise.
     */
    public boolean isIngredientInsideRecipeByName(String nameIngredient, String nameRecipe) {
        this.db = getReadableDatabase();

        int id = getIdRecipeByName(nameRecipe);

        String query = "SELECT * FROM " + INGREDIENT_TABLE_NAME
                + " WHERE " + NAME_INGREDIENT_COLUMN + "='" + nameIngredient
                + "' AND " + ID_RECIPE_FK_COLUMN + "=" + id;

        Cursor cur = this.db.rawQuery(query, null);

        int sizeCursor = cur.getCount();
        cur.close();

        return sizeCursor != 0;
    }

    /**
     * Method used to check if an ingredient is already present inside a specific recipe.
     * The databse is opened only in read mode.
     * @param nameIngredient String value that represents the name of the ingredient to check if it's present inside the recipe.
     * @param idRecipe Integer value that represents the ID of the recipe.
     * @return True if the ingredient is already present inside the recipe, false otherwise.
     */
    public boolean isIngredientInsideRecipeById(String nameIngredient, int idRecipe) {
        this.db = getReadableDatabase();

        String query = "SELECT * FROM " + INGREDIENT_TABLE_NAME
                + " WHERE " + NAME_INGREDIENT_COLUMN + "='" + nameIngredient
                + "' AND " + ID_RECIPE_FK_COLUMN + "=" + idRecipe;

        Cursor cur = this.db.rawQuery(query, null);

        int sizeCursor = cur.getCount();
        cur.close();

        return sizeCursor != 0;
    }

    /**
     * Method used to modify the value of product table column AlreadyTaken.
     * The database is opened in read/write mode.
     * @param nameProduct String that represents the name of the product.
     * @param idShoppingList Integer that represents the id of shopping list which the product belongs to.
     */
    public void setProductAsTaken(String nameProduct, int idShoppingList) {
        this.db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(ALREADY_TAKEN_COLUMN, 1);

        this.db.update(PRODUCT_TABLE_NAME, cv, NAME_PRODUCT_COLUMN + "='" + nameProduct + "' AND " + ID_SHOPPING_LIST_FK_COLUMN + "=" + idShoppingList, null);
    }

    public void setProductAsTaken(int idProduct) {
        this.db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(ALREADY_TAKEN_COLUMN, 1);

        this.db.update(PRODUCT_TABLE_NAME, cv, ID_PRODUCT_COLUMN + "=" + idProduct, null);
    }

    public void setProductAsNoTaken(int idProduct) {
        this.db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(ALREADY_TAKEN_COLUMN, 0);

        this.db.update(PRODUCT_TABLE_NAME, cv, ID_PRODUCT_COLUMN + "=" + idProduct, null);
    }

    /**
     * Method used to check if a shopping list has already been stored with the same name.
     * The database is opened only in read mode.
     * @param nameShoppingList String value that represents the name of the shopping list to check.
     * @return True if it's been already stored a shopping list with the same name, false otherwise.
     */
    public boolean isShoppingListAlreadyPresentInDb(String nameShoppingList) {
        this.db = getReadableDatabase();

        String query = "SELECT * FROM " + SHOPPING_LIST_TABLE_NAME + " WHERE " + NAME_SHOPPING_LIST_COLUMN + "='" + nameShoppingList + "'";

        Cursor cur = this.db.rawQuery(query, null);

        int sizeCursor = cur.getCount();
        cur.close();

        return sizeCursor != 0;
    }

    /**
     * Method used to check if a recipe has already been stored with the same name.
     * @param nameRecipe String value that represents the name of the recipe to check.
     * @return True if it's been already stored a recipe with the same name, false otherwise.
     */
    public boolean isRecipeAlreadyPresentInDb(String nameRecipe) {
        this.db = getReadableDatabase();

        String query = "SELECT * FROM " + RECIPE_TABLE_NAME + " WHERE " + NAME_RECIPE_COLUMN + "='" + nameRecipe + "'";

        Cursor cur = this.db.rawQuery(query, null);

        int sizeCursor = cur.getCount();
        cur.close();

        return sizeCursor != 0;
    }

    /**
     * Method used to update the name of a specific shopping list.
     * The database is opened in read/write mode.
     * @param idShoppingList Integer that represents the ID of the shopping list to update.
     * @param nameShoppingList String value that represents the new name of the shopping list.
     * */
    public void updateNameShoppingList(int idShoppingList, String nameShoppingList) {
        this.db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(NAME_SHOPPING_LIST_COLUMN, nameShoppingList);

        this.db.update(SHOPPING_LIST_TABLE_NAME, cv, ID_SHOPPING_LIST_COLUMN + "=" + idShoppingList, null);
    }
}
