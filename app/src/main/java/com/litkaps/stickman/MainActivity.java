package com.litkaps.stickman;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.annotation.KeepName;
import com.google.android.material.snackbar.Snackbar;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.litkaps.stickman.mediaviewer.MediaViewerActivity;
import com.litkaps.stickman.posedetector.PoseDetectorProcessor;
import com.litkaps.stickman.posedetector.StickmanImageDrawer;
import com.litkaps.stickman.preference.PreferenceUtils;
import com.litkaps.stickman.preference.SettingsActivity;
import com.litkaps.stickman.preference.SettingsActivity.LaunchSource;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback,
        CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUESTS = 1;
    private static final int REQUEST_CHOOSE_IMAGE = 1002;

    private static final String STATE_LENS_FACING = "lens_facing";

    private PreviewView previewView;
    private GraphicOverlay graphicOverlay;

    private Uri imageUri;
    private int colorValue;
    private Size targetResolution;

    private LinearLayout secondLevelControl;
    private SeekBar lineWidthBar;
    private Button recordButton;
    private TextView recordTime;
    private boolean isRecording = false;
    @Nullable
    private ProcessCameraProvider cameraProvider;
    @Nullable
    private ImageAnalysis analysisUseCase;
    @Nullable
    private VisionImageProcessor imageProcessor;
    @Nullable
    private StickmanImageDrawer stickmanImageDrawer;
    private boolean needUpdateGraphicOverlayImageSourceInfo;

    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private CameraSelector cameraSelector;
    private OptionsAdapter optionsAdapter;

    private CountUpTimer timer;

    /**
     * change background image options
     */
    final OptionModel[] backgroundImageOptionsArray = {
            new OptionModel("none", R.drawable.ic_baseline_close_24, Color.parseColor("#277E8A")),
            new OptionModel("custom_graphic", R.drawable.ic_baseline_add_photo_alternate_24, Color.parseColor("#277E8A"))
    };

    final ArrayList<OptionModel> backgroundImageOptions = new ArrayList<>(Arrays.asList(backgroundImageOptionsArray));

    /**
     * change background color options
     */
    final OptionModel[] backgroundColorOptionsArray = {
            new OptionModel("none", R.drawable.ic_baseline_close_24, Color.parseColor("#277E8A")), // clear background icon
            new OptionModel(Color.parseColor("#277E8A")),
            new OptionModel(Color.parseColor("#38BEA3")),
            new OptionModel(Color.parseColor("#F7B536")),
            new OptionModel(Color.parseColor("#22201E")),
            new OptionModel(Color.parseColor("#F6532D"))
    };

    final ArrayList<OptionModel> backgroundColorOptions = new ArrayList<>(Arrays.asList(backgroundColorOptionsArray));

    /**
     * change figure(stickman type) options
     */
    final OptionModel[] figureOptionsArray = {
            new OptionModel("classic_stickman", R.drawable.classic_stickman, Color.DKGRAY),
            new OptionModel("comic_stickman", R.drawable.comic_stickman, Color.DKGRAY),
            new OptionModel("flexible_comic_stickman", R.drawable.flexible_comic_stickman, Color.DKGRAY)

    };

    final ArrayList<OptionModel> figureOptions = new ArrayList<>(Arrays.asList(figureOptionsArray));

    /**
     * change figure color options
     */
    final OptionModel[] figureColorOptionsArray = {
            new OptionModel("none", R.drawable.ic_baseline_close_24, Color.parseColor("#277E8A")),
            new OptionModel(Color.parseColor("#000000")),
            new OptionModel(Color.parseColor("#0E5475")),
            new OptionModel(Color.parseColor("#66C3BE")),
            new OptionModel(Color.parseColor("#AFB582")),
            new OptionModel(Color.parseColor("#DB8D37"))
    };

    final ArrayList<OptionModel> figureColorOptions = new ArrayList<>(Arrays.asList(figureColorOptionsArray));

    /**
     * change accessory
     */
    final OptionModel[] accessoryOptionsArray = {
            new OptionModel("none", R.drawable.ic_baseline_close_24, Color.parseColor("#277E8A")),
            new OptionModel("glasses", R.drawable.icon_glasses, -1, "glasses"),
            new OptionModel("helmet", R.drawable.icon_helmet, -1, "hat"),
            new OptionModel("indiana_jones", R.drawable.icon_indiana_jones, R.drawable.accessory_indiana_jones, "hat"),
            new OptionModel("shield", R.drawable.icon_shield, -1, "handheld"),
            new OptionModel("sword", R.drawable.icon_sword, R.drawable.accessory_sword, "handheld"),
            new OptionModel("witch_hat", R.drawable.icon_witch_hat, R.drawable.accessory_witch_hat, "hat"),
            new OptionModel("graduation_hat", R.drawable.icon_graduation_hat, R.drawable.accessory_graduation_hat, "hat"),
    };

    final ArrayList<OptionModel> accessoryOptions = new ArrayList<>(Arrays.asList(accessoryOptionsArray));

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

    View.OnClickListener takePhotoListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Bitmap bitmap = graphicOverlay.getGraphicBitmap();
            ContentResolver resolver = getApplicationContext().getContentResolver();
            ImageWriter imageWriter = new ImageWriter(resolver);

            String uniqueName = "Stickman " + System.currentTimeMillis();
            boolean result = imageWriter.saveBitmapToImage(bitmap, uniqueName);

            String resultText = result ? "Zdjęcie zostało zapisane!" : "Spróbuj ponownie!";

            Snackbar.make(view.getRootView(), resultText, Snackbar.LENGTH_SHORT)
                    .setAnchorView(recordButton)
                    .show();
        }
    };

    View.OnClickListener recordListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isRecording) {
                isRecording = false;
                view.setBackground(
                        ContextCompat.getDrawable(getApplicationContext(),
                                R.drawable.record_video_button)
                );

                timer.cancel();
                recordTime.setVisibility(View.GONE);

                stickmanImageDrawer.clearVideoEncoder();
            } else {
                isRecording = true;
                view.setBackground(
                        ContextCompat.getDrawable(getApplicationContext(),
                                R.drawable.stop_recording_button)
                );

                recordTime.setVisibility(View.VISIBLE);

                timer = new CountUpTimer(7200000) {
                    public void onTick(int msSecond) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
                        recordTime.setText(dateFormat.format(new Date(msSecond)));
                    }
                };
                timer.start();

                String uniqueName = "Stickman " + System.currentTimeMillis();
                ContentResolver resolver = getApplication().getContentResolver();

                VideoEncoder videoEncoder = new VideoEncoder(
                        uniqueName,
                        targetResolution.getWidth(),
                        targetResolution.getHeight(),
                        resolver,
                        outputFile -> {
                            String resultText = "Film został zapisany!";

                            Snackbar.make(view.getRootView(), resultText, Snackbar.LENGTH_SHORT)
                                    .setAnchorView(recordButton)
                                    .show();
                        });

                stickmanImageDrawer.setVideoEncoder(videoEncoder);
            }
        }
    };

    // toggle between recording a video or taking a photo
    CompoundButton.OnCheckedChangeListener changeRecordModeListener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // switch to video recording
            if (isChecked) {
                recordButton.setBackground(getDrawable(R.drawable.record_video_button));
                recordButton.setOnClickListener(recordListener);
            } else { // switch to taking photos
                recordButton.setBackground(getDrawable(R.drawable.take_photo_button));
                recordButton.setOnClickListener(takePhotoListener);
            }
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

            // set tint for the color icons
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
                            removeBackground();
                        } else {
                            setBackgroundColor(options.get(position).tint);
                            colorValue = options.get(position).tint;
                        }
                    } else if (options == backgroundImageOptions) {
                        if (position == 0) {
                            removeBackground();
                        } else {
                            startChooseImageIntentForResult();
                        }
                    } else if (options == figureOptions) {
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
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            tryReloadAndDetectInImage();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            String errorMessage = "CameraX is only supported on SDK version >=21. Current SDK version is "
                    + VERSION.SDK_INT;

            Snackbar.make(getWindow().getDecorView().getRootView(), errorMessage, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        if (savedInstanceState != null) {
            lensFacing = savedInstanceState.getInt(STATE_LENS_FACING, CameraSelector.LENS_FACING_BACK);
        }
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        setContentView(R.layout.activity_vision_camerax_live_preview);

        secondLevelControl = findViewById(R.id.control_level1);
        lineWidthBar = findViewById(R.id.line_width_seekbar);
        recordTime = findViewById(R.id.record_time);

        recordButton = findViewById(R.id.record_button);
        recordButton.setOnClickListener(takePhotoListener);

        findViewById(R.id.change_figure_button).setOnClickListener(unrollOptionsListener);
        findViewById(R.id.change_color_button).setOnClickListener(unrollOptionsListener);
        findViewById(R.id.change_background_image_button).setOnClickListener(unrollOptionsListener);
        findViewById(R.id.change_accessory_button).setOnClickListener(unrollOptionsListener);
        findViewById(R.id.change_style_button).setOnClickListener(unrollOptionsListener);

        // display settings fragment
        findViewById(R.id.settings).setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            intent.putExtra(SettingsActivity.EXTRA_LAUNCH_SOURCE, LaunchSource.CAMERA_LIVE_PREVIEW);
            startActivity(intent);
        });

        ((CompoundButton) findViewById(R.id.record_mode_toggle)).setOnCheckedChangeListener(changeRecordModeListener);
        findViewById(R.id.media_viewer_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MediaViewerActivity.class);
                startActivity(intent);
            }
        });

        lineWidthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (stickmanImageDrawer != null && i >= 1) {
                    stickmanImageDrawer.setFigureLineWidth(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        previewView = findViewById(R.id.preview_view);
        if (previewView == null) {
            Log.d(TAG, "previewView is null");
        }
        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        ToggleButton facingSwitch = findViewById(R.id.facing_switch);
        facingSwitch.setOnCheckedChangeListener(this);

        new ViewModelProvider(this, AndroidViewModelFactory.getInstance(getApplication()))
                .get(CameraViewModel.class)
                .getProcessCameraProvider()
                .observe(
                        this,
                        provider -> {
                            cameraProvider = provider;
                            if (allPermissionsGranted()) {
                                bindAllCameraUseCases();
                            }
                        });

        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }

        bindAnalysisUseCase();

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        optionsAdapter = new OptionsAdapter(figureOptions);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(optionsAdapter);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt(STATE_LENS_FACING, lensFacing);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Set facing");
        if (cameraProvider == null) {
            return;
        }

        int newLensFacing =
                lensFacing == CameraSelector.LENS_FACING_FRONT
                        ? CameraSelector.LENS_FACING_BACK
                        : CameraSelector.LENS_FACING_FRONT;
        CameraSelector newCameraSelector =
                new CameraSelector.Builder().requireLensFacing(newLensFacing).build();
        try {
            if (cameraProvider.hasCamera(newCameraSelector)) {
                lensFacing = newLensFacing;
                cameraSelector = newCameraSelector;
                bindAllCameraUseCases();
                return;
            }
        } catch (CameraInfoUnavailableException e) {
            // Falls through
        }

        Snackbar.make(buttonView.getRootView(),
                "This device does not have lens with facing: " + newLensFacing,
                Snackbar.LENGTH_SHORT
        ).setAnchorView(buttonView).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        bindAllCameraUseCases();

        if (optionsAdapter.options == backgroundColorOptions) {
            setBackgroundColor(colorValue);
        } else if (optionsAdapter.options == backgroundImageOptions) {
            tryReloadAndDetectInImage();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    private void tryReloadAndDetectInImage() {
        Log.d(TAG, "Try reload and detect image");
        try {
            if (imageUri == null) {
                return;
            }

            Bitmap imageBitmap = BitmapUtils.getBitmapFromContentUri(getContentResolver(), imageUri);
            if (imageBitmap == null) {
                return;
            }

            setBackgroundImage(imageBitmap);
        } catch (IOException e) {
            Log.e(TAG, "Error retrieving saved image");
            imageUri = null;
        }
    }

    private void bindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider.unbindAll();
            bindAnalysisUseCase();
        }
    }

    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }
        if (imageProcessor != null) {
            imageProcessor.stop();
        }

        try {
            PoseDetectorOptionsBase poseDetectorOptions =
                    PreferenceUtils.getPoseDetectorOptionsForLivePreview(this);
            imageProcessor =
                    new PoseDetectorProcessor(this, poseDetectorOptions);
            stickmanImageDrawer = new StickmanImageDrawer(
                    (PoseDetectorProcessor) imageProcessor
            );
        } catch (Exception e) {
            Snackbar.make(getWindow().getDecorView().getRootView(),
                    "Can not create image processor: " + e.getLocalizedMessage(),
                    Snackbar.LENGTH_LONG
            ).show();

            return;
        }

        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
        targetResolution = PreferenceUtils.getCameraXTargetResolution(this);
        if (targetResolution == null) {
            targetResolution = new Size(720, 1280);
        }

        builder.setTargetResolution(targetResolution);
        analysisUseCase = builder.build();

        needUpdateGraphicOverlayImageSourceInfo = true;
        analysisUseCase.setAnalyzer(
                // imageProcessor.processImageProxy will use another thread to run the detection underneath,
                // thus we can just runs the analyzer itself on main thread.
                ContextCompat.getMainExecutor(this),
                imageProxy -> {
                    if (needUpdateGraphicOverlayImageSourceInfo) {
                        boolean isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT;
                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        if (rotationDegrees == 0 || rotationDegrees == 180) {
                            graphicOverlay.setImageSourceInfo(
                                    imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
                        } else {
                            graphicOverlay.setImageSourceInfo(
                                    imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
                        }
                        needUpdateGraphicOverlayImageSourceInfo = false;
                    }
                    try {
                        imageProcessor.processImageProxy(imageProxy, graphicOverlay);
                    } catch (MlKitException e) {
                        Log.e(TAG, "Failed to process image. Error: " + e.getLocalizedMessage());
                        String localizedMessage = e.getLocalizedMessage() == null
                                ? "Failed to process image."
                                : e.getLocalizedMessage();

                        Snackbar.make(getWindow().getDecorView().getRootView(), localizedMessage, Snackbar.LENGTH_SHORT)
                                .show();
                    }
                });

        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            bindAllCameraUseCases();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    private void setBackgroundColor(int colorValue) {
        Bitmap backgroundImage = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        backgroundImage.eraseColor(colorValue);

        setBackgroundImage(backgroundImage);
    }

    private void removeBackground() {
        imageUri = null;
        setBackgroundImage(null);
    }

    private void setBackgroundImage(Bitmap backgroundImage) {
        if (stickmanImageDrawer != null) {
            stickmanImageDrawer.setBackgroundImage(backgroundImage);
        }
    }

    private void startChooseImageIntentForResult() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE);
    }

    private void setFigure(String name) {
        if (stickmanImageDrawer != null) {
            switch (name) {
                case "classic_stickman": {
                    stickmanImageDrawer.setFigureID(0);
                    break;
                }
                case "comic_stickman": {
                    stickmanImageDrawer.setFigureID(1);
                    break;
                }
                case "flexible_comic_stickman": {
                    stickmanImageDrawer.setFigureID(2);
                    break;
                }
            }
        }
    }

    private void setFigureColor(int colorValue) {
        if (stickmanImageDrawer != null) {
            stickmanImageDrawer.setFigureColor(colorValue);
        }
    }

    private void setFigureAccessory(int accessoryID, String accessoryType) {
        int type;
        switch (accessoryType) {
            case "hat": {
                type = 0;
                break;
            }
            case "handheld": {
                type = 1;
                break;
            }
            case "helmet": {
                type = 2;
                break;
            }
            case "glasses": {
                type = 3;
                break;
            }
            default: {
                type = 0;
            }
        }

        if (stickmanImageDrawer != null) {
            stickmanImageDrawer.setFigureAccessory(accessoryID, type);
        }
    }

    private void removeAccessory() {
        if (stickmanImageDrawer != null) {
            stickmanImageDrawer.setFigureAccessory(0, -1);
        }
    }
}