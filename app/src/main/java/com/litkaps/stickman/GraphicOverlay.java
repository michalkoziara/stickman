package com.litkaps.stickman;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * A view which renders a series of custom graphics to be overlayed on top of an associated preview
 * (i.e., the camera preview). The creator can add graphics objects, update the objects, and remove
 * them, triggering the appropriate drawing and invalidation within the view.
 *
 * <p>Supports scaling and mirroring of the graphics relative the camera's preview properties. The
 * idea is that detection items are expressed in terms of an image size, but need to be scaled up
 * to the full view size, and also mirrored in the case of the front-facing camera.
 *
 * <p>Associated {@link Graphic} items should use the following methods to convert to view
 * coordinates for the graphics that are drawn:
 *
 * <ol>
 *   <li>{@link Graphic#scale(float)} adjusts the size of the supplied value from the image scale
 *       to the view scale.
 *   <li>{@link Graphic#translateX(float)} and {@link Graphic#translateY(float)} adjust the
 *       coordinate from the image's coordinate system to the view coordinate system.
 * </ol>
 */
public class GraphicOverlay extends View {
  private final Object lock = new Object();
  private final List<Graphic> graphics = new ArrayList<>();
  // Matrix for transforming from image coordinates to overlay view coordinates.
  private final Matrix transformationMatrix = new Matrix();

  private int imageWidth;
  private int imageHeight;
  // The factor of overlay View size to image size. Anything in the image coordinates need to be
  // scaled by this amount to fit with the area of overlay View.
  private float scaleFactor = 1.0f;
  // The number of horizontal pixels needed to be cropped on each side to fit the image with the
  // area of overlay View after scaling.
  private float postScaleWidthOffset;
  // The number of vertical pixels needed to be cropped on each side to fit the image with the
  // area of overlay View after scaling.
  private float postScaleHeightOffset;
  private boolean isImageFlipped;
  private boolean needUpdateTransformation = true;

  /**
   * Base class for a custom graphics object to be rendered within the graphic overlay. Subclass
   * this and implement the {@link Graphic#draw(Canvas)} method to define the graphics element. Add
   * instances to the overlay using {@link GraphicOverlay#add(Graphic)}.
   */
  public abstract static class Graphic {
    private static final float DOT_RADIUS = 20.0f;
    private GraphicOverlay overlay;

    public Graphic(GraphicOverlay overlay) {
      this.overlay = overlay;
    }

    /**
     * Draw the graphic on the supplied canvas. Drawing should use the following methods to convert
     * to view coordinates for the graphics that are drawn:
     *
     * <ol>
     *   <li>{@link Graphic#scale(float)} adjusts the size of the supplied value from the image
     *       scale to the view scale.
     *   <li>{@link Graphic#translateX(float)} and {@link Graphic#translateY(float)} adjust the
     *       coordinate from the image's coordinate system to the view coordinate system.
     * </ol>
     *
     * @param canvas drawing canvas
     */
    public abstract void draw(Canvas canvas);

    /** Adjusts the supplied value from the image scale to the view scale. */
    public float scale(float imagePixel) {
      return imagePixel * overlay.scaleFactor;
    }

    /** Returns the application context of the app. */
    public Context getApplicationContext() {
      return overlay.getContext().getApplicationContext();
    }

    public boolean isImageFlipped() {
      return overlay.isImageFlipped;
    }

    /**
     * Adjusts the x coordinate from the image's coordinate system to the view coordinate system.
     */
    public float translateX(float x) {
      if (overlay.isImageFlipped) {
        return overlay.getWidth() - (scale(x) - overlay.postScaleWidthOffset);
      } else {
        return scale(x) - overlay.postScaleWidthOffset;
      }
    }

    /**
     * Adjusts the y coordinate from the image's coordinate system to the view coordinate system.
     */
    public float translateY(float y) {
      return scale(y) - overlay.postScaleHeightOffset;
    }

    /**
     * Adjusts the point coordinate from the image's coordinate system to the view coordinate system.
     */
    public PointF translatePoint(PointF point) {
      return new PointF(translateX(point.x), translateY(point.y));
    }

    /**
     * Returns a {@link Matrix} for transforming from image coordinates to overlay view coordinates.
     */
    public Matrix getTransformationMatrix() {
      return overlay.transformationMatrix;
    }

    public void postInvalidate() {
      overlay.postInvalidate();
    }

    public void drawCircle(Canvas canvas, @Nullable PointF point, float radius, Paint paint, boolean fill) {
      if (point == null) {
        return;
      }
      if (fill) {
        paint.setStyle(Paint.Style.FILL);
      }
      canvas.drawCircle(point.x, point.y, radius, paint);
    }

    public void drawPoint(Canvas canvas, @Nullable PointF point, Paint paint) {
      if (point == null) {
        return;
      }

      drawCircle(canvas, point, DOT_RADIUS, paint, true);
    }

    public void drawLine(Canvas canvas, @Nullable PointF start, @Nullable PointF end, Paint paint) {
      if (start == null || end == null) {
        return;
      }
      canvas.drawLine(start.x, start.y, end.x, end.y, paint);
    }

    public void drawPath(Canvas canvas, @Nullable Path path, Paint paint) {
      if (path == null) {
        return;
      }
      canvas.drawPath(path, paint);
    }

    public void drawCurvedLine(Canvas canvas, float x1, float y1, float x2, float y2, float curveRadius, Paint paint) {
      paint.setAntiAlias(true);
      paint.setStyle(Paint.Style.STROKE);

      final Path path = new Path();
      float midX = x1 + ((x2 - x1) / 2);
      float midY = y1 + ((y2 - y1) / 2);
      float xDiff = midX - x1;
      float yDiff = midY - y1;
      double angle = (Math.atan2(yDiff, xDiff) * (180 / Math.PI)) - 90;
      double angleRadians = Math.toRadians(angle);
      float pointX = (float) (midX + curveRadius * Math.cos(angleRadians));
      float pointY = (float) (midY + curveRadius * Math.sin(angleRadians));

      path.moveTo(x1, y1);
      path.cubicTo(x1, y1, pointX, pointY, x2, y2);

      canvas.drawPath(path, paint);
    }

    public PointF getPointBetween(PointF a, PointF b) {
      return new PointF((a.x + b.x) / 2, (a.y + b.y) / 2);
    }

    public float getDistance(PointF a, PointF b) {
      return (float) Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }

    /**
     * Calculates rotation between point (x1, y1) and point (x2, y2), then translates it
     * to point (x3, y3) and applies additional rotation.
     */
    public Matrix calculateTransformMatrix (
            float x1, float y1, float x2, float y2, float x3, float y3, float objectWidth,
            float objectHeight, float sx, float rotation) {

      Matrix matrix = new Matrix();

      float deltaX = x2 - x1;
      float deltaY =  y2 - y1;
      float thetaRadians = (float)Math.atan2(deltaY, deltaX);
      thetaRadians = isImageFlipped() ? thetaRadians - 3.142f : thetaRadians;
      matrix.setRotate((float)Math.toDegrees(thetaRadians) + rotation, objectWidth/2, objectHeight/2);
      matrix.postScale(sx, sx, objectWidth/2, objectHeight/2);

      matrix.postTranslate(x3 - objectWidth/2, y3 - objectHeight/2);

      return matrix;
    }

  }

  public GraphicOverlay(Context context, AttributeSet attrs) {
    super(context, attrs);
    addOnLayoutChangeListener(
            (view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                    needUpdateTransformation = true);
  }

  /** Removes all graphics from the overlay. */
  public void clear() {
    synchronized (lock) {
      graphics.clear();
    }
    postInvalidate();
  }

  /** Adds a graphic to the overlay. */
  public void add(Graphic graphic) {
    synchronized (lock) {
      graphics.add(graphic);
    }
  }

  /** Removes a graphic from the overlay. */
  public void remove(Graphic graphic) {
    synchronized (lock) {
      graphics.remove(graphic);
    }
    postInvalidate();
  }

  /**
   * Sets the source information of the image being processed by detectors, including size and
   * whether it is flipped, which informs how to transform image coordinates later.
   *
   * @param imageWidth the width of the image sent to ML Kit detectors
   * @param imageHeight the height of the image sent to ML Kit detectors
   * @param isFlipped whether the image is flipped. Should set it to true when the image is from the
   *     front camera.
   */
  public void setImageSourceInfo(int imageWidth, int imageHeight, boolean isFlipped) {
    Preconditions.checkState(imageWidth > 0, "image width must be positive");
    Preconditions.checkState(imageHeight > 0, "image height must be positive");
    synchronized (lock) {
      this.imageWidth = imageWidth;
      this.imageHeight = imageHeight;
      this.isImageFlipped = isFlipped;
      needUpdateTransformation = true;
    }
    postInvalidate();
  }

  public int getImageWidth() {
    return imageWidth;
  }

  public int getImageHeight() {
    return imageHeight;
  }

  private void updateTransformationIfNeeded() {
    if (!needUpdateTransformation || imageWidth <= 0 || imageHeight <= 0) {
      return;
    }
    float viewAspectRatio = (float) getWidth() / getHeight();
    float imageAspectRatio = (float) imageWidth / imageHeight;
    postScaleWidthOffset = 0;
    postScaleHeightOffset = 0;
    if (viewAspectRatio > imageAspectRatio) {
      // The image needs to be vertically cropped to be displayed in this view.
      scaleFactor = (float) getWidth() / imageWidth;
      postScaleHeightOffset = ((float) getWidth() / imageAspectRatio - getHeight()) / 2;
    } else {
      // The image needs to be horizontally cropped to be displayed in this view.
      scaleFactor = (float) getHeight() / imageHeight;
      postScaleWidthOffset = ((float) getHeight() * imageAspectRatio - getWidth()) / 2;
    }

    transformationMatrix.reset();
    transformationMatrix.setScale(scaleFactor, scaleFactor);
    transformationMatrix.postTranslate(-postScaleWidthOffset, -postScaleHeightOffset);

    if (isImageFlipped) {
      transformationMatrix.postScale(-1f, 1f, getWidth() / 2f, getHeight() / 2f);
    }

    needUpdateTransformation = false;
  }

  /** Draws the overlay with its associated graphic objects. */
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    synchronized (lock) {
      updateTransformationIfNeeded();

      for (Graphic graphic : graphics) {
        graphic.draw(canvas);
      }
    }
  }

  public Bitmap getGraphicBitmap() {
    Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);

    synchronized (lock) {
      updateTransformationIfNeeded();

      for (GraphicOverlay.Graphic graphic : graphics) {
        graphic.draw(canvas);
      }
      bitmap = getResizedBitmap(bitmap, getImageWidth(), getImageHeight());
      return bitmap;
    }
  }

  private Bitmap getResizedBitmap(Bitmap bitmap, int newWidth, int newHeight) {
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    float scaleWidth = ((float) newWidth) / width;
    float scaleHeight = ((float) newHeight) / height;

    Matrix matrix = new Matrix();
    matrix.postScale(scaleWidth, scaleHeight);

    Bitmap resizedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, width, height, matrix, false);
    bitmap.recycle();
    return resizedBitmap;
  }
}
