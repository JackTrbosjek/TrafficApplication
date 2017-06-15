package diplomski.jakov.trafficapplication.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.models.Enums.RecordType;

public class Converters {
    @TypeConverter
    public static String fromRecordType(RecordType recordType){
        return recordType.name();
    }
    @TypeConverter
    public static RecordType toRecordType(String recordType){
        return RecordType.valueOf(recordType);
    }

    @TypeConverter
    public static String fromFileType(FileType fileType){
        return fileType.name();
    }
    @TypeConverter
    public static FileType toFileType(String fileType){
        return FileType.valueOf(fileType);
    }

    @TypeConverter
    public static long fromDate(Date date){
        return date.getTime();
    }
    @TypeConverter
    public static Date toDate(long date){
        return new Date(date);
    }
}
