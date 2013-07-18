package com.av1rus.adblockremover;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MyListAdapter extends ArrayAdapter<MyList> {

	Context context;
	int layoutResourceId;
	MyList data[] = null;

	public MyListAdapter(Context context, int layoutResourceId, MyList[] data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ListHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new ListHolder();
			//holder.imgIcon = (ImageView) row.findViewById(R.id.imgIcon);
			holder.txtTitle = (TextView) row.findViewById(R.id.txtTitle);
			holder.txtDesc = (TextView) row.findViewById(R.id.txtDesc);

			row.setTag(holder);
		} else {
			holder = (ListHolder) row.getTag();
		}

		MyList list = data[position];
		holder.txtTitle.setText(list.title);
		holder.txtDesc.setText(list.desc);
		//holder.imgIcon.setImageResource(list.icon);

		return row;
	}

	static class ListHolder {
		//ImageView imgIcon;
		TextView txtTitle;
		TextView txtDesc;
	}
}
