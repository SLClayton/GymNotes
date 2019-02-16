package sam.gymnotes;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Workout {

    private long database_ID;
    private CustomCalendar date;
    private String note;

    private ArrayList<Set> setList;

    public Workout(long ID, CustomCalendar DATE, String NOTE){
        database_ID = ID;
        date = DATE;
        note = NOTE;

        setList = new ArrayList<Set>();
    }

    public void setDate(CustomCalendar d){
        date = d;
    }
    public CustomCalendar getDate(){
        return date;
    }

    public long getID(){
        return database_ID;
    }

    public void setNote(String n){
        note = n;
    }
    public String getNote(){
        return note;
    }

    public Set getSet(int index){
        return setList.get(index);
    }

    public ArrayList<Set> getSetList(){
        return setList;
    }
    public void addSet(Set set){
        setList.add(set);
    }
    public void removeSet(int index){
        setList.remove(index);
    }


    public Set getBestSet(){
        Set best = null;

        for (Set set : setList){
            if (best == null){
                best = set;
            }
            else{
                BigDecimal bestORM = best.getRepMax(1);
                BigDecimal setORM = set.getRepMax(1);

                if (setORM.compareTo(bestORM) == 1){
                    best = set;
                }
            }

        }

        return best;
    }

    public int getTotalReps(){
        int setCount = 0;
        for (int i=0; i<setList.size(); i++){
            setCount += setList.get(i).getReps();
        }
        return setCount;
    }

    public BigDecimal totalWeightLifted(){
        BigDecimal weightCount = BigDecimal.ZERO;
        for (int i=0; i<setList.size(); i++){
            weightCount = weightCount.add(setList.get(i).totalWeightLifted());
        }
        return weightCount;
    }

    public BigDecimal maxWeight(){
        BigDecimal max = null;
        for (int i=0; i<setList.size(); i++){
            BigDecimal weight = setList.get(i).getWeight();
            if (max == null || max.compareTo(weight) == -1){
                max = weight;
            }
        }
        return max;
    }

}
