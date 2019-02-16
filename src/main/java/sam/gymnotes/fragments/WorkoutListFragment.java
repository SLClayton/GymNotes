package sam.gymnotes.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import sam.gymnotes.Exercise;
import sam.gymnotes.R;
import sam.gymnotes.Workout;
import sam.gymnotes.activities.WorkoutView;
import sam.gymnotes.array_adapters.WorkoutListAdapter;
import sam.gymnotes.dialogs.edit_workout;

/**
 * Created by Sam on 22/08/2016.
 */
public class WorkoutListFragment extends Fragment {

    private boolean activityCreated;

    private String TABLE_WORKOUT;

    private ListView list;
    private TextView message;


    public WorkoutListFragment(){
        // Has to have empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TABLE_WORKOUT = getString(R.string.TABLE_WORKOUT);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_workout_list, container, false);

        list = (ListView) v.findViewById(R.id.workoutList);
        message = (TextView) v.findViewById(R.id.message);

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshPage();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activityCreated = true;
    }

    public void refreshPage(){
        if (activityCreated != true){
            return;
        }
        WorkoutView activity = ((WorkoutView) getActivity());

        if (activity.dataLoaded != true){
            return;
        }

        Exercise exercise = activity.getExercise();
        ArrayList<Workout> workoutList = activity.getWorkoutList();

        message.setVisibility(View.GONE);


        //------------------------------------------------------------------------------
        // Remember the current scroll position of the list
        //------------------------------------------------------------------------------
        Parcelable listState = list.onSaveInstanceState();


        //------------------------------------------------------------------------------
        // Set adapter to listview
        //------------------------------------------------------------------------------
        list.setAdapter(new WorkoutListAdapter(getActivity(), workoutList, exercise.getUnit()));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gotoWorkout(position);
            }});
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                openEditWorkout(i);
                return true;
            }
        });


        //------------------------------------------------------------------------------
        // Restore position of list
        //------------------------------------------------------------------------------
        if (listState != null){
            list.onRestoreInstanceState(listState);
        }


        //------------------------------------------------------------------------------
        // Show empty message if list is empty
        //------------------------------------------------------------------------------
        if (workoutList.size() <= 0){
            message.setText(getString(R.string.NoWorkouts));
            message.setVisibility(View.VISIBLE);
        }
    }

    public void gotoWorkout(int pos) {
        ArrayList<Workout> workoutList = ((WorkoutView)getActivity()).getWorkoutList();

        ((WorkoutView) getActivity()).gotoWorkout(workoutList.get(pos).getID());
    }

    public void openEditWorkout(int position) {
        ArrayList<Workout> workoutList = ((WorkoutView)getActivity()).getWorkoutList();

        Intent i = new Intent(getActivity(), edit_workout.class);
        i.putExtra("Workout_ID", workoutList.get(position).getID());
        startActivity(i);
    }

}
