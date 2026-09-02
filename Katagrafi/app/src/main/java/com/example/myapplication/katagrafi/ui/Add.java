package com.example.myapplication.katagrafi.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.katagrafi.R;
import com.example.myapplication.katagrafi.data.Expenses;
import com.example.myapplication.katagrafi.databinding.AddBinding;
import com.example.myapplication.katagrafi.util.Date;

import java.time.LocalDate;

public class Add extends Fragment {
    private AddBinding b;
    private LocalDate date = LocalDate.now();

    public Add() { super(R.layout.add); }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        b = AddBinding.bind(view);
        refreshDate();

        b.btnPickDate.setOnClickListener(v -> {
            LocalDate d = date;
            new DatePickerDialog(requireContext(), (dpY, y, m, day) -> {
                date = LocalDate.of(y, m + 1, day);
                refreshDate();
                Double existing = Expenses.get(requireContext(), date);
                b.etAmount.setText(existing == null ? "" : String.valueOf(existing));
            }, d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth()).show();
        });

        b.btnSave.setOnClickListener(v -> {
            String t = b.etAmount.getText().toString().trim().replace(",", ".");
            Double amt;
            try { amt = Double.parseDouble(t); } catch (Exception e) { amt = null; }
            if (amt == null || amt < 0) {
                android.widget.Toast.makeText(requireContext(), "Δώσε ποσό.", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            Expenses.put(requireContext(), date, amt);
            android.widget.Toast.makeText(requireContext(),
                    "Αποθήκευση για " + Date.longText(date) + ": €" + amt,
                    android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void refreshDate() { b.tvDate.setText(Date.longText(date)); }
}
