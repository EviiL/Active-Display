package me.thomasdotters.activedisplay.Views;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by Thomas Dotters on 05/09/2016.
 */
public class ResizeAnimation extends Animation {

	private int mWidth;
	private int mHeight;
	private int mStartWidth;
	private int mStartHeight;
	private View mView;
	private int mParentY;
	private int mPosY;

	public ResizeAnimation(View view, int width, int height)
	{
		mView = view;
		mWidth = width;
		mHeight = height;
		mStartWidth = view.getWidth();
		mStartHeight = view.getHeight();
		mPosY = mView.getTop();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t)
	{
		int newWidth = mStartWidth + (int) ((mWidth - mStartWidth) * interpolatedTime);
		int newHeight = mStartHeight + (int) ((mHeight - mStartHeight) * interpolatedTime);
		mView.getLayoutParams().width = newWidth;
		mView.getLayoutParams().height = newHeight;
		mView.setTop(mPosY);
		mView.requestLayout();
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight)
	{
		super.initialize(width, height, parentWidth, parentHeight);
	}

	@Override
	public boolean willChangeBounds()
	{
		return true;
	}
}
