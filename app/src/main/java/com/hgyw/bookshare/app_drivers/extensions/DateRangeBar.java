package com.hgyw.bookshare.app_drivers.extensions;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hgyw.bookshare.R;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A bar of date range, that allow to user to choose range of dates.
 */
public class DateRangeBar extends FrameLayout implements View.OnClickListener {
    public static final int DAY_MILLIS = 24 * 60 * 60 * 1000;
    private TextView[] dateViews = new TextView[2];
    private Date[] dates = new Date[2];
    private DateFormat dateFormat;
    final Calendar cal = GregorianCalendar.getInstance(); // a calendar for the datePickerDialog
    private DateRangeListener listener;

    public DateRangeBar(Context context) {
        this(context, null);
    }

    public DateRangeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateView(context);
    }

    // trying to save the date. not working. TODO!
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putLong("dateFrom", dates[0].getTime());
        bundle.putLong("dateTo", dates[1].getTime());
        return bundle;
    }

    // not working. TODO!
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            setDate(0 ,new Date(bundle.getLong("dateFrom")));
            setDate(1 ,new Date(bundle.getLong("dateTo")));
            state = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
    }

    /**
     * Inflate the view
     */
    private void inflateView(Context context) {
        dateFormat = android.text.format.DateFormat.getDateFormat(context);

        addView(inflate(context, R.layout.bar_range_date, null));
        dateViews[0] = (TextView) findViewById(R.id.fromDate);
        dateViews[1] = (TextView) findViewById(R.id.toDate);

        cal.setTime(new Date()); cal.add(Calendar.YEAR, -1);
        dates[0] = cal.getTime(); // set default value to year before
        dates[1] = new Date(); // set default value to now

        for (int i = 0; i < 2; i++) {
            dateViews[i].setOnClickListener(this);
            setDate(i, dates[i]); // for updating dateViews
        }
    }

    @Override
    public void onClick(View v) {
        int i = Arrays.<View>asList(dateViews).indexOf(v); // the date index (0 to start 1 to end)
        if (i < 0) return; // view 'v' is not one of dateViews

        cal.setTime(dates[i]);
        DatePickerDialogLollipop datePickerDialog = new DatePickerDialogLollipop(
                getContext(),
                (dialogView, year, monthOfYear, dayOfMonth) ->
                        cal.set(year, monthOfYear, dayOfMonth,0,0,0), // update the calendar on each change
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.ok),
                (dialog, which) -> {
                    setDate(i, cal.getTime());
                    if (listener != null) listener.onRangeChange(this);
                });
        datePickerDialog.show();
    }

    private void setDate(int index, Date date) {
        dates[index] = date;
        dateViews[index].setText(dateFormat.format(date));
        avoidStartAfterEnd(index);
    }

    /**
     * get date of start of rang.
     */
    public Date getDateFrom() {
        return (Date) dates[0].clone();
    }

    /**
     * get date of end of rang.
     */
    public Date getDateTo() {
        return new Date(dates[1].getTime() + DAY_MILLIS);
    }

    /**
     * if start-date is after end-date then the dominant date will set to the other.
     * @param dominant dominant date index (0 or 1)
     */
    private void avoidStartAfterEnd(int dominant) {
        if (dates[0].compareTo(dates[1]) > 0) {
            int slave = dominant == 0 ? 1 : 0;
            dates[slave] = dates[dominant];
            dateViews[slave].setText(dateViews[dominant].getText());
        }
    }

    /**
     * Listener class for DateRangeBar
     */
    public interface DateRangeListener {
        void onRangeChange(DateRangeBar dateRangeBar);
    }

    /**
     * Notify the listener when the date
     */
    public void setDateRangeListener(DateRangeListener l) {
        this.listener = l;
    }

    /**
     * DatePickerDialog that the OnDateChangedListener is called on changing the date. <br>
     * This should be the default behaviour, but in Android Lollipop it's not working.
     * see
     * <a href="http://stackoverflow.com/questions/27407441/lollipop-calenderview-datepicker-doesnt-call-ondatechanged-method">
     * stack-overflow question</a>.
     */
    private class DatePickerDialogLollipop extends DatePickerDialog{

        private final DatePicker.OnDateChangedListener listener;
        public DatePickerDialogLollipop(Context context, DatePicker.OnDateChangedListener onDateChangedListener, int year, int monthOfYear, int dayOfMonth) {
            //Do nothing onDateSetListener
            super(context, null, year, monthOfYear, dayOfMonth);
            listener = onDateChangedListener;
        }

        @Override
        public void onDateChanged(DatePicker view, int year, int month, int day) {
            listener.onDateChanged(view,year,month,day);
        }
    }
}
