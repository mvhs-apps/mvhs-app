package net.mvla.mvhs.model;

import java.util.List;

public class Schedule {
    public boolean initialized;

    public BellSchedule bellSchedule;
    public List<StudentPeriodInfo> studentPeriodInfos;

    public void init(Schedule o) {
        bellSchedule = o.bellSchedule;
        studentPeriodInfos = o.studentPeriodInfos;
        initialized = true;
    }
}
