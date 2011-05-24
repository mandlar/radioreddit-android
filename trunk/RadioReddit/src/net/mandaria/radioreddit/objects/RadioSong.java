package net.mandaria.radioreddit.objects;

public class RadioSong
{
	public int ID; 
	public String Title;
	public String Artist;
	public String Redditor; //(note: return Artist (redditor) as one String)
	public String Genre;
	public String Playlist;// (pulled from parent node)
	public String Reddit_title; //(Name of reddit link)
	public String Reddit_url; //(reddit link)
	public String Preview_url; //(Used for previewing song? Always exists?)
	public String Download_url; //(Used for downloading song? Different url, but same mp3 as preview_url? May not exist)
	public String Bandcamp_link; //(Link to song on bandcamp, May not exist)
	public String Bandcamp_art; //(album art from bandcamp, May not exist)
	public String Itunes_link; //(Link to song on itunes, May not exist)
	public String Itunes_art; //(Link to album art from itunes, May not exist)
	public String Itunes_price; //(Price of song on itunes, May not exist)
}
