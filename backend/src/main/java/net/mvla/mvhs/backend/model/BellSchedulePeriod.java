package net.mvla.mvhs.backend.model;

import java.io.Serializable;

public class BellSchedulePeriod implements Comparable, Serializable {
    public String name;

    public int startHour;
    public int startMinute;
    public int endHour;
    public int endMinute;

    @Override
    public String toString() {
        return name + " " + startHour + ":" + startMinute + "-" + endHour + ":" + endMinute;
    }

    @Override
    public int compareTo(Object another) {
        BellSchedulePeriod o = (BellSchedulePeriod) another;

        if (startHour < o.startHour || (startHour == o.startHour && startMinute < o.startMinute)) {
            return -1;
        } else if (startHour == o.startHour && startMinute == o.startMinute) {
            return 0;
        } else {
            return 1;
        }
    }
}
