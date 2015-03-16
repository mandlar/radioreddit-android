# Introduction #

This will hold a rough list of database designs.


# StreamsCache #

This database will hold the most recent information of all of the streams. This is used for the next start-up so we do not have to wait to retrieve all of the streams again.  Most likely, the streams haven't changed since the last time we've seen them (and if they have, we'll shortly after get the a fresher version of them).

| **`_id`** | INTEGER | NOT NULL PRIMARY KEY AUTOINCREMENT |
|:----------|:--------|:-----------------------------------|
| **Name** | TEXT | NOT NULL |
| **Type** | TEXT | NOT NULL |
| **Description** | TEXT | NOT NULL |
| **Status** | TEXT | NOT NULL |
| **Relay** | TEXT | NOT NULL |
| **Online** | BOOLEAN | NOT NULL |

# RecentlyPlayed #

This database will hold songs/episodes that were recently listened to.

| **`_id`** | INTEGER | NOT NULL PRIMARY KEY AUTOINCREMENT |
|:----------|:--------|:-----------------------------------|
| **ListenDate** | TEXT | NOT NULL |
| **Type** | TEXT | NOT NULL |
| **id** | INTEGER | NULL |
| **Title** | TEXT | NULL |
| **Artist** | TEXT | NULL |
| **Redditor** | TEXT | NULL |
| **Genre** | TEXT | NULL |
| **Reddit\_title** | TEXT | NULL |
| **Reddit\_url** | TEXT | NULL |
| **Preview\_url** | TEXT | NULL |
| **Download\_url** | TEXT | NULL |
| **Bandcamp\_link** | TEXT | NULL |
| **Bandcamp\_art** | TEXT | NULL |
| **Itunes\_link** | TEXT | NULL |
| **Itunes\_art** | TEXT | NULL |
| **Itunes\_price** | TEXT | NULL |
| **Name** | TEXT | NULL |
| **EpisodeTitle** | TEXT | NULL |
| **EpisodeDescription** | TEXT | NULL |
| **EpisodeKeywords** | TEXT | NULL |
| **ShowTitle** | TEXT | NULL |
| **ShowHosts** | TEXT | NULL |
| **ShowRedditors** | TEXT | NULL |
| **ShowGenre** | TEXT | NULL |
| **ShowFeed** | TEXT | NULL |