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
 * Adapter that uses Cursor object that contains all shopping list data stored into local database.
 * In this adapter its items can be modified or deleted.
 * This adapter must be set for MainActivity's RecyclerView.
 */
public class CustomAdapterShoppingList extends RecyclerView.Adapter<ShoppingListViewHolder> {

    /**
     * Context object that represents where the object class has been instanced.
     */
    Context context;
    /**
     * Cursor object that contains all shopping list data stored int local database.
     */
    Cursor cursor;
    /**
     * Integer value that represents the position of the item into the adapter cursor.
     */
    private int position;
    /**
     * OnClickListener interface used to set up some methods for adapter's item.
     */
    OnClickListener onClickListener;

    /**
     * CustomAdapterShoppingList constructor.
     * @param context Context object that represents where the object class has been instanced.
     */
    public CustomAdapterShoppingList(Context context) {
        this.context = context;
        this.cursor = null;
    }

    @NonNull
    @Override
    public ShoppingListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.card_shopping_list, parent, false);

        return new ShoppingListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ShoppingListViewHolder holder, int position) {
        cursor.moveToPosition(position);

        holder.shoppingListId.setText(cursor.getString(0));
        holder.shoppingListName.setText(cursor.getString(1));

        holder.itemView.setOnLongClickListener(view -> {
            setPosition(holder.getAdapterPosition());
            return false;
        });

        holder.itemView.setOnClickListener(view -> {
            if (onClickListener != null) {
                cursor.moveToPosition(position);
                onClickListener.onClickCardView(position, cursor.getString(1));
            }
        });

        holder.buttonDeleteShoppingList.setOnClickListener(view -> {
            if (onClickListener != null) {
                cursor.moveToPosition(position);
                onClickListener.onDelete(position, cursor.getInt(0));
            }
        });

        holder.buttonEditContentShoppingList.setOnClickListener(view -> {
            if (onClickListener != null) {
                cursor.moveToPosition(position);
                onClickListener.onEdit(position, cursor.getString(1));
            }
        });
    }

    @Override
    public int getItemCount() {
        if (cursor == null) return 0;
        else return cursor.getCount();
    }

    /**
     * Method used to get the position of item into the adapter cursor.
     * @return Integer value that represents the position of the item.
     */
    public int getPosition() {
        return position;
    }

    /**
     * Method used to set the item position.
     * @param position Integer value used to set item position.
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Method used to set/upload the adapter cursor. If it isn't null then close and set/upload it.
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
         * Method used to check which item has been clicked and then the method tries to launch an Intent to NoEditableShoppingListActivity.class
         * @param position Integer value that represents the position of the selected item into the adapter cursor.
         * @param nameShoppingList String value that represents the name of the shopping list of the selected item.
         */
        void onClickCardView(int position, String nameShoppingList);

        /**
         * Method used to check which item has been clicked and then the method tries to delete the selected item.
         * @param position Integer value that represents the position of the selected item into the adapter cursor.
         * @param idShoppingList Integer value that represents the ID of shopping list of the selected item. This value is used to delete the shopping list.
         */
        void onDelete(int position, int idShoppingList);

        /**
         * Method used to check which item has been clicked and then the method tries to modify the selected item.
         * @param position Integer value that represents the position of the selected item into the adapter cursor.
         * @param nameShoppingList String value that represents the name of the shopping list of the selected item.
         */
        void onEdit(int position, String nameShoppingList);
    }

    /**
     * Method used to set the OnClickListener of the adapter.
     * @param onClickListener OnClickListener which has used to set the one of the adapter.
     */
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
