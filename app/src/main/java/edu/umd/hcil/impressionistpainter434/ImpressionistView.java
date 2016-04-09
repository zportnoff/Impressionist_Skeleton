package edu.umd.hcil.impressionistpainter434;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Stack;


/**
 * Created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {

    private ImageView _imageView;
    static private final String TAG = "ImpressionistView";

    private Bitmap img = null;
    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();

    private int _alpha = 150;
    private int _defaultRadius = 25;
    private Point _lastPoint = null;
    private long _lastPointTime = -1;
    private boolean _useMotionSpeedForBrushStrokeSize = true;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Square;
    private float _minBrushRadius = 5;
    int rSize = 40;
    float xScale;
    float yScale;


    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     *
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle) {

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(4);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);

        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if (bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     *
     * @param imageView
     */
    public void setImageView(ImageView imageView) {
        _imageView = imageView;
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     *
     * @param brushType
     */
    public void setBrushType(BrushType brushType) {
        _brushType = brushType;
    }

    /**
     * Clears the painting
     */
    public void clearPainting() {

        if (_offScreenCanvas != null) {
            _offScreenCanvas.drawColor(Color.WHITE);
            invalidate();
        }


    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //xScale = _offScreenBitmap.getScaledWidth(_offScreenCanvas)/this.getScaleX();
        //yScale = _offScreenBitmap.getScaledHeight(_offScreenCanvas)/this.getScaleY();

        if (_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        }
        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // Log.i(TAG,_brushType.toString());
        //TODO
        //Basically, the way this works is to liste for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_MOVE) {

            float x = motionEvent.getX();
            float y = motionEvent.getY();
            Log.i(TAG, String.valueOf("Height " + this.getHeight()) + " Width " + String.valueOf(this.getWidth()));
            Log.i(TAG, String.valueOf("Height " + _offScreenBitmap.getHeight()) + " Width " + String.valueOf(_offScreenBitmap.getWidth()));
            Log.i(TAG, String.valueOf("Height " + img.getHeight()) + " Width " + String.valueOf(img.getWidth()));
            Log.i(TAG, String.valueOf("Height " + _imageView.getHeight()) + " Width " + String.valueOf(_imageView.getWidth()));
            Rect rect = getBitmapPositionInsideImageView(_imageView);
            Log.i(TAG, String.valueOf("Height " + rect.height()) + " Width " + String.valueOf(rect.width()));


            float percOfDrawingX = x / this.getWidth();
            float percOfDrawingY = y / this.getHeight();

            float xScale = percOfDrawingX * img.getWidth();
            float yScale = percOfDrawingY * img.getHeight();


            Log.i(TAG, String.valueOf(x) + " " + String.valueOf(y));
            if (img != null && x >= 0 && y >= 0 && x < _offScreenBitmap.getWidth() && y < _offScreenBitmap.getHeight()) {
                try {
                    _paint.setColor(img.getPixel((int) (xScale), (int) yScale));
                    switch (_brushType) {
                        case Square:
                            Log.i(TAG, String.valueOf(x) + " " + String.valueOf(y));
                            _offScreenCanvas.drawRect(x, y, x + rSize, y + rSize, _paint);
                            invalidate();
                            return true;
                        case Circle:
                            Log.i(TAG, String.valueOf(x) + " " + String.valueOf(y));
                            _offScreenCanvas.drawCircle(x, y, 15, _paint);
                            invalidate();
                            return true;
                        case CircleSplatter:
                            int rand = (int) (Math.random() * 20 + 5);
                            _offScreenCanvas.drawCircle(x, y, rand, _paint);

                            rand = (int) (Math.random() * 20 + 5);
                            _paint.setColor(img.getPixel((int) (x + 10), (int) (y + 10)));
                            _offScreenCanvas.drawCircle(x + 10, y + 10, rand, _paint);

                            rand = (int) (Math.random() * 20 + 5);
                            _paint.setColor(img.getPixel((int) (x - 10), (int) (y - 10)));
                            _offScreenCanvas.drawCircle(x - 10, y - 10, rand, _paint);

                            rand = (int) (Math.random() * 20 + 5);
                            _paint.setColor(img.getPixel((int) (x + 10), (int) (y - 10)));
                            _offScreenCanvas.drawCircle(x + 10, y - 10, rand, _paint);

                            rand = (int) (Math.random() * 20 + 5);
                            _paint.setColor(img.getPixel((int) (x - 10), (int) (y + 10)));
                            _offScreenCanvas.drawCircle(x - 10, y + 10, rand, _paint);

                            invalidate();
                            return true;
                    }
                } catch (java.lang.IllegalArgumentException l) {
                    return true;
                }
            }
        }


        return true;
    }


    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     * - http://stackoverflow.com/a/15538856
     * - http://stackoverflow.com/a/26930938
     *
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView) {
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual) / 2;
        int left = (int) (imgViewW - widthActual) / 2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }

    public void setImage(Bitmap bitmap) {
        Log.i(TAG, "Called setImg");
        img = bitmap;
    }



    public void saveDrawing() {
        FileOutputStream out = null;
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/Pictures", "drawing.jpg");

        try {
            out = new FileOutputStream(file);
            _offScreenBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
            Log.i(TAG, "sucess");

        } catch (Exception e) {
            Log.i(TAG, "failed");
            e.printStackTrace();
        } finally {
            Log.i(TAG, "Finally");
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

