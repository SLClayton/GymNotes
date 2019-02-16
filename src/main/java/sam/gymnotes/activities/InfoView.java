package sam.gymnotes.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.math.BigDecimal;
import java.math.RoundingMode;

import sam.gymnotes.DBHandler;
import sam.gymnotes.R;
import sam.gymnotes.ThemeHandler;


public class InfoView extends AppCompatActivity {

    private String TABLE_EXERCISE;
    private String TABLE_WORKOUT;
    private String TABLE_SET;
    private String ICON_DIRECTORY;
    private String[] units;
    private BigDecimal unitRatio;
    private DBHandler DB;

    private Toolbar toolbar;
    private TextView exercises;
    private TextView workouts;
    private TextView sets;
    private TextView weight;
    private TextView reps;

    private TextView appInfo;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(new ThemeHandler(this).getActivityTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);


        TABLE_EXERCISE = getString(R.string.TABLE_EXERCISE);
        TABLE_WORKOUT = getString(R.string.TABLE_WORKOUT);
        TABLE_SET = getString(R.string.TABLE_SET);
        ICON_DIRECTORY = getString(R.string.ICON_DIRECTORY);
        units = getResources().getStringArray(R.array.units);
        unitRatio = new BigDecimal(getResources().getString(R.string.LbKgRatio));


        DB = new DBHandler(getApplicationContext());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        exercises = (TextView) findViewById(R.id.exercises);
        workouts = (TextView) findViewById(R.id.workouts);
        sets = (TextView) findViewById(R.id.sets);
        weight = (TextView) findViewById(R.id.totalweight);
        reps = (TextView) findViewById(R.id.totalreps);


        appInfo = (TextView) findViewById(R.id.AppNameAndVersion);

        setupToolbar();
        fillViews();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }



    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);
        a.setTitle(R.string.Info);

    }

    public int indexOf(String[] list, String find) {
        for (int i = 0; i < list.length; i++) {
            if (list[i].equals(find)) {
                return i;
            }
        }
        return 0;
    }

    public void fillViews() {
        new getData().execute();

        getAppInfo();
    }

    public class getData extends AsyncTask {

        String exerciseString;
        String workoutString;
        String weightString;
        String repString;
        String setString;

        @Override
        protected Object doInBackground(Object[] objects) {

            DB.open();

            // Get Exercises
            Cursor allExercises = DB.getAllRows(TABLE_EXERCISE, new String[]{"_id"});
            exerciseString = allExercises.getCount()+"";
            allExercises.close();

            // Get Workouts
            Cursor allWorkouts = DB.getAllRows(TABLE_WORKOUT, new String[]{"_id"});
            workoutString = allWorkouts.getCount()+"";
            allWorkouts.close();


            // Get Set data
            BigDecimal[] totalWeight = new BigDecimal[2];
            totalWeight[0] = BigDecimal.ZERO;
            totalWeight[1] = BigDecimal.ZERO;

            int totalSets = 0;
            int totalReps = 0;
            BigDecimal LbKgRatio = new BigDecimal(getString(R.string.LbKgRatio));

            Cursor allSets = DB.getAllRows(TABLE_SET, new String[]{"_id", "exercise_id", "weight", "reps"});
            allSets.moveToPosition(-1);
            while (allSets.moveToNext()) {
                totalSets++;
                totalReps += allSets.getInt(3);

                Cursor exercise = DB.getRow(TABLE_EXERCISE, allSets.getLong(1), new String[]{"_id", "unit"});
                exercise.moveToFirst();

                int unitIndex = indexOf(units, exercise.getString(1));
                BigDecimal weightTimesReps = new BigDecimal(allSets.getString(2)).multiply(new BigDecimal(allSets.getString(3)));

                totalWeight[unitIndex] = totalWeight[unitIndex].add(weightTimesReps);

                exercise.close();
            }
            allSets.close();


            BigDecimal totalWeightKG = totalWeight[0].add(totalWeight[1].divide(LbKgRatio, 2, RoundingMode.HALF_UP));
            String strWeightKG = String.format("%,d", totalWeightKG.setScale(0, RoundingMode.HALF_UP).intValueExact());
            String strWeightLB = String.format("%,d", totalWeightKG.multiply(LbKgRatio).setScale(0, RoundingMode.HALF_UP).intValueExact());

            setString = totalSets + "";
            weightString = strWeightKG + "kg or " + strWeightLB + "lb";
            repString = totalReps + "";

            DB.close();

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            exercises.setText(exerciseString);
            workouts.setText(workoutString);
            weight.setText(weightString);
            reps.setText(repString);
            sets.setText(setString);
        }
    }

    public class getExercises extends AsyncTask {

        String exerciseString;
        String workoutString;

        @Override
        protected Object doInBackground(Object[] objects) {
            Cursor allExercises = DB.getAllRows(TABLE_EXERCISE, new String[]{"_id"});
            exerciseString = allExercises.getCount()+"";
            allExercises.close();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            exercises.setText(exerciseString);
        }
    }

    public class getWorkouts extends AsyncTask {
        String workoutString;

        @Override
        protected Object doInBackground(Object[] objects) {
            Cursor allWorkouts = DB.getAllRows(TABLE_WORKOUT, new String[]{"_id"});
            workoutString = allWorkouts.getCount()+"";
            allWorkouts.close();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            workouts.setText(workoutString);
        }
    }

    public class getSetsAndSetData extends AsyncTask {

        String weightString;
        String repString;
        String setString;


        @Override
        protected Object doInBackground(Object[] objects) {

            BigDecimal[] totalWeight = new BigDecimal[2];
            totalWeight[0] = BigDecimal.ZERO;
            totalWeight[1] = BigDecimal.ZERO;

            int totalSets = 0;
            int totalReps = 0;
            BigDecimal LbKgRatio = new BigDecimal(getString(R.string.LbKgRatio));

            Cursor allSets = DB.getAllRows(TABLE_SET, new String[]{"_id", "exercise_id", "weight", "reps"});
            allSets.moveToPosition(-1);
            while (allSets.moveToNext()) {
                totalSets++;
                totalReps += allSets.getInt(3);

                Cursor exercise = DB.getRow(TABLE_EXERCISE, allSets.getLong(1), new String[]{"_id", "unit"});
                exercise.moveToFirst();

                int unitIndex = indexOf(units, exercise.getString(1));
                BigDecimal weightTimesReps = new BigDecimal(allSets.getString(2)).multiply(new BigDecimal(allSets.getString(3)));

                totalWeight[unitIndex] = totalWeight[unitIndex].add(weightTimesReps);

                exercise.close();
            }
            allSets.close();


            BigDecimal totalWeightKG = totalWeight[0].add(totalWeight[1].divide(LbKgRatio, 2, RoundingMode.HALF_UP));
            String strWeightKG = String.format("%,d", totalWeightKG.setScale(0, RoundingMode.HALF_UP).intValueExact());
            String strWeightLB = String.format("%,d", totalWeightKG.multiply(LbKgRatio).setScale(0, RoundingMode.HALF_UP).intValueExact());

            setString = totalSets + "";
            weightString = strWeightKG + "kg or " + strWeightLB + "lb";
            repString = totalReps + "";

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            weight.setText(weightString);
            reps.setText(repString);
            sets.setText(setString);
        }
    }

    public void getAppInfo() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            appInfo.setText(getString(R.string.app_name) + "\n" + getString(R.string.version) + " - " + pInfo.versionName + " (" + pInfo.versionCode + ")");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void openGoogleOpinionRewards(View view){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.google.android.apps.paidtasks"));
        startActivity(intent);
    }

}
