package diplomski.jakov.trafficapplication;

import android.content.Intent;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import diplomski.jakov.trafficapplication.base.Application;
import diplomski.jakov.trafficapplication.base.BaseActivity;
import diplomski.jakov.trafficapplication.rest.models.LoginModel;
import diplomski.jakov.trafficapplication.rest.models.UserInfoResponse;
import diplomski.jakov.trafficapplication.rest.services.AuthenticationService;
import diplomski.jakov.trafficapplication.services.PreferenceService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {
    @Inject
    AuthenticationService authenticationService;

    @Inject
    PreferenceService preferenceService;

    @BindView(R.id.email)
    EditText mEmailView;
    @BindView(R.id.password)
    EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        ((Application) getApplication()).getNetComponent().inject(this);
    }

    @OnClick(R.id.login_button)
    public void login() {
        showProgress();
        authenticationService.login(mEmailView.getText().toString(), mPasswordView.getText().toString(), "password").enqueue(new Callback<LoginModel>() {
            @Override
            public void onResponse(Call<LoginModel> call, Response<LoginModel> response) {
                if (response.isSuccessful()) {
                    preferenceService.saveToken(response.body().accessToken);
                    getUserInfo(response.body().accessToken);
                } else {
                    hideProgress();
                    try {
                        Toast.makeText(LoginActivity.this, response.errorBody().string(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginModel> call, Throwable t) {
                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                hideProgress();
            }
        });
    }

    private void getUserInfo(String accessToken) {
        authenticationService.userInfo("Bearer " + accessToken).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                hideProgress();
                if (response.isSuccessful()) {
                    preferenceService.saveUsername(response.body().userName);
                    preferenceService.saveUserId(response.body().id);
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_LONG).show();
                    navigateToMain();
                } else {
                    try {
                        Toast.makeText(LoginActivity.this, response.errorBody().string(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                hideProgress();
            }
        });
    }

    private void navigateToMain() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.register_button)
    public void goToRegister() {
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }
}

