package sam.gymnotes.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import sam.gymnotes.CustomCalendar;
import sam.gymnotes.Exercise;
import sam.gymnotes.R;
import sam.gymnotes.Set;
import sam.gymnotes.Workout;
import sam.gymnotes.activities.WorkoutView;

/**
 * Created by Sam on 25/08/2016.
 */
public class WorkoutGraphFragment extends Fragment implements OnChartValueSelectedListener {

    public boolean activityCreated;

    private Spinner chartSpinner;

    private LineChart chart;

    private TextView selectedValue;
    private TextView selectedDate;
    private CheckBox YAxisZero;
    private CheckBox XAxisToday;
    private RadioGroup rangeChooser;

    private Exercise exercise;

    private LinearLayout selectedPoint;

    private String[] spinnerOptions;

    private int colorAccent;
    private int colorPrimary;

    public WorkoutGraphFragment(){
        // Empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity a = getActivity();

        spinnerOptions = new String[] {a.getString(R.string.Estimated_One_Rep_Max),
                a.getString(R.string.Total_weight_lifted),
                a.getString(R.string.max_weight_lifted),
                a.getString(R.string.TotalSets),
                a.getString(R.string.Total_reps)
                };



    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_workout_graph, container, false);

        chart = (LineChart) v.findViewById(R.id.chart);

