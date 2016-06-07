package com.hgyw.bookshare.app_drivers;

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
 * Created by haim7 on 23/05/2016.
 */
public class DateRangeBar extends FrameLayout implements View.OnClickListener {
    private TextView[] views = new TextView[2];
    private Date[] dates = new Date[2];
    private DateFormat dateFormat;
    Calendar cal = GregorianCalendar.getInstance();
    private DateRangeListener listener;
    private boolean dialogResultOk;

    public DateRangeBar(Context context) {
        super(context);
        inflateView(context);
    }

    public DateRangeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateView(context);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putLong("dateFrom", dates[0].getTime());
        bundle.putLong("dateTo", dates[1].getTime());
        return bundle;
    }

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

    private void inflateView(Context context) {
        dateFormat = android.text.format.DateFormat.getDateFormat(context);

        addView(inflate(context, R.layout.bar_range_date, null));
        views[0] = (TextView) findViewById(R.id.fromDate);
        views[1] = (TextView) findViewById(R.id.toDate);

        cal.setTime(new Date()); cal.add(Calendar.YEAR, -1);
        dates[0] = cal.getTime();
        dates[1] = new Date();

        for (int i = 0; i < 2; i++) {
            views[i].setOnClickListener(this);
            setDate(i, dates[i]); // for update view
        }
    }

    @Override
    public void onClick(View v) {
        int i = Arrays.asList(views).indexOf(v);
        if (i < 0) return;

        dialogResultOk = false;
        cal.setTime(dates[i]);
        DatePickerDialogLollipop datePickerDialog = new DatePickerDialogLollipop(
                getContext(),
                (dialogView, year, monthOfYear, dayOfMonth) ->
                        cal.set(year, monthOfYear, dayOfMonth),
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
        views[index].setText(dateFormat.format(date));
        avoidStartAfterEnd(index);
    }


    public Date getDateFrom() {
        return dates[0];
    }

    public Date getDateTo() {
        return dates[1];
    }

    public Date[] getDates() {
        return new Date[] {dates[0], dates[1]};
    }

    private void avoidStartAfterEnd(int dominant) {
        if (dates[0].compareTo(dates[1]) > 0) {
            int slave = dominant == 0 ? 1 : 0;
            dates[slave] = dates[dominant];
            views[slave].setText(views[dominant].getText());
        }
    }

    public interface DateRangeListener {
        void onRangeChange(DateRangeBar dateRangeBar);
    }

    public void setDateRangeListener(DateRangeListener l) {
        this.listener = l;
    }

    private class DatePickerDialogLollipop extends DatePickerDialog{

        private final DatePicker.OnDateChangedListener listener;
        public DatePickerDialogLollipop(Context context, DatePicker.OnDateChangedListener callBack, int year, int monthOfYear, int dayOfMonth) {
            //Do nothing onDateSetListener
            super(context, (a,b,c,d)->{}, year, monthOfYear, dayOfMonth);
            listener = callBack;
        }

        @Override
        public void onDateChanged(DatePicker view, int year, int month, int day) {
            listener.onDateChanged(view,year,month,day);
        }
    }
}
