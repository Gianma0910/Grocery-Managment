package classRecyclerView.CustomAdapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerymanagement.R;
import java.util.ArrayList;
import java.util.List;

import classRecyclerView.ViewHolder.DiscoveredDevicesViewHolder;

/**
 * Adapter that uses a List of BluetoothDevice and a List object of String to show discovered devices data.
 * This adapter must be set for ConnectToDeviceActivity's RecyclerView.
 */
public class CustomAdapterDiscoveryDevices extends RecyclerView.Adapter<DiscoveredDevicesViewHolder> {

    /**
     * List of BluetoothDevice objects that contains all the discovered devices.
     */
    List<BluetoothDevice> discoveredDevices;
    /**
     * List of String that contains all the name of the discovered devices.
     */
    List<String> nameDiscoveredDevices;
    /**
     * Context object where the CustomAdapterDiscoveryDevices has been instanced.
     */
    Context context;
    /**
     * OnClickListener interface used to set up some methods for adapter's item.
     */
    OnClickListener onClickListener;

    /**
     * CustomAdapterDiscoveryDevices constructor.
     * @param context Context object where the object class has been instanced.
     */
    public CustomAdapterDiscoveryDevices(Context context) {
        this.context = context;
        this.nameDiscoveredDevices = new ArrayList<>();
        this.discoveredDevices = new ArrayList<>();
    }

    @NonNull
    @Override
    public DiscoveredDevicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.card_discovered_devices, parent, false);

        return new DiscoveredDevicesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscoveredDevicesViewHolder holder, int position) {
        BluetoothDevice device = discoveredDevices.get(position);
        String name = nameDiscoveredDevices.get(position);

        holder.textViewNameDevice.setText(name);
        holder.textViewAddressDevice.setText(device.getAddress());

        holder.itemView.setOnClickListener(view -> {
            if (onClickListener != null) {
                onClickListener.onClick(holder.getAdapterPosition(), device, name);
            }
        });
    }

    @Override
    public int getItemCount() {
        return discoveredDevices.size();
    }

    /**
     * Method used to update/set the List of discovered devices used by the adapter.
     * @param discoveredDevices List of BluetoothDevice objects which has to use to set the adapter list.
     */
    public void setDiscoveredDevices(List<BluetoothDevice> discoveredDevices) {
        this.discoveredDevices = discoveredDevices;
    }

    /**
     * Method used to update/set the List of name of the discovered devices used by the adapter.
     * @param nameDiscoveredDevices List of String which has to use to set the adapter list.
     */
    public void setNameDiscoveredDevices(List<String> nameDiscoveredDevices) {
        this.nameDiscoveredDevices = nameDiscoveredDevices;
    }

    /**
     * Interface that declare method for the adapter's item.
     */
    public interface OnClickListener{
        /**
         * Method used to check which adapter's list has been clicked and then this method
         * tries to start a Bluetooth connection with the BluetoothDevice selected.
         * @param position Integer that represents the position into the adapter list of the clicked item.
         * @param device BluetoothDevice object of the adapter list.
         * @param nameDevice String that represents the name of the device to connect to of the adapter list.
         */
        void onClick(int position, BluetoothDevice device, String nameDevice);
    }

    /**
     * Method used to set the OnClickListener of the adapter.
     * @param onClickListener OnClickLister used to set the one of the adapter.
     */
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
