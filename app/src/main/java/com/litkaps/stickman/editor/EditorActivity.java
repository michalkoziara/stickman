package com.litkaps.stickman.editor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.util.Size;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.litkaps.stickman.CountUpTimer;
import com.litkaps.stickman.GraphicOverlay;
import com.litkaps.stickman.R;
import com.litkaps.stickman.SerializationUtils;
import com.litkaps.stickman.VideoEncoder;
import com.litkaps.stickman.posedetector.PosePositions;
import com.litkaps.stickman.posedetector.StickmanData;
import com.litkaps.stickman.posedetector.StickmanImageDrawer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT;

public class EditorActivity extends AppCompatActivity {
    MediaMetadataRetriever mediaMetadataRetriever;

    private Uri mVideoUri;
    private ImageButton mPlayPauseButton;
    private SurfaceView mSurfaceView;
    private int frameRate;
    private int frameCount;
    private int videoLength;

    private GraphicOverlay graphicOverlay;
    private GraphicOverlay thumbnailsGraphicOverlay;

    private RecyclerView framesRecyclerView;
    private StickmanImageDrawer stickmanImageDrawer = new StickmanImageDrawer();
    private ArrayList<StickmanData> stickmanData;
    private ArrayList<Frame> frames;

    private Size targetResolution;

    private boolean isRecording = false;

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
            Bitmap bmFrame = mediaMetadataRetriever.getFrameAtIndex(position);
            drawStickmanData(thumbnailsGraphicOverlay, bmFrame, position);

            holder.framePreview.setImageBitmap(thumbnailsGraphicOverlay.getGraphicBitmap());
            holder.frameIndex.setText(frame.frameTime / 1000f + "s");
        }

        @Override
        public int getItemCount() {
            return frames.size();
        }

        class FrameViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView framePreview;
            TextView frameIndex;

            FrameViewHolder(View view) {
                super(view);
                framePreview = view.findViewById(R.id.frame_preview);
                frameIndex = view.findViewById(R.id.frame_index);
                view.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                Bitmap bmFrame = mediaMetadataRetriever.getFrameAtIndex(getAdapterPosition());
                drawStickmanData(graphicOverlay, bmFrame, getAdapterPosition());
            }
        }
    }

    View.OnClickListener recordListener = view -> {
        String uniqueName = "Stickman " + System.currentTimeMillis();
        VideoEncoder videoEncoder = new VideoEncoder(
                uniqueName,
                targetResolution.getWidth(),
                targetResolution.getHeight(),
                getContentResolver(),
                outputFile -> {
                    String resultText = "Film został zapisany!";

                    Snackbar.make(view.getRootView(), resultText, LENGTH_SHORT)
                            .setAnchorView(framesRecyclerView)
                            .show();
                });
        stickmanImageDrawer.setVideoEncoder(videoEncoder);
        stickmanImageDrawer.setEncodeStickmanData();

        for (int i = 0; i < frameCount; i++) {
            Bitmap bmFrame = mediaMetadataRetriever.getFrameAtIndex(i);
            drawStickmanData(thumbnailsGraphicOverlay, bmFrame, i);
        }

        stickmanImageDrawer.clearVideoEncoder();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editor);

        Intent callingIntent = getIntent();
        if (callingIntent != null) {
            mVideoUri = callingIntent.getData();
        }

        framesRecyclerView = findViewById(R.id.frames_recycler_view);
        graphicOverlay = findViewById(R.id.graphic_overlay);


        mediaMetadataRetriever = new MediaMetadataRetriever();

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        framesRecyclerView.setLayoutManager(mLayoutManager);
        framesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        thumbnailsGraphicOverlay = findViewById(R.id.thumbnails_graphic_overlay);

        Observable.zip(loadFrames(), loadStickmanData(), Pair::new)
                .subscribe(
                        result -> {
                            frames = (ArrayList<Frame>) result.first;
                            stickmanData = (ArrayList<StickmanData>) result.second;
                            framesRecyclerView.setAdapter(new FrameAdapter(frames));

                            Bitmap firstFrame = mediaMetadataRetriever.getFrameAtTime(0);

                            targetResolution = new Size(firstFrame.getWidth(), firstFrame.getHeight());

                            graphicOverlay.setImageSourceInfo(targetResolution.getWidth(), targetResolution.getHeight(), false);
                            thumbnailsGraphicOverlay.setImageSourceInfo(firstFrame.getWidth(), firstFrame.getHeight(), false);

                            drawStickmanData(graphicOverlay, firstFrame, 0);
                        }
                );

        findViewById(R.id.save_as_video_button).setOnClickListener(recordListener);
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

                        ByteBuffer inputBuffer = ByteBuffer.allocate(1000);
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

    private void drawStickmanData(GraphicOverlay graphicOverlay, Bitmap frame, int index) {
        graphicOverlay.clear();

        stickmanImageDrawer.setFigureID(stickmanData.get(index).stickmanTypeId);
        stickmanImageDrawer.setFigureAccessory(stickmanData.get(index).accessoryId, stickmanData.get(index).accessoryType);
        stickmanImageDrawer.setBackgroundColor(stickmanData.get(index).backgroundColor);
        stickmanImageDrawer.setFigureColor(stickmanData.get(index).stickmanColor);
        stickmanImageDrawer.setFigureLineWidth(stickmanData.get(index).stickmanLineThickness);

        stickmanImageDrawer.draw(
                new PosePositions(
                        stickmanData.get(index).poseLandmarkPositionX,
                        stickmanData.get(index).poseLandmarkPositionY),
                graphicOverlay, frame
        );

        graphicOverlay.postInvalidate();
    }
}
