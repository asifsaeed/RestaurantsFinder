package resturantfinder.apps.com.resturantfinder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {

    private final Context context;
    public CustomInfoWindow(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        @SuppressLint("InflateParams") View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.map_custom_infowindow, null);
        //Settings custom window attributes
        TextView name_tv = (TextView)view.findViewById(R.id.name);
        TextView address_tv = (TextView)view.findViewById(R.id.address);
        RatingBar rating =(RatingBar) view.findViewById(R.id.rating);
        InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();
        rating.setRating(infoWindowData != null ? infoWindowData.getRating().floatValue() : 0);
        name_tv.setText(infoWindowData != null ? infoWindowData.getName() : null);
        address_tv.setText(infoWindowData != null ? infoWindowData.getAddress() : null);
        return view;
    }
}