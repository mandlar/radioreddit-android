package net.mandaria.radioreddit.objects;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class ScrollToBottomView extends ScrollView
{

	public ScrollToBottomView(Context context)
	{
		super(context);
	}
	
	public ScrollToBottomView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	
	public ScrollToBottomView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override 
	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
	{
		scrollTo(0, h);
	}
}
