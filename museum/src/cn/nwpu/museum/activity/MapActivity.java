package cn.nwpu.museum.activity;

import cn.nwpu.museum.bean.Hall;
import cn.nwpu.museum.service.HallService;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;

public class MapActivity extends Activity {

	private ImageView imageview;
    private Bitmap mBitmap;
    private Matrix matrix = new Matrix();   
    private Matrix savedMatrix = new Matrix();
    private Button mButton;
    private HallService hallService;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		imageview = (ImageView)this.findViewById(R.id.imagemap);
		imageview.setOnTouchListener(new MulitPointTouchListener());
		
		mBitmap =BitmapFactory.decodeResource(this.getResources(), R.drawable.map);
		
		Bitmap tempBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.RGB_565);
		Canvas tempCanvas = new Canvas(tempBitmap);
		
		tempCanvas.drawBitmap(mBitmap, 0, 0, null);
		
		Bitmap location_normal = BitmapFactory.decodeResource(this.getResources(), R.drawable.map_location_normal);
		//标记所有展馆位置
		hallService = new HallService(this);
		for(Hall hall : hallService.getAll()){
			tempCanvas.drawBitmap(location_normal, mBitmap.getWidth() * hall.getPositionx()/100, 
					mBitmap.getHeight() * hall.getPositiony()/100, null);
		}		
		imageview.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
		
	    initbtn();
	}
    private void initbtn(){
    	
    	mButton = (Button) this.findViewById(R.id.btnmap);
    	mButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Bitmap tempBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.RGB_565);
				Canvas tempCanvas = new Canvas(tempBitmap);
				
				tempCanvas.drawBitmap(mBitmap, 0, 0, null);
				
				Bitmap location_normal = BitmapFactory.decodeResource(MapActivity.this.getResources(), R.drawable.map_location_current);
				tempCanvas.drawBitmap(location_normal, mBitmap.getWidth() /2, mBitmap.getHeight() / 2, null);
				
				imageview.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
				imageview.setImageMatrix(matrix); 
	            imageview.invalidate(); 
			}
		});
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_map, menu);
		return true;
	}
	
	public class MulitPointTouchListener implements OnTouchListener {   
          
        static final int NONE = 0;   
        static final int DRAG = 1;   
        static final int ZOOM = 2;   
        int mode = NONE;   
        PointF start = new PointF();   
        PointF mid = new PointF();   
        float oldDist = 1f;   
        @Override   
        public boolean onTouch(View v, MotionEvent event) {   
                ImageView view = (ImageView) v;   
                
                switch (event.getAction() & MotionEvent.ACTION_MASK) {   
                case MotionEvent.ACTION_DOWN:   
                        matrix.set(view.getImageMatrix());   
                        savedMatrix.set(matrix);   
                        start.set(event.getX(), event.getY());   
                        mode = DRAG;   
                         
                        break;   
                case MotionEvent.ACTION_POINTER_DOWN:   
                        oldDist = spacing(event);   
                        if (oldDist > 10f) {   
                                savedMatrix.set(matrix);   
                                midPoint(mid, event);   
                                mode = ZOOM;   
                        }   
                        break;   
                case MotionEvent.ACTION_UP:   
                case MotionEvent.ACTION_POINTER_UP:   
                        mode = NONE;   
                        break;   
                case MotionEvent.ACTION_MOVE:  
                        if (mode == DRAG) {   
                        	
                                matrix.set(savedMatrix);   
                                matrix.postTranslate(event.getX() - start.x, event.getY()   
                                                - start.y);   
                        } else if (mode == ZOOM) {   
                                float newDist = spacing(event);   
                                if (newDist > 10f) {   
                                        matrix.set(savedMatrix);   
                                        float scale = newDist / oldDist;   
                                        matrix.postScale(scale, scale, mid.x, mid.y);   
                                }   
                        }   
                        break;   
                }   
                imageview.setImageMatrix(matrix); 
                imageview.invalidate(); 
                Log.i("map", "redraw");
                return true;   
        }   
          
         
        private float spacing(MotionEvent event) {   
                float x = event.getX(0) - event.getX(1);   
                float y = event.getY(0) - event.getY(1);   
                return FloatMath.sqrt(x * x + y * y);   
        }   
         
        private void midPoint(PointF point, MotionEvent event) {   
                float x = event.getX(0) + event.getX(1);   
                float y = event.getY(0) + event.getY(1);   
                point.set(x / 2, y / 2);   
        }   
   }   

}
