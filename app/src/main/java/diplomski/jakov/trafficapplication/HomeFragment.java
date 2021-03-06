package diplomski.jakov.trafficapplication;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;
import diplomski.jakov.trafficapplication.base.Application;
import diplomski.jakov.trafficapplication.database.LocalFileDao;
import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.models.Enums.RecordType;
import diplomski.jakov.trafficapplication.models.Enums.TimeUnits;
import diplomski.jakov.trafficapplication.models.Enums.VideoDurationUnits;
import diplomski.jakov.trafficapplication.database.LocalFile;
import diplomski.jakov.trafficapplication.services.LocalFileService;
import diplomski.jakov.trafficapplication.services.PreferenceService;
import diplomski.jakov.trafficapplication.services.ProactiveService;
import diplomski.jakov.trafficapplication.services.ReactiveService;
import diplomski.jakov.trafficapplication.services.TrafficJamGPSService;
import diplomski.jakov.trafficapplication.services.UserService;
import diplomski.jakov.trafficapplication.util.Util;

import static android.app.Activity.RESULT_OK;


public class HomeFragment extends Fragment {

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_TAKE_VIDEO = 2;

    LocalFile localFile;

    View mainView;


    @Inject
    LocalFileService localFileService;

    @Inject
    LocalFileDao localFileDao;

    @Inject
    PreferenceService preferenceService;

