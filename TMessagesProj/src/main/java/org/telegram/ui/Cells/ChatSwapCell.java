package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessageObject;
import org.telegram.ui.PhotoViewer;

/**
 * Created by jjie.choi on 2015-02-16.
 */
public class ChatSwapCell extends ChatBaseCell {

    public static interface ChatSwapCellDelegate {
        public abstract void didClickedImage(ChatSwapCell cell);
    }

    public static final int ONEOFF_TRUE = 99;
    public static final int ONEOFF_FALSE = 98;

    private int swapMsgType;
    private String swapImgUrl;

    private int photoWidth;
    private int photoHeight;

    private ImageReceiver photoImage;
    private boolean imagePressed = false;

    private boolean appLaunched = false;
    public ChatSwapCellDelegate swapDelegate = null;

    private ImageReceiver.ImageReceiverDelegate imgDelegate = new ImageReceiver.ImageReceiverDelegate() {
        @Override
        public void didSetImage(ImageReceiver imageReceiver, boolean set, boolean thumb) {
            if (!thumb && swapMsgType == ONEOFF_TRUE && appLaunched) {

//                photoImage.setDelegate(null);
                Bitmap img = imageReceiver.getBitmap();
                photoImage.clearImage();
                photoImage.setImageBitmap(makeGray(img));
                ChatSwapCell.this.invalidate();
            }
        }
    };

    public ChatSwapCell(Context context) {
        super(context);
        media = true;
        photoImage = new ImageReceiver();
        photoImage.setParentView(this);
        photoImage.setDelegate(imgDelegate);
    }


    public void setSWAPMsgInfo(int type, String imgUrl, boolean isLaunched) {
        if (swapMsgType < 0) {
            media = false;
        }
        swapMsgType = type;
        swapImgUrl = imgUrl;
        appLaunched = isLaunched;
    }

    public int getSwapMsgType() {
        return swapMsgType;
    }

    public boolean isLaunched() {
        return appLaunched;
    }

    public void appLaunched() {
        if (swapMsgType == ONEOFF_TRUE && !appLaunched) {
            appLaunched = true;
            Bitmap img = photoImage.getBitmap();
            if (img != null) {
                photoImage.setImageBitmap(makeGray(img));
                invalidate();
            }
        }
    }

    @Override
    public void setMessageObject(MessageObject messageObject) {
        super.setMessageObject(messageObject);

        photoWidth = AndroidUtilities.dp(80);
        photoHeight = AndroidUtilities.dp(80);
        backgroundWidth = photoWidth + AndroidUtilities.dp(12);
        if (swapImgUrl != null) {
            photoImage.setImage(swapImgUrl, "80_80", null, null, 0);

        }
        invalidate();
    }

    private void didClickedImage() {

        if (swapDelegate != null) {
            swapDelegate.didClickedImage(this);
            appLaunched();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        boolean result = false;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (delegate == null || delegate.canPerformActions()) {
                if (x >= photoImage.getImageX() && x <= photoImage.getImageX() + photoImage.getImageWidth() && y >= photoImage.getImageY() && y <= photoImage.getImageY() + photoImage.getImageHeight()) {
                    imagePressed = true;
                    result = true;
                }
                if (result) {
                    startCheckLongPress();
                }
            }
        } else {
            if (event.getAction() != MotionEvent.ACTION_MOVE) {
                cancelCheckLongPress();
            }
            if (imagePressed) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    imagePressed = false;
                    playSoundEffect(SoundEffectConstants.CLICK);
                    didClickedImage();
                    invalidate();
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    imagePressed = false;
                    invalidate();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!(x >= photoImage.getImageX() && x <= photoImage.getImageX() + photoImage.getImageWidth() && y >= photoImage.getImageY() && y <= photoImage.getImageY() + photoImage.getImageHeight())) {
                        imagePressed = false;
                        invalidate();
                    }
                }
            }
        }
        if (!result) {
            result = super.onTouchEvent(event);
        }

        return result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), photoHeight + AndroidUtilities.dp(14));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int x = 0;

        if (currentMessageObject.isOut()) {
            x = layoutWidth - backgroundWidth - AndroidUtilities.dp(3);
        } else {
            if (isChat) {
                x = AndroidUtilities.dp(67);
            } else {
                x = AndroidUtilities.dp(15);
            }
        }
        photoImage.setImageCoords(x, AndroidUtilities.dp(7), photoWidth, photoHeight);

    }

    @Override
    protected void onAfterBackgroundDraw(Canvas canvas) {
        photoImage.setVisible(!PhotoViewer.getInstance().isShowingImage(currentMessageObject), false);
        photoImage.draw(canvas);
        drawTime = photoImage.getVisible();
    }

    private Bitmap makeGray(Bitmap img) {
        int width, height;

        height = img.getHeight();
        width = img.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();

        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);

        paint.setColorFilter(f);
        c.drawBitmap(img, 0, 0, paint);

        return bmpGrayscale;
    }
}
