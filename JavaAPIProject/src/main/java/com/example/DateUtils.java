package com.example;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DateUtils {

    public static LocalDate getPreviousValidDate(LocalDate date) 
    {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) 
        {
            date = date.plusDays(1);
        }
        return date;
    }
}