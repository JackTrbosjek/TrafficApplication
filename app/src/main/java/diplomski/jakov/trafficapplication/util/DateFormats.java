package diplomski.jakov.trafficapplication.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

public final class DateFormats {
    public static final SimpleDateFormat TimeStamp =new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    public static final SimpleDateFormat DateFormat =new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
}
