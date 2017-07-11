package diplomski.jakov.trafficapplication.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface LocalFileDao {
    @Insert
    long insertLocalFile(LocalFile localFile);

    @Update
    void updateLocalFile(LocalFile localFile);

    @Delete
    void deleteLocalFile(LocalFile localFile);

    @Query("SELECT * FROM localFile WHERE id = :id")
    LocalFile getLocalFile(long id);

    @Query("SELECT * FROM localFile ORDER BY dateCreated DESC")
    List<LocalFile> getAllLocalFiles();

    @Query("SELECT * FROM localFile WHERE sync=0 AND latitude!=0 AND longitude!=0 ORDER BY dateCreated DESC")
    List<LocalFile> getAllUnsyncedLocalFiles();
}
