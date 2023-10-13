package Client;

import Common.MsgConsts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static Client.Main.*;

public class ClientUI {


    static void clientUI() {
        frame = new JFrame();
        frame.setName(username);
        frame.setBounds(300, 300, 620, 620);
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
                if(!textFieldInput.getText().isEmpty()){
                    request(MsgConsts.CHAT.getValue() + MsgConsts.AT.getValue() + gameId + MsgConsts.AT.getValue() + rank + MsgConsts.AT.getValue() + username + MsgConsts.AT.getValue() + textFieldInput.getText());
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
                quit(gameId, Main.chess);
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
}
