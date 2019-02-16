package sam.gymnotes.array_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.RoundingMode;
import java.util.ArrayList;

import sam.gymnotes.R;
import sam.gymnotes.Set;
import sam.gymnotes.Workout;
import sam.gymnotes.activities.SetView;

/**
 * Created by Sam on 04/08/2016.
 */
public class WorkoutListAdapter extends ArrayAdapter {

    String weightUnit;

    public WorkoutListAdapter(Context c, ArrayList<Workout> workouts, String unit) {
        super(c, Integer.valueOf(9999), workouts);
        weightUnit = unit;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getRow(position, parent);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getRow(position, parent);
    }

    public View getRow(int position, ViewGroup parent){
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        View row = inflater.inflate(R.layout.list_workout_row, parent, false);

        Workout workout = (Workout) getItem(position);

        // ------------------------------------------------------------------------
        // Set date
        // ------------------------------------------------------------------------
        TextView WorkoutDate = (TextView) row.findViewById(R.id.Date);
        WorkoutDate.setText(workout.getDate().toLongString(1,1,2,2));

        // ------------------------------------------------------------------------
        // Set days ago string
        // ------------------------------------------------------------------------
        TextView daysAgo = (TextView) row.findViewById(R.id.daysAgo);
        String s = workout.getDate().daysAgoString();
        s = s.substring(0,1).toUpperCase() + s.substring(1);
        daysAgo.setText(s);

        // ------------------------------------------------------------------------
        // Set note if not null, hide if null.
        // ------------------------------------------------------------------------
        TextView note = (TextView) row.findViewById(R.id.Note);
        String n = workout.getNote();
        note.setVisibility(View.GONE);
        if (n != null){
            note.setText(n);
            note.setVisibility(View.VISIBLE);
        }


        // ------------------------------------------------------------------------
        // Fill the view with rows of the sets in the workout
        // ------------------------------------------------------------------------
        TextView emptyMessage = (TextView) row.findViewById(R.id.emptyMessage);
        LinearLayout setList = (LinearLayout) row.findViewById(R.id.setList);

        ArrayList<Set> sets = workout.getSetList();

        // ------------------------------------------------------------------------
        // Use the static method is SetView Class to reorder the number and
        // position values for the sets to be correct.
        // ------------------------------------------------------------------------
        sets = SetView.orderByPosition(sets);


        emptyMessage.setVisibility(View.GONE);
        for (int i=0; i < sets.size(); i++){
            LayoutInflater setInflater = LayoutInflater.from(this.getContext());
            View setRow = inflater.inflate(R.layout.list_set_row_mini, parent, false);

            TextView number = (TextView) setRow.findViewById(R.id.number);
            TextView weight = (TextView) setRow.findViewById(R.id.weight);
            TextView unit = (TextView) setRow.findViewById(R.id.unit);
            TextView reps = (TextView) setRow.findViewById(R.id.reps);

            Set set = sets.get(i);


            number.setText(set.getNumber() + ".");
            weight.setText(set.getWeight().setScale(2, RoundingMode.HALF_UP).toPlainString());
            unit.setText(weightUnit);
            reps.setText(String.valueOf(set.getReps()));

            setList.addView(setRow);
        }
        if (sets.size() <= 0){
            emptyMessage.setVisibility(View.VISIBLE);
        }

        return row;
    }
}
