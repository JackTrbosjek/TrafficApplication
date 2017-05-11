package diplomski.jakov.trafficapplication.base;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    protected ProgressDialog progressDialog;

    protected void showProgress() {
        progressDialog = ProgressDialog.show(this, "Loading", "Please Wait", true);
    }

    protected void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
