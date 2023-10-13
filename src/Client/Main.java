package Client;

import Common.Consts;
import Common.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    public static Player opponent = new Player();
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
        initialize();
        Client client = new Client();
        client.start();
    }
    private void initialize() {
        frame = new JFrame();
        frame.setName(username);
        frame.setBounds(100, 100, 600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JLabel lblTimer = new JLabel("Timer");
        lblTimer.setBounds(30, 14, 61, 16);
        frame.getContentPane().add(lblTimer);

        textFieldTimer = new JTextField();
        textFieldTimer.setHorizontalAlignment(SwingConstants.CENTER);
        textFieldTimer.setEditable(false);
        textFieldTimer.setBounds(6, 34, 90, 90);
        frame.getContentPane().add(textFieldTimer);
        textFieldTimer.setColumns(10);
        textFieldTimer.setFont(new Font("Lucida Grande", Font.PLAIN, 50));
        textFieldTimer.setText("20");

        JLabel lblTicTacToe = new JLabel("Tic Tac Toe");
        lblTicTacToe.setBounds(15, 289, 81, 16);
        frame.getContentPane().add(lblTicTacToe);

        JButton btnBoard1 = new JButton("");
        btnBoard1.setFont(new Font("Lucida Grande", Font.PLAIN, 50));
        btnBoard1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                turn(btnBoard1, "1");
            }
        });
        btnBoard1.setBounds(120, 150, 100, 100);
        frame.getContentPane().add(btnBoard1);

        JButton btnBoard2 = new JButton("");
        btnBoard2.setFont(new Font("Lucida Grande", Font.PLAIN, 50));
        btnBoard2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                turn(btnBoard2, "2");
            }
        });
        btnBoard2.setBounds(220, 150, 100, 100);
        frame.getContentPane().add(btnBoard2);

        JButton btnBoard3 = new JButton("");
        btnBoard3.setFont(new Font("Lucida Grande", Font.PLAIN, 50));
        btnBoard3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                turn(btnBoard3, "3");
            }
        });
        btnBoard3.setBounds(320, 150, 100, 100);
        frame.getContentPane().add(btnBoard3);

        JButton btnBoard4 = new JButton("");
        btnBoard4.setFont(new Font("Lucida Grande", Font.PLAIN, 50));
        btnBoard4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                turn(btnBoard4, "4");
            }
        });
        btnBoard4.setBounds(120, 245, 100, 100);
        frame.getContentPane().add(btnBoard4);

        JButton btnBoard5 = new JButton("");
        btnBoard5.setFont(new Font("Lucida Grande", Font.PLAIN, 50));
        btnBoard5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                turn(btnBoard5, "5");
            }
        });
        btnBoard5.setBounds(220, 245, 100, 100);
        frame.getContentPane().add(btnBoard5);

        JButton btnBoard6 = new JButton("");
        btnBoard6.setFont(new Font("Lucida Grande", Font.PLAIN, 50));
        btnBoard6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                turn(btnBoard6, "6");
            }
        });
        btnBoard6.setBounds(320, 245, 100, 100);
        frame.getContentPane().add(btnBoard6);

        JButton btnBoard7 = new JButton("");
        btnBoard7.setFont(new Font("Lucida Grande", Font.PLAIN, 50));
        btnBoard7.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                turn(btnBoard7, "7");
            }
        });
        btnBoard7.setBounds(120, 345, 100, 100);
        frame.getContentPane().add(btnBoard7);

        JButton btnBoard8 = new JButton("");
        btnBoard8.setFont(new Font("Lucida Grande", Font.PLAIN, 50));
        btnBoard8.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                turn(btnBoard8, "8");
            }
        });
        btnBoard8.setBounds(220, 345, 100, 100);
        frame.getContentPane().add(btnBoard8);

        JButton btnBoard9 = new JButton("");
        btnBoard9.setFont(new Font("Lucida Grande", Font.PLAIN, 50));
        btnBoard9.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                turn(btnBoard9, "9");
            }
        });
        btnBoard9.setBounds(320, 345, 100, 100);
        frame.getContentPane().add(btnBoard9);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBounds(424, 42, 170, 480);
        frame.getContentPane().add(textArea);

        textFieldInput = new JTextField();
        textFieldInput.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(textFieldInput.getText().length() > 0){
                    request(Consts.CHAT + Consts.D + gameId + Consts.D + rank + Consts.D + username + Consts.D + textFieldInput.getText());
                }
                textFieldInput.setText("");
            }
        });
        textFieldInput.setBounds(424, 534, 170, 32);
        frame.getContentPane().add(textFieldInput);
        textFieldInput.setColumns(10);

        JLabel lblChat = new JLabel("Player Chat");
        lblChat.setBounds(470, 14, 81, 16);
        frame.getContentPane().add(lblChat);

        JButton btnQuit = new JButton("Quit");
        btnQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                quit(Main.gameId, Main.chess);
            }
        });
        btnQuit.setBounds(6, 515, 117, 29);
        frame.getContentPane().add(btnQuit);

        textFieldStatus = new JTextField();
        textFieldStatus.setHorizontalAlignment(SwingConstants.CENTER);
        textFieldStatus.setEditable(false);
        textFieldStatus.setBounds(130, 42, 274, 82);
        frame.getContentPane().add(textFieldStatus);
        textFieldStatus.setColumns(10);
        textFieldStatus.setText("Finding player...");

        buttonMap.put("1", btnBoard1);
        buttonMap.put("2", btnBoard2);
        buttonMap.put("3", btnBoard3);
        buttonMap.put("4", btnBoard4);
        buttonMap.put("5", btnBoard5);
        buttonMap.put("6", btnBoard6);
        buttonMap.put("7", btnBoard7);
        buttonMap.put("8", btnBoard8);
        buttonMap.put("9", btnBoard9);
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
            textFieldStatus.setText(String.format("RANK#%d %s's Turn(%s)", opponent.rank, opponent.name, chess.equals("X") ? "O" : "X"));
            request(Consts.TURN + Consts.D + gameId + Consts.D + chess + Consts.D + pos);
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
        textFieldStatus.setText(String.format("RANK#%d %s's Turn(%s)", rank, username, chess.equals("X") ? "O" : "X"));
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
        request(Consts.QUIT + Consts.D + gameId + Consts.D + chess);
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
            textFieldStatus.setText(String.format("RANK#%d %s's Turn(%s)", rank, username, chess.equals("X") ? "O" : "X"));
            isYourTurn = true;
            countdown = new Client.Countdown();
            countdown.start();
        } else {
            isYourTurn = false;
            textFieldStatus.setText(String.format("RANK#%d %s's Turn(%s)", opponent.rank, opponent.name, chess.equals("X") ? "O" : "X"));
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