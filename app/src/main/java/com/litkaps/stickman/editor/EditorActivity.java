package com.litkaps.stickman.editor;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.snackbar.Snackbar;
import com.litkaps.stickman.BitmapUtils;
import com.litkaps.stickman.CountUpTimer;
import com.litkaps.stickman.GraphicOverlay;
import com.litkaps.stickman.MainActivity;
import com.litkaps.stickman.OptionModel;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT;
import static com.litkaps.stickman.MainActivity.accessoryOptions;
import static com.litkaps.stickman.MainActivity.backgroundColorOptions;
import static com.litkaps.stickman.MainActivity.backgroundImageOptions;
import static com.litkaps.stickman.MainActivity.figureColorOptions;
import static com.litkaps.stickman.MainActivity.figureOptions;

public class EditorActivity extends AppCompatActivity {
    MediaMetadataRetriever mediaMetadataRetriever;

    private Uri mVideoUri;
    private ImageButton mPlayPauseButton;
    private int frameRate = 5;
    private int frameCount;
    private int frameInterval = 200; // 1000 ms / frameRate
    private int videoLength;
    private LinearLayout secondLevelControl;
    private SeekBar lineWidthBar;
    private OptionsAdapter optionsAdapter;

    private GraphicOverlay graphicOverlay, thumbnailsGraphicOverlay, backgroundGraphicOverlay;

    private RecyclerView framesRecyclerView;
    private StickmanImageDrawer stickmanImageDrawer = new StickmanImageDrawer();
    private StickmanImageDrawer stickmanThumbnailImageDrawer = new StickmanImageDrawer();

    private ArrayList<StickmanData> stickmanData;
    private ArrayList<Frame> frames;
    private FrameAdapter frameAdapter;
    private int previousFramePreviewIndex;
    private int currentFramePreviewIndex;
    private Size targetResolution;

    private Uri imageUri;
    private int colorValue = -1;

    private boolean isRecording = false;
    private boolean isPlaying = false;
    private CountUpTimer timer;

    private TextView recordTime;

    private int currentlyPlayedFrameIndex;

    /**
     * change background image options
     */
    private final OptionModel[] backgroundImageOptionsArray = {
            new OptionModel(R.drawable.ic_baseline_close_24, Color.parseColor(MainActivity.COLOR_1)),
            new OptionModel(R.drawable.ic_baseline_add_photo_alternate_24, Color.parseColor(MainActivity.COLOR_2))
    };

    private ArrayList<OptionModel> backgroundImageOptions = new ArrayList<>(Arrays.asList(backgroundImageOptionsArray));


//    View.OnClickListener playOnClickListener = view -> {
//        if(isPlaying) {
//            recordTime.setVisibility(View.GONE);
//            framesRecyclerView.setVisibility(View.VISIBLE);
//            timer.onFinish();
//            framesRecyclerView.scrollToPosition(currentlyPlayedFrameIndex);
//            framesRecyclerView.getLayoutManager().findViewByPosition(currentlyPlayedFrameIndex).callOnClick();
//            mPlayPauseButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
//            framesRecyclerView.setVisibility(View.VISIBLE);
//        }
//        else {
//            framesRecyclerView.setVisibility(View.GONE);
//            recordTime.setVisibility(View.VISIBLE);
//            mPlayPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
//            currentlyPlayedFrameIndex = 0;
//            timer = new CountUpTimer(frameCount * frameInterval, frameInterval) {
//                public void onTick(int msSecond) {
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Bitmap frameBitmap = mediaMetadataRetriever.getFrameAtIndex(currentlyPlayedFrameIndex);
//                            drawStickmanData(graphicOverlay, frameBitmap, currentlyPlayedFrameIndex, stickmanImageDrawer);
//                        }
//                    }).start();
//
//                    SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
//                    recordTime.setText(dateFormat.format(new Date(msSecond)));
//                    currentlyPlayedFrameIndex++;
//                }
//            };
//            timer.start();
//
//        }
//
//    };

