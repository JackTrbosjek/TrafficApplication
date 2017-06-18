package diplomski.jakov.trafficapplication;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import butterknife.OnItemSelected;
import diplomski.jakov.trafficapplication.base.Application;
import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.models.Enums.TimeUnits;
import diplomski.jakov.trafficapplication.models.Enums.VideoDurationUnits;
import diplomski.jakov.trafficapplication.services.PreferenceService;


public class SettingsFragment extends Fragment {

    @Inject
    PreferenceService preferenceService;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        loadPrefs();
        return view;
    }

    private void loadPrefs() {
        syncProactiveCb.setChecked(preferenceService.getSyncProactive());
        syncReactiveCb.setChecked(preferenceService.getSyncReactive());
        syncUserCb.setChecked(preferenceService.getSyncUser());
        syncWifiCb.setChecked(preferenceService.getSyncWifiOnly());
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
        suddenStoppingCb.setChecked(preferenceService.getReactiveSuddenStopping());
        trafficJamCb.setChecked(preferenceService.getReactiveTrafficJam());
    }

    @BindView(R.id.sync_proactive)
    CheckBox syncProactiveCb;
    @BindView(R.id.sync_reactive)
    CheckBox syncReactiveCb;
    @BindView(R.id.sync_user)
    CheckBox syncUserCb;
    @BindView(R.id.sync_wifi)
    CheckBox syncWifiCb;
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
    @BindView(R.id.reactive_sudden_stopping)
    CheckBox suddenStoppingCb;
    @BindView(R.id.reactive_traffic_jam)
    CheckBox trafficJamCb;
    @BindView(R.id.video_duration_layout)
    LinearLayout videoDurationLayout;


    @OnClick(R.id.save)
    public void savePrefs() {
        boolean syncProactive = syncProactiveCb.isChecked();
        boolean syncReactive = syncReactiveCb.isChecked();
        boolean syncUser = syncUserCb.isChecked();
        boolean syncWifi = syncWifiCb.isChecked();
        FileType fileType = FileType.from(proactiveTypeSp.getSelectedItemPosition());
        String interval = intervalEt.getText().toString();
        TimeUnits timeUnits = TimeUnits.from(everyUnitsSp.getSelectedItemPosition());
        String forInterval = forDurationEt.getText().toString();
        VideoDurationUnits videoDurationUnits = VideoDurationUnits.from(forUnitsSp.getSelectedItemPosition());
        boolean suddenStopping = suddenStoppingCb.isChecked();
        boolean trafficJam = trafficJamCb.isChecked();
        preferenceService.saveAllSettings(syncReactive, syncProactive, syncUser, syncWifi, fileType, interval, timeUnits, forInterval, videoDurationUnits, suddenStopping, trafficJam);
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
}
