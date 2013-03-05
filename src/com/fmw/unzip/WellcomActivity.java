package com.fmw.unzip;

import com.box.unzip.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;

public class WellcomActivity extends Activity {
	private static final long SPLASH_DISPLAY_LENGHT = 700;
	private Context context = this;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		setContentView(R.layout.wellcom);
		
		ImageView image=(ImageView)findViewById(R.id.wellcom_img);  
	    AnimationSet  animationset=new AnimationSet(true);  
	    AlphaAnimation alphaAnimation=new AlphaAnimation(0, 1);  
	    alphaAnimation.setDuration(500);  
	    animationset.addAnimation(alphaAnimation);  
	    image.startAnimation(animationset);  
	    new Handler().postDelayed(new Runnable(){   
	            
	        @Override   
	        public void run() {   
	            Intent mainIntent = new Intent(context,MainActivity.class);   
	            context.startActivity(mainIntent);   
	            ((Activity) context).finish();   
	        }   
	             
	       }, SPLASH_DISPLAY_LENGHT);   

		
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
}
