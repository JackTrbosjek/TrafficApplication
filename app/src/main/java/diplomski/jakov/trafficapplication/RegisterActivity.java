package diplomski.jakov.trafficapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import diplomski.jakov.trafficapplication.base.Application;
import diplomski.jakov.trafficapplication.base.BaseActivity;
import diplomski.jakov.trafficapplication.models.RegisterModel;
import diplomski.jakov.trafficapplication.models.RegisterResponseModel;
import diplomski.jakov.trafficapplication.services.AuthenticationService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {

    @Inject
    AuthenticationService authenticationService;

    @BindView(R.id.email)
    EditText mEmailView;
    @BindView(R.id.password)
    EditText mPasswordView;
    @BindView(R.id.confirm_password)
    EditText mConfirmPasswordView;
    @BindView(R.id.register_form)
    View mRegisterFormView;
    @BindView(R.id.registration_message)
    TextView mRegistrationMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        ((Application) getApplication()).getNetComponent().inject(this);
    }

    @OnClick(R.id.register_button)
    public void register() {
        RegisterModel registerModel = new RegisterModel(mEmailView.getText().toString(),mPasswordView.getText().toString(),mConfirmPasswordView.getText().toString());
        if (!validateRegisterData(registerModel)) {
            return;
        }
        authenticationService.register(registerModel).enqueue(
                new Callback<RegisterResponseModel>() {
                    @Override
                    public void onResponse(Call<RegisterResponseModel> call, Response<RegisterResponseModel> response) {
                        if(response.isSuccessful()){
                            mRegisterFormView.setVisibility(View.GONE);
                            mRegistrationMessageView.setVisibility(View.VISIBLE);
                            mRegistrationMessageView.setText("Registration successful. Please check your email for activation link and open it with Traffic Application.");
                        }else{
                            mRegistrationMessageView.setVisibility(View.VISIBLE);
                            try {
                                mRegistrationMessageView.setText(response.errorBody().string());
                            } catch (IOException e) {
                                mRegistrationMessageView.setText("Registration Error.");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<RegisterResponseModel> call, Throwable t) {
                        Toast.makeText(RegisterActivity.this,"Registration Error. Try again.",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateRegisterData(RegisterModel registerModel) {
        boolean valid;
        //Email Validation
        if (TextUtils.isEmpty(registerModel.email)) {
            mEmailView.setError("Email is required!");
            valid = false;
        } else {
            valid = Patterns.EMAIL_ADDRESS.matcher(registerModel.email).matches();
            if(!valid){
                mEmailView.setError("Email not valid!");
            }
        }
        if(TextUtils.isEmpty(registerModel.password)){
            mPasswordView.setError("Password is required");
            valid = false;
        }
        if(TextUtils.isEmpty(registerModel.confirmPassword)){
            mConfirmPasswordView.setError("Confirm Password is required");
            valid = false;
        }
        if(valid && registerModel.password.length() < 8){
            mPasswordView.setError("Password to short!");
            valid = false;
        }
        if(!TextUtils.equals(registerModel.password, registerModel.confirmPassword) && valid){
            mConfirmPasswordView.setError("Password and Confirm Password must match!");
            valid = false;
        }
        return valid;
    }
}
