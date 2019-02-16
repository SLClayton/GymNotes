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

import sam.gymnotes.ExerciseGroup;
import sam.gymnotes.R;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Sam on 04/08/2016.
 */
public class GroupListAdapter extends ArrayAdapter implements StickyListHeadersAdapter {

    private String ICON_DIRECTORY;

    private String sortPref;

    public GroupListAdapter(Context context, List<ExerciseGroup> groups, String sort) {
        super(context, Integer.valueOf(9999), groups);
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
        View row = inflater.inflate(R.layout.list_group_row, parent, false);


        ExerciseGroup group = (ExerciseGroup) getItem(position);


        // ------------------------------------------------------------------------
        // Set group name
        // ------------------------------------------------------------------------
        TextView groupName = (TextView) row.findViewById(R.id.groupName);
        groupName.setText(group.getName());


        int exercisesInGroup = group.exerciseListLength();
        TextView exerciseCount = (TextView) row.findViewById(R.id.exerciseCount);
        exerciseCount.setText(exercisesInGroup + " exercises");

        ImageView icon4_1 = (ImageView) row.findViewById(R.id.icon1);
        ImageView icon4_2 = (ImageView) row.findViewById(R.id.icon2);
        ImageView icon4_3 = (ImageView) row.findViewById(R.id.icon3);
        ImageView icon4_4 = (ImageView) row.findViewById(R.id.icon4);
        ImageView icon1_1 = (ImageView) row.findViewById(R.id.icon1_single);
        ImageView icon3_3 = (ImageView) row.findViewById(R.id.icon3_triple);
        TextView empty = (TextView) row.findViewById(R.id.empty);

        icon4_1.setVisibility(View.GONE);
        icon4_2.setVisibility(View.GONE);
        icon4_3.setVisibility(View.GONE);
        icon4_4.setVisibility(View.GONE);
        icon1_1.setVisibility(View.GONE);
        icon3_3.setVisibility(View.GONE);
        empty.setVisibility(View.GONE);



        // ------------------------------------------------------------------------
        // Add exercise icons in the different positions depending on how many
        // there are.
        // ------------------------------------------------------------------------
        try {

            if (exercisesInGroup <= 0){
                empty.setVisibility(View.VISIBLE);
            }
            else if (exercisesInGroup == 1){
                icon1_1.setImageDrawable(Drawable.createFromStream(this.getContext().getAssets().open(ICON_DIRECTORY + "/black/" + group.getExercise(0).getIconPath()), null));
                icon1_1.setVisibility(View.VISIBLE);
            }
            else if (exercisesInGroup == 2){
                icon4_1.setImageDrawable(Drawable.createFromStream(this.getContext().getAssets().open(ICON_DIRECTORY + "/black/" + group.getExercise(0).getIconPath()), null));
                icon4_1.setVisibility(View.VISIBLE);
                icon4_4.setImageDrawable(Drawable.createFromStream(this.getContext().getAssets().open(ICON_DIRECTORY + "/black/" + group.getExercise(1).getIconPath()), null));
                icon4_4.setVisibility(View.VISIBLE);
            }
            else if (exercisesInGroup == 3){
                icon4_1.setImageDrawable(Drawable.createFromStream(this.getContext().getAssets().open(ICON_DIRECTORY + "/black/" + group.getExercise(0).getIconPath()), null));
                icon4_1.setVisibility(View.VISIBLE);
                icon4_2.setImageDrawable(Drawable.createFromStream(this.getContext().getAssets().open(ICON_DIRECTORY + "/black/" + group.getExercise(1).getIconPath()), null));
                icon4_2.setVisibility(View.VISIBLE);
                icon3_3.setImageDrawable(Drawable.createFromStream(this.getContext().getAssets().open(ICON_DIRECTORY + "/black/" + group.getExercise(2).getIconPath()), null));
                icon3_3.setVisibility(View.VISIBLE);
            }
            else{
                icon4_1.setImageDrawable(Drawable.createFromStream(this.getContext().getAssets().open(ICON_DIRECTORY + "/black/" + group.getExercise(0).getIconPath()), null));
                icon4_1.setVisibility(View.VISIBLE);
                icon4_2.setImageDrawable(Drawable.createFromStream(this.getContext().getAssets().open(ICON_DIRECTORY + "/black/" + group.getExercise(1).getIconPath()), null));
                icon4_2.setVisibility(View.VISIBLE);
                icon4_3.setImageDrawable(Drawable.createFromStream(this.getContext().getAssets().open(ICON_DIRECTORY + "/black/" + group.getExercise(2).getIconPath()), null));
                icon4_3.setVisibility(View.VISIBLE);
                icon4_4.setImageDrawable(Drawable.createFromStream(this.getContext().getAssets().open(ICON_DIRECTORY + "/black/" + group.getExercise(3).getIconPath()), null));
                icon4_4.setVisibility(View.VISIBLE);
            }


        } catch (IOException e) {
            e.printStackTrace();
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
            text.setText("Groups");
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

        return 3;
    }
}
