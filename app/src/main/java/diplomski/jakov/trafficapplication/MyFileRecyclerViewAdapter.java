package diplomski.jakov.trafficapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import diplomski.jakov.trafficapplication.FileFragment.OnListFragmentInteractionListener;
import diplomski.jakov.trafficapplication.util.DateFormats;
import diplomski.jakov.trafficapplication.util.VideoRequestHandler;
import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.models.LocalFile;

import java.io.File;
import java.util.List;

public class MyFileRecyclerViewAdapter extends RecyclerView.Adapter<MyFileRecyclerViewAdapter.ViewHolder> {

    private final List<LocalFile> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final Context context;

    public MyFileRecyclerViewAdapter(List<LocalFile> items, OnListFragmentInteractionListener listener, Context context) {
        mValues = items;
        mListener = listener;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mDateCreated.setText(DateFormats.DateFormat.format(mValues.get(position).dateCreated));
        holder.mLocation.setText("Locations:\nLat:" + Math.round(holder.mItem.latitude) + "Lon:" + Math.round(holder.mItem.longitude));
        holder.mType.setText("Type:\n" + holder.mItem.fileType.name() + "-" + holder.mItem.recordType.name());
        holder.mSync.setText("Sync:\n");
        File file = new File(holder.mItem.localURI);
        if (holder.mItem.fileType == FileType.PHOTO) {
            Picasso.with(context).load(file).fit().centerInside().into(holder.mPreview);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        @BindView(R.id.item_date_created)
        public TextView mDateCreated;
        @BindView(R.id.item_location)
        public TextView mLocation;
        @BindView(R.id.item_type)
        public TextView mType;
        @BindView(R.id.item_sync)
        public TextView mSync;
        @BindView(R.id.item_preview)
        public ImageView mPreview;

        public LocalFile mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, view);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mLocation.getText() + "'";
        }
    }
}
