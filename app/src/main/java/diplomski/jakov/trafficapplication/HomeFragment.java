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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import diplomski.jakov.trafficapplication.base.Application;
import diplomski.jakov.trafficapplication.database.LocalFileDao;
import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.models.Enums.RecordType;
import diplomski.jakov.trafficapplication.models.Enums.TimeUnits;
import diplomski.jakov.trafficapplication.models.Enums.VideoDurationUnits;
import diplomski.jakov.trafficapplication.database.LocalFile;
import diplomski.jakov.trafficapplication.services.LocalFileService;
import diplomski.jakov.trafficapplication.services.ProactiveService;

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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isMyServiceRunning(ProactiveService.class)) {
            proactiveOptionsLayout.setVisibility(View.GONE);
            btnStartProactive.setText("Stop Service");
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
                Uri videoURI = FileProvider.getUriForFile(getActivity(),
                        "diplomski.jakov.trafficapplication.fileprovider",
                        videoFile.file);
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
                localFile = videoFile.localFile;
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

    @OnClick(R.id.proactive_mode_switch)
    public void onProactiveClick(Switch proactiveSwitch) {
        LinearLayout layout = (LinearLayout) mainView.findViewById(R.id.proactive_layout);
        if (proactiveSwitch.isChecked()) {
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.GONE);
        }
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

    @OnClick(R.id.start_proactive)
    public void startProactiveClick() {
        if (isMyServiceRunning(ProactiveService.class)) {
            Intent i = new Intent(getActivity(), ProactiveService.class);
            getActivity().stopService(i);
            btnStartProactive.setText("Start Service");
            return;
        }
        proactiveOptionsLayout.setVisibility(View.VISIBLE);
        FileType fileType;
        switch (proactiveTypeSp.getSelectedItemPosition()) {
            case 0:
                fileType = FileType.PHOTO;
                break;
            case 1:
                fileType = FileType.VIDEO;
                break;
            default:
                return;
        }
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

    }

    @OnClick(R.id.start_reactive)
    public void startReactiveClick() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_TAKE_PHOTO || requestCode == REQUEST_TAKE_VIDEO) && resultCode != RESULT_OK) {
            localFileDao.deleteLocalFile(localFile);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
