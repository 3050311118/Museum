package cn.nwpu.museum.adapter;

import java.util.List;

import cn.nwpu.museum.activity.R;
import cn.nwpu.museum.bean.Exhibit;
import cn.nwpu.museum.service.ExhibitService;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ExhibitAdapter extends BaseAdapter {

	
	private List<Exhibit> data;
	private int listviewitem;
	private LayoutInflater layoutInflator;
    private Context context;
	
    public ExhibitAdapter(Context context,List<Exhibit> data, int listviewitem){	
		this.data = data;
		this.context = context;
		this.listviewitem = listviewitem;
		this.layoutInflator = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
    
    public void setData(List<Exhibit> data){
    	this.data = data;
    }
    @Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int index) {
		return data.get(index);
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup group) {
		ImageView imageView;
		TextView textView;
		TextView textDescription;
		if(convertView == null){
			
			convertView = this.layoutInflator.inflate(listviewitem, null);
			imageView = (ImageView)convertView.findViewById(R.id.exhibit_image);
			textView =(TextView)convertView.findViewById(R.id.exhibit_name);	
			textDescription=(TextView)convertView.findViewById(R.id.exhibit_description);	
			convertView.setTag(new DataWrapper(imageView, textView,textDescription));
		}else{
		
			DataWrapper dataWrapper = (DataWrapper)convertView.getTag();
			imageView = dataWrapper.imageView;
			textView = dataWrapper.textView;
			textDescription = dataWrapper.textDescription;
		}
		
		Exhibit exhibit = data.get(position);
		textView.setText(exhibit.getName());
		textDescription.setText(exhibit.getDescription().substring(0, 20)+"...");
		//得到联系人头像Bitamp  
		if(exhibit.getBitmap() == null){
		   ExhibitService es = new ExhibitService(context);
		   Bitmap temp = es.getImageFromAssetsFile(String.valueOf(exhibit.getExhibitnumber())+".png"); 
		   exhibit.setBitmap(temp);
		}
		//imageView.setImageResource(R.drawable.testpic);
	    imageView.setImageBitmap(exhibit.getBitmap() );
		
		return convertView;
	}
	
	private final class DataWrapper{
		public ImageView imageView ;
		public TextView textView;
		public TextView textDescription;
		public DataWrapper(ImageView imageView,TextView textView,TextView textDescription){
			
			this.imageView = imageView;
			this.textView = textView;
			this.textDescription = textDescription;
		}
	}

}
