package com.outlook.notyetapp;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.TypedValue;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Neil on 11/17/2016.
 */

public class CustomLegendRenderer extends LegendRenderer {
    /**
     * wrapped styles regarding to the
     * legend
     */
    private final class Styles {
        float textSize;
        int spacing;
        int padding;
        int width;
        int backgroundColor;
        int textColor;
        int margin;
        com.jjoe64.graphview.LegendRenderer.LegendAlign align;
        Point fixedPosition;
    }

    /**
     * alignment of the legend
     */
    public enum LegendAlign {
        /**
         * top right corner
         */
        TOP,

        /**
         * middle right
         */
        MIDDLE,

        /**
         * bottom right corner
         */
        BOTTOM
    }

    /**
     * wrapped styles
     */
    private CustomLegendRenderer.Styles mStyles;

    /**
     * reference to the graphview
     */
    private final GraphView mGraphView;

    /**
     * flag whether legend will be
     * drawn
     */
    private boolean mIsVisible;

    /**
     * paint for the drawing
     */
    private Paint mPaint;

    /**
     * cached legend width
     * this will be filled in the drawing.
     * Can be cleared via {@link #resetStyles()}
     */
    private int cachedLegendWidth;

    /**
     * creates legend renderer
     *
     * @param graphView regarding graphview
     */
    public CustomLegendRenderer(GraphView graphView) {
        super(graphView);
        mGraphView = graphView;
        mIsVisible = false;
        mPaint = new Paint();
        mPaint.setTextAlign(Paint.Align.LEFT);
        mStyles = new CustomLegendRenderer.Styles();
        cachedLegendWidth = 0;
        resetStyles2();
    }

    /**
     * resets the styles to the defaults
     * and clears the legend width cache
     */
    public void resetStyles2() {
        if(mStyles == null){
            mStyles = new CustomLegendRenderer.Styles();
        }
        mStyles.align = com.jjoe64.graphview.LegendRenderer.LegendAlign.MIDDLE;
        mStyles.textSize = mGraphView.getGridLabelRenderer().getTextSize();
        mStyles.spacing = (int) (mStyles.textSize / 5);
        mStyles.padding = (int) (mStyles.textSize / 2);
        mStyles.width = 0;
        mStyles.backgroundColor = Color.argb(180, 100, 100, 100);
        mStyles.margin = (int) (mStyles.textSize / 5);

        // get matching styles from theme
        TypedValue typedValue = new TypedValue();
        mGraphView.getContext().getTheme().resolveAttribute(android.R.attr.textAppearanceSmall, typedValue, true);

        int color1;

        try {
            TypedArray array = mGraphView.getContext().obtainStyledAttributes(typedValue.data, new int[]{
                    android.R.attr.textColorPrimary});
            color1 = array.getColor(0, Color.BLACK);
            array.recycle();
        } catch (Exception e) {
            color1 = Color.BLACK;
        }

        mStyles.textColor = color1;

        cachedLegendWidth = 0;
    }

    /**
     * draws the legend if it is visible
     *
     * @param canvas canvas
     * @see #setVisible(boolean)
     */
    public void draw(Canvas canvas) {
        if (!mIsVisible) return;

        mPaint.setTextSize(mStyles.textSize);

        int shapeSize = (int) (mStyles.textSize*0.8d);

        List<Series> allSeries = new ArrayList<Series>();
        allSeries.addAll(mGraphView.getSeries());
//        if (mGraphView.mSecondScale != null) {
//            allSeries.addAll(mGraphView.getSecondScale().getSeries());
//        }

        // width
        int legendWidth = mStyles.width;
        if (legendWidth == 0) {
            // auto
            legendWidth = cachedLegendWidth;

            if (legendWidth == 0) {
                Rect textBounds = new Rect();
                for (Series s : allSeries) {
                    if (s.getTitle() != null) {
                        mPaint.getTextBounds(s.getTitle(), 0, s.getTitle().length(), textBounds);
                        legendWidth = Math.max(legendWidth, textBounds.width());
                    }
                }
                if (legendWidth == 0) legendWidth = 1;

                // add shape size
                legendWidth += shapeSize+mStyles.padding*2 + mStyles.spacing;
                cachedLegendWidth = legendWidth;
            }
        }

        // rect
        float legendHeight = (mStyles.textSize+mStyles.spacing)*allSeries.size() -mStyles.spacing;
        float lLeft;
        float lTop;
        if (mStyles.fixedPosition != null) {
            // use fied position
            lLeft = mGraphView.getGraphContentLeft() + mStyles.margin + mStyles.fixedPosition.x;
            lTop = mGraphView.getGraphContentTop() + mStyles.margin + mStyles.fixedPosition.y;
        } else {
            lLeft = mGraphView.getGraphContentLeft() + mGraphView.getGraphContentWidth() - legendWidth - mStyles.margin;
            switch (mStyles.align) {
                case TOP:
                    lTop = mGraphView.getGraphContentTop() + mStyles.margin;
                    break;
                case MIDDLE:
                    lTop = mGraphView.getHeight() / 2 - legendHeight / 2;
                    break;
                default:
                    lTop = mGraphView.getGraphContentTop() + mGraphView.getGraphContentHeight() - mStyles.margin - legendHeight - 2*mStyles.padding;
            }
        }
        float lRight = lLeft+legendWidth;
        float lBottom = lTop+legendHeight+2*mStyles.padding;
        mPaint.setColor(mStyles.backgroundColor);
        canvas.drawRoundRect(new RectF(lLeft, lTop, lRight, lBottom), 8, 8, mPaint);

        // Keep track of where each legend item is located.
        // Then since the mapping is public, the activity can toggle the color.
        // I know this is hacky.
        if(mLegendMapping == null)
        {
            mLegendMapping = new ArrayList<LegendMapping>(allSeries.size());
            for(int j = 0; j < allSeries.size(); j++){
                mLegendMapping.add(new LegendMapping());
            }
        }

        int i=0;
        for (Series series : allSeries) {
            mLegendMapping.get(i).mSeries = (LineGraphSeries<DataPoint>)series;
            mLegendMapping.get(i).mSeriesLegendRect = new RectF(lLeft, lTop + (i * (legendHeight/allSeries.size())), lRight, lTop + (i+1)*(legendHeight/allSeries.size()));

            mPaint.setColor(series.getColor());
            canvas.drawRect(new RectF(lLeft+mStyles.padding, lTop+mStyles.padding+(i*(mStyles.textSize+mStyles.spacing)), lLeft+mStyles.padding+shapeSize, lTop+mStyles.padding+(i*(mStyles.textSize+mStyles.spacing))+shapeSize), mPaint);
            if (series.getTitle() != null) {
                mPaint.setColor(mStyles.textColor);
                canvas.drawText(series.getTitle(), lLeft+mStyles.padding+shapeSize+mStyles.spacing, lTop+mStyles.padding+mStyles.textSize+(i*(mStyles.textSize+mStyles.spacing)), mPaint);
            }
            i++;
        }
    }

