package sam.gymnotes.dialogs;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import sam.gymnotes.DBHandler;
import sam.gymnotes.Exercise;
import sam.gymnotes.R;
import sam.gymnotes.Set;
import sam.gymnotes.ThemeHandler;
import sam.gymnotes.Workout;


public class edit_set extends Activity {

    private static final String log = "New Set Dialog";

    private String TABLE_EXERCISE;
    private String TABLE_WORKOUT;
    private String TABLE_SET;
    private DBHandler DB;

    private Boolean editMode;

    private Exercise exercise;
    private Workout workout;
    private Set editSet;

    private TextView header;
    private ToggleButton isWarmup;
    private NumberPicker weight;
    private NumberPicker reps;
    private EditText note;
    private ImageView delete;
    private TextView errorMessage;
    private TextView finish;

    private static final BigDecimal maxWeight = new BigDecimal("500");
    private static final int maxReps = 999;
    private String[] weights;
    private String[] repRange;

    private Integer workingPos;
    private Integer warmupPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(new ThemeHandler(this).getDialogTheme());
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_new_set);
        this.setFinishOnTouchOutside(false);

        TABLE_EXERCISE = getString(R.string.TABLE_EXERCISE);
        TABLE_WORKOUT = getString(R.string.TABLE_WORKOUT);
        TABLE_SET = getString(R.string.TABLE_SET);

        Intent i = getIntent();
        editMode = i.getBooleanExtra("EditMode", false);

        DB = new DBHandler(getApplicationContext());

        header = (TextView) findViewById(R.id.Header);
        isWarmup = (ToggleButton) findViewById(R.id.isWarmup);
        weight = (NumberPicker) findViewById(R.id.weight);
        reps = (NumberPicker) findViewById(R.id.reps);
        note = (EditText) findViewById(R.id.note);
        delete = (ImageView) findViewById(R.id.delete);
        errorMessage = (TextView) findViewById(R.id.Error);
        finish = (TextView) findViewById(R.id.FinishButton);

        weight.setWrapSelectorWheel(false);
        errorMessage.setVisibility(View.GONE);


        // ------------------------------------------------------------------------
        // Get colors from current theme
        // ------------------------------------------------------------------------
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        int colorAccent = typedValue.data;

        setDividerColor(weight, colorAccent);
        setDividerColor(reps, colorAccent);


        getDatabaseData();
        fillViews();

        note.requestFocus();
        note.setSelection(note.getText().length());
    }

    @Override
    protected void onPause(){
        super.onPause();
    }
    @Override
    protected void onResume(){
        super.onResume();
    }

    public void getDatabaseData(){
        DB.open();

        Cursor c;
        Intent i = getIntent();


        if (editMode){
            editSet = new Set(i.getLongExtra("Set_ID", 99999), null, null, null, null, null, null);

            c = DB.getRow(TABLE_SET, editSet.getID(), new String[]{"_id", "exercise_id", "workout_id", "position", "number", "weight", "reps", "note", "is_warmup"});
            c.moveToFirst();
            exercise = new Exercise(null, null, c.getLong(1), null, null, null, null, null, null);
            workout = new Workout(c.getLong(2), null, null);
            editSet = new Set(c.getLong(0), c.getInt(3), c.getInt(4), new BigDecimal(c.getString(5)), c.getInt(6), c.getString(7), (c.getInt(8) != 0));
            c.close();
        }
        else{
            workout = new Workout(i.getLongExtra("Workout_ID", 99999), null, null);
            c = DB.getRow(TABLE_WORKOUT, workout.getID(), new String[] {"_id", "exercise_id"});
            c.moveToFirst();
            exercise = new Exercise(null, null, c.getLong(1), null, null, null, null, null, null);
            c.close();
        }


        c = DB.getRow(TABLE_EXERCISE, exercise.getID(), new String[] {"_id", "exercise_name", "icon_path", "unit", "increment", "warmup_weight", "working_weight"});
        c.moveToFirst();

        BigDecimal warmup = null;
        BigDecimal working = null;
        if (c.getString(5) != null){
            warmup = new BigDecimal(c.getString(5));
        }
        if (c.getString(6) != null){
            working = new BigDecimal(c.getString(6));
        }

        exercise = new Exercise(null, null, c.getLong(0), c.getString(1), c.getString(2), c.getString(3), new BigDecimal(c.getString(4)), warmup, working);
        c.close();

        DB.close();
    }

    public void fillViews(){
        setReps();


        // ------------------------------------------------------------------------
        // For editing set
        // ------------------------------------------------------------------------
        if (editMode){
            header.setText(R.string.EditSet);
            isWarmup.setChecked(editSet.isWarmUp());

            setWeight(editSet.getWeight());

            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
                reps.setValue(maxReps - editSet.getReps());
            }
            else {
                reps.setValue(editSet.getReps());
            }

            note.setText(editSet.getNote());
            finish.setText(R.string.Done);
        }

        // ------------------------------------------------------------------------
        // For a new set
        // ------------------------------------------------------------------------
        else{
            delete.setVisibility(View.GONE);

            header.setText(R.string.NewSet);
            isWarmup.setChecked(false);

            if (isWarmup.isChecked() && exercise.getWarmupWeight() != null){
                setWeight(exercise.getWarmupWeight());
            }
            else if (!isWarmup.isChecked() && exercise.getWorkingWeight() != null){
                setWeight(exercise.getWorkingWeight());
            }
            else{
                setWeight(null);
            }




            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
                reps.setValue(repRange.length-1);
            }
            else {
                reps.setValue(0);
            }

            finish.setText("Create");
        }

    }

    private void setReps() {

        repRange = new String[maxReps+1];

        for (int i=0; i<repRange.length; i++){
            repRange[i] = Integer.valueOf(i).toString();
        }


        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
            List<String> list = Arrays.asList(repRange);
            Collections.reverse(list);
            repRange = (String[]) list.toArray();
        }

        reps.setDisplayedValues(null);
        reps.setMinValue(0);
        reps.setMaxValue(repRange.length -1);
        reps.setWrapSelectorWheel(false);
        reps.setDisplayedValues(repRange);
    }

    public void warmup(View view){
        if (isWarmup.isChecked()){
            workingPos =  weight.getValue();

            if (warmupPos != null){
                weight.setValue(warmupPos);
            }
            else{
                setWeight(exercise.getWarmupWeight());
            }
        }
        else{
            warmupPos =  weight.getValue();

            if (workingPos != null){
                weight.setValue(workingPos);
            }
            else{
                setWeight(exercise.getWorkingWeight());
            }
        }
    }

    public void setWeight(BigDecimal insertWeight){
        // ------------------------------------------------------------------------
        // Creates list of possible weights, and adds in the given value
        // ------------------------------------------------------------------------

        weights = getWeightIncrements();
        if (insertWeight != null) {
            weights = insertWeight(weights, insertWeight);
        }



        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            List<String> listWeights = Arrays.asList(weights);
            Collections.reverse(listWeights);
            weights = (String[]) listWeights.toArray();
        }
        weight.setDisplayedValues(null);
        weight.setMinValue(0);
        weight.setMaxValue(weights.length -1);
        weight.setWrapSelectorWheel(false);
        weight.setDisplayedValues(weights);



        if (insertWeight != null){
            weight.setValue(indexOf(weights, insertWeight.setScale(2).toPlainString() + " " + exercise.getUnit()));
        }
        else{
            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                weight.setValue(weight.getMaxValue());
            }
            else{
                weight.setValue(weight.getMinValue());
            }
        }
    }

    public int indexOf(String[] list, String find){
        for (int i = 0; i < list.length; i++){
            if (list[i].equals(find)) {
                return i;
            }
        }
        return 0;
    }

    public String[] getWeightIncrements(){
        ArrayList<String> list = new ArrayList<String>();
        BigDecimal count = BigDecimal.ZERO;

        while (maxWeight.compareTo(count) != -1){
            list.add(count.setScale(2, RoundingMode.UNNECESSARY).toPlainString() + " " + exercise.getUnit());
            count = count.add(exercise.getIncrement());
        }

        String[] listDone = new String[list.size()];
        listDone = list.toArray(listDone);

        return listDone;
    }

    public String[] insertWeight(String[] list, BigDecimal insert){

        ArrayList<String> doneList = new ArrayList<String>();
        Boolean inserted = false;

        for (int i = 0; i < list.length; i++){
            BigDecimal listItem = new BigDecimal(list[i].substring(0, list[i].indexOf(" ")));

            if (!inserted && insert.compareTo(listItem) != 1){
                inserted = true;
                if (insert.compareTo(listItem) != 0){
                    doneList.add(insert.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " " + exercise.getUnit());
                }
            }

            doneList.add(list[i]);
        }

        String[] l = new String[doneList.size()];
        return doneList.toArray(l);
    }

    public int findNextNumber(String column){

        Cursor c = DB.getAllRows(TABLE_SET, new String[] {"_id", column}, "workout_id = " + workout.getID());
        c.moveToPosition(-1);
        int lastPos = 0;
        while (c.moveToNext()){
            int check = c.getInt(1);
            if (check > lastPos){
                lastPos = check;
            }
        }
        c.close();
        return lastPos + 1;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == 1 && resultCode == RESULT_OK){
            DB.open();
            DB.deleteRow(TABLE_SET, editSet.getID());
            DB.close();
            finish();
        }
    }

    public void delete(View view){
        Intent i = new Intent(this, are_you_sure.class);
        i.putExtra("header", "Delete Set");
        i.putExtra("message", "Are you sure you would like to delete this Set?");
        i.putExtra("YES", "Delete");
        i.putExtra("NO", "Cancel");
        startActivityForResult(i, 1);
    }

    public void cancel(View view){
        finish();
    }

    public void finishButton(View view){
        String weightString = weights[weight.getValue()];
        weightString = weightString.substring(0, weightString.indexOf(" "));
        Boolean warmup = isWarmup.isChecked();

        // Gets note text if trimmed length > 0
        String NOTE = null;
        if (note.getText().toString().trim().length() > 0){
            NOTE = note.getText().toString().trim();
        }

        DB.open();

        // Assigns weight in correct table column depending on isWarmup
        ContentValues cv = new ContentValues();
        if (warmup){
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
        cv.put("reps", Integer.parseInt(repRange[reps.getValue()]));
        cv.put("note", NOTE);
        cv.put("is_warmup", warmup? 1 : 0);

        // If warmup, set number is 0
        if (warmup){
            cv.put("number", 0);
        }

        // Sets position and number are assigned
        if (editMode){
            cv.put("position", editSet.getPosition());
            if (!warmup){
                cv.put("number", editSet.getNumber());
            }
            DB.editRow(TABLE_SET, cv, editSet.getID());
        }
        else{
            cv.put("position", findNextNumber("position"));
            if (!warmup){
                cv.put("number", findNextNumber("number"));
            }
            DB.insertRow(TABLE_SET, cv);
        }

        DB.close();
        finish();
    }

    private void setDividerColor(NumberPicker picker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

}
