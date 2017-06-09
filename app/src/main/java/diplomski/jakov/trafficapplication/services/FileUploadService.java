package diplomski.jakov.trafficapplication.services;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import diplomski.jakov.trafficapplication.database.LocalFileDao;
import diplomski.jakov.trafficapplication.models.FileUploadResponse;
import diplomski.jakov.trafficapplication.database.LocalFile;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileUploadService {
    private FileService fileService;
    private Context context;
    private PreferenceService preferenceService;
    private LocalFileDao localFileDao;

    public FileUploadService(FileService fileService, Context context, PreferenceService preferenceService, LocalFileDao localFileDao) {
        this.fileService = fileService;
        this.context = context;
        this.preferenceService = preferenceService;
        this.localFileDao = localFileDao;
    }

    public void uploadFile(final LocalFile localFile) {
        File file = new File(localFile.localURI);
        RequestBody fbody = RequestBody.create(MediaType.parse("image/jpeg"), file);
        fileService.uploadFile("Bearer " + preferenceService.getToken(),
                fbody, localFile.fileName + localFile.fileExtension).enqueue(new Callback<FileUploadResponse>() {
            @Override
            public void onResponse(Call<FileUploadResponse> call, Response<FileUploadResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                    localFile.sync = true;
                    localFile.linkToFile = response.body()._links;
                    localFileDao.updateLocalFile(localFile);
                } else {
                    try {
                        Toast.makeText(context, response.errorBody().string(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<FileUploadResponse> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}
