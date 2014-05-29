package jp.co.spookies.android.blockbreaker;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.SensorManager;

/**
 * ブロック崩しゲームの管理を行う
 * 
 */
public class BreakerManager {
	private int width, height, bottom;
	private int blockWidth, blockHeight;
	private int boardWidth, boardHeight;
	public float[] ballPosition;
	private float[] ballBector;
	public List<BlockObject> blocks = new CopyOnWriteArrayList<BlockObject>();
	public float ballRadius = 10.0f;
	private final float ACCEL = 1.2f;
	public float[] board;
	private State state;
	private Bitmap boardImage;
	private Bitmap ballImage;
	private Bitmap[] blockImages;
	private Paint paint = new Paint();
	private int[][] blockLocation;

	public BreakerManager(int width, int height, Context context) {
		this.width = width;
		this.height = height;

		// ボード
		boardImage = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.bar);
		boardWidth = boardImage.getWidth();
		boardHeight = boardImage.getHeight();

		// ボール
		ballImage = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.ball);
		ballRadius = ballImage.getWidth() / 2;

		// ブロック
		blockImages = new Bitmap[7];
		blockWidth = blockHeight = 0;
		for (int i = 0; i < 7; i++) {
			blockImages[i] = BitmapFactory.decodeResource(
					context.getResources(),
					context.getResources().getIdentifier("block_" + i,
							"drawable", context.getPackageName()));
			blockWidth = Math.max(blockWidth, blockImages[i].getWidth());
			blockHeight = Math.max(blockHeight, blockImages[i].getHeight());
		}

		// 初期化
		init();
	}

	/**
	 * 初期化
	 */
	public void init() {
		state = State.PLAY;
		int maxCol = 0;

		// ブロックの初期配置を設定
		blockLocation = new int[3][4];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 4; j++) {
				blockLocation[i][j] = (int) (Math.random() * (blockImages.length + 1)) - 1;
			}
		}

		// 最大の列数
		for (int[] rows : blockLocation) {
			if (rows.length > maxCol) {
				maxCol = rows.length;
			}
		}

		ballPosition = new float[] { width / 2, height / 2 };
		ballBector = new float[] { 6.0f, -3.0f };
		board = new float[] { width / 2 - boardWidth / 2, height - boardHeight };

		// ブロック初期化
		blocks.clear();
		for (int row = 0; row < blockLocation.length; row++) {
			int COL = blockLocation[row].length;
			int LEFT = (width - blockWidth * COL) / 2;
			for (int col = 0; col < COL; col++) {
				if (blockLocation[row][col] < 0) {
					continue;
				}
				blocks.add(new BlockObject(blockLocation[row][col], new RectF(
						LEFT + col * blockWidth + 1, row * blockHeight + 1,
						LEFT + (col + 1) * blockWidth - 1, (row + 1)
								* blockHeight - 1)));
			}
		}
		// 底
		bottom = height - boardHeight;
	}

	/**
	 * 更新処理
	 */
	public void update() {

		// ボールの移動
		float x = ballPosition[0] + ballBector[0];
		float y = ballPosition[1] + ballBector[1];

		// 移動前と移動後のボールの位置を求める
		RectF nextR = new RectF(x - ballRadius, y - ballRadius, x + ballRadius,
				y + ballRadius);
		RectF preR = new RectF(ballPosition[0] - ballRadius, ballPosition[1]
				- ballRadius, ballPosition[0] + ballRadius, ballPosition[1]
				+ ballRadius);
		// ブロックとの衝突判定
		for (int i = 0; i < blocks.size(); i++) {
			RectF r = blocks.get(i).getRect();

			// 衝突検出
			if (RectF.intersects(r, nextR)) {
				// 左右の衝突判定
				if (preR.right <= r.left) {
					x = r.left - ballRadius;
					ballBector[0] = -ACCEL * ballBector[0];
					ballBector[1] = ACCEL * ballBector[1];
				} else if (preR.left >= r.right) {
					x = r.right + ballRadius;
					ballBector[0] = -ACCEL * ballBector[0];
					ballBector[1] = ACCEL * ballBector[1];
				}
				// 上下の衝突判定
				if (preR.bottom <= r.top) {
					y = r.top - ballRadius;
					ballBector[0] = ACCEL * ballBector[0];
					ballBector[1] = -ACCEL * ballBector[1];
				} else if (preR.top >= r.bottom) {
					y = r.bottom + ballRadius;
					ballBector[0] = ACCEL * ballBector[0];
					ballBector[1] = -ACCEL * ballBector[1];
				}
				// 衝突したのでブロックを消す
				blocks.remove(i);
				break;
			}
		}

		// 壁との衝突判定
		if (x - ballRadius < 0) {
			x = ballRadius;
			ballBector[0] = -ballBector[0];
		} else if (x + ballRadius > width) {
			x = width - ballRadius;
			ballBector[0] = -ballBector[0];
		}
		// 画面の上部との衝突判定
		if (y - ballRadius < 0) {
			y = ballRadius;
			ballBector[1] = -ballBector[1];
			// 画面の下部との衝突判定
		} else if (y + ballRadius > bottom) {
			if (state != State.PLAY && ballPosition[1] > height + ballRadius) {
				// 画面の下に落ちていったらゲーム終了
				state = State.FIN;
				return;
			}
			// ボードとの衝突判定
			if (board[0] < x && x < board[0] + boardWidth) {
				if (state != State.PLAY) {
					ballBector[0] = -ballBector[0];
				} else {
					y = bottom - ballRadius;
					ballBector[1] = -ballBector[1];
				}
			} else {
				state = State.OVER;
			}
		}
		// ボールの位置更新
		ballPosition[0] = x;
		ballPosition[1] = y;
	}

	/**
	 * ボール描画
	 * 
	 * @param canvas
	 *            draw先のcanvas
	 */
	public void drawBall(Canvas canvas) {
		canvas.drawBitmap(ballImage, ballPosition[0] - ballRadius,
				ballPosition[1] - ballRadius, paint);
	}

	/**
	 * ボード描画
	 * 
	 * @param canvas
	 *            draw先のcanvas
	 */
	public void drawBoard(Canvas canvas) {
		canvas.drawBitmap(boardImage, board[0], board[1], paint);
	}

	/**
	 * ブロック描画
	 * 
	 * @param canvas
	 *            draw先のcanvas
	 */
	public void drawBlocks(Canvas canvas) {
		for (BlockObject block : blocks) {
			canvas.drawBitmap(blockImages[block.getType()], block.getX(),
					block.getY(), paint);
		}
	}

	public boolean isFinished() {
		return state == State.FIN;
	}

	/**
	 * 画面の角度に合わせてボードを移動
	 * 
	 * @param azimuth
	 *            画面の傾き(azimuth)
	 */
	public void onChangedOrientation(float azimuth) {
		if (state != State.PLAY) {
			return;
		}
		float move = ((width - boardWidth) * azimuth * -2.0f
				/ SensorManager.GRAVITY_EARTH) + (width - boardWidth) / 2;
		move = board[0] * 0.9f + move * 0.1f;

		// 左右はみでないように
		if (move < 0) {
			move = 0;
		} else if (move > width - boardWidth) {
			move = width - boardWidth;
		}
		// ボールがあれば動かない
		if (ballPosition[1] > bottom - ballRadius
				&& move < ballPosition[0] + ballRadius
				&& ballPosition[0] - ballRadius < move + boardWidth) {
			move = board[0];
		}
		board[0] = move;
	}

	enum State {
		PLAY, OVER, FIN
	}
}
