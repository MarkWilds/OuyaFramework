OuyaFramework
=============

A game framework for the OUYA game console incorporating all features from the OUYA ODK. <br />
The goal is to build a solid and stable game framework for the OUYA game console which is build around performance and ease of use.
This allows us to build games without a fuss in a resource constrainted enviroment.

T.V. development guidelines: https://developers.google.com/tv/web/docs/optimization_guide

Features (These are wanted features!!!)
-----
* Abstract OUYA game class handling everything android and game related.
* OpenGL ES 2.0 for graphics.
* Advanced input manager specifically for games.
* Usefull game specific systems.

### System features
* Android life-cycle management.
* ResourceManager for audio, graphics, etc...
  * Handling context loss.
* Game clock with timers.

### Input features
* ButtonDown, ButtonPressed, ButtonUp
* Timed button presses (combo system...)
* Input mapping, allowing controller input to be mapped to ingame actions.(jump, forward, pause, etc...)
* Controller connected/disconnected listener.
* player controller management. (which controller belongs to which player)

### Audio features
* Audio manager for handling sounds and music.
* Playing sounds.
* Streaming music.

### File I/O features
* Loading resources from assets, internal and external storage.
* Text, binary, or InputStream.

### Graphics features
* General graphics object containing important data for games on a console.
	* Safe area dimensions.
* Textures
  * 2D and cubemap.
* Fonts
  * Create a bitmap font from .ttf files.
* Spritebatch
  * Drawing textures.
  * Drawing text.

### Game features.
* Screen manager.
	* Stack based screen management.
	* Screen transitions. (in/out)
	* Screen types:
		* Normal
		* Popup
* Menu system
	* Create menu's.
	* Menu transitions. (in/out)
	* Add menu items to menu's.
		* Allow for menu item subclassing creating specific menu items.
