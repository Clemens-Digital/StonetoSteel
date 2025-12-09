package io.github.anthonyclemens.Logic;

import java.io.Serializable;

public class DateTime implements Serializable {
    private int month;
    private int day;
    private int year;
    private int hour;
    private int minute;

    public DateTime(int month, int day, int year, int hour, int minute) {
        this.month = month;
        this.day = day;
        this.year = year;
        this.hour = hour;
        this.minute = minute;
    }

    public DateTime(DateTime dateTime){
        this.month = dateTime.getMonth();
        this.day = dateTime.getDay();
        this.year = dateTime.getYear();
        this.hour = dateTime.getHour();
        this.minute = dateTime.getMinute();
    }

    public int getMonth() {
        return this.month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return this.day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getHour() {
        return this.hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return this.minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public boolean isDaysAfter(DateTime other, int daysAfter) {
        int thisDays = toAbsoluteDays(this.year, this.month, this.day);
        int otherDays = toAbsoluteDays(other.getYear(), other.getMonth(), other.getDay());

        int diff = thisDays - otherDays;

        return diff == daysAfter && this.hour>=other.getHour();
    }

    private int toAbsoluteDays(int year, int month, int day) {
        int[] daysInMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

        boolean isLeap = (year % 400 == 0) || (year % 4 == 0 && year % 100 != 0);
        if (isLeap) daysInMonth[1] = 29;

        int days = year * 365 + countLeapYears(year);

        for (int m = 1; m < month; m++) {
            days += daysInMonth[m - 1];
        }

        days += day;

        return days;
    }

    private int countLeapYears(int year) {
        return year / 4 - year / 100 + year / 400;
    }
}