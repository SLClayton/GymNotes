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
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import sam.gymnotes.CustomCalendar;
import sam.gymnotes.DBHandler;
import sam.gymnotes.DynamicListView;
import sam.gymnotes.Exercise;
import sam.gymnotes.R;
import sam.gymnotes.Set;
import sam.gymnotes.ThemeHandler;
import sam.gymnotes.Workout;
import sam.gymnotes.array_adapters.SetListAdapter;
import sam.gymnotes.dialogs.edit_note;
import sam.gymnotes.dialogs.edit_set;
import sam.gymnotes.dialogs.edit_workout;


public class SetView extends AppCompatActivity {

    private String TABLE_EXERCISE;
    private String TABLE_WORKOUT;
    private String TABLE_SET;
    private String ICON_DIRECTORY;
    private DBHandler DB;

    private Exercise exercise;
    private Workout workout;

    private ArrayList<Set> setList;

    private Menu menu;
    private Toolbar toolbar;
    private TextView header1;
    private TextView header2;
    private ImageView icon;
    private TextView note;
    private DynamicListView list;
    private TextView message;
    private FloatingActionButton repeatLastSetButton;
    private FloatingActionButton newSetButton;

    private static final String logtag = "SetView";

    private SharedPreferences sharedPreferences;
    private String prefs;

    AdRequest adRequest;
    private AdView mAdView;

    IInAppBillingService mService;


    // ------------------------------------------------------------------------
    // OVERRIDE METHODS
    // ------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(new ThemeHandler(this).getActivityTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__set_view);


        TABLE_EXERCISE = getString(R.string.TABLE_EXERCISE);
        TABLE_WORKOUT = getString(R.string.TABLE_WORKOUT);
        TABLE_SET = getString(R.string.TABLE_SET);
        ICON_DIRECTORY = getString(R.string.ICON_DIRECTORY);

        DB = new DBHandler(getApplicationContext());


        toolbar = (Toolbar) findViewById(R.id.toolbar);

        icon = (ImageView) findViewById(R.id.icon);
        note = (TextView) findViewById(R.id.note);
        list = (DynamicListView) findViewById(R.id.List);
        message = (TextView) findViewById(R.id.message);
        repeatLastSetButton = (FloatingActionButton) findViewById(R.id.fab2);
        newSetButton = (FloatingActionButton) findViewById(R.id.fab);
        mAdView = (AdView) findViewById(R.id.adView);

        prefs = getString(R.string.shared_preferences);
        sharedPreferences = getSharedPreferences(prefs, MODE_PRIVATE);


        setupToolbar();

