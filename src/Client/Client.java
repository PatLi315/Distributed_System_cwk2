package Client;

import Common.MsgConsts;

import javax.swing.*;
import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static Client.Main.chess;

public class Client extends Thread {
    static class HeartBeat extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    Main.out.write(MsgConsts.DEFAULT_RESP.getValue() + "\n");
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
        Integer seconds = 20;
        Boolean cancelled = false;
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
        Main.request(MsgConsts.NEW_PLAYER.getValue() + MsgConsts.AT.getValue() + Main.username);
        String resp = null;
        try {
            resp = Objects.requireNonNull(Main.in).readLine(); // OK
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HeartBeat heartBeat = new HeartBeat();
        heartBeat.start();
        while (Objects.requireNonNull(resp).equals(MsgConsts.DEFAULT_RESP.getValue())) {
            // wait for opponent
            try {
                resp = Objects.requireNonNull(Main.in).readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("resp >> " + resp);
        String[] parsed = resp.split(MsgConsts.AT.getValue());
        if (parsed[0].equals(MsgConsts.RESUME.getValue())) {
            Main.gameId = Integer.parseInt(parsed[1]);
            Main.rank = Integer.parseInt(parsed[2]);
            Main.competitor.name = parsed[3];
            Main.competitor.rank = Integer.parseInt(parsed[4]);
            chess = parsed[5];
            System.out.println(parsed[6]);
            System.out.println(parsed[7]);
            Main.resumeBoard(parsed[6], parsed[7]);
        } else {
            // opponent found, new game start
            Main.gameId = Integer.parseInt(parsed[1]);
            Main.rank = Integer.parseInt(parsed[2]);
            Main.competitor.name = parsed[3];
            Main.competitor.rank = Integer.parseInt(parsed[4]);
            if (parsed[5].equals("X")) {
                chess = "X";
                Main.isYourTurn = true;
                Main.textFieldStatus.setText(String.format("Welcome %s \n %s's Turn(%s)", Main.username, Main.username, "X"));
            } else {
                chess = "O";
                Main.isYourTurn = false;
                Main.textFieldStatus.setText(String.format("Welcome %s \n %s's Turn(%s)", Main.username, Main.competitor.name, "X"));
            }
        }

        //change turn
        try {
            for (resp = Main.in.readLine(); resp != null; resp = Main.in.readLine()) {
                System.out.println("resp >> " + resp);
                if (resp.equals(MsgConsts.DEFAULT_RESP.getValue())) {
                    continue;
                }
                parsed = resp.split(MsgConsts.AT.getValue());
                System.err.println(parsed[0]);
                switch (MsgConsts.fromString(parsed[0])) {
                    case TURN -> Main.setTurn(parsed[1], parsed[2]);
                    case GAME_OVER -> {
                        if (parsed[1].equals(Main.username)) {
                            Main.rank += 5;
                        } else if (parsed[1].equals(Main.competitor.name)) {
                            Main.rank -= 5;
                            Main.rank = Math.max(Main.rank, 0);
                        } else {
                            Main.rank += 2;
                        }
                        if (parsed[1].equals("Nobody")) {
                            Main.textFieldStatus.setText("Game Drawn");
                        } else if (parsed[1].equals(Main.competitor.name)) {
                            Main.textFieldStatus.setText(String.format("%s Wins!", parsed[1]));
                        } else {
                            Main.textFieldStatus.setText(String.format("%s Wins!", parsed[1]));

                        }
                        if (Main.countdown != null) {
                            Main.countdown.cancelled = true;
                        }
                        playAgainPopup();
                    }
                    case CHAT -> Main.updateChatArea(parsed[1] + "\n");
                    case DRAW -> {
                        Main.rank += 2;
                        Main.textFieldStatus.setText("Game Drawn");
                        if (Main.countdown != null) {
                            Main.countdown.cancelled = true;
                        }
                        playAgainPopup();
                    }
                    case NEW_GAME -> {
                        // restart a new game
                        Main.gameId = Integer.parseInt(parsed[1]);
                        Main.rank = Integer.parseInt(parsed[2]);
                        Main.competitor.name = parsed[3];
                        Main.competitor.rank = Integer.parseInt(parsed[4]);
                        if (parsed[5].equals("X")) {
                            Main.chess = "X";
                            Main.isYourTurn = true;
                            Main.textFieldStatus.setText(String.format("Welcome %s \n %s's Turn(%s)", Main.username, Main.username, "X"));
                        } else {
                            Main.chess = "O";
                            Main.textFieldStatus.setText(String.format("Welcome %s \n %s's Turn(%s)", Main.username, Main.competitor.name, "X"));
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void playAgainPopup() {
        int reply = JOptionPane.showConfirmDialog(Main.frame, "Play again?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
            Main.resetBoard();
            Main.request(MsgConsts.NEW_PLAYER.getValue() + MsgConsts.AT.getValue() + Main.username);
        } else {
            Main.quit(Main.gameId, Main.chess);
        }
    }
}
