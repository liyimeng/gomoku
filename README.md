# Gomoku

## How to compile/Run 

This is a Eclipse project, you may import the project in Eclipse and build it. 

If you use Eclise or similar IDE, just hit run! 

then open http://localhost:8080 in your web browser, and start to play the game.
Each player should plays the game in a separate window. When playing over the Internet against
your friend or anonymous, you need to run the server on a public IP address.

## Features

- You can play against AI(the computer), your friend, or somebody from Internet.AI Player
  is implemented with a simple game theory algorithm, not any complex machine learning model, 
  but you are welcome to challenge it! :) 
- You could invite your friend by sending a link to the game.
- You may save the URL if you want to continue the game later.
- You may start many games in concurrently, only limit is your server resource.
- You may control the game difficulty by setting the board size and the match point.


## Third party library

The program use Apache httpcore library to build the web server, instead of built in http server
in Java SDK because my version of JDK dosen't have that package included, and Eclipse dose not
like it either.

# 五子棋
超简单五子棋游戏Java实现。可以人人对玩，或者人机对玩。人机对玩还挺刺激，我都没有怎么赢过，你也试试看！
## 如何运行
你可以用Elcipse or 类似的IDE来导入本项目。然后直接运行即可。运行之后打开浏览器， 输入网址http://localhost:8080
就可以开始玩了。 一局两个玩家，各用一个浏览器窗口。如果你要在互联网上，你需要运行服务器在公网IP地址上。

## 主要功能

- 人机对玩，机器部分只用博弈论实现，没有AlphaGo辣么牛的人工智能算法，但是我自己没有怎么赢过！ 
- 你可以通过发送链接邀请朋友对玩
- 通过保存链接，你可在方便时接着玩.
- 支持很多很多人同时玩，只要你的服务器资源够，
- 你可以自定义棋盘大小，赢棋的点数来改变游戏难度。
 