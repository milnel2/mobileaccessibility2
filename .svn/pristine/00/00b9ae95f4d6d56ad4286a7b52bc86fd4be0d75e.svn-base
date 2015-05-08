package com.dtt.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dtt.R;
import com.dtt.objs.TaskStep;

/**
 * Class for customized list adapter for step list 
 * 
 * @author Moon Hwan Oh, Amanda Shen
 * 
 *
 */
public class StepListAdapter extends ArrayAdapter<TaskStep> {
	
    private ArrayList<TaskStep> items;
    private Context context;

    public StepListAdapter(Context context, int textViewResourceId, ArrayList<TaskStep> items) {
        super(context, textViewResourceId, items);
        this.items = items;
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.step_list_row, null);
        }
        TaskStep s = items.get(position);
        if (s != null) {
        	TextView textView = (TextView) v.findViewById(R.id.step_num);
        	textView.setText("Step: " + s.getStepNum().toString());
        	
        	ImageView imageView = (ImageView) v.findViewById(R.id.step_picture);
        	if(s.hasPicture())
        		imageView.setImageResource(R.drawable.ic_picture);
        	else
        		imageView.setImageResource(R.drawable.ic_picture_bw);
        	
        	ImageView audioView = (ImageView) v.findViewById(R.id.step_audio);
        	if(s.hasAudio())
        		audioView.setImageResource(R.drawable.ic_audio);
        	else
        		audioView.setImageResource(R.drawable.ic_audio_bw);
        	
        	ImageView videoView = (ImageView) v.findViewById(R.id.step_video);
        	if(s.hasVideo())
        		videoView.setImageResource(R.drawable.ic_movie);
        	else
        		videoView.setImageResource(R.drawable.ic_movie_bw);
        }
        return v;
    }
}
