package cn.nwpu.museum.fragment;

import cn.nwpu.museum.activity.DescriptionActivity;
import cn.nwpu.museum.activity.R;
import cn.nwpu.museum.bean.Exhibit;
import cn.nwpu.museum.service.ExhibitService;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class DesciptionFragment extends Fragment {
	 public static final String ARG_OBJECT = "object";

	    private DescriptionActivity.exhibit exhibit;
	    public DesciptionFragment(DescriptionActivity.exhibit exhibit){
	    	this.exhibit = exhibit;
	    }
	    
	    public DesciptionFragment(){}
	    
	    private ImageView imageview;
	    private TextView  tx_name;
	    private TextView  tx_description;
	    @Override
	    public View onCreateView(LayoutInflater inflater,
	            ViewGroup container, Bundle savedInstanceState) {
	       
	        View rootView = inflater.inflate(
	                R.layout.fragment_description, container, false);
	        Bundle args = getArguments();
	        imageview = (ImageView) rootView.findViewById(R.id.image_description);
	        tx_name  = (TextView) rootView.findViewById(R.id.tx_title);
	        tx_description =(TextView) rootView.findViewById(R.id.tx_description);
	        
	        ExhibitService es = new ExhibitService(this.getActivity());
	        Log.i("DesciptionFragment", "exhibitBean:" +exhibit.exhibitnumber );
	        Exhibit exhibitBean = es.findByNumber(Integer.parseInt(exhibit.exhibitnumber));
	        Bitmap exhibitImage = es.getImageFromAssetsFile(exhibit.exhibitnumber+".png" );
	        tx_name.setText(exhibitBean.getName());
	        tx_description.setText(exhibitBean.getDescription());
	        imageview.setImageBitmap(exhibitImage);
	        return rootView;
	    }
}
