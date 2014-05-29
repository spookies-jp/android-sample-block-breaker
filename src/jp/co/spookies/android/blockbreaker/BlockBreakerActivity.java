package jp.co.spookies.android.blockbreaker;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.WindowManager;

public class BlockBreakerActivity extends Activity {
	private SensorManager sensorManager;
	BlockBreakerView view;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.block_breaker);

		// センサー
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		view = (BlockBreakerView) findViewById(R.id.blockbreaker_view);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// 加速度センサー登録
		Sensor sensor;
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (sensor != null) {
			sensorManager.registerListener(view, sensor,
					SensorManager.SENSOR_DELAY_UI);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		sensorManager.unregisterListener(view);
	}
}