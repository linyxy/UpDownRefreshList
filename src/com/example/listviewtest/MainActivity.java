package com.example.listviewtest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import com.app.mlistviewcomponent.PullUpAndDownList;

public class MainActivity extends Activity implements
		PullUpAndDownList.OnDataRequestListener {
	private int perLen = 5;
	private ArrayList<Integer> array;
	private ArrayAdapter<Integer> adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		array = new ArrayList<Integer>();
		loadData();
		adapter = new ArrayAdapter<Integer>(this,
				android.R.layout.simple_list_item_1, array);
		PullUpAndDownList list1 = (PullUpAndDownList) findViewById(R.id.list1);
		list1.setOnDataRequestListener(this);
		list1.setAdapter(adapter);
	}

	private void loadData() {
		int lastValue = array.size() == 0 ? 0 : array.get(array.size() - 1);
		int j = lastValue + perLen;
		for (int i = lastValue + 1; i <= j; i++) {
			array.add(i);
		}
	}

	@Override
	public void onLoadMore() {
		loadData();
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onRefresh() {
		int n = array.get(0) + 1;
		array.clear();
		for (int i = n + perLen; n < i; n++) {
			array.add(n);
		}
		adapter.notifyDataSetChanged();
	}
}