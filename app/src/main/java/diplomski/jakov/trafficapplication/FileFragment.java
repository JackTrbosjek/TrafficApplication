package diplomski.jakov.trafficapplication;

import android.content.Context;
import android.os.Bundle;
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

import javax.inject.Inject;

public class FileFragment extends Fragment implements MyFileRecyclerViewAdapter.OnItemInteractionListener {
    @Inject
    FileUploadService fileUploadService;

    @Inject
    LocalFileDao localFileDao;

    MyFileRecyclerViewAdapter adapter;

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
        }
        return view;
    }

    @Override
    public void onSyncClick(LocalFile item) {
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
