package io.github.anthonyclemens.Logic;

import java.io.Serializable;

/**
 * Calender class for tracking and incrementing dates.
 * Handles leap years and month transitions.
 */
public class Calender implements Serializable {
    private int day;
    private int month;
    private int year;
    private static final String[] MONTH_NAMES = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    /**
     * Constructs a Calender with the specified start date.
     * @param startDay   The starting day.
     * @param startMonth The starting month (1-based).
     * @param startYear  The starting year.
     */
    public Calender(int startDay, int startMonth, int startYear) {
        this.day = startDay;
        this.month = startMonth;
        this.year = startYear;
    }

    /**
     * Increments the day, handling month and year transitions.
     */
    public void incrementDay() {
        day++;
        // If day exceeds the number of days in the current month, reset and increment month
        if (day > getDaysInMonth(month, year)) {
            day = 1;
            month++;
            // If month exceeds December, reset and increment year
            if (month > 12) {
                month = 1;
                year++;
            }
        }
    }

    /**
     * Returns the number of days in a given month, accounting for leap years.
     */
    private int getDaysInMonth(int month, int year) {
        return switch (month) {
            case 2 -> isLeapYear(year) ? 29 : 28; // February
            case 4, 6, 9, 11 -> 30; // April, June, September, November
            default -> 31; // All other months
        };
    }

    /**
     * Determines if a year is a leap year.
     */
    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    /**
     * Returns a string representation of the current date.
     */
    @Override
    public String toString() {
        String monthName = MONTH_NAMES[month - 1];
        return String.format("%s %02d, %04d", monthName, day, year);
    }

    public int getDay(){
        return this.day;
    }

    public int getMonth(){
        return this.month;
    }

    public int getYear(){
        return this.year;
    }
}
