package sam.gymnotes;

import android.content.Context;
import android.database.Cursor;
import android.widget.CheckBox;

import java.math.BigDecimal;

public class Exercise {

    private Context APPLICATION_CONTEXT;
    private String TABLE_WORKOUT;


    private long database_ID;
    private String name;
    private String iconPath;
    private String unit;
    private BigDecimal increment;
    private BigDecimal warmupWeight;
    private BigDecimal workingWeight;

    private CheckBox checkbox;

    private CustomCalendar lastWorkout;


    public Exercise(Context app_context, String workout_table, long ID, String NAME, String ICONPATH, String UNIT, BigDecimal INCREMENT, BigDecimal WARMUP, BigDecimal WORKING){
        APPLICATION_CONTEXT = app_context;
        TABLE_WORKOUT = workout_table;

        database_ID = ID;
        name = NAME;
        iconPath = ICONPATH;
        unit = UNIT;
        increment = INCREMENT;
        warmupWeight = WARMUP;
        workingWeight = WORKING;

        if (APPLICATION_CONTEXT != null && TABLE_WORKOUT != null){
            lastWorkout = setLastWorkout();
        }
    }

    public Exercise(long ID, String NAME){
        database_ID = ID;
        name = NAME;
    }

    public Exercise(long ID, String NAME, String ICONPATH){
        database_ID = ID;
        name = NAME;
        iconPath = ICONPATH;
    }

    public void setID(long newID){
        database_ID = newID;
    }
    public long getID(){
        return database_ID;
    }

    public void setName(String newName){
        name = newName;
    }
    public String getName(){
        return name;
    }

    public String getIconPath(){
        return iconPath;
    }
    public void setIconPath(String newIconPath){
        iconPath = newIconPath;
    }

    public String getUnit(){
        return unit;
    }
    public void setUnit(String newUnit){
        unit = newUnit;
    }

    public BigDecimal getIncrement(){
        return increment;
    }
    public void setIncrement(BigDecimal newIncrement){
        increment = newIncrement;
    }

    public BigDecimal getWarmupWeight(){
        return warmupWeight;
    }
    public void setWarmupWeight(BigDecimal newWarmupWeight){
        warmupWeight = newWarmupWeight;
    }

    public BigDecimal getWorkingWeight(){
        return workingWeight;
    }
    public void setWorkingWeight(BigDecimal newWorkingWeight){
        workingWeight = newWorkingWeight;
    }

    public void addCheckbox(CheckBox CHECKBOX){
        checkbox = CHECKBOX;
    }

    public CheckBox getCheckbox(){
        return checkbox;
    }

    public CustomCalendar getLastWorkout(){
        return lastWorkout;
    }

    public CustomCalendar setLastWorkout(){
        DBHandler DB = new DBHandler(APPLICATION_CONTEXT);
        DB.open();

        Cursor c = DB.getAllRows(TABLE_WORKOUT, new String[] {"_id", "date"}, "exercise_id = " + database_ID);
        c.moveToPosition(-1);

        CustomCalendar LAST = new CustomCalendar(1900, 0, 1);

        while (c.moveToNext()){
            CustomCalendar THIS = new CustomCalendar(c.getString(1));

            if (THIS.after(LAST)){
                LAST = THIS;
            }
        }
        c.close();
        DB.close();

        if (LAST.equals(new CustomCalendar(1900, 0, 1))){
            return null;
        }
        else{
            return LAST;
        }
    }

}
