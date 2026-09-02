package com.example.myapplication.katagrafi.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Date {
    private static final DateTimeFormatter LONG  =
            DateTimeFormatter.ofPattern("EEE d MMM yyyy", new Locale("el","GR"));
    private static final DateTimeFormatter SHORT =
            DateTimeFormatter.ofPattern("EEE d MMM", new Locale("el","GR"));

    public static String longText(LocalDate d)  { return d.format(LONG); }
    public static String shortText(LocalDate d) { return d.format(SHORT); }

    private Date() {}
}

