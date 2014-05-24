package com.fz.nwpupharos;

import cn.nwpu.museum.activity.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MapView extends ImageView {
	private Paint mPaint;
	private Bitmap bBitMap;
	private static final int r = 50;

	// 5 fixed position
	Node[] nodes = { new Node(150, 150), new Node(650, 150), new Node(150, 700), new Node(650, 700), new Node(400, 450) };

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		if (!isInEditMode()) {
			bBitMap = BitmapFactory.decodeResource(getResources(), R.drawable.museum);
			mPaint = new Paint();
			mPaint.setColor(Color.BLACK);
//			mPaint.setAlpha(10);
		}
	}

	public void drawMyself(int index) {
		drawPosition(Color.BLUE, index);
	}

	public void drawLeader(int index) {
		drawPosition(Color.RED, index);
	}

	public void drawPosition(int color, int... index) {
		Bitmap mBitmap = Bitmap.createBitmap(bBitMap.getWidth(), bBitMap.getHeight(), Config.RGB_565);
		Canvas canvas = new Canvas(mBitmap);
		canvas.drawBitmap(bBitMap, 0, 0, null);
		mPaint.setColor(color);
		for (int i : index) {
			canvas.drawCircle(nodes[i].x, nodes[i].y, r, mPaint);
		}
		setImageDrawable(new BitmapDrawable(getResources(), mBitmap));
		invalidate();
	}

	class Node {
		int x, y;

		public Node(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}
