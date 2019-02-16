package sam.gymnotes.fragments;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.ArrayList;

import sam.gymnotes.Exercise;
import sam.gymnotes.R;
import sam.gymnotes.Set;
import sam.gymnotes.Workout;
import sam.gymnotes.activities.WorkoutView;

/**
 * Created by Sam on 22/08/2016.
 */
public class WorkoutStatsFragment extends Fragment {

    public boolean activityCreated;

    private String ICON_DIRECTORY;
    private FrameLayout bigIconContainer;
    private ImageView bigIcon;
    private TextView exerciseName;

    private TextView firstWorkout;
    private TextView workouts;
    private TextView totalSets;
    private TextView totalReps;
    private TextView totalWeight;

    private TextView bestSet;
    private TextView bestSetTime;
    private LinearLayout personalBestsContainer;
    private ToggleButton moreLessButton;

    private static final int[] includeReps = {1,3,5,8,10,12,15};


    public WorkoutStatsFragment() {
        //empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ICON_DIRECTORY = getString(R.string.ICON_DIRECTORY);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_workout_stats, container, false);

        bigIconContainer = (FrameLayout) v.findViewById(R.id.bigIconContainer);
        bigIcon = (ImageView) v.findViewById(R.id.bigIcon);
        exerciseName = (TextView) v.findViewById(R.id.exerciseName);

        firstWorkout = (TextView) v.findViewById(R.id.firstWorkout);
        workouts = (TextView) v.findViewById(R.id.workouts);
        totalSets = (TextView) v.findViewById(R.id.totalSets);
        totalReps = (TextView) v.findViewById(R.id.totalReps);
        totalWeight = (TextView) v.findViewById(R.id.totalWeight);

        bestSet = (TextView) v.findViewById(R.id.bestSet);
        bestSetTime = (TextView) v.findViewById(R.id.bestSetTime);
        personalBestsContainer = (LinearLayout) v.findViewById(R.id.personalBests);

        moreLessButton = (ToggleButton) v.findViewById(R.id.moreLessButton);
        moreLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fillPersonalBests();
            }
        });
        moreLessButton.setVisibility(View.GONE);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activityCreated = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshPage();
    }





    public void refreshPage() {
        if (activityCreated != true){
            return;
        }
        WorkoutView activity = (WorkoutView) getActivity();

        if (activity.dataLoaded != true){
            return;
        }

        Exercise exercise = activity.getExercise();

        try {
            setExerciseIcon();
        } catch (IOException e) {
            e.printStackTrace();
        }
        exerciseName.setText(exercise.getName());
        fillStats();
    }

    public void setExerciseIcon() throws IOException {

        WorkoutView activity = (WorkoutView) getActivity();

        Exercise exercise = activity.getExercise();

        if (exercise.getIconPath() == null) {
            bigIconContainer.setVisibility(View.GONE);
            return;
        }

        String iconPath = ICON_DIRECTORY + "/black/" + exercise.getIconPath();
        Resources resources = getResources();

        Drawable iconDrawable = Drawable.createFromResourceStream(resources, new TypedValue(), resources.getAssets().open(iconPath), null);

        bigIcon.setImageDrawable(iconDrawable);
        bigIconContainer.setVisibility(View.VISIBLE);
    }

    public void fillStats(){
        // ------------------------------------------------------------------------
        // Get some activity variables
        // ------------------------------------------------------------------------
        WorkoutView activity = (WorkoutView) getActivity();
        Exercise exercise = activity.getExercise();
        ArrayList<Workout> workoutList = activity.getWorkoutList();


        if (activity.first_workout != null){
            firstWorkout.setText(activity.first_workout.toShortString());
        }
        else{
            firstWorkout.setText(getString(R.string.NoWorkouts));
        }


        workouts.setText(String.valueOf(workoutList.size()));
        totalSets.setText(String.valueOf(activity.total_sets));
        totalReps.setText(String.valueOf(activity.total_reps));
        totalWeight.setText(activity.total_weight.setScale(2, RoundingMode.HALF_UP).toPlainString() + " " + exercise.getUnit());



        if (activity.best_set != null){
            bestSet.setText(activity.best_set.getWeight().toPlainString() + " " + exercise.getUnit() + "  x  " + activity.best_set.getReps());
            bestSetTime.setText("Completed " + activity.best_workout.getDate().daysAgoString() + ". " + activity.best_workout.getDate().toLongString(1,1,2,2));
            bestSetTime.setVisibility(View.VISIBLE);
        }
        else{
            bestSet.setText(R.string.elipses);
            bestSetTime.setVisibility(View.GONE);
        }


        fillPersonalBests();
    }

    public void fillPersonalBests(){
        moreLessButton.setVisibility(View.GONE);

        WorkoutView activity = (WorkoutView) getActivity();
        Exercise exercise = activity.getExercise();


        // ------------------------------------------------------------------------
        // Make sure personal bests table is empty, then fill with values
        // ------------------------------------------------------------------------
        personalBestsContainer.removeAllViews();

        for (int i = 1; i<= Set.repPercentages.length; i++){

            if (moreLessButton.isChecked() || contains(includeReps, i)){


                LayoutInflater inflater = LayoutInflater.from(this.getContext());
                View v = inflater.inflate(R.layout.list_pbs, null);

                TextView repNumber = (TextView) v.findViewById(R.id.repNumber);
                TextView estimated = (TextView) v.findViewById(R.id.estimated);
                TextView actual = (TextView) v.findViewById(R.id.actual);

                if (i == 1) {
                    repNumber.setText(i + " " + getString(R.string.rep));
                } else {
                    repNumber.setText(i + " " + getString(R.string.reps));
                }

                if (activity.best_set != null) {
                    estimated.setText(activity.best_set.getRepMax(i).toPlainString() + " " + exercise.getUnit());
                } else {
                    estimated.setText(R.string.elipses);
                }

                if (activity.personal_bests[i - 1] != null) {
                    actual.setText(activity.personal_bests[i - 1].toPlainString() + " " + exercise.getUnit());
                } else {
                    actual.setText(R.string.elipses);
                }

                personalBestsContainer.addView(v);

            }
        }
        moreLessButton.setVisibility(View.VISIBLE);
    }

    public boolean contains(int[] list, int check){
        for (int i=0; i < list.length; i++){
            if (list[i] == check){
                return true;
            }
        }
        return false;
    }




}