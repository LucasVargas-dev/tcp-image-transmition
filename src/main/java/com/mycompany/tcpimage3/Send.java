package com.mycompany.tcpimage3;

import java.awt.*;
import java.util.Random;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

public class Send {

    private static final int CHUNK_SIZE = 4096;
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 800;
    private static final int CHUNK_WIDTH = PANEL_WIDTH / 4;
    private static final int CHUNK_HEIGHT = PANEL_HEIGHT / 4;
    private static final int NUM_CHUNKS = 16;

    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();

        ServerSocket serverSocket = new ServerSocket(13085);

        while (true) {
            System.out.println("Waiting for connection...");

            Socket socket = serverSocket.accept();

            Thread sendThread = new Thread(() -> {
                try {
                    OutputStream outputStream = socket.getOutputStream();

                    System.out.println("Connection established!");

                    int imageWidth = PANEL_WIDTH;
                    int imageHeight = PANEL_HEIGHT;
                    BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

                    int chunkWidth = CHUNK_WIDTH;
                    int chunkHeight = CHUNK_HEIGHT;

                    for (int y = 0; y < imageHeight; y += chunkHeight) {
                        for (int x = 0; x < imageWidth; x += chunkWidth) {
                            int subImageWidth = Math.min(chunkWidth, imageWidth - x);
                            int subImageHeight = Math.min(chunkHeight, imageHeight - y);

                            BufferedImage subImage = robot.createScreenCapture(new Rectangle(x, y, subImageWidth, subImageHeight));

                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            ImageIO.write(subImage, "jpg", byteArrayOutputStream);

                            byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
                            outputStream.write(size);
                            outputStream.write(byteArrayOutputStream.toByteArray());
                            outputStream.flush();

                            System.out.println("Sent chunk at (" + x + "," + y + ")");
                        }
                    }

                    System.out.println("All chunks sent!");

                    Thread.sleep(2000);

                    while (true) {
                        Random random = new Random();
                        int randomChunkIndex = random.nextInt(NUM_CHUNKS);

                        int chunkX = (randomChunkIndex % 4) * CHUNK_WIDTH;
                        int chunkY = (randomChunkIndex / 4) * CHUNK_HEIGHT;
                        BufferedImage chunkImage = robot.createScreenCapture(new Rectangle(chunkX, chunkY, CHUNK_WIDTH, CHUNK_HEIGHT));
                        Graphics2D graphics = (Graphics2D) chunkImage.getGraphics();
                        graphics.setColor(Color.RED);
                        int margin = 10;
                        int posX = random.nextInt(CHUNK_WIDTH - 2 * margin) + margin;
                        int posY = random.nextInt(CHUNK_HEIGHT - 2 * margin) + margin;
                        graphics.fillOval(posX, posY, 10, 10);

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        ImageIO.write(chunkImage, "jpg", byteArrayOutputStream);

                        byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
                        byte[] position = ByteBuffer.allocate(4).putInt(randomChunkIndex).array();

                        outputStream.write(size);
                        outputStream.write(position);
                        outputStream.write(byteArrayOutputStream.toByteArray());
                        outputStream.flush();

                        System.out.println("Sent modified chunk at (" + chunkX + "," + chunkY + ")");

                        Thread.sleep(1000);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            sendThread.start();
        }
    }
}
