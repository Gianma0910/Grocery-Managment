package utilityClassDatabase;

public class ShoppingList {

    private int idShoppingList;
    private String nameShoppingList;
    private int statusShoppingList;

    public ShoppingList (int id, String name, int status) {
        this.idShoppingList = id;
        this.nameShoppingList = name;
        this.statusShoppingList = status;
    }

    public void setIdShoppingList(int idShoppingList) {
        this.idShoppingList = idShoppingList;
    }

    public void setNameShoppingList(String nameShoppingList) {
        this.nameShoppingList = nameShoppingList;
    }

    public void setStatusShoppingList(int statusShoppingList) {
        this.statusShoppingList = statusShoppingList;
    }

    public int getIdShoppingList() {
        return idShoppingList;
    }

    public String getNameShoppingList() {
        return nameShoppingList;
    }

    public int getStatusShoppingList() {
        return statusShoppingList;
    }
}


