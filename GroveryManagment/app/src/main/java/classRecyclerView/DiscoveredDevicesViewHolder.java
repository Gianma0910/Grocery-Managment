package classRecyclerView;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groverymanagment.R;

/**
 * ViewHolder subclass that describes an item view of the ConnectToDeviceActivity's RecyclerView.
 * This ViewHolder must be instanced by CustomAdapterDiscoveryDevices class.
 */
public class DiscoveredDevicesViewHolder extends RecyclerView.ViewHolder {

    /**
     * TextView of the item view that contains the name of discovered device.
     */
    TextView textViewNameDevice;
    /**
     * TextView of the item view that contains the address of discovered device.
     */
    TextView textViewAddressDevice;

    /**
     * DiscoveredDevicesViewHolder constructor.
     * @param itemView View object that represents the single item view of the recycler view.
     */
    public DiscoveredDevicesViewHolder(@NonNull View itemView) {
        super(itemView);

        textViewNameDevice = itemView.findViewById(R.id.text_view_name_device);
        textViewAddressDevice = itemView.findViewById(R.id.text_view_address_device);
    }
}
