package sam.gymnotes.array_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import sam.gymnotes.R;
import sam.gymnotes.Set;

/**
 * Created by Sam on 22/07/2016.
 */
public class SetListAdapter extends ArrayAdapter {

    private HashMap<Set, Integer> setMap;
    private String unit;

    public SetListAdapter(Context c, int textViewResourceId, ArrayList<Set> sets, String exerciseUnit) {
        super(c, textViewResourceId, sets);
        unit = exerciseUnit;

        setMap = new HashMap<Set, Integer>();
        for (int i = 0; i < sets.size(); i++) {
            setMap.put(sets.get(i), i);
        }
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= setMap.size()) {
            return -1;
        }
        Set item = (Set) getItem(position);
        return setMap.get(item);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        View row = inflater.inflate(R.layout.list_set_row, parent, false);

        Set s = (Set) getItem(position);

        // Hide Move Icon
        ImageView moveIcon = (ImageView) row.findViewById(R.id.moveIcon);
        moveIcon.setVisibility(View.INVISIBLE);

        // Hide note
        LinearLayout noteBar = (LinearLayout) row.findViewById(R.id.noteBar);
        noteBar.setVisibility(View.GONE);

        // Assign Set Number (W. if warmup)
        TextView number = (TextView) row.findViewById(R.id.number);
        if (s.isWarmUp()){
            number.setText("W");
            LinearLayout back = (LinearLayout) row.findViewById(R.id.main);

            // ------------------------------------------------------------------------
            // Get color from current theme
            // ------------------------------------------------------------------------


            back.setBackgroundColor(getContext().getResources().getColor(R.color.light_foreground3));
        }
        else{
            number.setText(s.getNumber() + "");
        }

        // Set Set weight
        TextView weight = (TextView) row.findViewById(R.id.weight);
        weight.setText(s.getWeight().setScale(2, BigDecimal.ROUND_HALF_UP).toString());

        // Set Unit
        TextView unitView = (TextView) row.findViewById(R.id.unit);
        unitView.setText(unit);

        // Set Reps
        TextView reps = (TextView) row.findViewById(R.id.reps);
        reps.setText(s.getReps()+"");

        // Set note and show if not null
        String note = s.getNote();
        if (note != null){
            TextView noteLine = (TextView) row.findViewById(R.id.note);
            noteLine.setText(note);
            noteBar.setVisibility(View.VISIBLE);
        }

        return row;
    }

}