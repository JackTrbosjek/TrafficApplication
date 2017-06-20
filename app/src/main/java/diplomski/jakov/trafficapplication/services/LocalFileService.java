package diplomski.jakov.trafficapplication.services;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import diplomski.jakov.trafficapplication.database.LocalFileDao;
import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.models.Enums.RecordType;
import diplomski.jakov.trafficapplication.database.LocalFile;
import diplomski.jakov.trafficapplication.util.DateFormats;

public class LocalFileService {
    private Context mContext;
    private LocalFileDao localFileDao;

    public LocalFileService(Context context, LocalFileDao localFileDao) {
        mContext = context;
        this.localFileDao = localFileDao;
    }

    public FileModel createImageFileFromBytes(RecordType recordType, byte[] bytes) {
        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Setting post rotate to 90
            Matrix mtx = new Matrix();
            mtx.postRotate(90);
            // Rotating Bitmap
            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mtx, true);
        } else {// LANDSCAPE MODE
        }
        String timeStamp = DateFormats.TimeStamp.format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            FileOutputStream fos = new FileOutputStream(image);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
        LocalFile localFile = new LocalFile();
        localFile.fileType = FileType.PHOTO;
        localFile.fileName = imageFileName;
        localFile.fileExtension = ".jpg";
        localFile.localURI = image.getAbsolutePath();
        localFile.dateCreated = new Date();
        localFile.sync = false;
        localFile.recordType = recordType;
        localFile.id = localFileDao.insertLocalFile(localFile);

        startLocationService(localFile.id);

        return new FileModel(image,localFile);
    }

    public FileModel createImageFile(RecordType recordType) {
        // Create an image file name
        String timeStamp = DateFormats.TimeStamp.format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


        LocalFile localFile = new LocalFile();
        localFile.fileType = FileType.PHOTO;
        localFile.fileName = imageFileName;
        localFile.fileExtension = ".jpg";
        localFile.localURI = image.getAbsolutePath();
        localFile.dateCreated = new Date();
        localFile.sync = false;
        localFile.recordType = recordType;
        localFile.id = localFileDao.insertLocalFile(localFile);

        startLocationService(localFile.id);
        return new FileModel(image, localFile);
    }

    public FileModel createVideoFile(RecordType recordType) {
        // Create an image file name
        String timeStamp = DateFormats.TimeStamp.format(new Date());
        String videoFileName = "MP4_" + timeStamp;
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File video = null;
        try {
            video = File.createTempFile(
                    videoFileName,  /* prefix */
                    ".mp4",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        LocalFile localFile = new LocalFile();
        localFile.fileType = FileType.VIDEO;
        localFile.fileName = videoFileName;
        localFile.fileExtension = ".mp4";
        localFile.localURI = video.getAbsolutePath();
        localFile.dateCreated = new Date();
        localFile.sync = false;
        localFile.recordType = recordType;
        localFile.id  = localFileDao.insertLocalFile(localFile);

        startLocationService(localFile.id);
        return new FileModel(video, localFile);
    }

    private void startLocationService(long id) {
        Intent i = new Intent(mContext, LocalFileGPSService.class);
        i.putExtra(LocalFileGPSService.FILE_ID_ARG, id);
        mContext.startService(i);
    }

    public class FileModel {
        public File file;
        public LocalFile localFile;

        private FileModel(File file, LocalFile localFile) {
            this.file = file;
            this.localFile = localFile;
        }
    }
}
