package net.mvla.mvhs.model;

import java.util.ArrayList;
import java.util.List;

public class BellSchedule {

    public String name;
    public List<BellSchedulePeriod> bellSchedulePeriods;

    public BellSchedule() {
        bellSchedulePeriods = new ArrayList<>();
    }

    @Override
    public String toString() {
        String periods = "";
        for (BellSchedulePeriod period : bellSchedulePeriods) {
            periods += period.toString() + "\n";
        }
        return name + "\n" + periods;
    }
}
