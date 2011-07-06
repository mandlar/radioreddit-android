package net.mandaria.radioreddit.objects;

public class RadioEpisode
{
	public int ID;
	public String EpisodeTitle;
	public String EpisodeDescription;
	public String EpisodeKeywords;
	public String ShowTitle;
	public String ShowHosts;
	public String ShowRedditors;
	public String ShowGenre;
	public String ShowFeed;
	public String Playlist;// (pulled from parent node)
	public String Reddit_title; // (Name of reddit link)
	public String Reddit_url; // (reddit link)
	public String Preview_url; // (Used for previewing song? Always exists?)
	public String Download_url; // (Used for downloading song? Different url, but same mp3 as preview_url? May not exist)
	public String Score; // Current voting score of the song
	public String ErrorMessage;
}