        chartSpinner = (Spinner) v.findViewById(R.id.chartSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout_charttype, spinnerOptions);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout_charttype);
        chartSpinner.setAdapter(adapter);
        chartSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                chart.highlightValue(0, -1);
                refreshPage();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        selectedPoint = (LinearLayout) v.findViewById(R.id.selectedPoint);

        selectedValue = (TextView) v.findViewById(R.id.value);
        selectedDate = (TextView) v.findViewById(R.id.workout);

        YAxisZero = (CheckBox) v.findViewById(R.id.YAxisZero);
        YAxisZero.setOnClickListener(refreshPageListener);

        XAxisToday = (CheckBox) v.findViewById(R.id.XAxisToday);
        XAxisToday.setOnClickListener(refreshPageListener);

        rangeChooser = (RadioGroup) v.findViewById(R.id.rangeChooser);
        rangeChooser.check(R.id.radio_all);
        rangeChooser.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshPage();
            }
        });

        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activityCreated = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        setGraphConstants();
        refreshPage();
    }

    public android.view.View.OnClickListener refreshPageListener = new android.view.View.OnClickListener() {
        @Override
        public void onClick(android.view.View view) {
            refreshPage();
        }
    };

    public void setGraphConstants(){

    }

    public void refreshPage() {
        if (activityCreated != true) {
            return;
        }
        WorkoutView activity = (WorkoutView) getActivity();

        if (activity.dataLoaded != true) {
            return;
        }

        // ------------------------------------------------------------------------
        // Get some activity variables
        // ------------------------------------------------------------------------
        exercise = activity.getExercise();
        ArrayList<Workout> workoutList = activity.getWorkoutList();

        Highlight[] highlights = chart.getHighlighted();
        chart.clear();
        this.onNothingSelected();


        // ------------------------------------------------------------------------
        // Get colors from current theme
        // ------------------------------------------------------------------------
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        colorPrimary = typedValue.data;

        getActivity().getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        colorAccent = typedValue.data;


        chart.setTouchEnabled(true);
        chart.setNoDataTextDescription("Not enough data");
        chart.setDescription("");
        chart.setDrawBorders(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setOnChartValueSelectedListener(this);


        // ------------------------------------------------------------------------
        // Collect all entry points for all workouts
        // ------------------------------------------------------------------------
        String chartType = chartSpinner.getSelectedItem().toString();
        ArrayList<Entry> entries = new ArrayList<Entry>();
        float Xmax = 0;
        for (int i = 0; i < workoutList.size(); i++) {
            Workout workout = workoutList.get(workoutList.size() - 1 - i);

            Float value = getY(chartType, workout);
            float dateInDaysFloat = Long.valueOf(workout.getDate().getTimeInDays()).floatValue();

            if (value != null) {
                if (dateInDaysFloat > Xmax) {
                    Xmax = dateInDaysFloat;
                }
                entries.add(new Entry(dateInDaysFloat, value, workout));
            }
        }


        // ------------------------------------------------------------------------
        // Change last date on x axis depending on checkbox state
        // ------------------------------------------------------------------------
        CustomCalendar lastDate;
        if (XAxisToday.isChecked()) {
            lastDate = new CustomCalendar();
        } else {
            lastDate = new CustomCalendar(new Date(Float.valueOf(Xmax).longValue() * 1000 * 60 * 60 * 24));
        }


        CustomCalendar firstDate;
        int radioID = rangeChooser.getCheckedRadioButtonId();
        if (radioID == R.id.radio_all) {
            firstDate = null;
        } else {
            firstDate = (CustomCalendar) lastDate.clone();
            switch (radioID) {
                case R.id.radio_1y:
                    firstDate.add(Calendar.YEAR, -1);
                    break;

                case R.id.radio_6m:
                    firstDate.add(Calendar.MONTH, -6);
                    break;

                case R.id.radio_3m:
                    firstDate.add(Calendar.MONTH, -3);
                    break;

                case R.id.radio_1m:
                    firstDate.add(Calendar.MONTH, -1);
                    break;
            }
        }

        if (firstDate != null) {
            for (int i = entries.size()-1; i>=0; i--) {
                Entry entry = entries.get(i);
                CustomCalendar date = ((Workout) entry.getData()).getDate();

                if (date.before(firstDate)){
                    entries.remove((int) i);
                }
            }
        }

        int lineWidth = 2;

        AxisFormatter axisFormatter = new AxisFormatter();
        // ------------------------------------------------------------------------
        // X axis options
        // ------------------------------------------------------------------------
        XAxis x = chart.getXAxis();
        x.setDrawGridLines(false);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setValueFormatter(axisFormatter);
        x.setLabelCount(7);
        x.setGranularity(1);
        x.setGranularityEnabled(true);
        x.setLabelRotationAngle(45);
        x.setAxisLineWidth(lineWidth);
        x.setAxisLineColor(colorPrimary);

        x.resetAxisMaxValue();
        x.resetAxisMinValue();
        if (XAxisToday.isChecked()){
            float today = lastDate.getTimeInDays();
            x.setAxisMaxValue(today);
        }


        // ------------------------------------------------------------------------
        // Y axis options
        // ------------------------------------------------------------------------
        chart.getAxisRight().setEnabled(false);
        YAxis y = chart.getAxisLeft();
        y.setGranularity(1);
        y.setGranularityEnabled(true);
        y.setAxisLineWidth(lineWidth);
        y.setAxisLineColor(colorPrimary);
        y.setGridColor(colorPrimary);

        y.setValueFormatter(axisFormatter);

        y.resetAxisMinValue();
        if (YAxisZero.isChecked()){
            y.setAxisMinValue(0);
        }



        LineDataSet dataSet = new LineDataSet(entries, chartSpinner.getSelectedItem().toString());
        dataSet.setColor(colorAccent);
        dataSet.setLineWidth(lineWidth);

        dataSet.setHighLightColor(colorPrimary);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(colorAccent);
        dataSet.setCircleHoleRadius(0);
        dataSet.setCircleRadius(2);
        dataSet.setDrawValues(false);



        // ------------------------------------------------------------------------
        // Set chart settings if empty
        // ------------------------------------------------------------------------
        if (entries.size() <=0){
            y.setAxisMaxValue(10);
            y.setAxisMinValue(0);

            CustomCalendar today = new CustomCalendar();

            x.setAxisMaxValue(today.getTime().getTime());
            x.setAxisMinValue(today.getTime().getTime());
            x.setLabelCount(2, true);

            chart.setTouchEnabled(false);
        }


        // ------------------------------------------------------------------------
        // Legend
        // ------------------------------------------------------------------------
        Legend legend = chart.getLegend();
        legend.setEnabled(false);


        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.animateY(300);
        if (highlights != null){
            chart.highlightValue(highlights[0], true);
        }
    }

    private Float getY(String chartType, Workout workout) {


        // ------------------------------------------------------------------------
        // Estimated One Rep Max
        // ------------------------------------------------------------------------
        if (chartType.equals(getString(R.string.Estimated_One_Rep_Max))){
            Set best = workout.getBestSet();
            if (best != null){
                return best.getRepMax(1).floatValue();
            }
        }

        // ------------------------------------------------------------------------
        // Total Reps
        // ------------------------------------------------------------------------
        else if (chartType.equals(getString(R.string.Total_reps))){
            return Integer.valueOf(workout.getTotalReps()).floatValue();
        }

        // ------------------------------------------------------------------------
        // Total Weight Lifted
        // ------------------------------------------------------------------------
        else if (chartType.equals(getString(R.string.Total_weight_lifted))){
            return workout.totalWeightLifted().floatValue();
        }

        // ------------------------------------------------------------------------
        // Max Weight Lifted
        // ------------------------------------------------------------------------
        else if (chartType.equals(getString(R.string.max_weight_lifted))){
            BigDecimal maxWeight = workout.maxWeight();
            if (maxWeight != null){
                return maxWeight.floatValue();
            }
        }

        // ------------------------------------------------------------------------
        // Total Sets
        // ------------------------------------------------------------------------
        else if (chartType.equals(getString(R.string.TotalSets))){
            return Integer.valueOf(workout.getSetList().size()).floatValue();
        }

        return null;
    }

    public static double[] upperLowerLimit(double minValue, double maxValue){

        double mutiple = 10;
        double[] limits = new double[2];

        limits[0] = minValue - 10;
        limits[1] = maxValue + 10;

        if (limits[0] < 0){
            limits[0] = 0;
        }

        return limits;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        final Workout workout = (Workout) e.getData();
        float value = e.getY();

        String chartType = chartSpinner.getSelectedItem().toString();

        String valueString;

        if (chartType.equals(getString(R.string.Estimated_One_Rep_Max))){
            Set bestSet = workout.getBestSet();

            String s = bestSet.getRepMax(1).toPlainString();
            s = s.indexOf(".") < 0 ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
            s += " " + exercise.getUnit();

            String s2 = bestSet.getWeight().toPlainString();
            s2 = s2.indexOf(".") < 0 ? s2 : s2.replaceAll("0*$", "").replaceAll("\\.$", "");
            s2 = "(" + s2 + " " + exercise.getUnit() + " x " + bestSet.getReps() + ")";

            valueString = s + "\n" + s2;
        }
        else if (chartType.equals(getString(R.string.Total_weight_lifted))
                    || chartType.equals(getString(R.string.max_weight_lifted))){

            String s = Float.valueOf(value).toString();
            s = s.indexOf(".") < 0 ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
            valueString = s + " " + exercise.getUnit();
        }
        else {
            String s = Float.valueOf(value).toString();
            s = s.indexOf(".") < 0 ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
            valueString = s;
        }



        selectedValue.setText(valueString);


        selectedDate.setText(workout.getDate().toLongString(0, 1, 1, 2));

        selectedPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WorkoutView) getActivity()).gotoWorkout(workout.getID());
            }
        });
        selectedPoint.setClickable(true);
    }

    @Override
    public void onNothingSelected() {
        selectedValue.setText(R.string.elipses);
        selectedDate.setText(R.string.elipses);
        selectedPoint.setClickable(false);
    }

    public class AxisFormatter implements AxisValueFormatter {

        public AxisFormatter() {}

        @Override
        public String getFormattedValue(float value, AxisBase axis) {

            if (XAxis.class.isInstance(axis)){
                XAxis x = (XAxis) axis;

                long days = Float.valueOf(value).longValue();
                long milliseconds = days*24*60*60*1000;
                CustomCalendar date = new CustomCalendar(new Date(milliseconds));

                days = Float.valueOf(x.getAxisMinimum()).longValue();
                milliseconds = days*24*60*60*1000;
                CustomCalendar firstDate = new CustomCalendar(new Date(milliseconds));

                days = Float.valueOf(x.getAxisMaximum()).longValue();
                milliseconds = days*24*60*60*1000;
                CustomCalendar lastDate = new CustomCalendar(new Date(milliseconds));

                if (firstDate.get(Calendar.YEAR) != lastDate.get(Calendar.YEAR)){
                    x.setGranularity(30);
                    return date.toLongString(0,0,1,1);
                }
                else{
                    x.setGranularity(1);
                    return date.toLongString(0,1,1,0);
                }


            }
            else{
                String chartType = chartSpinner.getSelectedItem().toString();

                String s = String.valueOf(value);
                s = s.indexOf(".") < 0 ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");

                if (chartType.equals(getString(R.string.Estimated_One_Rep_Max))
                        || chartType.equals(getString(R.string.Total_weight_lifted))
                        || chartType.equals(getString(R.string.max_weight_lifted))){

                    s += " " + exercise.getUnit();
                }
                else if (chartType.equals(getString(R.string.Total_reps))
                        || chartType.equals(getString(R.string.TotalSets))){
                    // Nothing
                }

                return s;
            }
        }

        @Override
        public int getDecimalDigits() { return 1; }
    }

}
