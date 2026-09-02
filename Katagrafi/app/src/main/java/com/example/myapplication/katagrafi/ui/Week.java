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
import com.example.myapplication.katagrafi.databinding.WeekBinding;
import com.example.myapplication.katagrafi.util.Date;

import java.time.LocalDate;
import java.util.Map;

public class Week extends Fragment {
    private WeekBinding b;
    private LocalDate anchor = LocalDate.now();

    public Week() { super(R.layout.week); }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        b = WeekBinding.bind(view);
        b.btnPrev.setOnClickListener(v -> { anchor = anchor.minusWeeks(1); render(); });
        b.btnNext.setOnClickListener(v -> { anchor = anchor.plusWeeks(1); render(); });
        render();
    }

    private void render() {
        Map<LocalDate, Double> all = Expenses.all(requireContext());
        Statistics.WeekReport s = Statistics.week(anchor, all);
        b.tvRange.setText(Date.shortText(s.start) + " – " + Date.shortText(s.end));
        b.tvTotal.setText("Σύνολο: €" + s.total);
        b.tvAvg.setText("Μ.Ο. ανά ημέρα: €" + s.avgPerDay);
        b.tvMax.setText("MAX ημέρα: " + Date.shortText(s.dayMax.date) + " (€" + s.dayMax.value + ")");
        b.tvMin.setText("MIN ημέρα: " + Date.shortText(s.dayMin.date) + " (€" + s.dayMin.value + ")");


        b.weekList.removeAllViews();

        double max = 0.0;
        for (Statistics.Pair p : s.breakdown) if (p.value > max) max = p.value;
        int maxProgress = (int) Math.max(1, Math.round(max * 100));

        int red = requireContext().getColor(R.color.button_red);

        for (Statistics.Pair p : s.breakdown) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(dp(4), dp(6), dp(4), dp(6));

            TextView day = new TextView(requireContext());
            day.setText(Date.shortText(p.date));
            day.setWidth(dp(120));

            ProgressBar bar = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(0, dp(12), 1f);
            bar.setLayoutParams(barParams);
            bar.setMax(maxProgress);
            bar.setProgress((int) Math.round(p.value * 100));
            bar.setProgressTintList(ColorStateList.valueOf(red));

            TextView amount = new TextView(requireContext());
            amount.setText(" €" + p.value);
            amount.setPadding(dp(8), 0, 0, 0);

            row.addView(day);
            row.addView(bar);
            row.addView(amount);
            b.weekList.addView(row);
        }
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(v * d);
    }
}