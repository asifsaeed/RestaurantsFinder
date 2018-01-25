package resturantfinder.apps.com.resturantfinder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;


@SuppressWarnings("unused")
public class SplashActivity extends Activity {

	private final boolean _active = true;
	private final int _splashTime = 2000;
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		//start splash screen timer
		startSplashing();
	}
	private void startSplashing()

	{
		Thread splashTread = new Thread()
		{
			@Override
			public void run()
			{
				try {
					int waited = 0;
					while(waited < _splashTime)
					{
						sleep(100);
						waited += 100;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				}
				finally
				{
					Intent i=new Intent(SplashActivity.this,MainActivity.class);
					startActivity(i);
					finish();
				}
			}
		};
		splashTread.start();
	}
}
