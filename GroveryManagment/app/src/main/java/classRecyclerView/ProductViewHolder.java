package classRecyclerView;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.groverymanagment.R;

/**
 * ViewHolder subclass that describes the item view of the ShoppingListActivity's RecyclerView.
 * This ViewHolder must be instanced by CustomAdapterProduct class.
 */
public class ProductViewHolder extends RecyclerView.ViewHolder {

    /**
     * TextView of the item view that contains the string "Name:".
     */
    TextView textNameProduct;
    /**
     * TextView of the item view that contains the name of the product.
     */
    TextView textViewNameProduct;
    /**
     * TextView of the item view that contains the string "Amount:".
     */
    TextView textAmountProduct;
    /**
     * TextView of the item view that contains the amount of the product.
     */
    TextView textViewAmountProduct;
    /**
     * TextView of the item view that contains the string "ID:".
     */
    TextView textIdProduct;
    /**
     * TextView of the item view that contains the ID of the product.
     */
    TextView textViewIdProduct;
    /**
     * ImageButton of the item view that modifies the item view in the recycler view and in the local database.
     */
    ImageButton buttonEdit;
    /**
     * ImageButton of the item view that deletes the item view from the recycler view and from the local database.
     */
    ImageButton buttonDelete;
    /**
     * CheckBox of the item view that specifies if the product has already been taken or not.
     */
    CheckBox checkProductTaken;

    /**
     * ProductViewHolder constructor.
     * @param itemView View object that represents the single item of the recycler view.
     */
    public ProductViewHolder(View itemView) {
        super(itemView);

        textNameProduct = itemView.findViewById(R.id.text_name_product);
        textViewNameProduct = itemView.findViewById(R.id.text_view_name_product);
        textAmountProduct = itemView.findViewById(R.id.text_amount_product);
        textViewAmountProduct = itemView.findViewById(R.id.text_view_amount_product);
        textIdProduct = itemView.findViewById(R.id.text_id_product);
        textViewIdProduct = itemView.findViewById(R.id.text_view_id_product);
        buttonEdit = itemView.findViewById(R.id.button_edit_product);
        buttonDelete = itemView.findViewById(R.id.button_delete_product);
        checkProductTaken = itemView.findViewById(R.id.check_product_taken);

        checkProductTaken.setClickable(false);
    }
}
