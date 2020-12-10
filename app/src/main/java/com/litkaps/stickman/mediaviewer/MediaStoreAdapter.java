package com.litkaps.stickman.mediaviewer;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.litkaps.stickman.BuildConfig;
import com.litkaps.stickman.R;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import static android.provider.MediaStore.MediaColumns.DATA;
import static java.text.DateFormat.getDateInstance;


public class MediaStoreAdapter extends RecyclerView.Adapter<MediaStoreAdapter.ViewHolder> {

    private Cursor mMediaStoreCursor;
    private final Activity mActivity;
    private final OnClickThumbListener mOnClickThumbListener;

    public interface OnClickThumbListener {
        void onClickImage(Uri imageUri);

        void onClickRawVideo(Uri videoUri);

        void onClickVideo(Uri videoUri);
    }

    public MediaStoreAdapter(Activity activity) {
        this.mActivity = activity;
        this.mOnClickThumbListener = (OnClickThumbListener) activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.media_item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Glide.with(mActivity)
                .load(getUriFromMediaStore(position))
                .centerCrop()
                .override(200, 200)
                .into(holder.getImageView());

        DateFormat dateFormat = getDateInstance();
        mMediaStoreCursor.moveToPosition(position);

        holder.thumbnailLabel.setText(dateFormat.format(new Date(mMediaStoreCursor.getLong(1) * 1000)));

        if (mMediaStoreCursor.getInt(3) == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
            holder.thumbnailLabel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_image_24, 0, 0, 0);
        } else {
            holder.thumbnailLabel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_movie_24, 0, 0, 0);
        }
    }

    @Override
    public int getItemCount() {
        return (mMediaStoreCursor == null) ? 0 : mMediaStoreCursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView mImageView;
        private final TextView thumbnailLabel;

        public ViewHolder(View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.media_thumbnail);
            mImageView.setOnClickListener(this);
            thumbnailLabel = itemView.findViewById(R.id.media_thumbnail_label);
        }

        public ImageView getImageView() {
            return mImageView;
        }

        @Override
        public void onClick(View v) {
            getOnClickUri(getAdapterPosition());
        }

        private void getOnClickUri(int position) {
            int mediaTypeIndex = mMediaStoreCursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
            int dataIndex = mMediaStoreCursor.getColumnIndex(DATA);
            int displayNameIndex = mMediaStoreCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

            mMediaStoreCursor.moveToPosition(position);
            String dataString = mMediaStoreCursor.getString(dataIndex);
            String displayName = mMediaStoreCursor.getString(displayNameIndex);
            String authorities = BuildConfig.APPLICATION_ID + ".provider";
            Uri mediaUri = FileProvider.getUriForFile(mActivity, authorities, new File(dataString));

            switch (mMediaStoreCursor.getInt(mediaTypeIndex)) {
                case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                    mOnClickThumbListener.onClickImage(mediaUri);
                    break;
                case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                    if (displayName.startsWith("StickmanRaw")) {
                        mOnClickThumbListener.onClickRawVideo(mediaUri);
                    } else {
                        mOnClickThumbListener.onClickVideo(mediaUri);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private Cursor swapCursor(Cursor cursor) {
        if (mMediaStoreCursor == cursor) {
            return null;
        }
        Cursor oldCursor = mMediaStoreCursor;
        this.mMediaStoreCursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }

    public void changeCursor(Cursor cursor) {
        Cursor oldCursor = swapCursor(cursor);
        if (oldCursor != null) {
            oldCursor.close();
        }
    }

    private Uri getUriFromMediaStore(int position) {
        int dataIndex = mMediaStoreCursor.getColumnIndex(DATA);

        mMediaStoreCursor.moveToPosition(position);

        String dataString = mMediaStoreCursor.getString(dataIndex);
        return Uri.parse("file://" + dataString);
    }
}