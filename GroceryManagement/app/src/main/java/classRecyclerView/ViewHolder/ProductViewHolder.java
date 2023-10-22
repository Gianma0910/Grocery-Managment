package classRecyclerView.ViewHolder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerymanagement.R;

/**
 * ViewHolder subclass that describes the item view of the ShoppingListActivity's RecyclerView.
 * This ViewHolder must be instanced by CustomAdapterProduct class.
 */
public class ProductViewHolder extends RecyclerView.ViewHolder {
    /**
     * TextView of the item view that contains the name of the product.
     */
    public TextView textViewNameProduct;
    /**
     * TextView of the item view that contains the amount of the product.
     */
    public TextView textViewAmountProduct;
    /**
     * TextView of the item view that contains the ID of the product.
     */
    public TextView textViewIdProduct;
    /**
     * ImageButton of the item view that modifies the item view in the recycler view and in the local database.
     */
    public ImageButton buttonEdit;
    /**
     * ImageButton of the item view that deletes the item view from the recycler view and from the local database.
     */
    public ImageButton buttonDelete;
    /**
     * CheckBox of the item view that specifies if the product has already been taken or not.
     */
    public CheckBox checkProductTaken;

    /**
     * ProductViewHolder constructor.
     * @param itemView View object that represents the single item of the recycler view.
     */
    public ProductViewHolder(View itemView) {
        super(itemView);

        textViewNameProduct = itemView.findViewById(R.id.text_view_name_product);
        textViewAmountProduct = itemView.findViewById(R.id.text_view_amount_product);
        textViewIdProduct = itemView.findViewById(R.id.text_view_id_product);
        buttonEdit = itemView.findViewById(R.id.button_edit_product);
        buttonDelete = itemView.findViewById(R.id.button_delete_product);
        checkProductTaken = itemView.findViewById(R.id.check_product_taken);

        checkProductTaken.setClickable(true);
    }
}
