package com.mycompany.tcpimage3;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Random;

import java.awt.*;
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
    private static final int CHUNK_WIDTH = PANEL_WIDTH / 2;
    private static final int CHUNK_HEIGHT = PANEL_HEIGHT / 2;
    private static final int NUM_CHUNKS = 4;

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

                    // Monta os 4 pedaços da imagem
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

                    Thread.sleep(2000); // Aguarda 2 segundos

                    // Mantém a conexão aberta indefinidamente
                    while (true) {
                        // Gera um índice aleatório para o chunk que será alterado
                        Random random = new Random();
                        int randomChunkIndex = random.nextInt(NUM_CHUNKS);

                        // Adiciona um ponto vermelho no chunk selecionado
                        int chunkX = (randomChunkIndex % 2) * CHUNK_WIDTH;
                        int chunkY = (randomChunkIndex / 2) * CHUNK_HEIGHT;
                        BufferedImage chunkImage = robot.createScreenCapture(new Rectangle(chunkX, chunkY, CHUNK_WIDTH, CHUNK_HEIGHT));
                        Graphics2D graphics = (Graphics2D) chunkImage.getGraphics();
                        graphics.setColor(Color.RED);
                        graphics.fillOval(CHUNK_WIDTH / 2 - 5, CHUNK_HEIGHT / 2 - 5, 10, 10);

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        ImageIO.write(chunkImage, "jpg", byteArrayOutputStream);

                        byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
                        outputStream.write(size);
                        outputStream.write(byteArrayOutputStream.toByteArray());
                        outputStream.flush();

                        System.out.println("Sent modified chunk at (" + chunkX + "," + chunkY + ")");

                        Thread.sleep(1000); // Aguarda 1 segundo antes de enviar novamente o chunk modificado
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            sendThread.start();
        }
    }
}
