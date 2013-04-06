/*
 *	radio reddit for android: mobile app to listen to radioreddit.com
 *  Copyright (C) 2011 Bryan Denny
 *  
 *  This file is part of "radio reddit for android"
 *
 *  "radio reddit for android" is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  "radio reddit for android" is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with "radio reddit for android".  If not, see <http://www.gnu.org/licenses/>.
 */

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
