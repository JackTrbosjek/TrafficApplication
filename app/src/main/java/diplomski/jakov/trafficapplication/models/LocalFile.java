package diplomski.jakov.trafficapplication.models;



import com.orm.SugarRecord;
import com.orm.dsl.Unique;

import java.util.Date;

import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.models.Enums.RecordType;


public class LocalFile extends SugarRecord {
    @Unique
    public String id;

    public FileType fileType;

    public String fileName;

    public String fileExtension;

    public String localURI;

    public double latitude;

    public double longitude;

    public String linkToFile;

    public Date dateCreated;

    public boolean sync;

    public RecordType recordType;

    public LocalFile() {
    }
}

