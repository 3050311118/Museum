package cn.nwpu.museum.fragment;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import cn.nwpu.museum.activity.R;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView.FindListener;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class MemoFragment extends Fragment {
	private ListView memoList;
	private EditText etMemo;
	private Button btnAdd;
	private ArrayAdapter<String> adapter;
	String[] memos = { "王五到此一游", "齐天大圣爱紫霞！", "刘若峰到此一游", "嫦娥一去不复返" };
	List<String> memosList = new LinkedList<String>(Arrays.asList(memos));

	public MemoFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rv = inflater.inflate(R.layout.fragment_memo, container, false);
		memoList = (ListView) rv.findViewById(R.id.lvMemos);
		etMemo = (EditText) rv.findViewById(R.id.etNewMemo);
		adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, memosList);
		memoList.setAdapter(adapter);
		btnAdd = (Button) rv.findViewById(R.id.btnAddMemo);
		btnAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String memo = etMemo.getText().toString().trim();
				memosList.add(memo);
				adapter.notifyDataSetChanged();
				memoList.invalidate();
				etMemo.setText("");
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		});
		return rv;
	}

	private boolean checkInput(String input) {
		return true;
	}
}