    View.OnClickListener unrollOptionsListener = view -> {
        lineWidthBar.setVisibility(View.GONE);

        ArrayList<OptionModel> options = null;
        int viewId = view.getId();

        if (viewId == R.id.change_figure_button) {
            options = figureOptions;
        } else if (viewId == R.id.change_color_button) {
            options = backgroundColorOptions;
        } else if (viewId == R.id.change_background_image_button) {
            options = backgroundImageOptions;
        } else if (viewId == R.id.change_accessory_button) {
            options = accessoryOptions;
        } else if (viewId == R.id.change_style_button) {
            options = figureColorOptions;
        }

        if (options == optionsAdapter.options && secondLevelControl.getVisibility() == View.VISIBLE) {
            // hide the panel if the same option was clicked again
            secondLevelControl.setVisibility(View.GONE);
            lineWidthBar.setVisibility(View.GONE);
        } else {
            optionsAdapter.setOptions(options);
            optionsAdapter.notifyDataSetChanged();
            secondLevelControl.setVisibility(View.VISIBLE);
            if (options == figureColorOptions)
                lineWidthBar.setVisibility(View.VISIBLE);
        }
    };

    class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.OptionViewHolder> {
        ArrayList<OptionModel> options;

        OptionsAdapter(ArrayList<OptionModel> options) {
            super();
            this.options = options;
        }

        void setOptions(ArrayList<OptionModel> options) {
            this.options = options;
        }

        @NonNull
        @Override
        public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.option_item, parent, false);

            return new OptionViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
            OptionModel option = options.get(position);
            holder.optionButton.setImageResource(option.imageResourceID);

            // set tint of the icon
            if (option.tint != -1)
                ImageViewCompat.setImageTintList(holder.optionButton, ColorStateList.valueOf(option.tint));
            else
                ImageViewCompat.setImageTintList(holder.optionButton, null); // reset tint

        }

        @Override
        public int getItemCount() {
            return options.size();
        }

        class OptionViewHolder extends RecyclerView.ViewHolder {
            ImageButton optionButton;

            OptionViewHolder(View view) {
                super(view);
                optionButton = view.findViewById(R.id.option_button);
                optionButton.setOnClickListener(v -> {
                    int position = getAdapterPosition();

                    if (options == backgroundColorOptions) {
                        if (position == 0) {
                            removeBackgroundColor();
                        } else {
                            setBackgroundColor(options.get(position).tint);
                            colorValue = options.get(position).tint;
                        }
                    }
                    else if (options == backgroundImageOptions) {
                        if (position == 0) {
                            removeBackgroundImage();
                        }
                        else {
                            startChooseImageIntentForResult();
                        }
                    }
                    else if (options == figureOptions) {
                        setFigure(options.get(position).name);
                    } else if (options == figureColorOptions) {
                        setFigureColor(options.get(position).tint);
                    } else if (options == accessoryOptions) {
                        if (position == 0)
                            removeAccessory();
                        else
                            setFigureAccessory(
                                    options.get(position).accessoryID == -1 ? options.get(position).imageResourceID : options.get(position).accessoryID,
                                    options.get(position).accessoryType
                            );
                    }

                    updatePreview(currentFramePreviewIndex);
                });
            }

            private void startChooseImageIntentForResult() {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 0);
            }

        }

        private void setFigure(String name) {
            switch (name) {
                case "classic_stickman": {
                    stickmanData.get(currentFramePreviewIndex).stickmanTypeId = 0;
                    break;
                }
                case "comic_stickman": {
                    stickmanData.get(currentFramePreviewIndex).stickmanTypeId = 1;
                    break;
                }
                case "flexible_comic_stickman": {
                    stickmanData.get(currentFramePreviewIndex).stickmanTypeId = 2;
                    break;
                }
            }
        }

        private void setFigureColor(int colorValue) {
            stickmanData.get(currentFramePreviewIndex).stickmanColor = colorValue;
        }

        private void setFigureAccessory(int accessoryID, int accessoryType) {
            stickmanData.get(currentFramePreviewIndex).accessoryId = accessoryID;
            stickmanData.get(currentFramePreviewIndex).accessoryType = accessoryType;
        }

        private void removeAccessory() {
            stickmanData.get(currentFramePreviewIndex).accessoryId = 0;
            stickmanData.get(currentFramePreviewIndex).accessoryType = -1;
        }

        private void removeBackgroundColor() {
            colorValue = -1;
            setBackgroundColor(-1);
        }

        private void setBackgroundImage(Bitmap backgroundImage) {
            stickmanImageDrawer.setBackgroundImage(backgroundImage);
        }

        private void removeBackgroundImage() {
            imageUri = null;
            setBackgroundImage(null);
        }

        private void setBackgroundColor(int colorValue) {
            stickmanData.get(currentFramePreviewIndex).backgroundColor = colorValue;
        }

    }

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
//            Glide.with(EditorActivity.this)
//                    .load(loadStickmanImageThumbnail(position))
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .thumbnail(0.1f)
//                    .into(holder.framePreview);

            holder.framePreview.setImageBitmap(null);
            int positionOnLoadingStart = holder.getAdapterPosition();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = loadStickmanImageThumbnail(position);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // check if the holder should still be updated by this thread
                            if (holder.getAdapterPosition() == positionOnLoadingStart)
                                holder.framePreview.setImageBitmap(bitmap);
                        }
                    });

                }
            }).start();


            if(position == currentFramePreviewIndex) {
                holder.itemView.setBackgroundColor(getColor(R.color.colorPrimaryDark));
            }
            else {
                holder.itemView.setBackgroundColor(Color.WHITE);
            }

            holder.frameIndex.setText(String.format("%.01f s", position * frameInterval / 1000f));
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
                drawStickmanData(graphicOverlay, bmFrame, getAdapterPosition(), stickmanImageDrawer);
                previousFramePreviewIndex = currentFramePreviewIndex;
                currentFramePreviewIndex = getAdapterPosition();

