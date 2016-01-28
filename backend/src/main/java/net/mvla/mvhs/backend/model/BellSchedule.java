package net.mvla.mvhs.backend.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BellSchedule implements Serializable {

    public String name;
    public List<BellSchedulePeriod> bellSchedulePeriods;

    public BellSchedule() {
        bellSchedulePeriods = new ArrayList<>();
    }

    public void addPeriod(String name) {
        BellSchedulePeriod period = new BellSchedulePeriod();
        period.name = name;
        bellSchedulePeriods.add(period);
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
