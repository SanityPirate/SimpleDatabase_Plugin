# SimpleDatabase

## Introduction

The purpose of this plugin is to implement and showcase basic database interaction using a Minecraft server. At the moment, its
only functionality is to create and store coordinates for a player in a database, but more commands can be added to it.

At the moment, this plugin can:
<ol>
<li>Set a player's home.</li>
<li>Retrieve a list of the homes a player has created from the database.</li>
<li>Tell a player the coordinates of a home they have set.</li>
<li>Delete a player's home from the database.</li>
<li>Say "hi"  to the player.</li>
</ol>

This is not by any means the best way to write a database plugin (notably, it is vulnerable to SQL injections). This was
part of a project created for one of my classes and also
my first time writing any Minecraft plugin. I hope that by posting it here it can help others get their own
plugins started if they are trying to use a database.

## config.yml
This file **must** be configured with the database's connection info in order for the plugin to work.
  
## Screenshots
![](https://user-images.githubusercontent.com/32273966/79284537-f3d9a300-7e88-11ea-9d51-37e51b01133b.png)
![](https://user-images.githubusercontent.com/32273966/79284630-3a2f0200-7e89-11ea-8b93-1dbdc6f3c8c6.png)
![Database View](https://user-images.githubusercontent.com/32273966/79284649-4adf7800-7e89-11ea-96ed-df43662a27ac.PNG)
## Commands
<ul>
<li>/sayhi</li>
<li>/sethome</li>
<li>/findhome</li>
<li>/delhome</li>
</ul>

## Permissions
<ul>
<li>None Required</li>
</ul>
