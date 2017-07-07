package diplomski.jakov.trafficapplication.services;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import diplomski.jakov.trafficapplication.R;
import diplomski.jakov.trafficapplication.database.LocalFileDao;
import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.rest.models.DynamicFileModel;
import diplomski.jakov.trafficapplication.rest.models.FileUploadResponse;
import diplomski.jakov.trafficapplication.database.LocalFile;
import diplomski.jakov.trafficapplication.rest.models.LoginModel;
import diplomski.jakov.trafficapplication.rest.models.RegisterResponseModel;
import diplomski.jakov.trafficapplication.rest.models.UserInfoResponse;
import diplomski.jakov.trafficapplication.rest.services.AuthenticationService;
import diplomski.jakov.trafficapplication.rest.services.DynamicResourceService;
import diplomski.jakov.trafficapplication.rest.services.FileService;
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
    private AuthenticationService authenticationService;
    private DynamicResourceService dynamicResourceService;


    public FileUploadService(FileService fileService, Context context, PreferenceService preferenceService, LocalFileDao localFileDao, AuthenticationService authenticationService, DynamicResourceService dynamicResourceService) {
        this.fileService = fileService;
        this.context = context;
        this.preferenceService = preferenceService;
        this.localFileDao = localFileDao;
        this.authenticationService = authenticationService;
        this.dynamicResourceService = dynamicResourceService;
    }

    public void uploadFile(final LocalFile localFile, final OnFileUploadListener listener) {
        File file = new File(localFile.localURI);
        RequestBody fbody = RequestBody.create(MediaType.parse("image/jpeg"), file);
        fileService.uploadFile(preferenceService.getAuthorization(),
                fbody, localFile.fileName + localFile.fileExtension).enqueue(new Callback<FileUploadResponse>() {
            @Override
            public void onResponse(Call<FileUploadResponse> call, Response<FileUploadResponse> response) {
                if (response.isSuccessful()) {
                    localFile.sync = true;
                    localFile.linkToFile = "https://api.baasic.com/v1/traffic-application/file-streams/" + response.body().id;
                    uploadDynamicResource(localFile, listener);

                } else {
                    if (response.code() == 401) {
                        listener.unauthorized();
                    } else {
                        try {
                            Toast.makeText(context, response.errorBody().string(), Toast.LENGTH_LONG).show();
                            listener.onError();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<FileUploadResponse> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
                listener.onError();
            }
        });
    }

    private void uploadDynamicResource(final LocalFile localFile, final OnFileUploadListener listener) {
        DynamicFileModel model = new DynamicFileModel();
        model.id = "";
        model.fileUrl = localFile.linkToFile;
        model.isImage = localFile.fileType == FileType.PHOTO;
        model.userId = preferenceService.getUserId();
        model.latitude = localFile.latitude;
        model.longitude = localFile.longitude;
        model.dateCreated = localFile.dateCreated.getTime();
        dynamicResourceService.createFile(preferenceService.getAuthorization(), model).enqueue(new Callback<RegisterResponseModel>() {
            @Override
            public void onResponse(Call<RegisterResponseModel> call, Response<RegisterResponseModel> response) {
                if (response.isSuccessful()) {
                    localFileDao.updateLocalFile(localFile);
                    listener.onSuccess();
                } else {
                    try {
                        Toast.makeText(context, response.errorBody().string(), Toast.LENGTH_LONG).show();
                        listener.onError();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterResponseModel> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
                listener.onError();
            }
        });

    }

    public void checkLogin(final Activity dialogContext, final OnCheckLogin onCheckLogin) {

        authenticationService.userInfo(preferenceService.getAuthorization()).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                if (response.isSuccessful()) {
                    onCheckLogin.loginOK();
                } else {
                    if (response.code() == 401) {
                        showLoginDialog(dialogContext, onCheckLogin);
                    } else {
                        try {
                            Toast.makeText(context, response.errorBody().string(), Toast.LENGTH_LONG).show();
                            onCheckLogin.invalidLogin();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void showLoginDialog(Activity dialogContext, final OnCheckLogin onCheckLogin) {
        LayoutInflater li = (LayoutInflater) dialogContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View promptsView = li.inflate(R.layout.login_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                dialogContext);
        alertDialogBuilder.setView(promptsView);

        final EditText emailEt = (EditText) promptsView
                .findViewById(R.id.email);
        emailEt.setText(preferenceService.getUsername());
        final EditText passwordEt = (EditText) promptsView
                .findViewById(R.id.password);

        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                login(emailEt.getText().toString(), passwordEt.getText().toString(), dialogInterface, onCheckLogin);
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        alertDialogBuilder.create().show();
    }

    private void login(String email, String password, final DialogInterface dialogInterface, final OnCheckLogin onCheckLogin) {
        authenticationService.login(email, password, "password").enqueue(new Callback<LoginModel>() {
            @Override
            public void onResponse(Call<LoginModel> call, Response<LoginModel> response) {
                if (response.isSuccessful()) {
                    preferenceService.saveToken(response.body().accessToken);
                    dialogInterface.dismiss();
                    onCheckLogin.loginOK();
                    Toast.makeText(context, "Login Successful", Toast.LENGTH_LONG).show();
                } else {
                    onCheckLogin.invalidLogin();
                    try {
                        Toast.makeText(context, response.errorBody().string(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginModel> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public interface OnCheckLogin{
        void loginOK();
        void invalidLogin();
    }

    public interface OnFileUploadListener {
        void onSuccess();

        void unauthorized();

        void onError();
    }

}
