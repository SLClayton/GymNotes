package sam.gymnotes.array_adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import sam.gymnotes.CustomCalendar;
import sam.gymnotes.Exercise;
import sam.gymnotes.R;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Sam on 04/08/2016.
 */
public class ExerciseListAdapter extends ArrayAdapter implements StickyListHeadersAdapter {

    private String ICON_DIRECTORY;

    private String sortPref;

    public ExerciseListAdapter(Context context, List<Exercise> exercises, String sort) {
        super(context, Integer.valueOf(9999), exercises);
        ICON_DIRECTORY = this.getContext().getString(R.string.ICON_DIRECTORY);
        sortPref = sort;
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        View row = inflater.inflate(R.layout.list_exercise_row, parent, false);


        Exercise exercise = (Exercise) getItem(position);


        // ------------------------------------------------------------------------
        // Set exercise name
        // ------------------------------------------------------------------------
        TextView exerciseName = (TextView) row.findViewById(R.id.exerciseName);
        exerciseName.setText(exercise.getName());


        // ------------------------------------------------------------------------
        // Set the last workout field
        // ------------------------------------------------------------------------
        TextView LastWorkout = (TextView) row.findViewById(R.id.LastWorkout);
        CustomCalendar last = exercise.getLastWorkout();
        if (last == null){
            LastWorkout.setText(this.getContext().getString(R.string.Never));
        }
        else{
            String str = last.daysAgoString() + ". " + last.toLongString(1,1,1,2);
            LastWorkout.setText(str.substring(0, 1).toUpperCase() + str.substring(1));
        }


        // ------------------------------------------------------------------------
        // If iconPath not null, then set the icon from resources, otherwise
        // make it transparent.
        // ------------------------------------------------------------------------
        ImageView icon = (ImageView) row.findViewById(R.id.icon);
        String iconPath = exercise.getIconPath();
        if (iconPath != null) {
            try {
                icon.setImageDrawable(Drawable.createFromStream(this.getContext().getAssets().open(ICON_DIRECTORY + "/black/" + iconPath), null));
            } catch (IOException e) {e.printStackTrace();}
        }
        else{
            icon.setImageResource(android.R.color.transparent);
        }


        return row;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        View v = inflater.inflate(R.layout.list_exercise_header, parent, false);
        TextView text = (TextView) v.findViewById(R.id.text);

        long headerID = getHeaderId(position);

        if (headerID == 3){
            text.setText(R.string.AtoZ);
        }
        else if (headerID == 2){
            text.setText(R.string.New);
        }
        else if (headerID == 1){
            text.setText(R.string.Today);
        }
        else if (headerID == 0){
            text.setText(R.string.upNext);
        }


        return v;
    }

    @Override
    public long getHeaderId(int position) {

        CustomCalendar lastWorkout = ((Exercise) getItem(position)).getLastWorkout();

        if (!sortPref.equals(this.getContext().getString(R.string.date))){
            return 3;
        }
        else if (lastWorkout == null){
            return 2;
        }
        else if (lastWorkout.isToday()){
            return 1;
        }
        else if (lastWorkout.daysAgo() > 0){
            return 0;
        }
        return 0;
    }
}
