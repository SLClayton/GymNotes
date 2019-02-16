package sam.gymnotes;

import java.math.BigDecimal;

public class Set {

    private long database_ID;

    private int position;
    private int number;
    private BigDecimal weight;      // Weight Lifted
    private int reps;               // Reps done
    private String note;            // Any notes about the set
    private Boolean isWarmUp;       // Is this a warm up set?

    public static final int[] repPercentages = {100, 95, 93, 90, 87, 85, 83, 80, 77, 75, 73, 70, 68, 66, 65};



    public Set(long ID, Integer POSITION, Integer NUMBER, BigDecimal WEIGHT, Integer REPS, String NOTE, Boolean WARMUP){
        database_ID = ID;

        if (POSITION != null){
            position = POSITION;
        }
        if (NUMBER != null){
            number = NUMBER;
        }
        weight = WEIGHT;
        if (REPS != null){
            reps = REPS;
        }
        note = NOTE;
        isWarmUp = WARMUP;

    }



    public long getID(){
        return database_ID;
    }

    public void setPosition(int pos){
        position = pos;
    }
    public int getPosition(){
        return position;
    }

    public void setNumber(int num){
        number = num;
    }
    public int getNumber(){
        return number;
    }

    public void setWeight(BigDecimal weight){
        weight = weight;
    }

    public BigDecimal getWeight(){
        return weight;
    }

    public String getWeightString(String unit){
        return weight.setScale(2, BigDecimal.ROUND_HALF_EVEN).toPlainString() + " " + unit;
    }

    public void setReps(int r){
        reps = r;
    }
    public int getReps(){
        return reps;
    }

    public void setNote(String note){
        note = note;
    }
    public String getNote(){
        return note;
    }

    public void setIsWarmUp(Boolean isTrue){
        isWarmUp = isTrue;
    }
    public Boolean isWarmUp(){
        return isWarmUp;
    }

    public BigDecimal getRepMax(int repRange){
        int REPS;

        if (reps <= 0){
            return BigDecimal.ZERO;
        }
        else if ((reps > repPercentages.length)){
            REPS = repPercentages.length;
        }
        else{
            REPS = reps;
        }

        BigDecimal multiplier = new BigDecimal(String.valueOf(repPercentages[repRange-1]));
        BigDecimal divisor = new BigDecimal(String.valueOf(repPercentages[REPS-1]));


        return multiplier.divide(divisor, 2, BigDecimal.ROUND_HALF_UP).multiply(weight).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal totalWeightLifted(){
        BigDecimal REPS = new BigDecimal(reps);

        return weight.multiply(REPS);
    }


}
