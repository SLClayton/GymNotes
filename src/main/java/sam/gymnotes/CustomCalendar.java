package sam.gymnotes;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class CustomCalendar extends GregorianCalendar {

    public CustomCalendar(){
        super();
        sterilize();
    }

    public CustomCalendar(Date date){
        super();
        this.setTime(date);
        sterilize();
    }

    public CustomCalendar(int YEAR, int MONTH, int DAY){
        super(YEAR, MONTH, DAY);
        sterilize();
    }

    public CustomCalendar(String StringDate){
        super(findYear(StringDate), findMonth(StringDate) - 1, findDay(StringDate));
        sterilize();
    }

    public void sterilize(){
        this.set(Calendar.HOUR_OF_DAY, 0);
        this.set(Calendar.MINUTE, 0);
        this.set(Calendar.SECOND, 0);
        this.set(Calendar.MILLISECOND, 0);
    }

    public static int findDay(String date){
        return Integer.valueOf(date.substring(0, date.indexOf("/")));
    }
    public static int findMonth(String date){
        return Integer.valueOf(date.substring(date.indexOf("/") + 1, date.replaceFirst("/", "").indexOf("/") + 1));
    }
    public static int findYear(String date){
        return Integer.valueOf(date.substring(date.replaceFirst("/", "").indexOf("/") + 2));
    }

    public long getTimeInDays(){
        long day = 1000*60*60*24;
        return (this.getTimeInMillis() + day) / day;
    }


    public String toShortString(){
        return this.get(Calendar.DAY_OF_MONTH) + "/" + (this.get(Calendar.MONTH) + 1) + "/" + this.get(Calendar.YEAR);
    }

    public String toLongString(int DayOfWeek, int DayOfMonth, int Month, int Year){

        // ----------------------------------------------------------------------
        // Day of the week
        // ----------------------------------------------------------------------
        String DOTW = "";
        if (DayOfWeek != 0){
            DOTW = this.getDisplayName(DAY_OF_WEEK, LONG, Locale.ENGLISH);

            if (DayOfWeek == 1){
                DOTW = DOTW.substring(0, 3);
            }

            DOTW += ", ";
        }

        // ----------------------------------------------------------------------
        // Day of the month
        // ----------------------------------------------------------------------
        String DOTM = "";
        if (DayOfMonth != 0) {
            DOTM = Integer.toString(this.get(Calendar.DAY_OF_MONTH));

            if (DayOfMonth > 1) {
                if ((DOTM.equals("1")) || (DOTM.equals("21")) || (DOTM.equals("31"))) {
                    DOTM += "st";
                } else if ((DOTM.equals("2")) || (DOTM.equals("22"))) {
                    DOTM += "nd";
                } else if ((DOTM.equals("3")) || (DOTM.equals("23"))) {
                    DOTM += "rd";
                } else {
                    DOTM += "th";
                }
            }

            DOTM += " ";
        }

        // ----------------------------------------------------------------------
        // Month
        // ----------------------------------------------------------------------
        String M = "";
        if (Month != 0){
            M = this.getDisplayName(Calendar.MONTH, LONG, Locale.ENGLISH);
            if (Month == 1){
                M = M.substring(0, 3);
            }
            M += " ";
        }


        // ----------------------------------------------------------------------
        // Year
        // ----------------------------------------------------------------------
        String Y = "";
        if (Year != 0){
            Y = Integer.toString(this.get(Calendar.YEAR));
            if (Year == 1){
                Y = "'" + Y.substring(Y.length() - 2);
            }
        }

        return DOTW + DOTM + M + Y;
    }

    public int daysAgo(){
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar DATE = (GregorianCalendar) this.clone();
        int daysAgo = -1;

        while (DATE.before(today)){
            DATE.add(Calendar.DAY_OF_MONTH, 1);
            daysAgo++;
        }

        return daysAgo;
    }

    public String daysAgoString(){
        int daysAgo = daysAgo();
        String str;

        switch (daysAgo){
            case -1:
                str = "future"; break;
            case 0:
                str = "today"; break;
            case 1:
                str = "yesterday"; break;
            default:
                str = daysAgo + " days ago"; break;
        }

        return str;
    }

    public boolean isToday(){
        Calendar today = Calendar.getInstance();

        if (this.get(YEAR) == today.get(YEAR) && this.get(MONTH) == today.get(MONTH) && this.get(DAY_OF_MONTH) == today.get(DAY_OF_MONTH)){
            return true;
        }
        return false;
    }
}
