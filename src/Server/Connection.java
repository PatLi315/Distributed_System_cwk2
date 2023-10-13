package Server;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import Common.Consts;
import Common.Player;

public class Connection extends Thread {
    public BufferedReader in;
    public BufferedWriter out;
    private Random random = new Random();
    public GameInfo game;
    public Countdown countdown;
    public Connection(Socket socket) throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }
    class HeartBeat extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    out.write(Consts.DEFAULT_RESP + "\n");
                    out.flush();
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                countdown = new Countdown();
                countdown.start();
            }
        }
    }
    class Countdown {
        int seconds = 30;
        boolean cancelled = false;
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                if (game != null && game.isResumed) {
                    seconds = 30;
                    timer.cancel();
                    cancelled = true;
                } else if (seconds > 0 && !cancelled) {
                    System.out.println("resume time left: " + seconds);
                    seconds--;
                } else {
                    System.out.println("resume time out");
                    seconds = 30;
                    timer.cancel();
                    cancelled = true;
                    game.isTimeout = true;
                    Player updatedX = Server.players.get(game.x.name);
                    Player updatedO = Server.players.get(game.o.name);
                    updatedX.rank += 2;
                    updatedO.rank += 2;
                    Server.players.put(game.x.name, updatedX);
                    Server.players.put(game.o.name, updatedO);
                    try {
                        game.x.out.write(Consts.DRAW + "\n");
                        game.x.out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        game.o.out.write(Consts.DRAW + "\n");
                        game.o.out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        public void start(){
            timer.scheduleAtFixedRate(task, 0, 1000);
        }
    }
    public static void sendMessage(BufferedWriter out, String msg) throws IOException {
        out.write(msg + "\n");
        out.flush();
        System.out.println("resp >> " + msg);
    }

    public void run() {
        System.out.println("Connection running");
        String input;
        try {
            for (input = in.readLine(); input != null; input = in.readLine()) {
                System.out.println("req >>" + input);
                // <CMD>#<PARAMS#SPLIT#WITH#DELIMITER>
                String[] parsed = input.split(Consts.D);
                switch (parsed[0]) {
                    case Consts.DEFAULT_RESP:
                        break;
                    case Consts.NEW_PLAYER:
                        System.out.println("Games: " + Server.games.size());
                        game = null;
                        for (Map.Entry<Integer, GameInfo> g : Server.games.entrySet()) {
                            if (g.getValue().o.name.equals(parsed[1]) || g.getValue().x.name.equals(parsed[1])) {
                                game = g.getValue();
                                break;
                            }
                        }
                        if (game == null || game.isTimeout) {
                            Player player;
                            Player p = Server.players.get(parsed[1]);
                            if (p != null) {
                                p.out = out;
                                player = p;
                            } else {
                                player = new Player(parsed[1], out);
                                Server.players.put(parsed[1], player);
                                System.out.println("New player added: " + player.name);
                            }
                            if (Server.waitingPool.isEmpty()) {
                                Server.waitingPool.add(player);
                                sendMessage(out, Consts.DEFAULT_RESP);
                                System.out.println("New waiter added: " + player.name);
                            } else{
                                Player opponent = Server.waitingPool.remove(0);
                                if (random.nextBoolean()) {
                                    // player first
                                    int gameId = Server.generateGameId();
                                    sendMessage(opponent.out, Consts.NEW_GAME + Consts.D + gameId + Consts.D
                                            + opponent.rank + Consts.D + player.name + Consts.D + player.rank + Consts.D + "O");
                                    GameInfo newGame = new GameInfo(gameId, player, opponent);
                                    Server.games.put(gameId, newGame);
                                    sendMessage(out, Consts.NEW_GAME + Consts.D + gameId + Consts.D
                                            + player.rank + Consts.D + opponent.name + Consts.D + opponent.rank + Consts.D + "X");
                                } else {
                                    // opponent first
                                    int gameId = Server.generateGameId();
                                    sendMessage(opponent.out, Consts.NEW_GAME + Consts.D + gameId + Consts.D
                                            + opponent.rank + Consts.D + player.name + Consts.D + player.rank + Consts.D + "X");
                                    GameInfo newGame = new GameInfo(gameId, opponent, player);
                                    Server.games.put(gameId, newGame);
                                    sendMessage(out, Consts.NEW_GAME + Consts.D + gameId + Consts.D + player.rank
                                            + Consts.D + opponent.name + Consts.D + opponent.rank + Consts.D + "O");
                                }
                                System.out.println(opponent.name);
                            }
                        } else {
                            // game is not null, which means there's an ongoing game that should be resumed
                            System.out.println("resume " + game.x + game.o + " " + game.gameId);
                            sendMessage(out, Consts.DEFAULT_RESP);

                            Player player, opponent;
                            String chess;
                            game.isResumed = true;

                            if (game.o.name.equals(parsed[1])) {
                                player = new Player(parsed[1], out);
                                opponent = game.x;
                                chess = "O";
                                GameInfo updatedInfo = game;
                                updatedInfo.o = player;
                                Server.games.put(game.gameId, updatedInfo);
                            } else {
                                player = new Player(parsed[1], out);
                                opponent = game.o;
                                chess = "X";
                                GameInfo updatedInfo = game;
                                updatedInfo.x = player;
                                Server.games.put(game.gameId, updatedInfo);
                            }

                            sendMessage(out, Consts.RESUME + Consts.D + game.gameId + Consts.D + player.rank + Consts.D + opponent.name +
                                    Consts.D + opponent.rank + Consts.D + chess + Consts.D + game.getPos("X") + Consts.D + game.getPos("O"));
                        }
                        HeartBeat heartBeat = new HeartBeat();
                        heartBeat.start();
                        break;
                    case Consts.TURN:
                        game = Server.games.get(Integer.parseInt(parsed[1]));
                        boolean isX = parsed[2].equals("X");
                        boolean isGameOver = game.updateBoard(parsed[2], parsed[3]);

                        if (isX) {
                            sendMessage(game.x.out, Consts.DEFAULT_RESP);
                            sendMessage(game.o.out, Consts.TURN + Consts.D + parsed[2] + Consts.D + parsed[3]);
                        } else {
                            sendMessage(game.o.out, Consts.DEFAULT_RESP);
                            sendMessage(game.x.out, Consts.TURN + Consts.D + parsed[2] + Consts.D + parsed[3]);
                        }
                        if (isGameOver) {
                            Player updatedX = Server.players.get(game.x.name);
                            Player updatedO = Server.players.get(game.o.name);
                            if (game.remainedTurns == 0) {
                                updatedX.rank += 2;
                                updatedO.rank += 2;
                                sendMessage(game.o.out, Consts.GAME_OVER + Consts.D + "Nobody");
                                sendMessage(game.x.out, Consts.GAME_OVER + Consts.D + "Nobody");
                            } else {
                                if (isX) {
                                    updatedX.rank += 5;
                                    updatedO.rank = Math.max(updatedO.rank - 5, 0);
                                    sendMessage(game.x.out, Consts.GAME_OVER + Consts.D + game.x.name);
                                    sendMessage(game.o.out, Consts.GAME_OVER + Consts.D + game.x.name);
                                } else {
                                    updatedO.rank += 5;
                                    updatedX.rank = Math.max(updatedX.rank - 5, 0);
                                    sendMessage(game.x.out, Consts.GAME_OVER + Consts.D + game.o.name);
                                    sendMessage(game.o.out, Consts.GAME_OVER + Consts.D + game.o.name);
                                }
                            }
                            Server.players.put(game.x.name, updatedX);
                            Server.players.put(game.o.name, updatedO);
                            Server.games.remove(game.gameId);
                            game = null;
                        }
                        break;
                    case Consts.CHAT:
                        game = Server.games.get(Integer.parseInt(parsed[1]));
                        String msg = String.format("RANK#%d %s: %s", Integer.parseInt(parsed[2]), parsed[3], parsed[4]);
                        sendMessage(game.x.out, Consts.CHAT + Consts.D + msg);
                        sendMessage(game.o.out, Consts.CHAT + Consts.D + msg);
                        break;
                    case Consts.QUIT:
                        Player updatedX = Server.players.get(game.x.name);
                        Player updatedO = Server.players.get(game.o.name);
                        game = Server.games.get(Integer.parseInt(parsed[1]));
                        if (parsed[2].equals("X")) {
                            updatedO.rank += 5;
                            updatedX.rank = Math.max(updatedX.rank - 5, 0);
                            sendMessage(game.o.out, Consts.GAME_OVER + Consts.D + game.o.name);
                        } else {
                            updatedX.rank += 5;
                            updatedO.rank = Math.max(updatedO.rank - 5, 0);
                            sendMessage(game.x.out, Consts.GAME_OVER + Consts.D + game.x.name);
                        }
                        Server.players.put(game.x.name, updatedX);
                        Server.players.put(game.o.name, updatedO);
                        Server.games.remove(game.gameId);
                        game = null;
                        break;
                    default:
                        sendMessage(out, Consts.DEFAULT_RESP);
                        break;

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

