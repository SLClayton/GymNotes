package sam.gymnotes;

import android.content.ContentValues;
import android.content.Context;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by Sam on 23/09/2016.
 */

public class exampleGenerator {

    private String[] setComments = {
            "That was easy.",
            "Felt pretty hard.",
            "Needed a spotter on the last.",
            "Felt like I could do 1 more.",
            "Easy",
            "Woah that was tough.",
            "Could have tried harder",
            "Feeling good",
            "People saw that",
            "Strained a bit at the end there.",
            "Felt a bit unbalanced",
            "Feel really good",
            "wow",
            "my right side felt a bit weaker",
            "Last one was spotted a little bit",
            "Last one was spotted quite a lot",
            "Hard",

    };

    private String[] workoutComments = {
            "Felt really strong today",
            "Felt pretty tired",
            "Morning before the big exam, nervous!",
            "Some guy kept staring at me and it started to put me off",
            "Forgot my water, dammit!",
            "Wow",
            "Today is going to be a good day",
            "First workout back from holiday!",
            "In a bit of a rush today",
            "Took me ages to get things done",
            "Forgot my water today",
            "First day on the new programme!",
            "Left the gym early today",
            "Gonna try and go heavy today",
            "Training with Jack today",
            "Training with Dan today",
            "Trying out a new pre-workout"
    };


    DBHandler DB;

    private String TABLE_EXERCISE;
    private String TABLE_WORKOUT;
    private String TABLE_SET;
    private String ICON_DIRECTORY;

    private Context context;

    private String name;
    private BigDecimal startingWeight;
    private String iconPath;
    private int startDaysAgo;
    private BigDecimal increment = new BigDecimal("2.50");



    public exampleGenerator(Context CONTEXT, String NAME, BigDecimal STARTINGWEIGHT, String ICONPATH, int STARTDAYSAGO){
        context = CONTEXT;
        name = NAME;
        startingWeight = STARTINGWEIGHT;
        iconPath = ICONPATH;
        startDaysAgo = STARTDAYSAGO;


        TABLE_EXERCISE = context.getString(R.string.TABLE_EXERCISE);
        TABLE_WORKOUT = context.getString(R.string.TABLE_WORKOUT);
        TABLE_SET = context.getString(R.string.TABLE_SET);
        ICON_DIRECTORY = context.getString(R.string.ICON_DIRECTORY);
        DB = new DBHandler(context);
    }

    public int random(int low, int high){
        return new Random().nextInt(high) + low;
    }

    public boolean prob(int numerator, int denominator){
        int number = random(1, denominator);
        if (number <= numerator){
            return true;
        }
        return false;
    }

    public String getSetNote(){
        return setComments[random(0, setComments.length-1)];
    }

    public String getWorkoutNote(){
        return workoutComments[random(0, workoutComments.length-1)];
    }

    public int getNumberOfSets(){
        int numberOfSets = 3;
        if (prob(1,2)){
            if (prob(4,5)){
                numberOfSets = 4;
            }
            else{
                numberOfSets = 5;
            }
        }
        return numberOfSets;
    }

    public int getNumberOfReps(){
        int[] possibilities = {5,5,5,6,6,6,6,7,7,7,7,7,7,7,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,9,9,9,9,9,9,9,9,9,10,10};

        return possibilities[random(0, possibilities.length-1)];
    }

    public int getDaysBetweenWorkout(){
        int[] possibilities = {2,3,3,4,4,4,5,5,5,5,6,6,6,6,6,6,6,6,6,7,7,7,7,7,7,7,7,8,8,8,8,8,8,8,9,9,9,10};

        return possibilities[random(0, possibilities.length-1)];
    }

    public BigDecimal getWeightDifference(){
        if (prob(3,10)){
            return increment;
        }
        else if (prob(6,7)){
            return BigDecimal.ZERO;
        }
        else{
            return BigDecimal.ZERO.subtract(increment);
        }
    }

    public void generate(){
        DB.open();


        // ------------------------------------------------------------------------
        // Create exercise
        // ------------------------------------------------------------------------
        ContentValues cv = new ContentValues();
        cv.put("exercise_name", name);
        cv.put("icon_path", iconPath);
        cv.put("unit", "kg");
        cv.put("increment", increment.toPlainString());
        long exerciseID = DB.insertRow(TABLE_EXERCISE, cv);


        CustomCalendar today = new CustomCalendar();
        CustomCalendar date = new CustomCalendar();
        date.add(Calendar.DAY_OF_YEAR, (0 - startDaysAgo));
        BigDecimal weight = startingWeight;


        while (date.before(today)){

            // ------------------------------------------------------------------------
            // Create workout
            // ------------------------------------------------------------------------
            cv = new ContentValues();
            cv.put("exercise_id", exerciseID);
            cv.put("date", date.toShortString());
            if (prob(1,6)){
                cv.put("note", getWorkoutNote());
            }
            long workoutID = DB.insertRow(TABLE_WORKOUT, cv);

            int numberOfSets = getNumberOfSets();

            for (int i=1; i<=numberOfSets; i++) {
                // ------------------------------------------------------------------------
                // Create set
                // ------------------------------------------------------------------------

                cv = new ContentValues();
                cv.put("exercise_id", exerciseID);
                cv.put("workout_id", workoutID);
                cv.put("weight", weight.toPlainString());
                cv.put("reps", getNumberOfReps());
                cv.put("is_warmup", 0);
                cv.put("position", i);
                cv.put("number", i);
                if (prob(1,7)){
                    cv.put("note", getSetNote());
                }

                DB.insertRow(TABLE_SET, cv);
            }


            date.add(Calendar.DAY_OF_YEAR, getDaysBetweenWorkout());
            weight = weight.add(getWeightDifference());
        }

        DB.close();
    }


}
