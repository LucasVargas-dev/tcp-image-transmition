package com.mycompany.tcpimage3;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Receive extends JFrame {

    private static final int NUM_CHUNKS = 4;
    private JPanel panel;
    private JLabel[] labels;
    private BufferedImage[] receivedImages;

    public Receive() {
        setTitle("Received Image");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel(new GridLayout(2, 2));
        labels = new JLabel[NUM_CHUNKS];
        for (int i = 0; i < NUM_CHUNKS; i++) {
            labels[i] = new JLabel();
            panel.add(labels[i]);
        }
        add(panel);

        setSize(800, 800); // Defina o tamanho desejado para o painel
        setVisible(true);

        receivedImages = new BufferedImage[NUM_CHUNKS];
        startClient();
    }

    private void startClient() {
        Thread clientThread = new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", 13085);
                InputStream inputStream = socket.getInputStream();

                for (int i = 0; i < NUM_CHUNKS; i++) {
                    byte[] sizeBuffer = new byte[4];
                    inputStream.read(sizeBuffer);
                    int size = ByteBuffer.wrap(sizeBuffer).asIntBuffer().get();

                    byte[] imageBuffer = new byte[size];
                    inputStream.read(imageBuffer);

                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBuffer);
                    BufferedImage receivedImage = ImageIO.read(byteArrayInputStream);

                    receivedImages[i] = receivedImage;
                    System.out.println("Received chunk " + i);
                }

                socket.close();

                SwingUtilities.invokeLater(() -> {
                    for (int i = 0; i < NUM_CHUNKS; i++) {
                        labels[i].setIcon(new ImageIcon(receivedImages[i]));
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        clientThread.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Receive::new);
    }
}
