package Client;

import Common.Consts;

import javax.swing.*;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static Client.Main.chess;

public class Client extends Thread {
    static class HeartBeat extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    Main.out.write(Consts.DEFAULT_RESP + "\n");
                    Main.out.flush();
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                Main.textFieldStatus.setText("Server unavailable");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    System.exit(0);
                }
            }
        }
    }

    static class Countdown {
        int seconds = 20;
        boolean cancelled = false;
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (seconds > 0 && !cancelled) {
                    System.out.println("countdown left " + seconds + " s");
                    seconds--;
                    Main.textFieldTimer.setText(Integer.toString(seconds));
                } else if (cancelled) {
                    seconds = 20;
                    Main.textFieldTimer.setText("20");
                    timer.cancel();
                } else {
                    System.out.println("time is up");
                    Main.pickRandomTurn();
                    seconds = 20;
                    Main.textFieldTimer.setText("20");
                    timer.cancel();
                }
            }
        };
        public void start() {
            timer.scheduleAtFixedRate(task, 0, 1000);
        }
    }

    @Override
    public void run() {
        try {
            Main.request(Consts.NEW_PLAYER + Consts.D + Main.username);
            String resp = Main.in.readLine(); // OK
            HeartBeat heartBeat = new HeartBeat();
            heartBeat.start();

            while (resp.equals(Consts.DEFAULT_RESP)) {
                // wait for opponent
                resp = Main.in.readLine();
            }

            System.out.println("resp >> " + resp);
            String[] parsed = resp.split(Consts.D);

            if (parsed[0].equals(Consts.RESUME)) {
                Main.gameId = Integer.parseInt(parsed[1]);
                Main.rank = Integer.parseInt(parsed[2]);
                Main.opponent.name = parsed[3];
                Main.opponent.rank = Integer.parseInt(parsed[4]);
                chess = parsed[5];
                System.out.println(parsed[6]);
                System.out.println(parsed[7]);
                Main.resumeBoard(parsed[6], parsed[7]);
            } else {
                // opponent found, new game start
                Main.gameId = Integer.parseInt(parsed[1]);
                Main.rank = Integer.parseInt(parsed[2]);
                Main.opponent.name = parsed[3];
                Main.opponent.rank = Integer.parseInt(parsed[4]);
                if (parsed[5].equals("X")) {
                    chess = "X";
                    Main.isYourTurn = true;
                    Main.textFieldStatus.setText(String.format("RANK#%d %s's Turn(%s)", Main.rank, Main.username, "X"));
                } else {
                    chess = "O";
                    Main.isYourTurn = false;
                    Main.textFieldStatus.setText(String.format("RANK#%d %s's Turn(%s)", Main.opponent.rank, Main.opponent.name, "X"));
                }
            }

            //change turn
            for (resp = Main.in.readLine(); resp != null; resp = Main.in.readLine()) {
                System.out.println("resp >> " + resp);
                if (resp.equals(Consts.DEFAULT_RESP)) {
                    continue;
                }
                parsed = resp.split(Consts.D);

                switch (parsed[0]) {
                    case Consts.TURN:
                        Main.setTurn(parsed[1], parsed[2]);
                        break;
                    case Consts.GAME_OVER:
                        if (parsed[1].equals(Main.username)) {
                            Main.rank += 5;
                        } else if (parsed[1].equals(Main.opponent.name)) {
                            Main.rank -= 5;
                            Main.rank = Math.max(Main.rank, 0);
                        } else {
                            Main.rank += 2;
                        }

                        if (parsed[1].equals("Nobody")) {
                            Main.textFieldStatus.setText("Game Drawn");
                        } else if(parsed[1].equals(Main.opponent.name)){
                            Main.textFieldStatus.setText(String.format("RANK#%d %s Wins!", Main.opponent.rank, parsed[1]));
                        }else{
                            Main.textFieldStatus.setText(String.format("RANK#%d %s Wins!", Main.rank, parsed[1]));

                        }

                        if (Main.countdown != null) {
                            Main.countdown.cancelled = true;
                        }
                        playAgainPopup();
                        break;
                    case Consts.CHAT:
                        Main.updateChatArea(parsed[1] + "\n");
                        break;
                    case Consts.DRAW:
                        Main.rank += 2;
                        Main.textFieldStatus.setText("Game Drawn");
                        if (Main.countdown != null) {
                            Main.countdown.cancelled = true;
                        }
                        playAgainPopup();
                        break;
                    case Consts.NEW_GAME:
                        // restart a new game
                        Main.gameId = Integer.parseInt(parsed[1]);
                        Main.rank = Integer.parseInt(parsed[2]);
                        Main.opponent.name = parsed[3];
                        Main.opponent.rank = Integer.parseInt(parsed[4]);
                        if (parsed[5].equals("X")) {
                            Main.chess = "X";
                            Main.isYourTurn = true;
                            Main.textFieldStatus.setText(String.format("RANK#%d %s's Turn(%s)", Main.rank, Main.username, "X"));
                        } else {
                            Main.chess = "O";
                            Main.textFieldStatus.setText(String.format("RANK#%d %s's Turn(%s)", Main.opponent.rank, Main.opponent.name, "X"));
                        }
                        break;

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playAgainPopup() {
        int reply = JOptionPane.showConfirmDialog(Main.frame, "Play again?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
            Main.resetBoard();
            Main.request(Consts.NEW_PLAYER + Consts.D + Main.username);
        } else {
            Main.quit(Main.gameId, Main.chess);
        }
    }
}
