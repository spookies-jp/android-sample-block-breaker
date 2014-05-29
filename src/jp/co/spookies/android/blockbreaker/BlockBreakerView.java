package jp.co.spookies.android.blockbreaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class BlockBreakerView extends SurfaceView implements
		SurfaceHolder.Callback, Runnable, SensorEventListener {
	private int width;
	private int height;
	private Thread thread;
	private static final long INTERVAL = 20;
	private boolean runFlag = true;
	private Canvas canvas;
	private Paint paint;
	private BreakerManager manager;
	private Bitmap bgImage;

	public BlockBreakerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getHolder().addCallback(this);

		// 背景イメージ
		bgImage = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.bg_block);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		paint = new Paint();
		paint.setStrokeWidth(2.0f);
		paint.setAntiAlias(true);
		width = getWidth();
		height = getHeight();

		// ゲームの管理を任せる
		manager = new BreakerManager(width, height, getContext());

		if (thread == null) {
			runFlag = true;
			thread = new Thread(this);
			thread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		runFlag = false;
		thread = null;
	}

	@Override
	public void run() {
		while (runFlag) {
			manager.update();
			doDraw();
			try {
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// 描画
	public void doDraw() {
		canvas = getHolder().lockCanvas();
		canvas.save();
		canvas.drawColor(Color.BLACK);
		// 背景イメージで一度塗りつぶす
		canvas.drawBitmap(bgImage, 0, 0, paint);

		if (manager.isFinished()) {
			// ゲームが終了ならGAME OVERを描画
			drawOver(canvas);
		}
		// ブロック描画
		manager.drawBlocks(canvas);
		// ボール描画
		manager.drawBall(canvas);
		// ボードは最後に描画
		manager.drawBoard(canvas);

		canvas.restore();
		getHolder().unlockCanvasAndPost(canvas);
	}

	// GAMEOVERを表示
	private void drawOver(Canvas canvas) {
		paint.setTextSize(50.0f);
		paint.setColor(Color.CYAN);
		paint.setTextAlign(Align.CENTER);
		canvas.drawText("GAME OVER", width / 2, height / 2, paint);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (manager == null
				|| event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
			return;
		}
		// 画面の横方向の傾き(加速度)を渡す
		manager.onChangedOrientation(event.values[0]);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		if (manager.isFinished()) {
			// ゲームが終了していたらリスタート
			manager.init();
			return true;
		}
		return super.onTouchEvent(e);
	}
}
