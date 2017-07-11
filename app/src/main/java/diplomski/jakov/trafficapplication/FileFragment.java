package diplomski.jakov.trafficapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import diplomski.jakov.trafficapplication.base.Application;
import diplomski.jakov.trafficapplication.database.LocalFile;
import diplomski.jakov.trafficapplication.database.LocalFileDao;
import diplomski.jakov.trafficapplication.services.FileUploadService;
import diplomski.jakov.trafficapplication.services.SyncService;
import diplomski.jakov.trafficapplication.util.Util;

import javax.inject.Inject;

import static diplomski.jakov.trafficapplication.services.SyncService.ARG_FILE_ID;

public class FileFragment extends Fragment implements MyFileRecyclerViewAdapter.OnItemInteractionListener {
    @Inject
    FileUploadService fileUploadService;

    @Inject
    LocalFileDao localFileDao;

    MyFileRecyclerViewAdapter adapter;

    private boolean mBound;

    public FileFragment() {
    }

    public static FileFragment newInstance() {
        FileFragment fragment = new FileFragment();
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
    public void onDestroy() {
        super.onDestroy();
        if (mBound) {
            getActivity().unbindService(mConnection);
        }
    }

    private void bindSyncService() {
        if (Util.isMyServiceRunning(SyncService.class, getActivity()) && !mBound) {
            Intent intent = new Intent(getActivity(), SyncService.class);
            getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            SyncService.SyncBinder binder = (SyncService.SyncBinder) service;
            adapter.updateItemsInSync(binder.getSyncFiles());
            binder.setSyncListener(adapter);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_list, container, false);
        adapter = new MyFileRecyclerViewAdapter(localFileDao.getAllLocalFiles(), this, getContext());
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(adapter);
            bindSyncService();
        }
        return view;
    }

    @Override
    public void onSyncClick(LocalFile item) {
        Intent i = new Intent(getActivity(), SyncService.class);
        i.putExtra(ARG_FILE_ID, item.id);
        getActivity().startService(i);
        bindSyncService();
    }

    @Override
    public void onDeleteClick(LocalFile item) {
        File file = new File(item.localURI);
        if (file.delete()) {
            localFileDao.deleteLocalFile(item);
            adapter.removeItem(item);
        }
    }

    @Override
    public void onShowOnMapClick(LocalFile item) {

    }


}
