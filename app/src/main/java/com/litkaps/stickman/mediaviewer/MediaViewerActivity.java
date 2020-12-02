package com.litkaps.stickman.mediaviewer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.litkaps.stickman.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MediaViewerActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, MediaStoreAdapter.OnClickThumbListener {
    private final static int READ_EXTERNAL_STORAGE_PERMISSION_RESULT = 0;
    private final static int MEDIASTORE_LOADER_ID = 0;
    private RecyclerView mThumbnailRecyclerView;
    private MediaStoreAdapter mMediaStoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_viewer);

        mThumbnailRecyclerView = findViewById(R.id.thumbnailRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mThumbnailRecyclerView.setLayoutManager(gridLayoutManager);
        mMediaStoreAdapter = new MediaStoreAdapter(this);
        mThumbnailRecyclerView.setAdapter(mMediaStoreAdapter);
        LoaderManager.getInstance(this).initLoader(MEDIASTORE_LOADER_ID, null, this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION_RESULT:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Call cursor loader
                    // Toast.makeText(this, "Now have access to view thumbs", Toast.LENGTH_SHORT).show();
                    LoaderManager.getInstance(this).initLoader(MEDIASTORE_LOADER_ID, null, this);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void checkReadExternalStoragePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Start cursor loader
                LoaderManager.getInstance(this).initLoader(MEDIASTORE_LOADER_ID, null, this);
            } else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "App needs to view thumbnails", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }
        } else {
            // Start cursor loader
            LoaderManager.getInstance(this).initLoader(MEDIASTORE_LOADER_ID, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String dateColumnName = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? MediaStore.Files.FileColumns.DATE_TAKEN
                : MediaStore.Files.FileColumns.DATE_MODIFIED;

        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                dateColumnName,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MEDIA_TYPE
        };
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        return new CursorLoader(
                this,
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                null,
                dateColumnName + " DESC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMediaStoreAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMediaStoreAdapter.changeCursor(null);
    }

    @Override
    public void OnClickImage(Uri imageUri) {
//        // Toast.makeText(MediaThumbMainActivity.this, "Image uri = " + imageUri.toString(), Toast.LENGTH_SHORT).show();
//        Intent fullScreenIntent = new Intent(this, FullScreenImageActivity.class);
//        fullScreenIntent.setData(imageUri);
//        startActivity(fullScreenIntent);
    }

    @Override
    public void OnClickVideo(Uri videoUri) {
//        Intent videoPlayIntent = new Intent(this, VideoPlayActivity.class);
//        videoPlayIntent.setData(videoUri);
//        startActivity(videoPlayIntent);

    }

}
