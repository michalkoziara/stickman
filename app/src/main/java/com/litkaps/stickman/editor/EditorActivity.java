package com.litkaps.stickman.editor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.litkaps.stickman.R;
import com.litkaps.stickman.SerializationUtils;
import com.litkaps.stickman.posedetector.StickmanData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EditorActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    MediaMetadataRetriever mediaMetadataRetriever;

    private Uri mVideoUri;
    private ImageButton mPlayPauseButton;
    private SurfaceView mSurfaceView;
    private int frameRate;
    private int frameCount;
    private int videoLength;

    private RecyclerView framesRecyclerView;

    class FrameAdapter extends RecyclerView.Adapter<FrameAdapter.FrameViewHolder> {
        ArrayList<Frame> frames;

        FrameAdapter(ArrayList<Frame> frames) {
            super();
            this.frames = frames;
        }

        @NonNull
        @Override
        public FrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.frame_item, parent, false);

            return new FrameViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull FrameViewHolder holder, int position) {
            Frame frame = frames.get(position);

            Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(frame.frameTime * 1000); // to microseconds
            holder.framePreview.setImageBitmap(bmFrame);
            holder.frameIndex.setText(Float.toString(frame.frameTime));

        }

        @Override
        public int getItemCount() {
            return frames.size();
        }

        class FrameViewHolder extends RecyclerView.ViewHolder {
            ImageView framePreview;
            TextView frameIndex;

            FrameViewHolder(View view) {
                super(view);
                framePreview = view.findViewById(R.id.frame_preview);
                frameIndex = view.findViewById(R.id.frame_index);

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editor);

        Intent callingIntent = getIntent();
        if (callingIntent != null) {
            mVideoUri = callingIntent.getData();
        }

        framesRecyclerView = findViewById(R.id.frames_recycler_view);
        mSurfaceView = findViewById(R.id.videoSurfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);

        mediaMetadataRetriever = new MediaMetadataRetriever();

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        framesRecyclerView.setLayoutManager(mLayoutManager);
        framesRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Observable.zip(loadFrames(), loadStickmanData(), Pair::new)
                .subscribe(
                        result -> {
                            ArrayList<Frame> frames = (ArrayList<Frame>) result.first;
                            ArrayList<StickmanData> stickmanData = (ArrayList<StickmanData>) result.second;
                            framesRecyclerView.setAdapter(new FrameAdapter(frames));
                        }
                );
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    @NonNull
    private Observable<Object> loadFrames() {
        return Observable
                .create(emitter -> {
                    mediaMetadataRetriever.setDataSource(this, mVideoUri);

                    videoLength = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    frameCount = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT));
                    frameRate = (int) (frameCount / (videoLength / 1000f));

                    ArrayList<Frame> frames = new ArrayList<>();
                    float secondsPerFrame = 1f / frameRate;
                    for (int i = 0; i < frameCount; i++) {
                        frames.add(new Frame(i, (long) (i * secondsPerFrame * 1000)));
                    }

                    emitter.onNext(frames);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @NonNull
    private Observable<Object> loadStickmanData() {
        return Observable
                .create(emitter -> {
                    MediaExtractor extractor = new MediaExtractor();
                    List<StickmanData> stickmanDataList = new ArrayList<>();

                    try {
                        extractor.setDataSource(getApplicationContext(), mVideoUri, null);

                        int numTracks = extractor.getTrackCount();
                        for (int i = 0; i < numTracks; ++i) {
                            MediaFormat format = extractor.getTrackFormat(i);
                            String mime = format.getString(MediaFormat.KEY_MIME);
                            if ("application/pose".equals(mime)) {
                                extractor.selectTrack(i);
                            }
                        }

                        ByteBuffer inputBuffer = ByteBuffer.allocate(180);
                        while (extractor.readSampleData(inputBuffer, 0) >= 0) {
                            try {
                                StickmanData stickmanData =
                                        (StickmanData) SerializationUtils.convertFromBytes(inputBuffer.array());
                                stickmanDataList.add(stickmanData);
                                SerializationUtils.convertFromBytes(inputBuffer.array());
                            } catch (IOException | ClassNotFoundException exception) {
                                exception.printStackTrace();
                                emitter.onError(exception);
                            }

                            extractor.advance();
                        }

                        extractor.release();
                        emitter.onNext(stickmanDataList);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                        emitter.onError(ioException);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
