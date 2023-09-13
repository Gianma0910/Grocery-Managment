package classRecyclerView;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groverymanagment.R;

/**
 * Adapter that uses the Cursor object that contains all the products data of a specific shopping list stored into the local database.
 * In this adapter its items can't be modified or deleted.
 * This adapter must be set for NoEditableShoppingListActivity's RecyclerView.
 */
public class CustomAdapterNoEditableProduct extends RecyclerView.Adapter<NoEditableProductViewHolder> {

    /**
     * Context object that represents where the object class has been instanced.
     */
    Context context;
    /**
     * Cursor object that contains all the products data of a specific shopping list stored into the local database.
     */
    Cursor cursor;

    /**
     * CustomAdapterNoEditableProduct constructor.
     * @param context Context object that represents where the object class has been instanced.
     */
    public CustomAdapterNoEditableProduct(Context context) {
        this.context = context;
        this.cursor = null;
    }

    @NonNull
    @Override
    public NoEditableProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View v = layoutInflater.inflate(R.layout.no_editable_card_product, parent, false);

        return new NoEditableProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NoEditableProductViewHolder holder, int position) {
        this.cursor.moveToPosition(position);

        holder.textViewIdProduct.setText(cursor.getString(0));
        holder.textViewNameProduct.setText(cursor.getString(1));
        holder.textViewAmountProduct.setText(cursor.getString(2));

        int flagProductTaken = cursor.getInt(3);

        holder.checkProductTaken.setChecked(flagProductTaken == 1);
    }

    @Override
    public int getItemCount() {
        if (this.cursor == null) {
            return 0;
        }
        return this.cursor.getCount();
    }

    /**
     * Method used to set/upload the adapter cursor. If it's null then close and upload it.
     * @param cursor Cursor object which has used to set the one of the adapter.
     */
    public void setCursor(Cursor cursor) {
        if (this.cursor != null) {
            this.cursor.close();
        }
        this.cursor = cursor;
        notifyDataSetChanged();
    }
}
