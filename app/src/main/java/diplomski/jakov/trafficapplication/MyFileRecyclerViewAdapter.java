package diplomski.jakov.trafficapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import diplomski.jakov.trafficapplication.util.DateFormats;
import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.database.LocalFile;

import java.io.File;
import java.util.List;

public class MyFileRecyclerViewAdapter extends RecyclerView.Adapter<MyFileRecyclerViewAdapter.ViewHolder> {

    private List<LocalFile> mValues;
    private final OnItemInteractionListener mListener;
    private final Context context;

    public MyFileRecyclerViewAdapter(List<LocalFile> items, OnItemInteractionListener listener, Context context) {
        mValues = items;
        mListener = listener;
        this.context = context;
    }

    public void setItems(List<LocalFile> items) {
        mValues = items;
        notifyDataSetChanged();
    }

    public void removeItem(LocalFile item) {
        int position = mValues.indexOf(item);
        mValues.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mValues.size());
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
        holder.mLocation.setText("Lat: " + String.format("%.4f", holder.mItem.latitude) + "\nLon: " + String.format("%.4f", holder.mItem.longitude));
        holder.mType.setText("Type: " + holder.mItem.fileType.name() + "-" + holder.mItem.recordType.name());
        holder.mSync.setText("Synced: " + (holder.mItem.sync ? "Yes" : "No"));

        File file = new File(holder.mItem.localURI);
        if (holder.mItem.fileType == FileType.PHOTO) {
            Picasso.with(context).load(file).fit().centerInside().into(holder.mPreview);
        }

        holder.mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(holder.mItem.localURI), holder.mItem.fileType == FileType.PHOTO ? "image/*" : "video/*");
                context.startActivity(intent);
            }
        });
        holder.mBtnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, holder.mBtnMore, Gravity.START);
                popupMenu.getMenuInflater().inflate(R.menu.item_more_popup, popupMenu.getMenu());
                if (holder.mItem.sync) {
                    popupMenu.getMenu().removeItem(R.id.sync_now);
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.sync_now:
                                mListener.onSyncClick(holder.mItem);
                                break;
                            case R.id.delete_local:
                                mListener.onDeleteClick(holder.mItem);
                                break;
                            case R.id.show_on_map:
                                mListener.onShowOnMapClick(holder.mItem);
                                break;
                        }
                        Toast.makeText(context, item.getTitle(), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                popupMenu.show();
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
        @BindView(R.id.item_button_more)
        public ImageButton mBtnMore;

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

    public interface OnItemInteractionListener {
        void onSyncClick(LocalFile item);

        void onDeleteClick(LocalFile item);

        void onShowOnMapClick(LocalFile item);
    }
}
