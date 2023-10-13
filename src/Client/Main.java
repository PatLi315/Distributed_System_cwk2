package Client;

import Common.MsgConsts;
import Common.Player;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static String username;
    public static int rank;
    public static String ip = "localhost";
    public static int port = 8888;
    public static int gameId;
    public static Player competitor = new Player();
    public static BufferedReader in;
    public static BufferedWriter out;
    public static boolean isYourTurn = false;
    public static String chess = "";
    public static ConcurrentHashMap<String, JButton> buttonMap = new ConcurrentHashMap<>();
    public static Client.Countdown countdown;

    public static JFrame frame;
    public static JTextField textFieldTimer;
    public static JTextField textFieldInput;
    public static JTextField textFieldStatus;
    public static JTextArea textArea;
    public static ArrayList<String> msgList = new ArrayList<>();
    public static void main(String[] args) {
        if (args.length != 3) {
            Random r = new Random();
            username = "player" + r.nextInt(10000);
        } else {
            username = args[0];
            ip = args[1];
            port = Integer.parseInt(args[2]);
        }
        connect();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Main window = new Main();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public Main() {
        ClientUI.clientUI();
        Client client = new Client();
        client.start();
    }

    public static void connect() {
        try {
            Socket socket = new Socket(ip, port);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void request(String msg) {
        try {
            System.out.println("req >> " + msg);
            out.write(msg + "\n");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void turn(JButton btnBoard, String pos) {
        if (isYourTurn) {
            btnBoard.setText(chess);
            btnBoard.setEnabled(false);
            textFieldStatus.setText(String.format("Welcome %s \n %s's Turn(%s)", username, competitor.name, chess.equals("X") ? "O" : "X"));
            request(MsgConsts.TURN.getValue() + MsgConsts.AT.getValue()+ gameId + MsgConsts.AT.getValue() + chess + MsgConsts.AT.getValue() + pos);
        } else {
            JOptionPane.showMessageDialog(frame, "Not Your Turn");
        }
        isYourTurn = false;
        if (countdown != null) {
            countdown.cancelled = true;
        }
    }
    public static void setTurn(String chess, String pos) {
        JButton btnBoard = buttonMap.get(pos);
        btnBoard.setText(chess);
        btnBoard.setEnabled(false);
        textFieldStatus.setText(String.format("Welcome %s \n %s's Turn(%s)", username, username, chess.equals("X") ? "O" : "X"));
        isYourTurn = true;
        countdown = new Client.Countdown();
        countdown.start();
    }
    public static void pickRandomTurn() {
        for (int i = 1; i <= 9; i++) {
            JButton btnBoard = buttonMap.get(Integer.toString(i));
            if (btnBoard.isEnabled()) {
                btnBoard.doClick();
                return;
            }
        }
    }
    public static void quit(int gameId, String chess) {
        request(MsgConsts.QUIT.getValue() + MsgConsts.AT.getValue() + gameId + MsgConsts.AT.getValue() + chess);
        System.exit(0);
    }
    public static void resetBoard() {
        for (int i = 1; i <= 9; i++) {
            JButton btnBoard = buttonMap.get(Integer.toString(i));
            btnBoard.setText("");
            btnBoard.setEnabled(true);
        }
        textFieldStatus.setText("Finding player...");
        textArea.setText("");
        isYourTurn = false;
        if (countdown != null) {
            countdown.cancelled = true;
        }
    }
    public static void resumeBoard(String x, String o) {
        char[] xs = x.toCharArray();
        char[] os = o.toCharArray();
        for (char i : xs) {
            System.out.println("x:" + i);
            JButton btnBoard = buttonMap.get(Character.toString(i));
            btnBoard.setText("X");
            btnBoard.setEnabled(false);
        }
        for (char i : os) {
            System.out.println("o:" + i);
            JButton btnBoard = buttonMap.get(Character.toString(i));
            btnBoard.setText("O");
            btnBoard.setEnabled(false);
        }
        if ((xs.length == os.length && chess.equals("X")) || (xs.length > os.length && chess.equals("O"))) {
            textFieldStatus.setText(String.format("Welcome %s \n  %s's Turn(%s)", username, username, chess.equals("X") ? "O" : "X"));
            isYourTurn = true;
            countdown = new Client.Countdown();
            countdown.start();
        } else {
            isYourTurn = false;
            textFieldStatus.setText(String.format("Welcome %s \n  %s's Turn(%s)", username, competitor.name, chess.equals("X") ? "O" : "X"));
        }
    }
    public static void updateChatArea(String newMsg) {
        if (msgList.size() == 10) {
            msgList.remove(0);
            msgList.add(9, newMsg);
        }else{
            msgList.add(newMsg);
        }
        StringBuilder result = new StringBuilder();
        for (String i : msgList) {
            result.append(i);
        }
        textArea.setText(result.toString());
    }
}
