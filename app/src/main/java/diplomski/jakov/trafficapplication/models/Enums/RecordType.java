package diplomski.jakov.trafficapplication.models.Enums;

import android.content.Intent;

public enum RecordType {
    USER,
    REACTIVE,
    PROACTIVE;
    private static final String name = RecordType.class.getName();
    public void attachTo(Intent intent) {
        intent.putExtra(name, ordinal());
    }
    public static RecordType detachFrom(Intent intent) {
        if(!intent.hasExtra(name)) throw new IllegalStateException();
        return values()[intent.getIntExtra(name, -1)];
    }
}
