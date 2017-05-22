package diplomski.jakov.trafficapplication.services;

import diplomski.jakov.trafficapplication.models.FileUploadResponse;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface FileService {

    @Multipart
    @POST("file-streams/{filePath}")
    Call<FileUploadResponse> uploadFile(@Header("Authorization") String authorization, @Part("file\"; filename=\"image.jpg") RequestBody file, @Path("filePath") String filePath);
}
