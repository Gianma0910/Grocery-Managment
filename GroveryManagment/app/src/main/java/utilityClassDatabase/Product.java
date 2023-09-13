package utilityClassDatabase;

public class Product {

    private int idProduct;
    private String nameProduct;
    private int amount;
    private String nameShoppingListForProduct;

    public Product (int id, String name, int amount, String nameShoppingList) {
        this.idProduct = id;
        this.nameProduct = name;
        this.amount = amount;
        this.nameShoppingListForProduct = nameShoppingList;
    }

    public void setIdProduct(int idProduct) {
        this.idProduct = idProduct;
    }

    public int getIdProduct() {
        return idProduct;
    }

    public void setNameProduct(String nameProduct) {
        this.nameProduct = nameProduct;
    }

    public String getNameProduct() {
        return nameProduct;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public void setNameshoppingList(String nameShoppingList) {
        this.nameShoppingListForProduct = nameShoppingList;
    }

    public String getNameShoppingList() {
        return nameShoppingListForProduct;
    }
}

