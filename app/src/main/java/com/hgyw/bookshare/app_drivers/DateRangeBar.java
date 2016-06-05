package com.hgyw.bookshare.app_drivers;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
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
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (dialogView, year, monthOfYear, dayOfMonth) -> {
                    cal.set(year, monthOfYear, dayOfMonth);
                    if (dialogResultOk) {
                        setDate(i, cal.getTime());
                        if (listener != null) listener.onRangeChange(this);
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.ok), (dialog, which) -> dialogResultOk = true);
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
}
