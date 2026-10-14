package mz.co.amaterasu.gestao.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    private DateUtil() {
    }

    public static Date now() {
        return new Date();
    }

    public static LocalDate dateToLocalDate(Date date) {
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    public static Date getCurrentDate() {
        return new Date();
    }

    public static Date getCurrentDateFormated() {
        return new Date();
    }

    public static String defaultFormatDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date);
    }

    public static int extractYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.get(Calendar.YEAR);
    }

    public static Date getLastDayOfYear(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        LocalDate lastDayOfYear = LocalDate.of(localDate.getYear(), 12, 31);

        return Date.from(lastDayOfYear.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate getLastDayOfYear(LocalDate date) {
        return LocalDate.of(date.getYear(), 12, 31);
    }

    public static Date getFirstDayOfYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.DAY_OF_YEAR, 1);

        return calendar.getTime();
    }

    public static Date subtractDays(Date date, int daysToSubtract) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.add(Calendar.DAY_OF_MONTH, -daysToSubtract);

        return calendar.getTime();
    }

    public static Date convertToDate(Date inputDate) {
        if (inputDate == null) {
            return null;
        }
        LocalDate localDate = inputDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Date convertLocalDateTimeToDate(LocalDateTime inputDateTime) {
        if (inputDateTime == null) {
            return null;
        }
        return Date.from(inputDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

}
