package teamcherrypicker.com.ui.main.map

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class StoreClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<StoreClusterItem>
) : DefaultClusterRenderer<StoreClusterItem>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(item: StoreClusterItem, markerOptions: MarkerOptions) {
        val hue = when (item.store.normalizedCategory) {
            "CAFE" -> BitmapDescriptorFactory.HUE_YELLOW
            "DINING" -> BitmapDescriptorFactory.HUE_ORANGE
            "SHOPPING" -> BitmapDescriptorFactory.HUE_VIOLET
            else -> BitmapDescriptorFactory.HUE_AZURE
        }
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(hue))
        markerOptions.title(item.title)
        markerOptions.snippet(item.snippet)
        super.onBeforeClusterItemRendered(item, markerOptions)
    }
}
