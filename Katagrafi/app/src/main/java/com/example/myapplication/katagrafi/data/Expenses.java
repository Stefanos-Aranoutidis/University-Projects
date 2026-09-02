package com.example.myapplication.katagrafi.data;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public final class Expenses {
    private static final String PREF = "expenses_store_v2";
    private static final String PREFIX = "d:";

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static void put(Context ctx, LocalDate date, double amount) {
        prefs(ctx).edit().putString(PREFIX + date.toString(), String.valueOf(amount)).apply();
    }

    public static Double get(Context ctx, LocalDate date) {
        String s = prefs(ctx).getString(PREFIX + date.toString(), null);
        if (s == null) return null;
        try { return Double.parseDouble(s); } catch (Exception e) { return 0.0; }
    }

    public static Map<LocalDate, Double> all(Context ctx) {
        Map<String, ?> raw = prefs(ctx).getAll();
        Map<LocalDate, Double> out = new HashMap<>();
        for (Map.Entry<String, ?> e : raw.entrySet()) {
            String k = e.getKey();
            if (!k.startsWith(PREFIX)) continue;
            LocalDate d = LocalDate.parse(k.substring(PREFIX.length()));
            Object v = e.getValue();
            double val = (v instanceof Number) ? ((Number) v).doubleValue()
                    : (v instanceof String) ? parse((String) v) : 0.0;
            out.put(d, val);
        }
        return out;
    }

    public static void clear(Context ctx) { prefs(ctx).edit().clear().apply(); }

    private static double parse(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0.0; }
    }

    private Expenses() {}
}
