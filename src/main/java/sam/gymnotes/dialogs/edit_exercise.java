package sam.gymnotes.dialogs;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import sam.gymnotes.DBHandler;
import sam.gymnotes.Exercise;
import sam.gymnotes.R;
import sam.gymnotes.ThemeHandler;
import sam.gymnotes.activities.IconChooser;


public class edit_exercise extends Activity {

    private String TABLE_GROUP_EXERCISES;
    private String TABLE_EXERCISE;
    private String TABLE_WORKOUT;
    private String TABLE_SET;
    private String ICON_DIRECTORY;
    private DBHandler DB;

    private Exercise exercise;

    private Boolean editMode;
    private Long groupID;

    private TextView header;
    private EditText insertName;
    private ImageView selectedImage;
    private Spinner spinnerUnit;
    private Spinner insertIncrement;
    private TextView finishButton;
    private TextView errorMessage;
    private TextView noIcon;

    private static final int maxChars = 25;
    private String iconPath;
    private String[] units;
    private String[] increments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(new ThemeHandler(this).getDialogTheme());
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_new_exercise);
        this.setFinishOnTouchOutside(false);

        TABLE_GROUP_EXERCISES = getString(R.string.TABLE_GROUP_EXERCISES);
        TABLE_EXERCISE = getString(R.string.TABLE_EXERCISE);
        TABLE_WORKOUT = getString(R.string.TABLE_WORKOUT);
        TABLE_SET = getString(R.string.TABLE_SET);
        ICON_DIRECTORY = getString(R.string.ICON_DIRECTORY);
        units = getResources().getStringArray(R.array.units);

        DB = new DBHandler(getApplicationContext());

        Intent i = getIntent();
        editMode = i.getBooleanExtra("EditMode", true);

        header = (TextView) findViewById(R.id.Header);
        insertName = (EditText) findViewById(R.id.insertName);
        selectedImage = (ImageView) findViewById(R.id.icon);
        spinnerUnit = (Spinner) findViewById(R.id.spinnerUnit);
        insertIncrement = (Spinner) findViewById(R.id.spinnerIncrement);
        finishButton = (TextView) findViewById(R.id.FinishButton);
        errorMessage = (TextView) findViewById(R.id.ErrorMessage);
        noIcon = (TextView) findViewById(R.id.noIcon);

        errorMessage.setVisibility(View.GONE);


        // ------------------------------------------------------------------------
        // Get colors from current theme
        // ------------------------------------------------------------------------
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        int colorAccent = typedValue.data;

        insertName.getBackground().mutate().setColorFilter(colorAccent, PorterDuff.Mode.SRC_ATOP);

        // Create Drop down options for unit
        ArrayAdapter AA = new ArrayAdapter(this, R.layout.spinner_layout, units);
        AA.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        spinnerUnit.setAdapter(AA);

        // Create drop down options for increment
        increments = getIncrements(new BigDecimal("0.25"), new BigDecimal("10.00"));
        AA = new ArrayAdapter(this, R.layout.spinner_layout, increments);
        AA.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        insertIncrement.setAdapter(AA);
        insertIncrement.setSelection(indexOf(increments, "2.50"));

        // For edit mode
        if (editMode){
            exercise = new Exercise(null, null, i.getLongExtra("Exercise_ID", 9999999), null, null, null, null, null, null);
            header.setText("Edit Exercise");
            finishButton.setText("Done");
            fillFields();
            insertName.setSelection(insertName.getText().toString().length() - 1);
        }
        // For new exercise mode
        else{
            header.setText("New Exercise");
            finishButton.setText("Create");
            ImageView I = (ImageView) findViewById(R.id.delete_button);
            I.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    public String[] getIncrements(BigDecimal inc, BigDecimal upto){
        ArrayList<String> x = new ArrayList<String>();
        BigDecimal next = inc;
        while (next.compareTo(upto) != 1){
            x.add(next.divide(BigDecimal.ONE, 2, RoundingMode.UNNECESSARY).toPlainString());
            next = next.add(inc);
        }

        String[] x1 = new String[x.size()];
        return x.toArray(x1);
    }

    public void fillFields(){

        DB.open();

        Cursor c = DB.getAllRows(TABLE_EXERCISE, new String[] {"_id", "exercise_name", "icon_path", "unit", "increment"}, "_id = " + exercise.getID());
        c.moveToPosition(0);
        exercise = new Exercise(null, null, c.getLong(0), c.getString(1), c.getString(2), c.getString(3), new BigDecimal(c.getString(4)), null, null);

        // Set Exercise Name
        insertName.setText(exercise.getName());
        //insertName.setSelection(insertName.getText().length());

        // Set Exercise image
        if (exercise.getIconPath() != null) {
            try {
                selectedImage.setImageDrawable(Drawable.createFromStream(getAssets().open(ICON_DIRECTORY + "/black/" + exercise.getIconPath()), null));
                iconPath = exercise.getIconPath();
                noIcon.setVisibility(View.GONE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            noIcon.setVisibility(View.VISIBLE);
        }

        // Set Exercise unit on spinner
        for (int i = 0; i < units.length; i++){
            if (c.getString(3).equals(units[i])){
                spinnerUnit.setSelection(i);
                break;
            }
        }

        //Set Excersise increment
        insertIncrement.setSelection(indexOf(increments, c.getString(4)));

        c.close();
        DB.close();
    }

    public int indexOf(String[] list, String find){
        for (int i = 0; i < list.length; i++){
            if (list[i].equals(find)) {
                return i;
            }
        }
        return 0;
    }

    public void gotoIconChooser(View view){
        Intent i = new Intent(getApplicationContext(), IconChooser.class);
        startActivityForResult(i, 1);
    }

    public void gotoAreYouSure(View view){
        Intent i = new Intent(getApplicationContext(), are_you_sure.class);
        i.putExtra("header", "Archive " + exercise.getName() + "?");
        i.putExtra("message", "Are you sure you want to archive this Exercise? It can be restored or permanently deleted from the archive menu later.");
        i.putExtra("NO", "Cancel");
        i.putExtra("YES", "Archive");
        startActivityForResult(i, 2);
    }

    public void gotoConvert(String oldUnit, String newUnit){
        Intent i = new Intent(getApplicationContext(), ThreeOption.class);
        i.putExtra("header", getString(R.string.ConvertQuestion));
        i.putExtra("message", String.format(getString(R.string.ConvertMessage), oldUnit, newUnit));
        i.putExtra("NO", getString(R.string.Cancel));
        i.putExtra("MAYBE", getString(R.string.Dontconvert));
        i.putExtra("YES", getString(R.string.convert));
        startActivityForResult(i, 3);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String path = data.getStringExtra("IconPath");
            try {
                selectedImage.setImageDrawable(Drawable.createFromStream(getAssets().open(ICON_DIRECTORY + "/black/" + path), null));
                iconPath = path;
                noIcon.setVisibility(View.GONE);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        else if (requestCode == 2 && resultCode == RESULT_OK){
            archiveExercise();
        }

        else if(requestCode == 3){
            if (resultCode == RESULT_OK){
                editDB(true);
            }
            else if (resultCode == RESULT_FIRST_USER){
                editDB(false);
            }
        }
    }

    public void convertWeights(String oldUnit, String newUnit){
        BigDecimal unitRatio = new BigDecimal(getResources().getString(R.string.LbKgRatio));
        BigDecimal from;
        BigDecimal to;

        if (oldUnit.equals("kg")){
            from = BigDecimal.ONE;
            to = unitRatio;
        }
        else{
            from = unitRatio;
            to = BigDecimal.ONE;
        }

        if (!from.equals(to)) {
            BigDecimal ratio = to.divide(from, 8, BigDecimal.ROUND_HALF_EVEN);

            Cursor c = DB.getAllRows(TABLE_SET, new String[]{"_id", "weight"}, "exercise_id = " + exercise.getID());
            c.moveToPosition(-1);
            ContentValues cv = new ContentValues();
            while (c.moveToNext()) {
                BigDecimal oldWeight = new BigDecimal(c.getString(1));
                BigDecimal newWeight = oldWeight.multiply(ratio).setScale(2, RoundingMode.HALF_UP);

                cv.put("weight", newWeight.toPlainString());
                DB.editRow(TABLE_SET, cv, c.getLong(0));
            }

            c.close();


        }
    }

    public boolean checkFields(){
        // Clears and hides error message
        errorMessage.setVisibility(View.GONE);
        errorMessage.setText("");

        String exerciseName = insertName.getText().toString();
        ArrayList<String> mes = new ArrayList<String>();
        boolean okay = true;

        // Checks exercise name for correct length
        if (exerciseName.trim().length() < 1){
            mes.add("Please insert an exercise name.");
            okay = false;
        }
        else if (exerciseName.length() > maxChars){
            mes.add("Exercise name cannot be more than " + maxChars + " characters.");
            okay = false;
        }

        // Sends error message and makes it visible
        if (!okay){
            String m = "";
            for (int i = 0; i < mes.size(); i++){
                if (i != 0){
                    m += "\n";
                }
                m += mes.get(i);
            }
            errorMessage.setVisibility(View.VISIBLE);
            errorMessage.setText(m);
        }

        return okay;
    }

    public void archiveExercise(){
        DB.open();

        ContentValues cv = new ContentValues();
        cv.put("archived", 1);

        DB.editRow(TABLE_EXERCISE, cv, exercise.getID());

        DB.close();
        finish();
    }

    public void cancel(View view){
        finish();
    }

    public void done(View view){
        if (checkFields()){
            String oldUnit;
            String newUnit = spinnerUnit.getSelectedItem().toString();

            if (editMode && !newUnit.equals(oldUnit = exercise.getUnit())){
                gotoConvert(oldUnit, newUnit);
            }
            else{
                editDB(false);
            }
        }
    }

    public void editDB(Boolean needToConvert){
        String exerciseName = insertName.getText().toString();
        String increment = insertIncrement.getSelectedItem().toString();
        String newUnit = spinnerUnit.getSelectedItem().toString();

        ContentValues cv = new ContentValues();
        cv.put("exercise_name", exerciseName);
        cv.put("icon_path", iconPath);
        cv.put("unit", newUnit);
        cv.put("increment", increment);


        Long exerciseID = null;
        DB.open();

        if (editMode){
            if (needToConvert){
                convertWeights(exercise.getUnit(), newUnit);
            }
            DB.editRow(TABLE_EXERCISE, cv, exercise.getID());
            exerciseID = exercise.getID();
        }
        else{
            exerciseID = DB.insertRow(TABLE_EXERCISE, cv);
        }

        DB.close();

        Intent i = new Intent();
        i.putExtra("Exercise_ID", exerciseID);
        setResult(RESULT_OK, i);

        finish();
    }



}