//                if(currentFramePreviewIndex != previousFramePreviewIndex) {
//                    frameAdapter.notifyItemChanged(previousFramePreviewIndex);
//                }
//                frameAdapter.notifyItemChanged(currentFramePreviewIndex);

                lineWidthBar.setProgress((int)stickmanData.get(currentFramePreviewIndex).stickmanLineThickness);

                View prevClickedView = framesRecyclerView.getLayoutManager().findViewByPosition(previousFramePreviewIndex);
                if(prevClickedView != null)
                    prevClickedView.setBackgroundColor(Color.WHITE);
                v.setBackgroundColor(getColor(R.color.colorPrimaryDark));

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

        StickmanImageDrawer tempDrawer = new StickmanImageDrawer();
        tempDrawer.setVideoEncoder(videoEncoder);
        tempDrawer.setEncodeStickmanData();

        for (int i = 0; i < frameCount; i++) {
            Bitmap bmFrame = mediaMetadataRetriever.getFrameAtIndex(i);
            drawStickmanData(backgroundGraphicOverlay, bmFrame, i, tempDrawer);
        }

        tempDrawer.clearVideoEncoder();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editor);

        Intent callingIntent = getIntent();
        if (callingIntent != null) {
            mVideoUri = callingIntent.getData();
        }


        graphicOverlay = findViewById(R.id.graphic_overlay);
        thumbnailsGraphicOverlay = findViewById(R.id.thumbnails_graphic_overlay);
        backgroundGraphicOverlay = findViewById(R.id.background_graphic_overlay);

        framesRecyclerView = findViewById(R.id.frames_recycler_view);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        framesRecyclerView.setLayoutManager(mLayoutManager);
        framesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mediaMetadataRetriever = new MediaMetadataRetriever();

        Observable.zip(loadFrames(), loadStickmanData(), Pair::new)
                .subscribe(
                        result -> {
                            frames = (ArrayList<Frame>) result.first;
                            stickmanData = (ArrayList<StickmanData>) result.second;
                            Bitmap firstFrame = mediaMetadataRetriever.getFrameAtIndex(0);

                            targetResolution = new Size(firstFrame.getWidth(), firstFrame.getHeight());
                            thumbnailsGraphicOverlay.setImageSourceInfo(targetResolution.getWidth(), targetResolution.getHeight(), false);
                            graphicOverlay.setImageSourceInfo(targetResolution.getWidth(), targetResolution.getHeight(), false);
                            backgroundGraphicOverlay.setImageSourceInfo(targetResolution.getWidth(), targetResolution.getHeight(), false);

                            drawStickmanData(graphicOverlay, firstFrame, 0, stickmanImageDrawer);
                            frameAdapter = new FrameAdapter(frames);
                            framesRecyclerView.setAdapter(frameAdapter);

                            lineWidthBar.setProgress((int)stickmanData.get(0).stickmanLineThickness);

                        }
                );

        findViewById(R.id.save_as_video_button).setOnClickListener(recordListener);

        secondLevelControl = findViewById(R.id.control_level1);
        lineWidthBar = findViewById(R.id.line_width_seekbar);

        findViewById(R.id.change_figure_button).setOnClickListener(unrollOptionsListener);
        findViewById(R.id.change_color_button).setOnClickListener(unrollOptionsListener);
        findViewById(R.id.change_background_image_button).setOnClickListener(unrollOptionsListener);
        findViewById(R.id.change_accessory_button).setOnClickListener(unrollOptionsListener);
        findViewById(R.id.change_style_button).setOnClickListener(unrollOptionsListener);

        RecyclerView recyclerView = findViewById(R.id.control_level1_recycler);
        optionsAdapter = new OptionsAdapter(figureOptions);
        LinearLayoutManager optionsLayoutManager = new LinearLayoutManager(getApplicationContext());
        optionsLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(optionsLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(optionsAdapter);

        lineWidthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                stickmanData.get(currentFramePreviewIndex).stickmanLineThickness = i;
                updatePreview(currentFramePreviewIndex);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

//        mPlayPauseButton = findViewById(R.id.video_play_pause_button);
//        mPlayPauseButton.setOnClickListener(playOnClickListener);
        recordTime = findViewById(R.id.record_time);

    }

    @NonNull
    private Observable<Object> loadFrames() {
        return Observable
                .create(emitter -> {
                    mediaMetadataRetriever.setDataSource(this, mVideoUri);

                    videoLength = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    frameCount = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT));

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

    private void drawStickmanData(GraphicOverlay graphicOverlay, Bitmap frame, int index, StickmanImageDrawer stickmanImageDrawer) {
        graphicOverlay.clear();

        stickmanImageDrawer.setFigureID(stickmanData.get(index).stickmanTypeId);
        stickmanImageDrawer.setFigureAccessory(stickmanData.get(index).accessoryId, stickmanData.get(index).accessoryType);
        stickmanImageDrawer.setBackgroundColor(stickmanData.get(index).backgroundColor);
        stickmanImageDrawer.setFigureColor(stickmanData.get(index).stickmanColor);
        stickmanImageDrawer.setFigureStrokeWidth(stickmanData.get(index).stickmanLineThickness);

        if(stickmanData.get(index).imageUri != null) {
            try {
                Bitmap imageBitmap = BitmapUtils.getBitmapFromContentUri(getContentResolver(), stickmanData.get(index).imageUri);
                stickmanImageDrawer.setBackgroundImage(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            stickmanImageDrawer.setBackgroundImage(null);


        stickmanImageDrawer.draw(
                new PosePositions(
                        stickmanData.get(index).poseLandmarkPositionX,
                        stickmanData.get(index).poseLandmarkPositionY),
                graphicOverlay, frame
        );

        graphicOverlay.postInvalidate();
    }

    private Bitmap loadStickmanImageThumbnail(int position) {
        Bitmap bmFrame = mediaMetadataRetriever.getFrameAtIndex(position);
        drawStickmanData(thumbnailsGraphicOverlay, bmFrame, position, stickmanThumbnailImageDrawer);

        return thumbnailsGraphicOverlay.getGraphicBitmap();

    }

    private void updatePreview(int frameIndex) {
        // update preview
        Bitmap frameBitmap = mediaMetadataRetriever.getFrameAtIndex(frameIndex);
        frameAdapter.notifyItemChanged(frameIndex);
        drawStickmanData(graphicOverlay, frameBitmap, frameIndex, stickmanImageDrawer);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            stickmanData.get(currentFramePreviewIndex).imageUri = data.getData();
            frameAdapter.notifyItemChanged(currentFramePreviewIndex);
            Bitmap frameBitmap = mediaMetadataRetriever.getFrameAtIndex(currentFramePreviewIndex);
            drawStickmanData(graphicOverlay, frameBitmap, currentFramePreviewIndex, stickmanImageDrawer);

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
