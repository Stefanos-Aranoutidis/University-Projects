package com.example.myapplication.katagrafi.data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Statistics {
    private static final WeekFields WF = WeekFields.of(Locale.getDefault());

    public static WeekReport week(LocalDate anchor, Map<LocalDate, Double> all) {
        LocalDate start = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end   = anchor.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<Pair> breakdown = new ArrayList<>();
        LocalDate d = start;
        while (!d.isAfter(end)) {
            breakdown.add(new Pair(d, val(all, d)));
            d = d.plusDays(1);
        }

        double total = 0.0;
        for (Pair p : breakdown) total += p.value;
        double avg = total / 7.0;

        Pair mx = breakdown.get(0), mn = breakdown.get(0);
        for (Pair p : breakdown) {
            if (p.value > mx.value) mx = p;
            if (p.value < mn.value) mn = p;
        }

        return new WeekReport(
                start, end,
                r2(total), r2(avg),
                new Pair(mx.date, r2(mx.value)),
                new Pair(mn.date, r2(mn.value)),
                roundList(breakdown)
        );
    }

    public static MonthReport month(LocalDate anchor, Map<LocalDate, Double> all) {
        LocalDate mStart = anchor.withDayOfMonth(1);
        LocalDate mEnd   = anchor.withDayOfMonth(anchor.lengthOfMonth());

        // bucket ανά (έτος, εβδομάδα)
        Map<Key, List<LocalDate>> buckets = new LinkedHashMap<>();
        LocalDate d = mStart;
        while (!d.isAfter(mEnd)) {
            int w = d.get(WF.weekOfWeekBasedYear());
            int y = d.get(WF.weekBasedYear());
            Key key = new Key(y, w);
            List<LocalDate> list = buckets.get(key);
            if (list == null) {
                list = new ArrayList<>();
                buckets.put(key, list);
            }
            list.add(d);
            d = d.plusDays(1);
        }

        List<WeekBucket> weekly = new ArrayList<>();
        for (Map.Entry<Key, List<LocalDate>> e : buckets.entrySet()) {
            List<LocalDate> days = e.getValue();
            LocalDate mon = days.get(0).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate sun = days.get(days.size() - 1).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            double tot = 0.0;
            for (LocalDate x : days) tot += val(all, x);
            weekly.add(new WeekBucket(e.getKey().year, e.getKey().week, mon, sun, r2(tot)));
        }

        double total = 0.0;
        for (WeekBucket w : weekly) total += w.total;
        double avgW = weekly.isEmpty() ? 0.0 : r2(total / weekly.size());

        WeekBucket max = weekly.isEmpty() ? new WeekBucket(anchor.getYear(),0,mStart,mEnd,0)
                : weekly.get(0);
        WeekBucket min = max;
        for (WeekBucket w : weekly) {
            if (w.total > max.total) max = w;
            if (w.total < min.total) min = w;
        }

        return new MonthReport(mStart, mEnd, r2(total), avgW, weekly, max, min);
    }

    // ===== helpers / models =====
    private static double val(Map<LocalDate, Double> all, LocalDate d) {
        Double v = all.get(d);
        return v == null ? 0.0 : v;
    }
    private static double r2(double x) { return Math.round(x * 100.0) / 100.0; }
    private static List<Pair> roundList(List<Pair> in) {
        List<Pair> out = new ArrayList<>();
        for (Pair p : in) out.add(new Pair(p.date, r2(p.value)));
        return out;
    }


    private static final class Key {
        final int year;
        final int week;
        Key(int year, int week) { this.year = year; this.week = week; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key k = (Key) o;
            return year == k.year && week == k.week;
        }
        @Override public int hashCode() { return 31 * year + week; }
    }

    public static class Pair {
        public final LocalDate date; public final double value;
        public Pair(LocalDate date, double value) { this.date = date; this.value = value; }
    }

    public static class WeekReport {
        public final LocalDate start, end;
        public final double total, avgPerDay;
        public final Pair dayMax, dayMin;
        public final List<Pair> breakdown;
        public WeekReport(LocalDate s, LocalDate e, double t, double avg, Pair max, Pair min, List<Pair> br) {
            start=s; end=e; total=t; avgPerDay=avg; dayMax=max; dayMin=min; breakdown=br;
        }
    }

    public static class WeekBucket {
        public final int year, week; public final LocalDate mon, sun; public final double total;
        public WeekBucket(int year, int week, LocalDate mon, LocalDate sun, double total) {
            this.year=year; this.week=week; this.mon=mon; this.sun=sun; this.total=total;
        }
        public String label() {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault());
            return "Εβδ. " + week + " (" + mon.format(df) + "–" + sun.format(df) + ")";
        }
    }

    public static class MonthReport {
        public final LocalDate monthStart, monthEnd;
        public final double total, avgPerWeek;
        public final List<WeekBucket> weeklyTotals;
        public final WeekBucket maxWeek, minWeek;
        public MonthReport(LocalDate s, LocalDate e, double t, double avgW,
                           List<WeekBucket> wk, WeekBucket max, WeekBucket min) {
            monthStart=s; monthEnd=e; total=t; avgPerWeek=avgW; weeklyTotals=wk; maxWeek=max; minWeek=min;
        }
    }

    private Statistics() {}
}
