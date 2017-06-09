package diplomski.jakov.trafficapplication.models.Enums;

import android.content.Intent;

public enum TimeUnits {
    SEC, MIN, HOUR;

    public static TimeUnits from(int position) {
        switch (position) {
            case 0:
                return SEC;
            case 1:
                return MIN;
            case 2:
                return HOUR;
            default:
                return null;
        }
    }
    private static final String name = TimeUnits.class.getName();
    public void attachTo(Intent intent) {
        intent.putExtra(name, ordinal());
    }
    public static TimeUnits detachFrom(Intent intent) {
        if(!intent.hasExtra(name)) throw new IllegalStateException();
        return values()[intent.getIntExtra(name, -1)];
    }
}
