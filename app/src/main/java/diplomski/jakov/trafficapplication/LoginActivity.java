package diplomski.jakov.trafficapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import diplomski.jakov.trafficapplication.base.Application;
import diplomski.jakov.trafficapplication.base.BaseActivity;
import diplomski.jakov.trafficapplication.models.LoginModel;
import diplomski.jakov.trafficapplication.services.AuthenticationService;
import diplomski.jakov.trafficapplication.services.PreferenceService;
import diplomski.jakov.trafficapplication.services.UserService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.Manifest.permission.READ_CONTACTS;

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
                hideProgress();
                if (response.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_LONG).show();
                    preferenceService.saveToken(response.body().accessToken);
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
            public void onFailure(Call<LoginModel> call, Throwable t) {
                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                hideProgress();
            }
        });
    }

    private void navigateToMain() {
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.register_button)
    public void goToRegister() {
        Intent i = new Intent(this,RegisterActivity.class);
        startActivity(i);
    }
}

