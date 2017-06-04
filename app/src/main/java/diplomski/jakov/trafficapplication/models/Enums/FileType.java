package diplomski.jakov.trafficapplication.models.Enums;

import android.content.Intent;

public enum FileType{
    PHOTO,
    VIDEO;
    private static final String name = FileType.class.getName();
    public void attachTo(Intent intent) {
        intent.putExtra(name, ordinal());
    }
    public static FileType detachFrom(Intent intent) {
        if(!intent.hasExtra(name)) throw new IllegalStateException();
        return values()[intent.getIntExtra(name, -1)];
    }
}
