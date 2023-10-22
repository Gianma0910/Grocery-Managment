package classRecyclerView.ViewHolder;

import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerymanagement.R;

/**
 * ViewHolder subclass that describes an item view of the MainActivity's RecyclerView.
 * This ViewHolder must be instanced by CustomAdapterShoppingList.
 */
public class ShoppingListViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

    /**
     * TextView of the item view that contains ID of the shopping list.
     */
    public TextView shoppingListId;
    /**
     * TextView of the item view that contains the name of the shopping list.
     */
    public TextView shoppingListName;
    /**
     * TextView that represents the status of shopping list (empty, completed, to complete).
     */
    public TextView statusShoppingList;
    /**
     * ImageButton of the item view that deletes the item from the recycler view and from the local database.
     */
    public ImageButton buttonDeleteShoppingList;
    /**
     * ImageButton of the item view that modifies the item in the recycler view and in the local database.
     */
    public ImageButton buttonEditContentShoppingList;

    /**
     * ShoppingListViewHolder constructor.
     * @param itemView View object that represents the single element of the recyclerView.
     */
    public ShoppingListViewHolder(View itemView) {
        super(itemView);

        shoppingListId = itemView.findViewById(R.id.shopping_list_id);
        shoppingListName = itemView.findViewById(R.id.shopping_list_name);
        buttonDeleteShoppingList = itemView.findViewById(R.id.button_delete_shopping_list);
        buttonEditContentShoppingList = itemView.findViewById(R.id.button_edit_content_shopping_list);
        statusShoppingList = itemView.findViewById(R.id.status_shopping_list);

        itemView.setOnCreateContextMenuListener(this);
    }

    /**
     * Method to create the context menu for every item view of the recycler view.
     */
    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.add(Menu.NONE, 1, 1, "Edit name shopping list");
    }
}
