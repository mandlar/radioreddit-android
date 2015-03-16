**Tasks have now moved to a Trello board here:** https://trello.com/board/android-tasks/512c3371f4e4e7692000ea20

The list below is now out of date! Please consult the Trello list instead.

## 1.0.1 ##

  * BUG: Submiting song in recently played (maybe Top Charts too) isn't showing the dialog
  * About activity
  * Change wording of "vote error" when not logged in
  * Refresh for Top Charts
  * Refresh for Recently Played
  * Clear for Recently Played
  * Cache top charts lists to memory
  * Bookmarks
  * Download song (if available) (Use a queue and an option for wifi only download?)
  * Link to reddit comments for current submitted song/episode
  * Link to subreddit
  * Use Android 3.0 fragments
  * Tablet interface
  * If talk or music stations are empty, don't show the "heading" for it in the "Choose Stations" screen
  * Add a manual refresh button to "Choose Stations" screen?
  * Return an error message if content from HTTPUtil.Get is empty?
  * Check if user's login password change (detect if cookie is no longer valid?)
  * Pressing stop should kill the service completely
  * Notification of when "Buffering..."
  * Attempt to reconnect X times when completely stopped (usually because of loss of signal).
  * Headset controls support (play/stop? next/prev switch streams?)
  * Lockscreen controls support
    * http://stuff.mit.edu/afs/sipb/project/android/docs/resources/samples/RandomMusicPlayer/src/com/example/android/musicplayer/index.html
    * http://stackoverflow.com/questions/12168046/remote-control-client-for-android
    * http://developer.android.com/reference/android/media/RemoteControlClient.html
    * Look at sample SDK project: android-sdk-windows\samples\android-13\RandomMusicPlayer\
  * iTunes link (Amazon???)
  * Album art?
  * Widget?
  * Android 4.0: add playback controls to notification
  * Add dashclock support?
  * Stop streaming when removed from Car dock? (option in settings)
  * (Idea) Auto starting on Car Dock?
  * Remember last station selected
  * (DONE) Display timeout (option in settings)
  * (DONE) Confirm dialog for logout
  * (DONE) BUG: After listening to an individual song it says Buffering and the Stop button is shown (should show a Play button)

## 1.0 ##

  * (DONE) IMPORTANT: implement ad/broadcast system for when radioreddit.com is down
  * (DONE) Change free version to LITE app icon

## Beta ##

BETA COMPLETE

  * (DONE) SherlockActionBar for older devices
  * (DONE) Darken and disable vote buttons when song/episode information is not yet available (and therefore cannot vote yet)
  * (DONE) Show who you are logged in as (maybe in Settings?) --- Show reddit username beside of "Logout" in menu when logged in
  * (DONE) If not logged in, dialog should give option to go to Login activity
  * (DONE) Handle cumulative scores
  * (DONE) Login to Reddit
  * (DONE) Remove GPS from permissions
  * (DONE) Fix notifications for talk streams
  * (DONE) Look into startForeground to keep service from being killed: http://developer.android.com/intl/de/reference/android/app/Service.html#startForeground%28int,%20android.app.Notification%29
  * (DONE) Break down listview of stations into music and talk
    * See this: http://android.cyrilmottier.com/?p=440
  * (DONE) Cache streams to database after first load
  * (DONE) Bug: if user presses play, it will switch to random (on first load).  Need to select main stream first
  * (DONE) Show dialog if voting for a song will submit it
  * (DONE) Voting
    * (DONE) Submit song if doesn't exist
      * (DONE) Show dialog that song will be submitted as user
    * (DONE) Handle CAPTCHA
    * (DONE) After voting, make a call to get new song info
  * (DONE) On login page, have a link to login page on reddit to give the user a way to register a new account: https://ssl.reddit.com/login
  * (DONE) Top charts
  * (DONE) Previewing songs (bookmarked or top charts)
  * (DONE) Move logout to bottom of menu
  * (DONE) Recently played
  * (DONE) Show dialog on free version if user tries to go to Recently Played or Top Charts (pro only feature, link to pro version, 25% of sale to radio reddit)

## Alpha ##

ALPHA COMPLETE

  * (DONE Handle condition of being in preparing state when switching stations (currently throws illegal state)
  * (DONE) Change station needs to be a button click on a layout, not the text (it is currently hard to tap on)
  * (DONE) If removed head phones, stop media player
  * (DONE) Override volume control to media volume
  * (DONE) Wifi Wake lock
  * (DONE) UI state for buttons (hover, pressed, etc.)
  * (DONE) Update notification with current song info
  * (DONE) Fix state issues with playback control
    * (DONE) isAborting - user decides to change mind and wants to play again while still isPreparing.  Remove isAborting flag?
  * (DONE) Fix weird state issue with activity sometimes spawning new, which then doesn't use the correct application state so it resets displayed info to main stream (even if it is playing a different stream). Proposed fix: don't use app state in activity, get the information from the service (service should push info to whatever activity is showing) Actual fix: getStreams API call was overwriting the current stream value
  * (DONE) Add Email Feedback menu button
  * (DONE) Add an Exit menu button
    * (DONE) Exit still isn't working just quite right
  * (DONE) Fix wrapping issues with songs/artists/playlists that are too long
  * (DONE) More robust API calling
    * (DONE) Check if stream is online
    * (DONE) Check if radioreddit is down
    * (DONE) Check if reddit is down
    * (DECLINED) Check for 404s or other possible errors... - don't think this is necessary for just getting information, for other methods (login, vote), yes
  * (DECLINED) Check if stream has changed when getting current info - not really an issue; very, very rare and would be correct in 30 seconds anyways
  * (DONE) Move get streams to async task
    * (DONE) Have it called every 30 seconds
    * (DONE) Fix behavior of startup with this now being async
  * (DONE) Show buffering spinning please wait
    * See: http://stackoverflow.com/questions/4445663/mediaplayer-prepare-is-throwing-an-illegalstateexception-when-playing-m4a-file
    * More things to look at:
      * https://github.com/Ramblurr/mp3tunes-android/blob/5588742d2b50a08bef686670d72c9770eac7a69c/src/com/mp3tunes/android/service/Mp3tunesService.java
      * https://github.com/atwupack/lastfm-android/blob/10f18a88465eb3d1f830ae431e2259dd4553151a/app/src/fm/last/android/player/RadioPlayerService.java
  * (DONE) Lose connection to stream: attempt to replay, etc.?
    * Stop worrying about onBuffering, focus on onCompletion (when it stops streaming) and whether to "re-try" the stream or not?
    * Official last.fm client: https://github.com/c99koder/lastfm-android/blob/master/app/src/fm/last/android/player/RadioPlayerService.java
  * (DONE) Better landscape orientation
  * (DONE) Add Arrow down under choosing streams
  * (DONE) Gray out alien eyes while connecting
  * (DONE) Scale text for tablets (create /values/ for -large -xlarge???)
  * (DONE) Move more things into styles
  * (DONE) Add "Change station" to menu
  * (DONE) Android 3.0 support (push to Beta?)
    * See: http://www.kaloer.com/making-your-app-tablet-friendly-3-steps
  * (DONE) Media volume should change on change station activity
  * (DONE) Add support for talk station
  * (DONE) Check for TODO's in code
  * (DONE) Clean up code, add GPL licensing to each file
  * (DONE) Add Flurry tracking