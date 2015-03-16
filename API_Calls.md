# Introduction #

This document is to show how to make the API calls to radio reddit

# Beta streams #

Beta streams for to the new stations (including the Talk station) are found here:

http://radioreddit.com/config/streams.new.json or .xml

http://radioreddit.com/api/talk/status.json or .xml

This was done because the API cannot be updated until the latest iOS version is released.


# Getting Streams Information #

Make a call to http://radioreddit.com/config/streams.json to get a list of all the streams.

The json returns a List of RadioStreams.

A RadioStream object consists of:
  * String Name (pulled from the parent JSON object)
  * String Type (BETA: switch for music / talk)
  * String Description
  * String Relay (pulled from each stream's status.json "Relay")
  * Boolean Online (pulled from each stream's status.json "Online")
  * String Server (not used)
  * String Transcoder (not used)
  * String Status (This is the path to other JSON calls, e.g. /api/electronic maps to http://radioreddit.com/api/electronic/status.json, etc.)
  * String Playlists (another directory mapping? Seems to be for playing in browser, not used)
  * List of Relays (list of stream addresses, not used)

A Relay object consists of
  * String Server (address to stream from, not used. Pull the one from status.json instead)

Stream information should be only called onCreate and should not need to be called again during lifecycle of the application

For now, stream information will be kept in memory in Application object. Later, maybe save it to a database? (E.g. to handle case of web server goes down but stream is still up)

# Getting Song Information #

Make a call to http://radioreddit.com(API)status.json to get the stream's current status. (API) is pulled from the streams.json's "Status" variable. For example, http://radioreddit.com/api/electronic/status.json is for electronic stream.

The json returns a List of Songs, the current playlist, online status, relay, and number of listeners

A Song object consists of:
  * Int ID (not used?)
  * String Title
  * String Artist
  * String redditor (note: return Artist (redditor) as one String)
  * String Genre
  * String Playlist (pulled from parent node)
  * String reddit\_title (Name of reddit link)
  * String reddit\_url (reddit link)
  * String preview\_url (Used for previewing song? Always exists?)
  * String download\_url (Used for downloading song? Different url, but same mp3 as preview\_url? May not exist)
  * String bandcamp\_link (Link to song on bandcamp, May not exist)
  * String bancamp\_art (album art from bandcamp, May not exist)
  * String itunes\_link (Link to song on itunes, May not exist)
  * String itunes\_art (Link to album art from itunes, May not exist)
  * String itunes\_price (Price of song on itunes, May not exist)
  * String Score (Current voting score of the song. Must call http://www.reddit.com/api/info.json?url=(URL ENCODED reddit\_url) and get "score". Example: http://www.reddit.com/api/info.json?url=http%3A%2F%2Fwww.radioreddit.com%2Fsongs%2F%3Fsong%3DBlack_Water_%2528zoq-fot-pik%2529_On_A_Grid ) Can be an int value, "?", or "vote to submit"
  * String Likes (null, or true if voted up, false if voted down)
  * String Name; (ID used to vote on)

# Getting Episode Information #

Make a call to http://radioreddit.com(API)status.json to get the stream's current status. (API) is pulled from the streams.json's "Status" variable. For example, http://radioreddit.com/api/talk/status.json is for talk stream.

The json returns a List of Episodes, the current playlist, online status, relay, and number of listeners

A Episode object consists of:
  * Int ID (not used?)
  * String EpisodeTitle
  * String EpisodeDescription
  * String[.md](.md) EpisodeKeywords (most likely not going to be used)
  * String ShowTitle
  * String[.md](.md) ShowHosts
  * String[.md](.md) ShowRedditors
  * String[.md](.md) ShowGenre (most likely not going to be used)
  * String ShowFeed (to subscribe to podcasts?)
  * String Playlist (pulled from parent node)
  * String reddit\_title (Name of reddit link)
  * String reddit\_url (reddit link)
  * String preview\_url (Used for previewing episode? Always exists?)
  * String download\_url (Used for downloading episode? Different url, but same mp3 as preview\_url? May not exist)
  * String Score (Current voting score of the song. Must call http://www.reddit.com/api/info.json?url=(URL ENCODED reddit\_url) and get "score". Example: http://www.reddit.com/api/info.json?url=http%3A%2F%2Fwww.radioreddit.com%2Fsongs%2F%3Fsong%3DBlack_Water_%2528zoq-fot-pik%2529_On_A_Grid ) Can be an int value, "?", or "vote to submit"
  * String Likes (null, or true if voted up, false if voted down)
  * String Name; (ID used to vote on)

# Logging in #

Make a post to http://www.reddit.com/api/login/USERNAME

Post values:
  * String user = username
  * String passwd = plaintext password
  * String api\_type = json (always this value)

Always returns 200 OK, check for WRONG\_PASSWORD or errors in return or empty data

Returns:
  * String modhash
  * String cookie

Now using SSL login: http://www.reddit.com/r/changelog/comments/l4n6y/reddit_change_log_in_with_ssl_javascript_fixes/

references:
  * https://github.com/talklittle/reddit-is-fun/blob/master/src/com/andrewshu/android/reddit/login/LoginTask.java
  * https://github.com/talklittle/reddit-is-fun/wiki/Login
  * https://github.com/reddit/reddit/wiki/API%3A-login

# Voting / Submitting #

1a. Get most up to date song information (in case cached info is old)

1b. Check if modhash is up to date

2a. If it exist:

> a. Get the FULLNAME from reddit and vote on it: http://www.reddit.com/api/vote

2b. If it exist, but is archived

> a. Submit as a new post to be voted on? Or simply say that the song has been archived and cannot be voted on?

> b. BUG: apparently the API allows votes on archived posts.  This needs to be discussed with reddit admins or similar

3. If it doesn't exist:

> a. Try to submit the post http://www.reddit.com/api/submit:

> b. If it fails, display error (or CAPTCHA) and try again

references:
  * https://github.com/reddit/reddit/wiki/API%3A-vote
  * https://github.com/reddit/reddit/wiki/API (submit is towards the bottom)
  * https://github.com/reddit/reddit/wiki/API%3A-submit
  * https://github.com/talklittle/reddit-is-fun/wiki/Voting
  * https://github.com/talklittle/reddit-is-fun/blob/master/src/com/andrewshu/android/reddit/common/tasks/VoteTask.java
  * https://github.com/talklittle/reddit-is-fun/blob/master/src/com/andrewshu/android/reddit/common/Common.java (for update modhash function)

# List of all API calls (from iOS version) #

//RadioRedditServer stuff

NSString **const RadioRedditBaseURL = @"http://radioreddit.com";**

NSString **const RadioRedditStreamsJSON = @"http://radioreddit.com/config/streams.json";**

NSString **const RadioRedditChartsAll = @"http://radioreddit.com/api/charts_all.json";**

NSString **const RadioRedditChartsMonth = @"http://radioreddit.com/api/charts_month.json";**

NSString **const RadioRedditChartsWeek = @"http://radioreddit.com/api/charts_week.json";**

NSString **const RadioRedditChartsDay = @"http://radioreddit.com/api/charts_day.json";**


//reddit links

NSString **const RedditLoginURL = @"http://www.reddit.com/api/login";**

NSString **const RedditVoteURL = @"http://www.reddit.com/api/vote";**

NSString **const RedditSaveURL = @"http://www.reddit.com/api/save";**

NSString **const RedditUnsaveURL = @"http://www.reddit.com/api/unsave";**

NSString **const RedditLinkByURL = @"http://www.reddit.com/api/info.json?url=";**

NSString **const RedditGetSavedURL = @"http://www.reddit.com/saved.json";**

NSString **const RedditSubmitURL = @"http://www.reddit.com/api/submit";**

NSString **const RedditCaptchaURL = @"http://www.reddit.com/captcha/";**

# Other info from Andreas #

## Song Info ##

While just listening In radio mode I make an API call every 30 seconds.
First I make a request to radio reddit to get which song is playing and when I get a response I send a request to reddit asking for vote info and so on for that song.

## Voting ##

When voting for a song we don't always know the name (an id that reddit uses for all submissions). This happens for example when voting for songs in the live stream before we have gotten the info from reddit, or if the song hasn't been submitted. We then have to:

1. Get the song info from radio reddit.

2. Ask reddit for the info about the song via this API:  http://www.reddit.com/api/info.json?url=
where the URL is the reddit\_url in the song info we got from radio reddit.

3. When we get response from 2 we see if it exists or not. If it exists we can get the "name" from reddit.

4. When we have the "name" we can vote by using http://www.reddit.com/api/vote with some different POST values. If it's not submitted we can use the submit API and then we get a response in a callback if there was an error (like requires captcha, already submitted (if someone submitted just before you did), submitting too fast etc).


---


When voting I first ask radio reddit for what song is playing, just to make sure I have the latest available information before sending the vote request to reddit (as the "cashed" song might differ from what's actually playing due to the 30 second delay).

After I've done this step I have two ways I can go and must choose:

1. The song I have cashed is the same as the one I got in the response from radio reddit. That means I most likely also have the "reddit id" /name stored and I can use that to send a vote request directly. So I just send the vote api with that song name and so on with reddits API.

2. If they differ it means I first have to get the reddit id / name for the submission I want to vote for. I get this by asking reddit for information via their info api (http://www.reddit.com/api/info.json?url=). When I get that response I can vote. If I get an empty response it means that it hasn't been submitted, so I will use the submit API (3).

3. When submitting i first try to just send the submit API. Then I get some different responses depending on some error happened. This response is not a JSON response but a JQuery string (ugh). In that string I check for known errors "RATELIMIT" "BAD CAPTCHA" and so on. Depending on what answer I get I present different message boxes to the user. Bad captcha means I have to present the captcha and send the submit request again with captcha code and answer POST values..

4. In the lists I usually have most information already so I don't have to ask for the reddit id for example (with the exception for non-submitted songs and the songs in the charts).

The order is pretty much the same as before otherwise.
I also store all reddit vote/save info and so in on in the song objects I have, so if you tap on song 1 in the lists, then song 2 and then song 1 again I don't have to download song 1 info from reddit all over again. I clear this "cache" when exiting the lists.

5. Everytime I fetch information from radio reddit or reddit for a song, a notification is sent that all songs in all lists get. If the information happens to relate to them they automatically updates itself. So if a song happens to be played on the radio that also exists in for example the bookmarks, the song in the bookmark get's updated.

## Bookmarks ##

Yes, I store bookmarks locally. I wanted the possibility to bookmark without using a service. I made the "save on reddit" feature as a way of getting bookmarks on/off the device. The reddit submission doesn't contain all info about the song that radio\_reddit API provides. Like the preview\_url for example. Not all songs have download\_url so then we have to use the preview\_url.

The bad thing about preview\_urls is that they will in the future be temporary and change every now and then. This means a bookmark can become non playable after a while if no dowload\_url or preiew\_url exists. I've thought about parsing the preview\_url from the songs permalink but that's quite messy. Bookmarks will at least get the user a way to remember songs for later and perhaps download in the browser at home.

I'm implementing a method that updates bookmarked songs with fresh info whenever a bookmarked song happens to be played in the radio streams.

## Heard on radio ##

Heard on radio, yes, I store them in an array that gets dumped each time you kill the application.

## Charts ##

That API looks like this:

NSString **const RadioRedditChartsAll = @"http://radioreddit.com/api/charts_all.json";**

NSString **const RadioRedditChartsMonth = @"http://radioreddit.com/api/charts_month.json";**

NSString **const RadioRedditChartsWeek = @"http://radioreddit.com/api/charts_week.json";**

NSString **const RadioRedditChartsDay = @"http://radioreddit.com/api/charts_day.json";**

# My API requests #

A short list of things I'd like to see in the API:
  * A better "name" of the station, e.g. "hiphop" vs "Hip Hop and Rap" (add to streams.json)
  * Amazon MP3 purchase links (for Android users)
  * A value that returns true when the station is playing an advertisement (for promoting donations, pro versions, etc.). Add to stream's status.json