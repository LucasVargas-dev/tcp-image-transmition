package com.mycompany.tcpimage3;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Receive extends JFrame {

    private static final int CHUNK_SIZE = 4096;
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 800;
    private static final int CHUNK_WIDTH = PANEL_WIDTH / 2;
    private static final int CHUNK_HEIGHT = PANEL_HEIGHT / 2;
    private static final int NUM_CHUNKS = 4;
    private JPanel panel;
    private JLabel[] labels;
    private BufferedImage[] receivedImages;

    private byte[][] previousChunks; // Armazena os chunks anteriores para comparação

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
        previousChunks = new byte[NUM_CHUNKS][];
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
                    int bytesRead = 0;
                    int offset = 0;

                    while (bytesRead != -1 && offset < size) {
                        bytesRead = inputStream.read(imageBuffer, offset, size - offset);
                        if (bytesRead != -1) {
                            offset += bytesRead;
                        }
                    }

                    if (i < NUM_CHUNKS) {
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBuffer);
                        BufferedImage receivedImage = ImageIO.read(byteArrayInputStream);

                        receivedImages[i] = receivedImage;
                        previousChunks[i] = Arrays.copyOf(imageBuffer, imageBuffer.length);
                        System.out.println("Received chunk " + i);
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    for (int i = 0; i < NUM_CHUNKS; i++) {
                        labels[i].setIcon(new ImageIcon(receivedImages[i]));
                    }
                });

                try {
                    while (true) {
                        for (int i = 0; i < NUM_CHUNKS; i++) {
                            final int index = i;
                            byte[] sizeBuffer = new byte[4];
                            inputStream.read(sizeBuffer);
                            int size = ByteBuffer.wrap(sizeBuffer).asIntBuffer().get();

                            byte[] imageBuffer = new byte[size];
                            int bytesRead = 0;
                            int offset = 0;

                            while (bytesRead != -1 && offset < size) {
                                bytesRead = inputStream.read(imageBuffer, offset, size - offset);
                                if (bytesRead != -1) {
                                    offset += bytesRead;
                                }
                            }

                            if (i < NUM_CHUNKS) {
                                int chunkX = (i % 2) * CHUNK_WIDTH;
                                int chunkY = (i / 2) * CHUNK_HEIGHT;

                                if (!Arrays.equals(imageBuffer, previousChunks[i])) {
                                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBuffer);
                                    BufferedImage receivedImage = ImageIO.read(byteArrayInputStream);

                                    receivedImages[i] = receivedImage;
                                    previousChunks[i] = Arrays.copyOf(imageBuffer, imageBuffer.length);
                                    System.out.println("Received updated chunk " + i);

                                    SwingUtilities.invokeLater(() -> {
                                        labels[index].setIcon(new ImageIcon(receivedImage));
                                        panel.repaint(chunkX, chunkY, CHUNK_WIDTH, CHUNK_HEIGHT);
                                    });
                                }
                            }

                        }
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
