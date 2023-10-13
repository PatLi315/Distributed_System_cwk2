package Server;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import Common.MsgConsts;
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
                    out.write(MsgConsts.DEFAULT_RESP.getValue() + "\n");
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
                        game.x.out.write(MsgConsts.DRAW + "\n");
                        game.x.out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        game.o.out.write(MsgConsts.DRAW + "\n");
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
                String[] parsed = input.split(MsgConsts.AT.getValue());
                switch (MsgConsts.fromString(parsed[0])) {
                    case DEFAULT_RESP -> {
                    }
                    case NEW_PLAYER -> {
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
                                sendMessage(out, MsgConsts.DEFAULT_RESP.getValue());
                                System.out.println("New waiter added: " + player.name);
                            } else {
                                Player opponent = Server.waitingPool.remove(0);
                                if (random.nextBoolean()) {
                                    // player first
                                    int gameId = Server.generateGameId();
                                    sendMessage(opponent.out, MsgConsts.NEW_GAME.getValue() + MsgConsts.AT.getValue() + gameId + MsgConsts.AT.getValue()
                                            + opponent.rank + MsgConsts.AT.getValue() + player.name + MsgConsts.AT.getValue() + player.rank + MsgConsts.AT.getValue() + "O");
                                    GameInfo newGame = new GameInfo(gameId, player, opponent);
                                    Server.games.put(gameId, newGame);
                                    sendMessage(out, MsgConsts.NEW_GAME.getValue() + MsgConsts.AT.getValue() + gameId + MsgConsts.AT.getValue()
                                            + player.rank + MsgConsts.AT.getValue() + opponent.name + MsgConsts.AT.getValue() + opponent.rank + MsgConsts.AT.getValue() + "X");
                                } else {
                                    // opponent first
                                    int gameId = Server.generateGameId();
                                    sendMessage(opponent.out, MsgConsts.NEW_GAME.getValue() + MsgConsts.AT.getValue() + gameId + MsgConsts.AT.getValue()
                                            + opponent.rank + MsgConsts.AT.getValue() + player.name + MsgConsts.AT.getValue() + player.rank + MsgConsts.AT.getValue() + "X");
                                    GameInfo newGame = new GameInfo(gameId, opponent, player);
                                    Server.games.put(gameId, newGame);
                                    sendMessage(out, MsgConsts.NEW_GAME.getValue() + MsgConsts.AT.getValue() + gameId + MsgConsts.AT.getValue() + player.rank
                                            + MsgConsts.AT.getValue() + opponent.name + MsgConsts.AT.getValue() + opponent.rank + MsgConsts.AT.getValue() + "O");
                                }
                                System.out.println(opponent.name);
                            }
                        } else {
                            // game is not null, which means there's an ongoing game that should be resumed
                            System.out.println("resume " + game.x + game.o + " " + game.gameId);
                            sendMessage(out, MsgConsts.DEFAULT_RESP.getValue());

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

                            sendMessage(out, MsgConsts.RESUME.getValue() + MsgConsts.AT.getValue() + game.gameId + MsgConsts.AT.getValue() + player.rank + MsgConsts.AT.getValue() + opponent.name +
                                    MsgConsts.AT.getValue() + opponent.rank + MsgConsts.AT.getValue() + chess + MsgConsts.AT.getValue() + game.getPos("X") + MsgConsts.AT + game.getPos("O"));
                        }
                        HeartBeat heartBeat = new HeartBeat();
                        heartBeat.start();
                    }
                    case TURN -> {
                        game = Server.games.get(Integer.parseInt(parsed[1]));
                        boolean isX = parsed[2].equals("X");
                        boolean isGameOver = game.updateBoard(parsed[2], parsed[3]);
                        if (isX) {
                            sendMessage(game.x.out, MsgConsts.DEFAULT_RESP.getValue());
                            sendMessage(game.o.out, MsgConsts.TURN.getValue() + MsgConsts.AT.getValue() +
                                    parsed[2] + MsgConsts.AT.getValue() + parsed[3]);
                        } else {
                            sendMessage(game.o.out, MsgConsts.DEFAULT_RESP.getValue());
                            sendMessage(game.x.out, MsgConsts.TURN.getValue() + MsgConsts.AT.getValue() +
                                    parsed[2] + MsgConsts.AT.getValue() + parsed[3]);
                        }
                        if (isGameOver) {
                            Player updatedX = Server.players.get(game.x.name);
                            Player updatedO = Server.players.get(game.o.name);
                            if (game.remainedTurns == 0) {
                                updatedX.rank += 2;
                                updatedO.rank += 2;
                                sendMessage(game.o.out, MsgConsts.GAME_OVER.getValue() + MsgConsts.AT.getValue() + "Nobody");
                                sendMessage(game.x.out, MsgConsts.GAME_OVER.getValue() + MsgConsts.AT.getValue() + "Nobody");
                            } else {
                                if (isX) {
                                    updatedX.rank += 5;
                                    updatedO.rank = Math.max(updatedO.rank - 5, 0);
                                    sendMessage(game.x.out, MsgConsts.GAME_OVER.getValue() + MsgConsts.AT.getValue() + game.x.name);
                                    sendMessage(game.o.out, MsgConsts.GAME_OVER.getValue() + MsgConsts.AT.getValue() + game.x.name);
                                } else {
                                    updatedO.rank += 5;
                                    updatedX.rank = Math.max(updatedX.rank - 5, 0);
                                    sendMessage(game.x.out, MsgConsts.GAME_OVER.getValue() + MsgConsts.AT.getValue() + game.o.name);
                                    sendMessage(game.o.out, MsgConsts.GAME_OVER.getValue() + MsgConsts.AT.getValue() + game.o.name);
                                }
                            }
                            Server.players.put(game.x.name, updatedX);
                            Server.players.put(game.o.name, updatedO);
                            Server.games.remove(game.gameId);
                            game = null;
                        }
                    }
                    case CHAT -> {
                        game = Server.games.get(Integer.parseInt(parsed[1]));
                        String msg = String.format("%s: %s", parsed[3], parsed[4]);
                        sendMessage(game.x.out, MsgConsts.CHAT.getValue() + MsgConsts.AT.getValue() + msg);
                        sendMessage(game.o.out, MsgConsts.CHAT.getValue() + MsgConsts.AT.getValue() + msg);
                    }
                    case QUIT -> {
                        Player updatedX = Server.players.get(game.x.name);
                        Player updatedO = Server.players.get(game.o.name);
                        game = Server.games.get(Integer.parseInt(parsed[1]));
                        if (parsed[2].equals("X")) {
                            updatedO.rank += 5;
                            updatedX.rank = Math.max(updatedX.rank - 5, 0);
                            sendMessage(game.o.out, MsgConsts.GAME_OVER.getValue() + MsgConsts.AT.getValue() + game.o.name);
                        } else {
                            updatedX.rank += 5;
                            updatedO.rank = Math.max(updatedO.rank - 5, 0);
                            sendMessage(game.x.out, MsgConsts.GAME_OVER.getValue() + MsgConsts.AT.getValue() + game.x.name);
                        }
                        Server.players.put(game.x.name, updatedX);
                        Server.players.put(game.o.name, updatedO);
                        Server.games.remove(game.gameId);
                        game = null;
                    }
                    default -> sendMessage(out, MsgConsts.DEFAULT_RESP.getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

