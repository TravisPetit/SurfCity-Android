# SurfCity-Android
Kotlin implementation of a Secure Scuttlebutt client-prototype for Android that was developed for a bachelor thesis at the University of Basel. Its based on the concept of and inspired by https://github.com/cn-uofbasel/SurfCity. 

Implementation choices were influenced by https://github.com/cherryasphalt/quilt and https://github.com/apache/incubator-tuweni.

## Features
The application functions as a passive SSB viewer, which processes the messages it received.

- Continuous sync of followed feeds
- Adding more feeds automatically when coming across contact messages
- Displaying (all) public posts in one feed, ordered by their timestamps
- Clean-up of messages older than seven days or of unsupported types
- Importing an identity by its secret key (for testing)

## Important Future Work
To be suitable for daily use, the following features have to be implemented first:

- Submitting own messages
- Redeeming Pub invites
- Management of connections
- Extended user interface for categorized feeds and thread structure
  (including replying to these threads)
