package com.dtt.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dtt.R;
import com.dtt.objs.Schedule;

/**
 * Class for customized list adapter for schedule list 
 * 
 * @author Moon Hwan Oh, Amanda Shen
 * 
 *
 */
public class ScheduleListAdapter extends ArrayAdapter<Schedule> {
	private ArrayList<Schedule> items;
    private Context context;

    public ScheduleListAdapter(Context context, int textViewResourceId, ArrayList<Schedule> items) {
        super(context, textViewResourceId, items);
        this.items = items;
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.schedule_list_row, null);
        }
        Schedule s = items.get(position);
        if (s != null) {
        	ImageView taskPictureView = (ImageView) v.findViewById(R.id.schedule_list_row_task_picture);
        	if(s.hasPicture())
        		taskPictureView.setImageURI(Uri.parse(s.getPictureUri()));
        	else
        		taskPictureView.setImageResource(R.drawable.ic_menu_close_clear_cancel);
        	
        	TextView taskNameView = (TextView) v.findViewById(R.id.schedule_list_row_task_name);
        	taskNameView.setText(s.getTaskName().toString());
        	
        	TextView timeView = (TextView) v.findViewById(R.id.schedule_list_row_time);
        	timeView.setText(s.getTime().toString());
        	
        	ImageView flagView = (ImageView) v.findViewById(R.id.schedule_list_row_flag);
        	if(s.isDone())
        		flagView.setImageResource(R.drawable.ic_check);
        	else
        		flagView.setImageResource(R.drawable.ic_check_bw);
        	
        	ImageView importantView = (ImageView) v.findViewById(R.id.schedule_list_row_important);
        	if(s.isImportant())
        		importantView.setImageResource(R.drawable.ic_star);
        	else
        		importantView.setImageResource(R.drawable.ic_star_bw);
        	
        	
        }
        return v;
    }
}
