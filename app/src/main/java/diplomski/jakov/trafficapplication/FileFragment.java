package diplomski.jakov.trafficapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import diplomski.jakov.trafficapplication.base.Application;
import diplomski.jakov.trafficapplication.models.LocalFile;
import diplomski.jakov.trafficapplication.services.FileUploadService;

import java.util.List;

import javax.inject.Inject;

public class FileFragment extends Fragment implements MyFileRecyclerViewAdapter.OnItemInteractionListener {
    @Inject
    FileUploadService fileUploadService;

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
        adapter = new MyFileRecyclerViewAdapter(LocalFile.listAll(LocalFile.class, "DATE_CREATED DESC"), this, getContext());
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
        fileUploadService.uploadFile(item);
    }

    @Override
    public void onDeleteClick(LocalFile item) {
        item.delete();
        adapter.removeItem(item);
    }

    @Override
    public void onShowOnMapClick(LocalFile item) {

    }


}
