package resturantfinder.apps.com.resturantfinder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.yelp.fusion.client.models.Business;
import com.yelp.fusion.client.models.Category;
import java.net.URLEncoder;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("unused")
public class DetailActivity extends Activity {
	private Business businessResponse;
	private Bitmap bitmap;
	ProgressDialog dialog;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		//call yelp business api to get the business detail
		try {
			String id = getIntent().getStringExtra("bid");
			Call<Business> call = MainActivity.yelpFusionApi.getBusiness(id);
			Callback<Business> callback = new Callback<Business>() {
				@Override
				public void onResponse(Call<Business> call, Response<Business> response) {
					Log.i("response.body()", response.body().toString());
					businessResponse = response.body();
					setDataValues();
				}

				@Override
				public void onFailure(Call<Business> call, Throwable t) {
					// HTTP error happened, do something to handle it.
					Log.i("Error", "INAPI");
				}
			};
			call.enqueue(callback);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("SetTextI18n")
	private void setDataValues() {
		//call yelp business api to get the business detail
			final RelativeLayout lyPortraite = (RelativeLayout) findViewById(R.id.layout_portrate);
			final RelativeLayout lyLandscape = (RelativeLayout) findViewById(R.id.layout_landscape);
			final ImageView imgPortraite = (ImageView) findViewById(R.id.imgLogoPortrate);
			final ImageView imgLandscape = (ImageView) findViewById(R.id.imgLogoLandscape);
			final TextView lblNameP = (TextView) findViewById(R.id.namep);
			final TextView lblNameL = (TextView) findViewById(R.id.namel);
			final TextView lblCategoryP = (TextView) findViewById(R.id.categoryp);
			final TextView lblCategoryL = (TextView) findViewById(R.id.categoryl);
			final RatingBar ratingp = (RatingBar) findViewById(R.id.ratingp);
			final RatingBar ratingl = (RatingBar) findViewById(R.id.ratingl);
			final TextView reviewl = (TextView) findViewById(R.id.reviewl);
			final TextView reviewp = (TextView) findViewById(R.id.reviewp);
			final TextView phone = (TextView) findViewById(R.id.phone);
			final TextView location = (TextView) findViewById(R.id.location);
			final RelativeLayout contents = (RelativeLayout) findViewById(R.id.contents);
			final LinearLayout contentsdetail = (LinearLayout) findViewById(R.id.contentsdetail);
			lblNameL.setText(businessResponse.getName());
			lblNameP.setText(businessResponse.getName());
			float f = (float) businessResponse.getRating();
			ratingp.setRating(f);
			ratingl.setRating(f);
			reviewp.setText(getString(R.string.totalreviews) + businessResponse.getReviewCount());
			reviewl.setText(getString(R.string.totalreviews) + businessResponse.getReviewCount());
			phone.setText(businessResponse.getPhone());
			location.setText(businessResponse.getLocation().getAddress1());
			Target target;
			ArrayList<Category> categories = businessResponse.getCategories();
			String cats = "";
			lyPortraite.setVisibility(View.GONE);
			for (Category cat : categories) {
				cats += cat.getTitle() + ", ";
			}
			lblCategoryL.setText(getString(R.string.categories) + cats.substring(0, cats.length() - 2));
			lblCategoryP.setText(getString(R.string.categories) + cats.substring(0, cats.length() - 2));
			try {
				Log.i("ImageURL", businessResponse.getImageUrl());
				target = new Target() {
					@Override
					public void onPrepareLoad(Drawable drawable) {
					}
					@Override
					public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom arg1) {
						if (bitmap != null) {
							if (bitmap.getHeight() > bitmap.getWidth()) {
								Log.i("Here", "Here");
								lyPortraite.setVisibility(View.VISIBLE);
								lyLandscape.setVisibility(View.GONE);
								imgPortraite.setImageBitmap(bitmap);
							} else {
								Log.i("Not Here", "Not Here");
								lyLandscape.setVisibility(View.VISIBLE);
								lyPortraite.setVisibility(View.GONE);
								imgLandscape.setImageBitmap(bitmap);
							}
						} else {
							Log.i("Bitmap null", "Isnull");
						}
					}

					@Override
					public void onBitmapFailed(Drawable drawable) {
					}
				};
				imgPortraite.setTag(target);
				imgLandscape.setTag(target);
				Picasso.with(this).load(businessResponse.getImageUrl()).into((Target) imgPortraite.getTag());
				Picasso.with(this).load(businessResponse.getImageUrl()).into((Target) imgLandscape.getTag());
				contents.setVisibility(View.VISIBLE);
				contentsdetail.setVisibility(View.VISIBLE);
//call phone number of business
				findViewById(R.id.btnPhone).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						Permissions.check(DetailActivity.this, Manifest.permission.CALL_PHONE, null,
								new PermissionHandler() {
									@Override
									public void onGranted() {
										String ph;
										ph = phone.getText().toString();
										Intent callIntent = new Intent(Intent.ACTION_CALL);
										callIntent.setData(Uri.parse("tel:" + ph));
										if (ActivityCompat.checkSelfPermission(DetailActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
											// TODO: Consider calling
											//    ActivityCompat#requestPermissions
											// here to request the missing permissions, and then overriding
											//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
											//                                          int[] grantResults)
											// to handle the case where the user grants the permission. See the documentation
											// for ActivityCompat#requestPermissions for more details.
											return;
										}
										startActivity(callIntent);
									}
								});
					}
				});
				//show direction in between user location and business
				findViewById(R.id.btnLocation).setOnClickListener(new View.OnClickListener() {
					public GPSTrack gps;
					@Override
					public void onClick(View v) {
						try {
							Intent callIntent = new Intent(Intent.ACTION_VIEW);
							callIntent.setData(Uri.parse("http://maps.google.com/maps?saddr=" + getIntent().getDoubleExtra("latitude",0.0) + "," + getIntent().getDoubleExtra("longitude",0.0) + "&daddr=" + URLEncoder.encode(location.getText().toString(), "UTF-8")));
							startActivity(callIntent);
						}
						catch (Exception e){
							e.printStackTrace();
						}
					}
				});

		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
