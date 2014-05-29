package jp.co.spookies.android.blockbreaker;

import android.graphics.RectF;

public class BlockObject {
	private int type;
	private float x, y;
	private RectF rect;

	public BlockObject(int type, RectF rect) {
		this.type = type;
		this.rect = rect;
		x = rect.left;
		y = rect.top;
	}

	/**
	 * x座標取得
	 * 
	 * @return
	 */
	public float getX() {
		return x;
	}

	/**
	 * y座標取得
	 * 
	 * @return
	 */
	public float getY() {
		return y;
	}

	/**
	 * ブロックのタイプを取得
	 * 
	 * @return
	 */
	public int getType() {
		return type;
	}

	/**
	 * ブロックの範囲を取得
	 * 
	 * @return
	 */
	public RectF getRect() {
		return rect;
	}
}