        // ------------------------------------------------------------------------
        // Gets the workout ID from the passed intent
        // ------------------------------------------------------------------------
        Intent i = getIntent();
        workout = new Workout(i.getLongExtra("Workout_ID", 99999), null, null);
    }


    public void requestBackup(){
        if (sharedPreferences.getBoolean("backup", false)){
            BackupManager bm = new BackupManager(this);
            bm.dataChanged();
            Log.i(logtag, "Backup requested");
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        requestBackup();

        // ------------------------------------------------------------------------
        // Copies the order of the sets in the listview to the database as they
        // may have been dragged and dropped by the user.
        // ------------------------------------------------------------------------
        new pauseActivity().execute();


    }

    @Override
    public void onResume() {
        super.onResume();
        refreshPage();

        mAdView.setVisibility(View.GONE);
        //
        //if (adRequest == null){
        //    new setupAd().execute();
        //}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_set_view, menu);
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

    // ------------------------------------------------------------------------
    // MAIN METHODS
    // ------------------------------------------------------------------------

    public class setupAd extends AsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAdView.setVisibility(View.GONE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            // ------------------------------------------------------------------------
            // Sets up the banner ad on the page
            // ------------------------------------------------------------------------
            SharedPreferences sp = getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE);
            if (!sp.getBoolean(getString(R.string.shared_preferences_pro), false)) {
                MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.AppID));
                adRequest = new AdRequest.Builder()
                        .addTestDevice(getResources().getString(R.string.test_device_id))
                        .build();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (adRequest != null){
                mAdView.setVisibility(View.VISIBLE);
                mAdView.loadAd(adRequest);
            }
        }
    }

    private void setupToolbar() {
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditWorkout();
            }
        });

        setSupportActionBar(toolbar);

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);
    }

    public void saveSetOrder(){
        // ------------------------------------------------------------------------
        // Edits each set row in the database from this workout. Changes their
        // current position and number to match that of the ones in the current
        // list. This is as the user can change the order manually.
        // ------------------------------------------------------------------------
        ArrayList<Set> sets = list.mList;
        DB.open();
        if (sets != null) {
            for (int i = 0; i < sets.size(); i++) {
                Log.v("", "number: " + String.valueOf(sets.get(i).getNumber()) + "  Position: " + String.valueOf(sets.get(i).getPosition()));

                ContentValues cv = new ContentValues();
                cv.put("number", sets.get(i).getNumber());
                cv.put("position", sets.get(i).getPosition());
                DB.editRow(TABLE_SET, cv, sets.get(i).getID());
            }
        }
        DB.close();
    }

    public class pauseActivity extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            saveSetOrder();

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

        }
    }

    public void refreshPage(){
        refreshWorkout();
        new refreshList().execute();
    }

    public void refreshWorkout(){
        // ------------------------------------------------------------------------
        // Get the Workout, and the ID of the exercise from the database
        // Closes the page if none is found to match ID
        // ------------------------------------------------------------------------
        DB.open();
        Cursor c = DB.getRow(TABLE_WORKOUT, workout.getID(), new String[] {"_id", "exercise_id", "date", "note"});
        if (c.moveToFirst()){
            exercise = new Exercise(null, null, c.getLong(1), null, null, null, null, null, null);
            workout = new Workout(c.getLong(0), new CustomCalendar(c.getString(2)), c.getString(3));
            c.close();

            // ------------------------------------------------------------------------
            // Get the exercise from the database using the  just retrieved exercise ID
            // ------------------------------------------------------------------------
            c = DB.getRow(TABLE_EXERCISE, exercise.getID(), new String[] {"_id", "exercise_name", "icon_path", "unit"});
            c.moveToFirst();
            exercise = new Exercise(null, null, c.getLong(0), c.getString(1), c.getString(2), c.getString(3), null, null, null);
            c.close();

            // ------------------------------------------------------------------------
            // Set the page headers to match the exercise and workout info
            // ------------------------------------------------------------------------
            getSupportActionBar().setTitle(exercise.getName());
            getSupportActionBar().setSubtitle(workout.getDate().toLongString(1,1,1,2));


            if (workout.getNote() != null){
                note.setText(workout.getNote());
            }
            else{
                note.setText("(Click to add)");
            }

            try {
                setExerciseIcon();
            } catch (IOException e) {
                e.printStackTrace();
            }
            DB.close();

        }
        else{
            c.close();
            DB.close();
            finish();
        }

    }

    public void setExerciseIcon() throws IOException {
        icon.setVisibility(View.GONE);
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

    public class refreshList extends AsyncTask{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            repeatLastSetButton.setVisibility(View.GONE);
            message.setText(R.string.loading);
            message.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            refreshSetsFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            refreshListView();
        }
    }

    public void refreshSetsFromDatabase(){
        Cursor c;
        setList = new ArrayList<Set>();

        DB.open();

        c = DB.getAllRows(TABLE_SET, new String[] {"_id", "position", "number", "weight", "reps", "note", "is_warmup"}, "workout_id = " + workout.getID());
        c.moveToPosition(-1);
        while (c.moveToNext()){
            setList.add(new Set(c.getLong(0), c.getInt(1), c.getInt(2), new BigDecimal(c.getString(3)), c.getInt(4), c.getString(5), (c.getInt(6) != 0)));
        }
        c.close();

        DB.close();

        setList = reNumberSets(setList);
    }

    public void refreshListView(){
        //------------------------------------------------------------------------------
        // Remember the current scroll position of the list
        //------------------------------------------------------------------------------
        Parcelable listState = list.onSaveInstanceState();


        list.setAdapter(new SetListAdapter(SetView.this, R.layout.list_set_row, setList, exercise.getUnit()));
        list.setList(setList);
        list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        list.setOnItemClickListener(itemSelected);


        //------------------------------------------------------------------------------
        // Restore position of list
        //------------------------------------------------------------------------------
        if (listState != null){
            list.onRestoreInstanceState(listState);
        }


        if (setList.size() <= 0){
            message.setText(R.string.NoSets);
            message.setVisibility(View.VISIBLE);
        }
        else{
            message.setVisibility(View.GONE);
            repeatLastSetButton.setVisibility(View.VISIBLE);
        }
    }

    public static ArrayList<Set> orderByPosition(ArrayList<Set> sets) {
        // ------------------------------------------------------------------------
        // Order Sets in ArrayList according to position
        // ------------------------------------------------------------------------
        for (int i = 1; i < sets.size(); i++) {
            Set x = sets.get(i);
            int j = i;
            while ((j > 0) && (sets.get(j - 1).getPosition() > x.getPosition())) {
                sets.set(j, sets.get(j - 1));
                j = j - 1;
            }
            sets.set(j, x);
        }
        return sets;
    }

    public ArrayList<Set> reNumberSets(ArrayList<Set> sets){

        sets = orderByPosition(sets);

        // ------------------------------------------------------------------------
        // Change each sets position value and number value to refreshed
        // consecutive values to fill in any gaps left by deleted sets.
        // eg {1,3,4,5,7} -> {1,2,3,4,5}
        // Also update database with these values as well as list
        // ------------------------------------------------------------------------
        int setNumber = 1;
        for (int i = 0; i < sets.size(); i++){

            ContentValues cv = new ContentValues();

            int newNumber = i + 1;
            sets.get(i).setPosition(newNumber);
            cv.put("position", newNumber);

            // ------------------------------------------------------------------------
            // The number value for warm up sets is always 0
            // ------------------------------------------------------------------------
            if (sets.get(i).isWarmUp()){
                sets.get(i).setNumber(0);
                cv.put("number", 0);
            }
            else{
                sets.get(i).setNumber(setNumber);
                cv.put("number", setNumber);
                setNumber++;
            }

            DB.open();
            DB.editRow(TABLE_SET, cv, sets.get(i).getID());
            DB.close();
       }

       return sets;
    }

    public void gotoNewSet(View view){
        Intent i = new Intent(this, edit_set.class);
        i.putExtra("EditMode", false);
        i.putExtra("Workout_ID", workout.getID());
        startActivity(i);
    }

    public AdapterView.OnItemClickListener itemSelected = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            itemSelected(position);
        }
    };


    public void itemSelected(int pos){
        Intent i = new Intent(this, edit_set.class);
        i.putExtra("EditMode", true);
        i.putExtra("Set_ID", setList.get(pos).getID());
        startActivity(i);
    }

    public void openEditNote(View view){
        Intent i = new Intent(this, edit_note.class);
        i.putExtra("Workout_ID", workout.getID());
        startActivity(i);
    }

    public void openEditWorkout(){
        Intent i = new Intent(this, edit_workout.class);
        i.putExtra("Workout_ID", workout.getID());
        startActivity(i);
    }

    public void openEditWorkout(View v){
        openEditWorkout();
    }

    public class repeatLastSetButton extends AsyncTask{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            repeatLastSetButton.setClickable(false);
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            repeatLastSet();
            refreshSetsFromDatabase();
            return null;
        }


        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            refreshListView();
            list.smoothScrollToPosition(list.getCount() - 1);
            repeatLastSetButton.setClickable(true);
        }


    }

    public void repeatLastSet(){
        Set lastSet = setList.get(setList.size() - 1);

        String weightString = lastSet.getWeight().setScale(2).toPlainString();

        // ------------------------------------------------------------------------
        // Updates exercise row with the new 'last weight used' depending on
        // if warm up or not.
        // ------------------------------------------------------------------------
        DB.open();

        ContentValues cv = new ContentValues();
        if (lastSet.isWarmUp()){
            cv.put("warmup_weight", weightString);
        }
        else{
            cv.put("working_weight", weightString);
        }
        DB.editRow(TABLE_EXERCISE, cv, exercise.getID());


        cv = new ContentValues();
        cv.put("exercise_id", exercise.getID());
        cv.put("workout_id", workout.getID());
        cv.put("weight", weightString);
        cv.put("reps", lastSet.getReps());
        cv.put("is_warmup", lastSet.isWarmUp()? 1 : 0);
        cv.put("position", lastSet.getPosition() + 1);

        // ------------------------------------------------------------------------
        // Number value for warm up sets is always 0
        // ------------------------------------------------------------------------
        if (lastSet.isWarmUp()){
            cv.put("number", 0);
        }
        else{
            cv.put("number", lastSet.getNumber() + 1);
        }

        DB.insertRow(TABLE_SET, cv);

        DB.close();
    }

    public void repeatLastSetButton(View v){
        repeatLastSetButton.setClickable(false);
        new repeatLastSetButton().execute();
    }

    public void getpro(View view){
        ((ExerciseView) getParent().getParent()).buyPro();
    }

}
