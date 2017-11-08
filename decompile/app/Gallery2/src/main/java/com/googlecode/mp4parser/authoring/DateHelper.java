package com.googlecode.mp4parser.authoring;

import java.util.Date;

public class DateHelper {
    public static Date convert(long secondsSince) {
        return new Date((secondsSince - 2082844800) * 1000);
    }

    public static long convert(Date date) {
        return (date.getTime() / 1000) + 2082844800;
    }
}
