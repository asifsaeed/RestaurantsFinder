package resturantfinder.apps.com.resturantfinder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yelp.fusion.client.connection.YelpFusionApi;
import com.yelp.fusion.client.connection.YelpFusionApiFactory;
import com.yelp.fusion.client.models.Business;
import com.yelp.fusion.client.models.SearchResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnInfoWindowClickListener {


    private SupportMapFragment mapFragment;
    private SearchResponse searchResponse;
    static YelpFusionApi yelpFusionApi;
    private GPSTrack gpsTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //check internet connection
        if(!Utils.CheckInternetConnection(getApplicationContext())){
        showSettingsAlert();
        }else{
            //if internet active call the map and gps functions
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            gpsTrack = new GPSTrack(getApplicationContext(),this);
            if (gpsTrack.canGetLocation()) {
                 callMethod();
            }
        }
    }

    void callMethod(){
        new RetrieveYELPTask().execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //clear old marker
        googleMap.clear();
        //get business array and parse from response
        ArrayList businesses= searchResponse.getBusinesses();
        if(businesses.size()>0) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            //get business detail and add as makrer
            for (int i = 0; i < businesses.size(); i++) {
                Business business = (Business) businesses.get(i);
                LatLng position = new LatLng(business.getCoordinates().getLatitude(), business.getCoordinates().getLongitude());
                builder.include(position);
                //set custom marker for each business
                Bitmap markerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
                markerBitmap = scaleBitmap(markerBitmap);
                //set data for custom window
                InfoWindowData info = new InfoWindowData();
                info.setId(business.getId());
                info.setName(business.getName());
                info.setRating(business.getRating());
                info.setAddress(business.getLocation().getAddress1());
                //set custom Info window for each busienss
                CustomInfoWindow customInfoWindow = new CustomInfoWindow(MainActivity.this);
                googleMap.setInfoWindowAdapter(customInfoWindow);
                Marker m = googleMap.addMarker(new MarkerOptions()
                        .position(position)
                        .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap)));
                m.setTag(info);
                // m.showInfoWindow();


            }
            LatLngBounds bounds = builder.build();
            googleMap.setOnInfoWindowClickListener(MainActivity.this);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
        else{
            AlertDialog.Builder alertbox = new AlertDialog.Builder(MainActivity.this);
            // Set the message to display
            alertbox.setMessage(getString(R.string.noresponseerror));
            // Add a neutral button to the alert box and assign a click listener
            alertbox.setNeutralButton("Ok",
                    new DialogInterface.OnClickListener() {
                        // Click listener on the neutral button of alert box
                        public void onClick(DialogInterface arg0, int arg1) {
                            // The neutral button was clicked
                        }
                    });

            // show the alert box
            alertbox.show();
        }
    }
    @Override
    public void onInfoWindowClick(Marker marker) {
        //call detail activitu on info window click
        InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();
        Intent i=new Intent(MainActivity.this,DetailActivity.class);
        i.putExtra("bid", infoWindowData != null ? infoWindowData.getId() : null);
        i.putExtra("latitude", gpsTrack.getLatitude());
        i.putExtra("longitude", gpsTrack.getLongitude());
        startActivity(i);

    }
    //scale bitmap to set the custom marker icon
    private static Bitmap scaleBitmap(Bitmap bitmap) {
        int newWidth =70;
        int  newHeight =114;
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0;
        float pivotY = 0;
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        return scaledBitmap;
    }

    //Call Yelp APi to get businesses api data
    class RetrieveYELPTask extends AsyncTask<String, Void, Void> {
        final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        protected Void doInBackground(String... urls) {
            try {
                YelpFusionApiFactory apiFactory = new YelpFusionApiFactory();
                //set Client ID and Client Secret
                yelpFusionApi = apiFactory.createAPI(getString(R.string.YELP_APP_ID), getString(R.string.YELP_CLINET_SECRET));
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
        protected void onPreExecute() {
            dialog.setTitle(getString(R.string.loading));
            dialog.setMessage(getString(R.string.waitwhileloading));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();

        }
        protected void onPostExecute(Void feed) {
            //set params before calling the businesses api call
            Map<String, String> params = new HashMap<>();
            params.put("term", "restaurants");
            params.put("latitude", String.valueOf(gpsTrack.getLatitude()));
            params.put("longitude", String.valueOf(gpsTrack.getLongitude()));
           // params.put("latitude", "40.581140");
            //params.put("longitude", "-111.914184");
            try {
                Call<SearchResponse> call = yelpFusionApi.getBusinessSearch(params);
                // Response<SearchResponse> response = call.execute();
                Callback<SearchResponse> callback = new Callback<SearchResponse>() {
                    @Override
                    public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                        searchResponse = response.body();
                        // Update UI text with the searchResponse.
                        mapFragment.getMapAsync(MainActivity.this);
                        dialog.dismiss();
                    }
                    @Override
                    public void onFailure(Call<SearchResponse> call, Throwable t) {
                        // HTTP error happened, do something to handle it.
                        Log.d("Error", "INAPI");
                    }
                };
                call.enqueue(callback);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        // Setting Dialog Title
        alertDialog.setTitle("No Internet Connection");
        // Setting Dialog Message
        alertDialog.setMessage("Check your internet connection.");
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getApplicationContext().startActivity(intent);
            }
        });
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }

}