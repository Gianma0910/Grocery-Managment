package classRecyclerView;

import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.groverymanagment.R;

/**
 * ViewHolder subclass that describes an item view of the MainActivity's RecyclerView.
 * This ViewHolder must be instanced by CustomAdapterShoppingList.
 */
public class ShoppingListViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

    /**
     * TextView of item view that contains the string "ID:".
     */
    TextView shoppingListId;
    /**
     * TextView of the item view that contains ID of the shopping list.
     */
    TextView shoppingListIdText;
    /**
     * TextView of the item view that contains the string "Name:"
     */
    TextView shoppingListName;
    /**
     * TextView of the item view that contains the name of the shopping list.
     */
    TextView shoppingListNameText;
    /**
     * ImageButton of the item view that deletes the item from the recycler view and from the local database.
     */
    ImageButton buttonDeleteShoppingList;
    /**
     * ImageButton of the item view that modifies the item in the recycler view and in the local database.
     */
    ImageButton buttonEditContentShoppingList;

    /**
     * ShoppingListViewHolder constructor.
     * @param itemView View object that represents the single element of the recyclerView.
     */
    public ShoppingListViewHolder(View itemView) {
        super(itemView);

        shoppingListId = itemView.findViewById(R.id.shopping_list_id);
        shoppingListIdText = itemView.findViewById(R.id.shopping_list_id_text_view);
        shoppingListName = itemView.findViewById(R.id.shopping_list_name);
        shoppingListNameText = itemView.findViewById(R.id.shopping_list_name_text_view);
        buttonDeleteShoppingList = itemView.findViewById(R.id.button_delete_shopping_list);
        buttonEditContentShoppingList = itemView.findViewById(R.id.button_edit_content_shopping_list);

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
