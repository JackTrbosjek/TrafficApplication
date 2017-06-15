package diplomski.jakov.trafficapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import diplomski.jakov.trafficapplication.base.Application;
import diplomski.jakov.trafficapplication.base.BaseActivity;
import diplomski.jakov.trafficapplication.rest.services.AuthenticationService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivateActivity extends BaseActivity {
    @Inject
    AuthenticationService authenticationService;

    @BindView(R.id.activationResponse)
    TextView activateView;

    private String activationToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate);
        ButterKnife.bind(this);
        ((Application) getApplication()).getNetComponent().inject(this);
        String tokenUrl = getIntent().getDataString();
        activationToken = tokenUrl.substring(tokenUrl.lastIndexOf("token=") + 6);
    }

    @OnClick(R.id.activate_button)
    public void activate() {
        showProgress();
        authenticationService.activate(activationToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                hideProgress();
                if(response.isSuccessful()){
                    Toast.makeText(ActivateActivity.this,"Activation Successful!",Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(ActivateActivity.this,LoginActivity.class);
                    startActivity(i);
                }else{
                    activateView.setText("Error while activating you account!");
                    try {
                        String error = response.errorBody().string();
                        activateView.append("\n"+error);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                hideProgress();
            }
        });
    }
}
