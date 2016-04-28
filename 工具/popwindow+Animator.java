package com.example.weiboepop;

import com.zdp.aseo.content.AseoZdpAseo;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class MoreWindow extends PopupWindow implements OnClickListener{

	private String TAG = MoreWindow.class.getSimpleName();
	Activity mContext;
	private int mWidth;
	private int mHeight;
	private int statusBarHeight ;
	private Bitmap mBitmap= null;
	private Bitmap overlay = null;
	
	private Handler mHandler = new Handler();

	public MoreWindow(Activity context) {
		mContext = context;
	}

	public void init() {
		Rect frame = new Rect();
		mContext.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);///取得整个视图部
		statusBarHeight = frame.top;
		DisplayMetrics metrics = new DisplayMetrics();//屏幕分辨率
		mContext.getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		mWidth = metrics.widthPixels;
		mHeight = metrics.heightPixels;
		
		setWidth(mWidth);
		setHeight(mHeight);
	}
	
	private Bitmap blur() {
		if (null != overlay) {
			return overlay;
		}
		long startMs = System.currentTimeMillis();

		View view = mContext.getWindow().getDecorView();  //decorView是window中的最顶层view
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache(true);
		mBitmap = view.getDrawingCache();//把View转换为Bitmap
		
		float scaleFactor = 8;//图片锟斤拷锟脚憋拷锟斤拷
		float radius = 10;//模锟斤拷潭锟�
		int width = mBitmap.getWidth();
		int height =  mBitmap.getHeight();

		//背景处理毛玻璃化
		overlay = Bitmap.createBitmap((int) (width / scaleFactor),(int) (height / scaleFactor),Bitmap.Config.ARGB_8888);//透明度。图片质量的参数
		Canvas canvas = new Canvas(overlay);//以bitmap对象创建一个画布，则将内容都绘制在bitmap上
		canvas.scale(1 / scaleFactor, 1 / scaleFactor);  //图片精度,缓冲一
		Paint paint = new Paint();
		paint.setFlags(Paint.FILTER_BITMAP_FLAG);//缓冲二
		canvas.drawBitmap(mBitmap, 0, 0, paint);
		overlay = FastBlur.doBlur(overlay, (int) radius, true);
		
		Log.i(TAG, "blur time is:"+(System.currentTimeMillis() - startMs));
		return overlay;
	}
	

	public void showMoreWindow(View anchor,int bottomMargin) {
		final RelativeLayout layout = (RelativeLayout)LayoutInflater.from(mContext).inflate(R.layout.center_music_more_window, null);
		setContentView(layout);
		
		ImageView close= (ImageView)layout.findViewById(R.id.center_music_window_close);
//		LayoutParams params =new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
//		params.bottomMargin = bottomMargin;
//		params.addRule(RelativeLayout.BELOW, R.id.more_window_auto);
//		params.addRule(RelativeLayout.RIGHT_OF, R.id.more_window_collect);
//		params.topMargin = 200;
//		params.leftMargin = 18;
//		close.setLayoutParams(params);
		
		close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isShowing()) {
					closeAnimation(layout);
				}
			}

		});
		
		showAnimation(layout);
		setBackgroundDrawable(new BitmapDrawable(mContext.getResources(),blur()));
		setOutsideTouchable(true);
		setFocusable(true);
		showAtLocation(anchor, Gravity.BOTTOM, 0, statusBarHeight);//popwindow显示
	}

	private void showAnimation(ViewGroup layout){
		for(int i=0;i<layout.getChildCount();i++){
			final View child = layout.getChildAt(i);
			if(child.getId() == R.id.center_music_window_close){
				continue;
			}
			child.setOnClickListener(this);
			child.setVisibility(View.INVISIBLE);
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					child.setVisibility(View.VISIBLE);
					ValueAnimator fadeAnim = ObjectAnimator.ofFloat(child, "translationY", 600, 0);
					//操纵的属性参数：x/y；scaleX/scaleY；rotationX/ rotationY；transitionX/ transitionY等等。
					fadeAnim.setDuration(300);     //持续时间是300ms.
					KickBackAnimator kickAnimator = new KickBackAnimator(); //自定义动画
					kickAnimator.setDuration(150);
					fadeAnim.setEvaluator(kickAnimator);
					fadeAnim.start();
				}
			}, i * 50);
		}
		
	}

	private void closeAnimation(ViewGroup layout){
		for(int i=0;i<layout.getChildCount();i++){
			final View child = layout.getChildAt(i);
			if(child.getId() == R.id.center_music_window_close){
				continue;
			}
			child.setOnClickListener(this);
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					child.setVisibility(View.VISIBLE);
					ValueAnimator fadeAnim = ObjectAnimator.ofFloat(child, "translationY", 0, 600);
					fadeAnim.setDuration(200);
					KickBackAnimator kickAnimator = new KickBackAnimator();
					kickAnimator.setDuration(100);
					fadeAnim.setEvaluator(kickAnimator);
					fadeAnim.start();
					fadeAnim.addListener(new AnimatorListener() {
						
						@Override
						public void onAnimationStart(Animator animation) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onAnimationRepeat(Animator animation) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onAnimationEnd(Animator animation) {
							child.setVisibility(View.INVISIBLE);
						}
						
						@Override
						public void onAnimationCancel(Animator animation) {
							// TODO Auto-generated method stub
							
						}
					});
				}
			}, (layout.getChildCount()-i-1) * 30);
			
			if(child.getId() == R.id.more_window_local){
				mHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						dismiss();//关闭popwindow
					}
				}, (layout.getChildCount()-i) * 30 + 80);
			}
		}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.more_window_local:
			break;
		case R.id.more_window_online:
			break;
		case R.id.more_window_delete:
			break;
		case R.id.more_window_collect:
			break;
		case R.id.more_window_auto:
			break;
		case R.id.more_window_external:
			break;

		default:
			break;
		}
	}
	
	public void destroy() {
		if (null != overlay) {
			overlay.recycle();
			overlay = null;
			System.gc();
		}
		if (null != mBitmap) {
			mBitmap.recycle();
			mBitmap = null;
			System.gc();
		}
	}
	
}
