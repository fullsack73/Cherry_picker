package teamcherrypicker.com.ui.main.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import teamcherrypicker.com.data.Store

data class StoreClusterItem(
    val store: Store
) : ClusterItem {

    private val position = LatLng(store.latitude, store.longitude)
    private val title = store.name
    private val snippet = store.normalizedCategory

    override fun getPosition(): LatLng = position

    override fun getTitle(): String = title

    override fun getSnippet(): String = snippet

    override fun getZIndex(): Float? = null
}
