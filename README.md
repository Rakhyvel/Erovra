# Erovra- From Swedish "To conquer"
### Erovra 1.0.16
- Upgrading function!
- Bombers now target buildings strategically
- Boosted AI infantry's will to target capitals
- Updated mountain terrain generation so that its a bit more realistic looking
- Added dropdown to capital
- Added tabs, reworked GUI
- Balanced game to work with upgrades
- Canceling now gives back half of what was paid, instead of a flat 10 coins
- Added shadows to planes, bombs, and artillery shells
- Artillery shells now curve
- Rivers flow at random angles
- Fixed some stuff with AI landing craft not sending craft after building them
- Added window icon
- Updated how images were drawn, was using too much memory
- Landing craft now have an icon of whats boarded

### Erovra 1.0.15
- Added cancel feature to industry
- Fixed shells lingering after they hit
- Updated plane AI to interact with patrol points
- Units calculate whether or not they are hit before other operations
- Ports, airfields, cities, and factories have the same defense (in prep for upgradable buildings)
- Can no longer select ships or planes to go inside of landing craft
- Added top bar for coins (In prep for task forces)
- Can no longer see enemy plane patrol point
- Ships don't overlap anymore
- Updated rendering process for images to be more stream lined
- Planes, especially fighters, have better targeting AI

### Erovra 1.0.14
- Fixed dropdown height for industry
- Updated bombers so that they don't show their first targets
- Updated bombs so that units hit will be shown
- Updated terrain generation, added function that flattens coastal regions
- Arrows no longer make the game crash
- Can no longer build factories, ports, or airfields on mountain tiles
- Added patrol points for fighters and attackers
- Cruiser speed boost
- Landing craft now land at the coast instead of a little ways in
- Artillery shells change size to simulate a parabolic arc
- Torpedo sprites now have screws
- MASSIVELY changed terrain generation, now uses true perlin noise with cosine interpolation

### Erovra 1.0.13
- Fixed arrows being wonky, now work with ships as well
- Bombers may be retargeted, show a target on screen, and return to base
- Landing craft selection made better **again**
- Improved artillery radii, added it to cruisers 
- Improved map colors, map generation
- Fixed mountain and ship collision avoidance *(FINALLY!!)*

### Erovra 1.0.12
- Ships will now convoy their own landing craft
- Updated colors to be prettier

### Erovra 1.0.11
- Fixed bomber sprites
- Improved ports finally so that nations will send landing craft if they can (Seriously, watch out!)

### Erovra 1.0.10
- Added custom maps

### Erovra 1.0.9
- Added difficulty factor
- Units have less firepower the smaller they are
- Fixed ship path finding
- Units are slowed down if they are engaged

### Erovra 1.0.8
- Added decommision to arifields and ports. Would add it to factories, but it looked weird.
- Added target arrow to infantry, artillery, cavalry, and ships

### Erovra 1.0.7 
- Fixed mouse input being slightly off
- Fixed dropdown height issues
- Prettified errormessages
- Added error messages for buildings
- AI team's landing craft are much smarter now, target more than capital
- Added bounding box collision detection to units moved by mouse, to make it absolutely accurate and far less annoying (fingers crossed)
- Added artillery range

### Erovra 1.0.6
- Fixed infantry spawning
- AI team now only builds ports where it makes sense
- Fixed unit selection bug that trapped user
- Only one unit can be selected at a time now (For real this time)
- Added error messages

### Erovra 1.0.5
- Tweaked AI a bit so that it builds more cities and prioritizes sea supremacy
- Fixed time display for industries
- Fixed buggy landing craft UI
- Capitals now produce infantry every 10 seconds, cities do not
- Tweaked cost scaling to even things out
- Finally got rid of floating units!

### Erovra 1.0.4
- Added kerning to improve text
- Created javadoc
- Reworked how dropdowns function, should improve performance slightly but also produce more readable code
- Added Industry class to derive factories, ports, and airfields from. Again to improve performance.

### Erovra 1.0.3
- Only one unit can now be selected at a time
- Buildings cannot be built on top of one another anymore
- Drop down logic is now way better
- Added health bar in drop down
- Factories/airfields/ports now show what is being built
- Drop downs now show what the player can afford
- Artillery is now avaiable to be purchased and controlled

### Erovra 1.0.2
- Fixed teleportation bug
- Rigorized mouse input logic
- Added landing craft drop down
- Fixed ship teleportation bug
- Added health bar to drop down

### Erovra 1.0.1
- Infantry drop down added
- Factory, airfield, and port drop down added

### Erovra 1.0.0
- Infantry units may now be moved using the mouse

### Erovra 0.5.5
- Reorganized resource folder
- Added a semi-fast zoom, could be way improved

### Erovra 0.5.4
- Added main menu
- Made menu buttons functional
- Added match clock at the end

### Erovra 0.5.3
- Optimized string rendering
- Added keyboard input

### Erovra 0.5.2
- Worked in mouse input
- Added buttons
- Fixed up pause menu

### Erovra 0.5.1
- Added string rendering
- Added defeat/victory screens

### Erovra 0.5.0
- Added game states
- Added menu objects
- Reorganized packages to look neat and orderly

### Erovra 0.4.6
- Finally balanced this thing
- Fixed projectile rendering

### Erovra 0.4.5
- Fixed AI so that it decides what to produce based on its current strengths/weaknesses

### Erovra 0.4.4
- Re-added factories
- Landing craft now board passengers

### Erovra 0.4.3
- Made shooting more random
- Balanced game further
- Added hitboxes for planes and boats
- AA guns now lead targets
- Spotted structures stay spotted (prep for singleplayer)

### Erovra 0.4.2
- Added strategic bomber
- Added bombs
- Sprites are now able to be resized
- Balanced game as best as I could
- Added AA guns

### Erovra 0.4.1
- Updated plane AI
- Added faster trig functions
- Added Open Sea map mode
- Updated boat AI

### Erovra 0.4.0
- Added airfields
- Added fighter plane
- Added attacker plane
- Added basic plane AI
