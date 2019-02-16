package sam.gymnotes.activities;

import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import sam.gymnotes.CustomCalendar;
import sam.gymnotes.DBHandler;
import sam.gymnotes.Exercise;
import sam.gymnotes.R;
import sam.gymnotes.Set;
import sam.gymnotes.ThemeHandler;
import sam.gymnotes.Workout;
import sam.gymnotes.dialogs.edit_exercise;
import sam.gymnotes.fragments.WorkoutGraphFragment;
import sam.gymnotes.fragments.WorkoutListFragment;
import sam.gymnotes.fragments.WorkoutStatsFragment;


public class WorkoutView extends AppCompatActivity {

    private Bundle savedInstanceState;
    private static final String tabTitles = "tabTitles";

    private String TABLE_EXERCISE;
    private String TABLE_WORKOUT;
    private String TABLE_SET;
    private String ICON_DIRECTORY;
    private DBHandler DB;

    private Menu menu;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageView icon;

    private Exercise exercise;
    private ArrayList<Workout> workoutList;

    // ------------------------------------------------------------------------
    // Exercise stat variables to be used by child fragments
    // ------------------------------------------------------------------------
    public int total_sets;
    public int total_reps;
    public BigDecimal total_weight;
    public CustomCalendar first_workout;
    public Set best_set;
    public Workout best_workout;
    public BigDecimal[] personal_bests;

    public SharedPreferences sharedPreferences;

    private static final String logtag = "Workout View";


    private ViewPagerAdapter viewPagerAdapter;

    public boolean dataLoaded;



    // ------------------------------------------------------------------------
    // OVERRIDE METHODS
    // ------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(new ThemeHandler(this).getActivityTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__workout_view);
        this.savedInstanceState = savedInstanceState;



        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        icon = (ImageView) findViewById(R.id.icon);


        TABLE_EXERCISE = getString(R.string.TABLE_EXERCISE);
        TABLE_WORKOUT = getString(R.string.TABLE_WORKOUT);
        TABLE_SET = getString(R.string.TABLE_SET);
        ICON_DIRECTORY = getString(R.string.ICON_DIRECTORY);

        DB = new DBHandler(getApplicationContext());

        sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE);

        dataLoaded = false;

        setupToolbar();
        setupFragmentTabs();

        // ------------------------------------------------------------------------
        // Retrieves ONLY the exercise ID from the passed intent
        // (NOT FROM THE DATABASE)
        // ------------------------------------------------------------------------
        Intent i = getIntent();
        exercise = new Exercise(null, null, i.getLongExtra("Exercise_ID", 99999), null, null, null, null, null, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        dataLoaded = false;
        requestBackup();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshExercise();
        new refreshWorkouts().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_workout_view, menu);
        this.menu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){

            case android.R.id.home:
                onBackPressed();
                break;

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArray(tabTitles, viewPagerAdapter.getTitles().toArray(new String[0]));
    }

    public void requestBackup(){
        if (sharedPreferences.getBoolean("backup", false)){
            BackupManager bm = new BackupManager(this);
            bm.dataChanged();
            Log.i(logtag, "Backup requested");
        }
    }

    // ------------------------------------------------------------------------
    // Getters for the child fragments to retrieve data
    // ------------------------------------------------------------------------

    public Exercise getExercise() {
        return exercise;
    }

    public ArrayList<Workout> getWorkoutList() {
        return workoutList;
    }

    public DBHandler getDBHandler(){
        return DB;
    }



    // ------------------------------------------------------------------------
    // Async Tasks
    // ------------------------------------------------------------------------
    public class refreshWorkouts extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            refreshWorkoutsFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            refreshTabs();
        }
    }


    // ------------------------------------------------------------------------
    // MAIN METHODS
    // ------------------------------------------------------------------------

    public void setupToolbar() {
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditExerciseDialog(view);
            }
        });

        setSupportActionBar(toolbar);
        ActionBar a = getSupportActionBar();

        a.setDisplayHomeAsUpEnabled(true);

    }

    public void setupFragmentTabs(){
        // ------------------------------------------------------------------------
        // Add fragments to tabs
        // ------------------------------------------------------------------------
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());


        if (savedInstanceState == null){
            viewPagerAdapter.addFragment(new WorkoutListFragment(), getString(R.string.Workouts));
            viewPagerAdapter.addFragment(new WorkoutStatsFragment(), getString(R.string.Stats));
            viewPagerAdapter.addFragment(new WorkoutGraphFragment(), getString(R.string.Graph));

            //for (int i=0; i<viewPagerAdapter.getCount(); i++){
            //    getSupportFragmentManager().beginTransaction().add(viewPagerAdapter.getItem(i), "" + viewPagerAdapter.getPageTitle(i));
            //}
        }
        else{
            String[] pageTitles = savedInstanceState.getStringArray(tabTitles);

            for (int i=0; i<pageTitles.length; i++){
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + i);
                viewPagerAdapter.addFragment(fragment, pageTitles[i]);
            }
        }

        viewPagerAdapter.notifyDataSetChanged();

        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(3);

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });
    }

    public void setExerciseIcon() throws IOException {
        if (exercise.getIconPath() == null){
            icon.setVisibility(View.GONE);
            return;
        }

        String iconPath = ICON_DIRECTORY + "/white/" + exercise.getIconPath();
        Resources resources = getResources();

        //Drawable iconDrawable = Drawable.createFromStream(getAssets().open(iconPath), null);
        Drawable iconDrawable = Drawable.createFromResourceStream(resources, new TypedValue(), resources.getAssets().open(iconPath), null);

        icon.setImageDrawable(iconDrawable);
        icon.setVisibility(View.VISIBLE);
    }

    public void refreshExercise(){
        // ------------------------------------------------------------------------
        // Gets exercise info from database and fills page with info.
        // closes the page if exercise doesn't exist.
        // ------------------------------------------------------------------------

        DB.open();

        Cursor c = DB.getRow(TABLE_EXERCISE, exercise.getID(), new String[]{"_id", "exercise_name", "icon_path", "unit"});
        if (c.moveToFirst()){

            exercise = new Exercise(getApplicationContext(), TABLE_WORKOUT, c.getLong(0), c.getString(1), c.getString(2), c.getString(3), null, null, null);
        }
        else{
            finish();
        }
        c.close();

        DB.close();


        getSupportActionBar().setTitle(exercise.getName());

        try {
            setExerciseIcon();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshWorkoutsFromDatabase(){
        // ------------------------------------------------------------------------
        // Gathers all workouts (with their sets) from database into objects
        // while also gathering stats at the same time.
        // ------------------------------------------------------------------------

        workoutList = new ArrayList<Workout>();

        total_sets = 0;
        total_reps = 0;
        total_weight = BigDecimal.ZERO;
        first_workout = null;
        best_set = null;
        best_workout = null;
        personal_bests = new BigDecimal[Set.repPercentages.length];


        DB.open();

        // ------------------------------------------------------------------------
        // Retrieves all the workouts from this exercise
        // ------------------------------------------------------------------------
        Cursor workourCursor = DB.getAllRows(   TABLE_WORKOUT, new String[] {"_id", "date", "note"}, "exercise_id = " + exercise.getID());

        workourCursor.moveToPosition(-1);
        while (workourCursor.moveToNext()){

            // ------------------------------------------------------------------------
            // Fills workout object from database rows
            // ------------------------------------------------------------------------
            Workout workout = new Workout(  workourCursor.getLong(0),
                    new CustomCalendar(workourCursor.getString(1)),
                    workourCursor.getString(2));



            // ------------------------------------------------------------------------
            // Check for the oldest workout
            // ------------------------------------------------------------------------
            if ((first_workout == null) || (first_workout.after(workout.getDate()))) {
                first_workout = workout.getDate();
            }




            // ------------------------------------------------------------------------
            // Fills each Workout object with Set objects retrieved from the database
            // ------------------------------------------------------------------------
            Cursor setCursor = DB.getAllRows(   TABLE_SET, new String[] {"_id", "position", "number", "weight", "reps",}, "workout_id = " + workout.getID() + " AND is_warmup = 0");
            setCursor.moveToPosition(-1);
            while (setCursor.moveToNext()){
                Set set = new Set( setCursor.getLong(0),
                        setCursor.getInt(1),
                        setCursor.getInt(2),
                        new BigDecimal(setCursor.getString(3)),
                        setCursor.getInt(4),
                        null,
                        null);

                int reps = set.getReps();
                BigDecimal weight = set.getWeight();


                // ------------------------------------------------------------------------
                // Adding up numbers for totals
                // ------------------------------------------------------------------------
                total_sets++;
                total_reps += reps;
                total_weight = total_weight.add(weight.multiply(BigDecimal.valueOf(reps)));



                // ------------------------------------------------------------------------
                // Find best set stats if rep range between certain values
                // ------------------------------------------------------------------------
                if (1 <= reps && reps <= Set.repPercentages.length) {

                    // ------------------------------------------------------------------------
                    // Finding best weight for any amount of reps
                    // ------------------------------------------------------------------------
                    BigDecimal previousBest = personal_bests[reps - 1];

                    if ((previousBest == null) || (weight.compareTo(previousBest) == 1)) {
                        personal_bests[reps - 1] = weight;
                    }


                    // ------------------------------------------------------------------------
                    // Finding best one rep max ESTIMATE
                    // ------------------------------------------------------------------------
                    BigDecimal estimatedORM = set.getRepMax(1);

                    if (best_set == null ||
                            estimatedORM.compareTo(best_set.getRepMax(1)) == 1) {

                        best_set = set;
                        best_workout = workout;
                    }
                }

                workout.addSet(set);
            }

            setCursor.close();



            workoutList.add(0, workout);
        }
        workourCursor.close();

        for (int i=personal_bests.length - 2; i>=0; i--){
            if (personal_bests[i + 1] != null &&
                    (personal_bests[i] == null || personal_bests[i].compareTo(personal_bests[i+1]) == -1)){

                personal_bests[i] = personal_bests[i+1];
            }
        }

        workoutList = sortByDate(workoutList);

        dataLoaded = true;

        DB.close();
    }

    public void refreshTabs(){
        ((WorkoutListFragment) viewPagerAdapter.getItem(0)).refreshPage();
        ((WorkoutStatsFragment) viewPagerAdapter.getItem(1)).refreshPage();
        ((WorkoutGraphFragment) viewPagerAdapter.getItem(2)).refreshPage();
    }

    public void openEditExerciseDialog(){
        Intent i = new Intent(this, edit_exercise.class);
        i.putExtra("EditMode", true);
        i.putExtra("Exercise_ID", exercise.getID());
        startActivity(i);
    }

    public void openEditExerciseDialog(View v){
         openEditExerciseDialog();
    }

    public static ArrayList<Workout> sortByDate(ArrayList<Workout> workouts){
        for (int i = 1; i < workouts.size(); i++){
            Workout x = workouts.get(i);
            int j = i;
            while (j > 0 && workouts.get(j - 1).getDate().before(x.getDate())){
                workouts.set(j, workouts.get(j-1));
                j = j - 1;
            }
            workouts.set(j, x);
        }

        return workouts;
    }

    public void gotoNewWorkout(View view){
        DB.open();

        ContentValues cv = new ContentValues();
        cv.put("exercise_id", exercise.getID());
        cv.put("date", new CustomCalendar().toShortString());
        long newID = DB.insertRow(TABLE_WORKOUT, cv);

        DB.close();

        Intent i = new Intent(this, SetView.class);
        i.putExtra("Workout_ID", newID);
        startActivity(i);
    }

    public void gotoWorkout(long ID) {
        Intent i = new Intent(this, SetView.class);
        i.putExtra("Workout_ID", ID);
        startActivity(i);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        public List<String> getTitles(){
            return mFragmentTitleList;
        }
    }


}
