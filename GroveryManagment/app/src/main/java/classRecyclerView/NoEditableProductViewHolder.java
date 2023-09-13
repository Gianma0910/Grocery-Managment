package classRecyclerView;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groverymanagment.R;

/**
 * ViewHolder subclass that describes item view of NoEditableShoppingListActivity's RecyclerView.
 * This ViewHolder must be instanced by CustomAdapterNoEditableProduct class.
 */
public class NoEditableProductViewHolder extends RecyclerView.ViewHolder {

    /**
     * TextView of the item view that contains the ID of the product.
     */
    TextView textViewIdProduct;
    /**
     * TextView of the item view that contains the name of the product.
     */
    TextView textViewNameProduct;
    /**
     * TextView of the item view that contains the amount of the product.
     */
    TextView textViewAmountProduct;
    /**
     * CheckBox of the item view that specifies if the product has already been taken or not.
     */
    CheckBox checkProductTaken;

    /**
     * NoEditableProductViewHolder constructor.
     * @param itemView View object that represents the single item of the recycler view.
     */
    public NoEditableProductViewHolder(@NonNull View itemView) {
        super(itemView);

        textViewIdProduct = itemView.findViewById(R.id.text_view_id_product);
        textViewNameProduct = itemView.findViewById(R.id.text_view_name_product);
        textViewAmountProduct = itemView.findViewById(R.id.text_view_amount_product);
        checkProductTaken = itemView.findViewById(R.id.check_product_taken);

        checkProductTaken.setClickable(false);
    }
}
