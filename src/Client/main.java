package Client;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.Random;
import java.net.*;

public class main {

    public static String username;
    public static int rank;
    public static String ip = "localhost";
    public static int port = 8888;
    public static int gameId;

    public static Player opponent = new Player();
    public static BufferedReader in;
    public static BufferedWriter out;
    public static boolean isYourTurn = false;
    public static String chess = "";
    public static HashMap<String, JButton> buttonMap = new HashMap<>();
    public static client.Countdown countdown;
    public static JFrame frame;
    public static JTextField textFieldTimer;
    public static JTextField textFieldnput;
    public static JTextArea textArea;
    public static JTextField textFieldStatus;

    public static void main(String[] args) {
        if (args.length != 3) {
            Random player = new Random();
            username = "Player" + player.nextInt(10000);
            System.out.println("args missed, using default port 8888 and random name:" + username);
        } else {
            username = args[0];
            ip = args[1];
            port = Integer.parseInt(args[2]);
        }
        connect();
    }

    public static void connect() {
        try {
            Socket socket = new Socket (ip, port);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void request (String msg) {
        try {
            System.out.println("req>> " + msg);
            out.write(msg + "\n");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void turn (JButton btnBoard, String pos) {
        if (isYourTurn) {
            btnBoard.setText(chess);
            btnBoard.setEnabled(false);
            textFieldStatus.setText(String.format("RANK%d %s's Turn(%s)", opponent.rank, opponent.name, chess.equals("X") ? "O" : "X"));
            request(Consts.TURN + Consts.D + gameID + Consts.D + pos);
        } else {
            JOptionPane.showMessageDialog(frame, "Not YOur Turn");
        }
        isYourTurn = false;
        if (countdown != null) {
            coundown.cancelled = true;
        }
    }

    public static void setTurn(String chess, String pos) {
        JButton btnBoard = buttonMap.get(pos);
        btnBoard.setText(chess);
        btnBoard.setEnabled(false);
        textFieldStatus.setText(String.format("RANK%d %s's Turn(%s)", rank, username, chess.equals("X") ? "O" : "X"));
        isYourTurn = true;
        countdown = new Client.Countdown();
        countdown.start();
    }
}

