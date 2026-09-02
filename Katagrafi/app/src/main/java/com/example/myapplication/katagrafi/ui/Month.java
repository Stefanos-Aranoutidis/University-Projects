package com.example.myapplication.katagrafi.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.katagrafi.R;
import com.example.myapplication.katagrafi.data.Expenses;
import com.example.myapplication.katagrafi.data.Statistics;
import com.example.myapplication.katagrafi.databinding.MonthBinding;
import com.example.myapplication.katagrafi.util.Date;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Month extends Fragment {
    private MonthBinding b;
    private LocalDate anchor = LocalDate.now();
    private final DateTimeFormatter monthFmt =
            DateTimeFormatter.ofPattern("LLLL yyyy", new Locale("el","GR"));

    public Month() { super(R.layout.month); }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        b = MonthBinding.bind(view);
        b.btnPrev.setOnClickListener(v -> { anchor = anchor.minusMonths(1); render(); });
        b.btnNext.setOnClickListener(v -> { anchor = anchor.plusMonths(1); render(); });
        render();
    }

    private void render() {
        Statistics.MonthReport s = Statistics.month(anchor, Expenses.all(requireContext()));

        String title = s.monthStart.format(monthFmt);
        b.tvMonth.setText(title.substring(0,1).toUpperCase() + title.substring(1));

        b.tvRange.setText(Date.shortText(s.monthStart) + " – " + Date.shortText(s.monthEnd));
        b.tvTotal.setText("Σύνολο: €" + s.total);
        b.tvAvgWeek.setText("Μ.Ο. ανά εβδομάδα: €" + s.avgPerWeek);
        b.tvMaxWeek.setText("MAX εβδομάδα: " + s.maxWeek.label() + " (€" + s.maxWeek.total + ")");
        b.tvMinWeek.setText("MIN εβδομάδα: " + s.minWeek.label() + " (€" + s.minWeek.total + ")");


        b.monthList.removeAllViews();

        double max = 0.0;
        for (Statistics.WeekBucket w : s.weeklyTotals) if (w.total > max) max = w.total;
        int maxProgress = (int) Math.max(1, Math.round(max * 100));

        int red = requireContext().getColor(R.color.button_red);

        for (Statistics.WeekBucket w : s.weeklyTotals) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(dp(4), dp(6), dp(4), dp(6));

            TextView label = new TextView(requireContext());
            label.setText(w.label());
            label.setWidth(dp(140));

            ProgressBar bar = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(0, dp(12), 1f);
            bar.setLayoutParams(barParams);
            bar.setMax(maxProgress);
            bar.setProgress((int) Math.round(w.total * 100));
            bar.setProgressTintList(ColorStateList.valueOf(red));

            TextView amount = new TextView(requireContext());
            amount.setText(" €" + w.total);
            amount.setPadding(dp(8), 0, 0, 0);

            row.addView(label);
            row.addView(bar);
            row.addView(amount);
            b.monthList.addView(row);
        }
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(v * d);
    }
}
