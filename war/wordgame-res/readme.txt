- each tap 10 points
- each wrong tap -2 points
- points cashed only when word finished
- game ends when words end

Client Architecture
===================

App.util.Game
-------------
Holds game state (current status of players and game stats) and 
exposes game logic methods. It interacts with App.util.GameClient
and with Controller.Game using events and direct method calls;

App.util.GameClient
-------------------
Holds the connection to the server and understands the client-server protocol.
It interacts with App.util.GameClient using events and direct method calls

Controller.Game
---------------
Interacts util.Game and View.Game.


server <--http--> util.GameClient <--> util.Game <--> Controller.Game <--> View.Game <--> View.GameWord

 