package diplomski.jakov.trafficapplication.database;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.models.Enums.RecordType;

@Entity
public class LocalFile {
    @PrimaryKey(autoGenerate = true)
    public long id;

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

    @Ignore
    public boolean syncInProgress;

    public LocalFile() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalFile localFile = (LocalFile) o;

        return id == localFile.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}

