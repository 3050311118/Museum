package cn.nwpu.museum.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class SSIDListActivity extends Activity implements OnItemClickListener {

	private ListView mListView;
	private ArrayAdapter mAdapter;
	private List<String> mSSIDList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ssidlist);
        mListView = (ListView) this.findViewById(R.id.list_ssid);
        WifiManager wifi_service = (WifiManager)getSystemService(WIFI_SERVICE); 
	 	List<ScanResult> results = wifi_service.getScanResults();
	 	if(results == null) return;
	 	mSSIDList = new ArrayList<String>();
	 	for(ScanResult result : results){
	 		if(mSSIDList.indexOf(result) == -1)
	 		  mSSIDList.add(result.SSID);
	 	}
        mAdapter = new  ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mSSIDList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_description, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent data=new Intent();  
        data.putExtra("ssid", mSSIDList.get(position));   
        setResult(20, data);
		this.finish();
	}
}
