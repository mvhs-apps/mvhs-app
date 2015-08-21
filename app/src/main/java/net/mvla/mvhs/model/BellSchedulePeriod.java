package net.mvla.mvhs.model;

public class BellSchedulePeriod {
    public String name;

    public int startHour;
    public int startMinute;
    public int endHour;
    public int endMinute;

    @Override
    public String toString() {
        return name + " " + startHour + ":" + startMinute + "-" + endHour + ":" + endMinute;
    }
}
