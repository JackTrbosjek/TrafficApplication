package diplomski.jakov.trafficapplication.models;



import com.orm.SugarRecord;

import java.util.Date;

import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.models.Enums.RecordType;


public class LocalFile extends SugarRecord {

    public FileType fileType;

    public String fileName;

    public String fileExtension;

    public String localURI;

    public double latitude;

    public double longitude;

    public float accuracy;

    public String linkToFile;

    public Date dateCreated;

    public boolean sync;

    public RecordType recordType;

    public LocalFile() {
    }
}

