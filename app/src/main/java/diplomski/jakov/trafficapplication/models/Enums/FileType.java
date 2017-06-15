package diplomski.jakov.trafficapplication.models.Enums;

import android.content.Intent;

public enum FileType{
    PHOTO,
    VIDEO;
    public static FileType from(int position) {
        switch (position) {
            case 0:
                return PHOTO;
            case 1:
                return VIDEO;
            default:
                return null;
        }
    }
    private static final String name = FileType.class.getName();
    public void attachTo(Intent intent) {
        intent.putExtra(name, ordinal());
    }
    public static FileType detachFrom(Intent intent) {
        if(!intent.hasExtra(name)) throw new IllegalStateException();
        return values()[intent.getIntExtra(name, -1)];
    }
}
