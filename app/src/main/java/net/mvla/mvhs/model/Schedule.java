package net.mvla.mvhs.model;

import java.io.Serializable;
import java.util.List;

public class Schedule implements Serializable {
    public BellSchedule bellSchedule;
    public List<UserPeriodInfo> userPeriodInfos;
}
