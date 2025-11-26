package teamcherrypicker.com.ui.main.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import teamcherrypicker.com.data.Store

class StoreClusterItem(
    val store: Store
) : ClusterItem {

    override fun getPosition(): LatLng = LatLng(store.latitude, store.longitude)

    override fun getTitle(): String = store.name

    override fun getSnippet(): String = store.normalizedCategory

    override fun getZIndex(): Float? = null
}