    /**
     * @return the flag whether the legend will be drawn
     */
    public boolean isVisible() {
        return mIsVisible;
    }

    /**
     * set the flag whether the legend will be drawn
     *
     * @param mIsVisible visible flag
     */
    public void setVisible(boolean mIsVisible) {
        this.mIsVisible = mIsVisible;
    }

    /**
     * @return font size
     */
    public float getTextSize() {
        return mStyles.textSize;
    }

    /**
     * sets the font size. this will clear
     * the internal legend width cache
     *
     * @param textSize font size
     */
    public void setTextSize(float textSize) {
        mStyles.textSize = textSize;
        cachedLegendWidth = 0;
    }

    /**
     * @return the spacing between the text lines
     */
    public int getSpacing() {
        return mStyles.spacing;
    }

    /**
     * set the spacing between the text lines
     *
     * @param spacing the spacing between the text lines
     */
    public void setSpacing(int spacing) {
        mStyles.spacing = spacing;
    }

    /**
     * padding is the space between the edge of the box
     * and the beginning of the text
     *
     * @return padding from edge to text
     */
    public int getPadding() {
        return mStyles.padding;
    }

    /**
     * padding is the space between the edge of the box
     * and the beginning of the text
     *
     * @param padding padding from edge to text
     */
    public void setPadding(int padding) {
        mStyles.padding = padding;
    }

    /**
     * the width of the box exclusive padding
     *
     * @return  the width of the box
     *          0 => auto
     */
    public int getWidth() {
        return mStyles.width;
    }

    /**
     * the width of the box exclusive padding
     * @param width     the width of the box exclusive padding
     *                  0 => auto
     */
    public void setWidth(int width) {
        mStyles.width = width;
    }

    /**
     * @return  background color of the box
     *          it is recommended to use semi-transparent
     *          color.
     */
    public int getBackgroundColor() {
        return mStyles.backgroundColor;
    }

    /**
     * @param backgroundColor   background color of the box
     *                          it is recommended to use semi-transparent
     *                          color.
     */
    public void setBackgroundColor(int backgroundColor) {
        mStyles.backgroundColor = backgroundColor;
    }

    /**
     * @return  margin from the edge of the box
     *          to the corner of the graphview
     */
    public int getMargin() {
        return mStyles.margin;
    }

    /**
     * @param margin    margin from the edge of the box
     *                  to the corner of the graphview
     */
    public void setMargin(int margin) {
        mStyles.margin = margin;
    }

    /**
     * @return the vertical alignment of the box
     */
    public com.jjoe64.graphview.LegendRenderer.LegendAlign getAlign() {
        return mStyles.align;
    }

    /**
     * @param align the vertical alignment of the box
     */
    public void setAlign(com.jjoe64.graphview.LegendRenderer.LegendAlign align) {
        mStyles.align = align;
    }

    /**
     * @return font color
     */
    public int getTextColor() {
        return mStyles.textColor;
    }

    /**
     * @param textColor font color
     */
    public void setTextColor(int textColor) {
        mStyles.textColor = textColor;
    }

    /**
     * Use fixed coordinates to position the legend.
     * This will override the align setting.
     *
     * @param x x coordinates in pixel
     * @param y y coordinates in pixel
     */
    public void setFixedPosition(int x, int y) {
        mStyles.fixedPosition = new Point(x, y);
    }

    public ArrayList<LegendMapping> mLegendMapping = null;

    public class LegendMapping{
        public LineGraphSeries<DataPoint> mSeries;
        public RectF mSeriesLegendRect;
        public int mColor;
    }
}

