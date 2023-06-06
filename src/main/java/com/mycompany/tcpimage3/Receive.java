package com.mycompany.tcpimage3;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

public class Receive {

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(13085);

        while (true) {
            System.out.println("Waiting for connection...");

            Socket socket = serverSocket.accept();
            InputStream inputStream = socket.getInputStream();

            System.out.println("Reading: " + System.currentTimeMillis());

            byte[] sizeAr = new byte[4];
            inputStream.read(sizeAr);
            int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();

            byte[] imageAr = new byte[size];
            inputStream.read(imageAr);

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageAr));

            System.out.println("Received " + image.getHeight() + "x" + image.getWidth() + ": " + System.currentTimeMillis());
            ImageIO.write(image, "jpg", new File("C:\\Users\\lucas.vargas\\Desktop\\TcpImage3\\src\\main\\java\\com\\mycompany\\tcpimage3\\resources\\tcheco.jpg"));

            // Criar uma nova imagem atualizada no mesmo caminho
            String path = "C:\\Users\\lucas.vargas\\Desktop\\TcpImage3\\src\\main\\java\\com\\mycompany\\tcpimage3\\resources\\tcheco_atualizada.jpg";
            ImageIO.write(image, "jpg", new File(path));
            System.out.println("Created updated image: " + path);

            // Fechar o socket da conexão atual
            System.out.println("Closing current connection!");
            //socket.close();
        }
    }
}
