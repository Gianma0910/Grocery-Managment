package classRecyclerView;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groverymanagment.R;

/**
 * Adapter that uses Cursor object that contains all products data of a specific shopping list stored into the local database.
 * In this adapter its item can be modified or deleted.
 * This adapter must be set for ShoppingListActivity's RecyclerView.
 */
public class CustomAdapterProduct extends RecyclerView.Adapter<ProductViewHolder> {

    /**
     * Context object that represents where the object class has been instanced.
     */
    Context context;
    /**
     * Cursor object that contains all the products data of a specific shopping list stored into the local database.
     */
    Cursor cursor;
    /**
     * OnClickListener interface used to set up some methods for adapter's item.
     */
    OnClickListener onClickListener;

    /**
     * CustomAdapterProduct construct.
     * @param context Context object that represents where the object has been instanced.
     */
    public CustomAdapterProduct (Context context) {
        this.context = context;
        this.cursor = null;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.card_product, parent,false);

        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        cursor.moveToPosition(position);

        holder.textViewIdProduct.setText(cursor.getString(0));
        holder.textViewNameProduct.setText(cursor.getString(1));
        holder.textViewAmountProduct.setText(cursor.getString(2));

        int flagProductTaken = cursor.getInt(3);

        holder.checkProductTaken.setChecked(flagProductTaken == 1);

        holder.buttonDelete.setOnClickListener(view -> {
            if (onClickListener != null) {
                cursor.moveToPosition(position);
                onClickListener.onDelete(position, cursor.getInt(0));
            }
        });

        holder.buttonEdit.setOnClickListener(view -> {
            if (onClickListener != null) {
                cursor.moveToPosition(position);
                onClickListener.onEdit(position, cursor.getInt(0), cursor.getString(1), cursor.getInt(2));
            }
        });
    }

    @Override
    public int getItemCount() {
        if (cursor == null) return 0;
        else return cursor.getCount();
    }

    /**
     * Method used to set/upload the adapter cursor. If it isn't null then close and upload it.
     * @param cursor Cursor object which has used to set the one of the adapter.
     */
    public void setCursor(Cursor cursor) {
        if (this.cursor != null) {
            this.cursor.close();
        }
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    /**
     * Interface that declares some methods for adapter items.
     */
    public interface OnClickListener{
        /**
         * Method used to check which item has been clicked and then the method tries to delete the selected item.
         * @param position Integer value that represents the position into the adapter cursor of the selected item.
         * @param idProduct Integer value that represents the ID of the selected product. This value is used to delete the selected product.
         */
        void onDelete(int position, int idProduct);
        /**
         * Method used to check which item has been clicked and then the method tries to modify the selected item.
         * @param position Integer value that represents the position into the adapter cursor of the selected item.
         * @param idProduct Integer value that represents the ID of the selected product. This value is used to modify the selected product.
         * @param nameProduct String value that represents the name of the selected product to modify.
         * @param amountProduct Integer value that represent the amount of product of the selected product to modify.
         */
        void onEdit(int position, int idProduct, String nameProduct, int amountProduct);
    }

    /**
     * Method used to set the OnClickListener of the adapter.
     * @param onClickListener OnClickListener which has used to set the one of the adapter.
     */
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
