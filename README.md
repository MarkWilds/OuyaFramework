OuyaFramework
=============

A game framework for the OUYA game console incorporating all features from the OUYA ODK.

Features
-----
* Abstract OUYA game class handling everything android and game related.
* OpenGL ES 2.0 for graphics.
* InputManager specifically for games, based on OuyaController.

### System features
* Android life-cycle management.
* ResourceManager for audio, graphics, etc...

### Input features
* ButtonDown, ButtonPressed, ButtonUp
* Timed button presses (combo system...)
* Action mapping, allowing controller input to be mapped to ingame actions.(jump, forward, pause, etc...)
* Controller connected/disconnected listener.

### Audio features
* Playing sounds.
* Streaming music.

### File I/O features
* Loading resources from assets, internal and external storage.

### Graphics features
* Spritebatch
