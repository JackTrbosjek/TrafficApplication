package diplomski.jakov.trafficapplication;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import diplomski.jakov.trafficapplication.base.Application;
import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.models.Enums.RecordType;
import diplomski.jakov.trafficapplication.models.LocalFile;
import diplomski.jakov.trafficapplication.services.GPSService;
import diplomski.jakov.trafficapplication.services.LocalFileService;
import diplomski.jakov.trafficapplication.util.DateFormats;

import static android.app.Activity.RESULT_OK;


public class HomeFragment extends Fragment {

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_TAKE_VIDEO = 2;

    LocalFile localFile;

    View mainView;

    @Inject
    LocalFileService localFileService;

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
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 1);
        }
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
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


    @OnClick(R.id.start_proactive)
    public void startProactiveClick() {
        Intent i = new Intent(getActivity(), CameraPreviewActivity.class);
        RecordType.PROACTIVE.attachTo(i);
        FileType.VIDEO.attachTo(i);
        startActivity(i);
    }

    @OnClick(R.id.start_reactive)
    public void startReactiveClick() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_TAKE_PHOTO || requestCode == REQUEST_TAKE_VIDEO) && resultCode != RESULT_OK) {
            localFile.delete();
        }
    }
}
