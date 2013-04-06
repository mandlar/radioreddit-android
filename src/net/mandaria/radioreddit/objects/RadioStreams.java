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

import java.util.ArrayList;
import java.util.Collections;

// This is a container class for the GetRadioStreams async task
public class RadioStreams
{
	public ArrayList<RadioStream> RadioStreams;
	public String ErrorMessage;
	
	public static ArrayList<RadioStream> getMusicStreams(ArrayList<RadioStream> streams)
	{
		ArrayList<RadioStream> musicStreams = new ArrayList<RadioStream>();
		
		for(RadioStream stream : streams)
		{
			if(stream.Type.equals("music"))
			{
				musicStreams.add(stream);
			}
		}
		
		Collections.sort(musicStreams);
		
		return musicStreams;
	}
	
	public static ArrayList<RadioStream> getTalkStreams(ArrayList<RadioStream> streams)
	{
		ArrayList<RadioStream> talkStreams = new ArrayList<RadioStream>();
		
		for(RadioStream stream : streams)
		{
			if(stream.Type.equals("talk"))
				talkStreams.add(stream);
		}
		
		Collections.sort(talkStreams);
		
		return talkStreams;
	}
	
	public static RadioStream getStreamByStreamName(ArrayList<RadioStream> streams, String streamName)
	{
		for(RadioStream stream : streams)
		{
			if(stream.Name.equals(streamName))
				return stream;
		}
		
		return null; // no stream found
	}
	
	public static RadioStream getMainStream(ArrayList<RadioStream> streams)
	{
		RadioStream stream = getStreamByStreamName(streams, "main");
		
		// if there is no main stream, then grab the first available stream
		if(stream == null)
			stream = streams.get(0);
		
		return stream;
	}
}