    @Inject
    UserService userService;


    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((Application) getActivity().getApplication()).getNetComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        mainView = view;
        setDefaultValues();
        return view;
    }

    private void setDefaultValues() {
        FileType fileType = preferenceService.getProactiveType();
        proactiveTypeSp.setSelection(fileType.ordinal());
        if(fileType == FileType.VIDEO){
            videoDurationLayout.setVisibility(View.VISIBLE);
        }
        intervalEt.setText(preferenceService.getProactiveInterval());
        everyUnitsSp.setSelection(preferenceService.getProactiveEveryUnits().ordinal());
        forDurationEt.setText(preferenceService.getProactiveForDuration());
        forUnitsSp.setSelection(preferenceService.getProactiveForUnits().ordinal());
        forDurationEt.setText(preferenceService.getProactiveForDuration());
        reactiveSuddenStoppingCb.setChecked(preferenceService.getReactiveSuddenStopping());
        reactiveTrafficJamCb.setChecked(preferenceService.getReactiveTrafficJam());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.isMyServiceRunning(ProactiveService.class, getActivity())) {
            proactiveOptionsLayout.setVisibility(View.GONE);
            proactiveLayout.setVisibility(View.VISIBLE);
            proactiveSwitch.setChecked(true);
            btnStartProactive.setText("Stop Service");
        }else{
            btnStartProactive.setText("Start Service");
            proactiveOptionsLayout.setVisibility(View.VISIBLE);
        }
        if (Util.isMyServiceRunning(ReactiveService.class, getActivity())) {
            reactiveOptionsLayout.setVisibility(View.GONE);
            reactiveLayout.setVisibility(View.VISIBLE);
            reactiveSwitch.setChecked(true);
            startReactiveBtn.setText("Stop Service");
        }else{
            startReactiveBtn.setText("Start Service");
            reactiveOptionsLayout.setVisibility(View.VISIBLE);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            LocalFileService.FileModel photoFile = localFileService.createImageFile(RecordType.USER);
            if (photoFile != null && photoFile.file != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "diplomski.jakov.trafficapplication.fileprovider",
                        photoFile.file);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                localFile = photoFile.localFile;
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            LocalFileService.FileModel videoFile = localFileService.createVideoFile(RecordType.USER);
            if (videoFile != null && videoFile.file != null) {
                localFile = videoFile.localFile;
                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                startActivityForResult(takeVideoIntent, REQUEST_TAKE_VIDEO);
            }
        }
    }

    @OnClick(R.id.camera_picture)
    public void cameraClick() {
        dispatchTakePictureIntent();
    }

    @OnClick(R.id.camera_video)
    public void videoClick() {
        dispatchTakeVideoIntent();
    }


    //region ProactiveMode
    @BindView(R.id.proactive_type)
    Spinner proactiveTypeSp;
    @BindView(R.id.interval)
    EditText intervalEt;
    @BindView(R.id.every_units)
    Spinner everyUnitsSp;
    @BindView(R.id.for_duration)
    EditText forDurationEt;
    @BindView(R.id.for_units)
    Spinner forUnitsSp;
    @BindView(R.id.proactive_options_layout)
    LinearLayout proactiveOptionsLayout;
    @BindView(R.id.start_proactive)
    Button btnStartProactive;
    @BindView(R.id.proactive_layout)
    LinearLayout proactiveLayout;
    @BindView(R.id.video_duration_layout)
    LinearLayout videoDurationLayout;
    @BindView(R.id.proactive_mode_switch)
    Switch proactiveSwitch;

    @OnClick(R.id.proactive_mode_switch)
    public void onProactiveClick(Switch aSwitch) {
        if (aSwitch.isChecked()) {
            proactiveLayout.setVisibility(View.VISIBLE);
        } else {
            proactiveLayout.setVisibility(View.GONE);
        }
    }

    @OnItemSelected(R.id.proactive_type)
    public void onProactiveTypeClick(Spinner typeSpinner) {
        FileType fileType = FileType.from(typeSpinner.getSelectedItemPosition());
        switch (fileType) {
            case PHOTO:
                videoDurationLayout.setVisibility(View.GONE);
                break;
            case VIDEO:
                videoDurationLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    @OnClick(R.id.start_proactive)
    public void startProactiveClick() {
        Util.hideKeyboard(getActivity());
        if (Util.isMyServiceRunning(ProactiveService.class, getActivity())) {
            Intent i = new Intent(getActivity(), ProactiveService.class);
            getActivity().stopService(i);
            btnStartProactive.setText("Start Service");
            proactiveOptionsLayout.setVisibility(View.VISIBLE);
            return;
        }
        proactiveOptionsLayout.setVisibility(View.GONE);
        btnStartProactive.setText("Stop Service");
        FileType fileType = FileType.from(proactiveTypeSp.getSelectedItemPosition());

        int interval = TextUtils.isEmpty(intervalEt.getText()) ? 5 : Integer.parseInt(intervalEt.getText().toString());
        TimeUnits timeUnits = TimeUnits.from(everyUnitsSp.getSelectedItemPosition());
        int forInterval = TextUtils.isEmpty(forDurationEt.getText()) ? 5 : Integer.parseInt(forDurationEt.getText().toString());
        VideoDurationUnits videoDurationUnits = VideoDurationUnits.from(forUnitsSp.getSelectedItemPosition());
        Intent i = new Intent(getActivity(), ProactiveService.class);
        i.putExtra(ProactiveService.ARG_INTERVAL, interval);
        i.putExtra(ProactiveService.ARG_FOR_INTERVAL, forInterval);
        fileType.attachTo(i);
        timeUnits.attachTo(i);
        videoDurationUnits.attachTo(i);
        getActivity().startService(i);
        btnStartProactive.setText("Stop Service");
        userService.checkAuthorizationAndStartSync(getActivity());
    }
    //endregion

    //region Reactive Mode
    @BindView(R.id.reactive_traffic_jam)
    CheckBox reactiveTrafficJamCb;
    @BindView(R.id.reactive_sudden_stopping)
    CheckBox reactiveSuddenStoppingCb;
    @BindView(R.id.reactive_jam_duration)
    EditText reactiveTrafficJamDurationEt;
    @BindView(R.id.start_reactive)
    Button startReactiveBtn;
    @BindView(R.id.reactive_layout)
    LinearLayout reactiveLayout;
    @BindView(R.id.reactive_mode_switch)
    Switch reactiveSwitch;
    @BindView(R.id.reactive_options_layout)
    LinearLayout reactiveOptionsLayout;

    @OnClick(R.id.start_reactive)
    public void startReactiveClick() {
        if (Util.isMyServiceRunning(ReactiveService.class, getActivity())) {
            Intent i = new Intent(getActivity(),ReactiveService.class);
            getActivity().stopService(i);
            startReactiveBtn.setText("Start Service");
            reactiveOptionsLayout.setVisibility(View.VISIBLE);
            return;
        }
        reactiveOptionsLayout.setVisibility(View.GONE);
        startReactiveBtn.setText("Stop Service");

        Intent i = new Intent(getActivity(), ReactiveService.class);
        i.putExtra(ReactiveService.ARG_TRAFFIC_JAM, reactiveTrafficJamCb.isChecked());
        i.putExtra(ReactiveService.ARG_SUDDEN_STOPPING, reactiveSuddenStoppingCb.isChecked());
        if (reactiveTrafficJamCb.isChecked()) {
            long trafficJamDuration = reactiveTrafficJamDurationEt.getText().length() > 0 ? Long.parseLong(reactiveTrafficJamDurationEt.getText().toString()) * 1000 : 60 * 1000;
            i.putExtra(TrafficJamGPSService.TRAFFIC_JAM_DURATION_ARG, trafficJamDuration);
        }
        getActivity().startService(i);
        userService.checkAuthorizationAndStartSync(getActivity());
    }

    @OnClick(R.id.reactive_mode_switch)
    public void onReactiveClick(Switch proactiveSwitch) {
        LinearLayout layout = (LinearLayout) mainView.findViewById(R.id.reactive_layout);
        if (proactiveSwitch.isChecked()) {
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.reactive_traffic_jam)
    public void onTrafficJamClick(CheckBox checkBox){
        if(checkBox.isChecked()){
            reactiveTrafficJamDurationEt.setVisibility(View.VISIBLE);
        }else {
            reactiveTrafficJamDurationEt.setVisibility(View.GONE);
        }
    }
    //endregion


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_TAKE_PHOTO || requestCode == REQUEST_TAKE_VIDEO) && resultCode != RESULT_OK) {
            localFileDao.deleteLocalFile(localFile);
        }
        if (requestCode == REQUEST_TAKE_VIDEO && resultCode == RESULT_OK) {
            LocalFile local = localFileDao.getLocalFile(localFile.id);
            local.localURI = data.getData().getPath();
            localFileDao.updateLocalFile(local);
        }
    }


}
