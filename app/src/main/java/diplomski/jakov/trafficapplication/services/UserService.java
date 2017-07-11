package diplomski.jakov.trafficapplication.services;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import diplomski.jakov.trafficapplication.MainActivity;
import diplomski.jakov.trafficapplication.R;
import diplomski.jakov.trafficapplication.rest.models.LoginModel;
import diplomski.jakov.trafficapplication.rest.models.UserInfoResponse;
import diplomski.jakov.trafficapplication.rest.services.AuthenticationService;
import diplomski.jakov.trafficapplication.util.Connection;
import diplomski.jakov.trafficapplication.util.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserService {
    private FileUploadService fileUploadService;
    private PreferenceService preferenceService;
    private AuthenticationService authenticationService;
    private Context context;

    public UserService(FileUploadService fileUploadService, PreferenceService preferenceService, AuthenticationService authenticationService, Context context) {
        this.fileUploadService = fileUploadService;
        this.preferenceService = preferenceService;
        this.authenticationService = authenticationService;
        this.context = context;
    }

    public void checkAuthorizationAndStartSync(final Activity activity) {
        if (!Util.isMyServiceRunning(SyncService.class, activity) && Connection.isInternetConnected(activity) && preferenceService.getSyncWifiOnly() && Connection.isWiFiConnection(activity)) {
            if (preferenceService.tokenExpired()) {
                checkLogin(activity, new UserService.OnCheckLogin() {
                    @Override
                    public void loginOK() {
                        Intent i = new Intent(activity, SyncService.class);
                        activity.startService(i);
                    }

                    @Override
                    public void invalidLogin() {

                    }
                });
            } else {
                Intent i = new Intent(context, SyncService.class);
                context.startService(i);
            }
        }
    }

    public void checkAuthorizationAndStartSync() {
        if (!Util.isMyServiceRunning(SyncService.class, context) && Connection.isInternetConnected(context) && preferenceService.getSyncWifiOnly() && Connection.isWiFiConnection(context)) {
            if (preferenceService.tokenExpired()) {
                checkLogin(null, new UserService.OnCheckLogin() {
                    @Override
                    public void loginOK() {
                        Intent i = new Intent(context, SyncService.class);
                        context.startService(i);
                    }

                    @Override
                    public void invalidLogin() {

                    }
                });
            } else {
                Intent i = new Intent(context, SyncService.class);
                context.startService(i);
            }
        }
    }

    private void checkLogin(final Activity dialogContext, final OnCheckLogin onCheckLogin) {

        authenticationService.userInfo(preferenceService.getAuthorization()).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                if (response.isSuccessful()) {
                    onCheckLogin.loginOK();
                } else {
                    if (response.code() == 401 && dialogContext != null) {
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
                    preferenceService.saveTokenExpiration(response.body().expiresIn);
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

    public interface OnCheckLogin {
        void loginOK();

        void invalidLogin();
    }
}
