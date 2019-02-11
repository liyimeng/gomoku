# Tic-Tac-Toe Game

## How to run the program

You need to JRE(Java Runtime) installed on your computer to run the program.
On Windows, start server with script run.bat
On *nix (Linux/OSX), start server with script run.sh

then open http://localhost:8080 in your web browser, and start to play the game.
Each player plays the game in a separate window. When playing over the Internet against
your friend or anonymous, you need to run the server on a public IP address.

## Features

- You can play against AI(the computer), your friend, or somebody from Internet.AI Player
  is implemented with a simple game theory algorithm, not any complex machine learning model, 
  but you are welcome to challenge it! :) 
- You could invite your friend by sending the link to the game.
- You may save the URL if you want to continue the game later.
- You may start many games in concurrently, only limit is your server resource.
- You may control the game difficulty by setting the board size and the match point.

If you want any technical support, please contact liyi.meng@me.com :D, enjoy the game.

## Third party library

The program use Apache httpcore library to build the web server, instead of built in http server
in Java SDK because my version of JDK dosen't have that package included, and Eclipse dose not
like it either.

 