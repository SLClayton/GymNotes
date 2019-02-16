package sam.gymnotes;

import java.util.ArrayList;

/**
 * Created by Sam on 17/05/2017.
 */

public class ExerciseGroup {

    private long id;
    private String group_name;
    private ArrayList<Exercise> exercises;

    public ExerciseGroup(String NAME){
        group_name = NAME;
        exercises = new ArrayList<Exercise>();
    }

    public ExerciseGroup(long ID){
        id = ID;
        exercises = new ArrayList<Exercise>();
    }

    public ExerciseGroup(long ID, String NAME){
        id = ID;
        group_name = NAME;
        exercises = new ArrayList<Exercise>();
    }

    public void addExercise(Exercise EXERCISE){
        exercises.add(EXERCISE);
    }

    public Exercise getExercise(int index){
        return exercises.get(index);
    }

    public int exerciseListLength(){
        return exercises.size();
    }

    public String getName(){
        return group_name;
    }

    public long getID(){
        return id;
    }

}
