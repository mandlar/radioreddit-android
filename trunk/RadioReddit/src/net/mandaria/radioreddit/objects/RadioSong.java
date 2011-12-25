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

public class RadioSong
{
	public int ID;
	public String Title;
	public String Artist;
	public String Redditor; // (note: return Artist (redditor) as one String)
	public String Genre;
	public String Playlist;// (pulled from parent node)
	public String Reddit_title; // (Name of reddit link)
	public String Reddit_url; // (reddit link)
	public String Preview_url; // (Used for previewing song? Always exists?)
	public String Download_url; // (Used for downloading song? Different url, but same mp3 as preview_url? May not exist)
	public String Bandcamp_link; // (Link to song on bandcamp, May not exist)
	public String Bandcamp_art; // (album art from bandcamp, May not exist)
	public String Itunes_link; // (Link to song on itunes, May not exist)
	public String Itunes_art; // (Link to album art from itunes, May not exist)
	public String Itunes_price; // (Price of song on itunes, May not exist)
	public String Score; // Current voting score of the song
	public String Likes; // Null if not voted, true if voted up, false if voted down
	public String Name; // ID used to vote on
	public String ErrorMessage;
}
