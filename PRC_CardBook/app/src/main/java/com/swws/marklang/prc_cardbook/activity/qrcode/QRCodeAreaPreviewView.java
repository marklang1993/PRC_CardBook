package com.swws.marklang.prc_cardbook.activity.qrcode;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Size;

public class QRCodeAreaPreviewView extends android.support.v7.widget.AppCompatImageView {

    private Size mCropAreaSize;
    private Size mPreviewImageSize;
    private Paint mPaint;

    public QRCodeAreaPreviewView(Context context) {
        super(context);
        initPaint();
    }

    public QRCodeAreaPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public QRCodeAreaPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    public void setCropAreaSize(Size cropAreaSize) {
        mCropAreaSize = cropAreaSize;
    }

    public void setPreviewImageSize(Size previewImageSize) {
        mPreviewImageSize = previewImageSize;
    }

    private void initPaint() {
        if (mPaint == null) {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(Color.RED);
            mPaint.setStrokeWidth(3);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mCropAreaSize != null && mPreviewImageSize != null) {

            // Calculate the size of displayed rectangle
            Size canvasSize = new Size(canvas.getHeight(), canvas.getWidth());
            float ratioWidth = ((float)canvasSize.getWidth()) / mPreviewImageSize.getWidth();
            float ratioHeight = ((float)canvasSize.getHeight()) / mPreviewImageSize.getHeight();
            float ratio;
            if (ratioWidth < ratioHeight) {
                ratio = ratioWidth;
            } else {
                ratio = ratioHeight;
            }
            Size displayedCropArea = new Size(
                    (int)(mCropAreaSize.getWidth() * ratio),
                    (int)(mCropAreaSize.getHeight() * ratio)
            );

            // Draw the final rectangle
            Rect drawRect = new Rect();
            drawRect.top = (canvasSize.getWidth() - displayedCropArea.getWidth()) / 2;
            drawRect.left = (canvasSize.getHeight() - displayedCropArea.getHeight()) / 2;
            drawRect.bottom = drawRect.top + displayedCropArea.getWidth();
            drawRect.right = drawRect.left + displayedCropArea.getHeight();
            canvas.drawRect(drawRect, mPaint);
        }
    }

}
