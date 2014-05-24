package com.fz.nwpupharos;

import cn.nwpu.museum.activity.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MapView extends ImageView {
	private Paint mPaint;
	private Bitmap bBitMap, pointMe, pointLeader;
	// 5 fixed position
	Node[] nodes = { new Node(100, 50), new Node(600, 50), new Node(100, 600), new Node(600, 600), new Node(350, 350) };
	Node[] leaderPosition = { new Node(-20, 50), new Node(480, 50), new Node(-20, 600), new Node(480, 600),
			new Node(230, 350) };

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		if (!isInEditMode()) {
			bBitMap = BitmapFactory.decodeResource(getResources(), R.drawable.museum);
			pointMe = BitmapFactory.decodeResource(getResources(), R.drawable.point_me);
			pointLeader = BitmapFactory.decodeResource(getResources(), R.drawable.point_leader);
			mPaint = new Paint();
			mPaint.setColor(Color.BLACK);
		}
	}

	public void drawMyself(int index) {
		drawMeAndLeader(index, -1);
	}

	public void drawLeader(int index) {
		drawMeAndLeader(-1, index);
	}

	public void drawMeAndLeader(int me, int leader) {
		Bitmap mBitmap = Bitmap.createBitmap(bBitMap.getWidth(), bBitMap.getHeight(), Config.RGB_565);
		Canvas canvas = new Canvas(mBitmap);
		canvas.drawBitmap(bBitMap, 0, 0, null);
		Matrix matrix = new Matrix();
		if (me >= 0 && me < 5) {
			matrix.postScale(0.5f, 0.5f);
			matrix.postTranslate(nodes[me].x, nodes[me].y);
			canvas.drawBitmap(pointMe, matrix, null);
		}
		if (leader >= 0 && leader < 5) {
			matrix.reset();
			matrix.postScale(0.5f, 0.5f);
			matrix.postTranslate(leaderPosition[leader].x, leaderPosition[leader].y);
			canvas.drawBitmap(pointLeader, matrix, null);
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
