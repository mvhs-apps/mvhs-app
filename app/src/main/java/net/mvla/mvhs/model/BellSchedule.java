package net.mvla.mvhs.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BellSchedule {

    public String name;
    public List<BellSchedulePeriod> bellSchedulePeriods;

    public BellSchedule() {
        bellSchedulePeriods = new ArrayList<>();
    }

    public void sort() {
        Collections.sort(bellSchedulePeriods);
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
