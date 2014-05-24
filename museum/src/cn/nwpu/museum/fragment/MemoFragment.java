package cn.nwpu.museum.fragment;

import java.util.Arrays;
import cn.nwpu.museum.activity.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MemoFragment extends Fragment {
	private ListView memoList;
	String[] memos = { "���嵽��һ��", "�����ʥ����ϼ��", "�����嵽��һ��", "�϶�һȥ������" };

	public MemoFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rv = inflater.inflate(R.layout.fragment_memo, container, false);
		memoList = (ListView) rv.findViewById(R.id.lvMemos);
		memoList.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, Arrays
				.asList(memos)));
		return rv;
	}
}
