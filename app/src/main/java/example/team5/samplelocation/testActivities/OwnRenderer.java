package example.team5.samplelocation.testActivities;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by Philip on 12/5/2016.
 */

class OwnRendered extends DefaultClusterRenderer<SimpleCoordinate> {

    public OwnRendered(Context context, GoogleMap map,
                       ClusterManager<SimpleCoordinate> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(SimpleCoordinate item, MarkerOptions markerOptions) {
        markerOptions.title(item.getTitle());
        super.onBeforeClusterItemRendered(item, markerOptions);
    }
}
