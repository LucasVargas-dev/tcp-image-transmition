package com.mycompany.tcpimage3;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Send {

    private static final int CHUNK_SIZE = 4096;

    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();

        ServerSocket serverSocket = new ServerSocket(13085);

        while (true) {
            System.out.println("Waiting for connection...");

            Socket socket = serverSocket.accept();
            OutputStream outputStream = socket.getOutputStream();

            System.out.println("Connection established!");

            int panelWidth = 800;
            int panelHeight = 800;
            Rectangle screenRect = new Rectangle(panelWidth, panelHeight);
            BufferedImage screenImage = robot.createScreenCapture(screenRect);

            int imageWidth = screenImage.getWidth();
            int imageHeight = screenImage.getHeight();

            int chunkWidth = imageWidth / 2;
            int chunkHeight = imageHeight / 2;

            for (int y = 0; y < imageHeight; y += chunkHeight) {
                for (int x = 0; x < imageWidth; x += chunkWidth) {
                    int subImageWidth = Math.min(chunkWidth, imageWidth - x);
                    int subImageHeight = Math.min(chunkHeight, imageHeight - y);

                    BufferedImage subImage = screenImage.getSubimage(x, y, subImageWidth, subImageHeight);

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

            socket.close();
        }
    }
}
