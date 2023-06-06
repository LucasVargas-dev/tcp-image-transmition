package com.mycompany.tcpimage3;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

public class Send {

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 13085);
        OutputStream outputStream = socket.getOutputStream();

        String imagePath = "C:\\Users\\lucas.vargas\\Desktop\\TcpImage3\\src\\main\\java\\com\\mycompany\\tcpimage3\\resources\\tcheco.jpg";

        // Obter o timestamp inicial do arquivo
        long previousModifiedTimestamp = getModifiedTimestamp(imagePath);

        while (true) {
            Thread.sleep(1000); // Aguardar um intervalo antes de verificar novamente

            long currentModifiedTimestamp = getModifiedTimestamp(imagePath);

            if (currentModifiedTimestamp != previousModifiedTimestamp) {
                // A imagem foi modificada, enviar para o Receive
                BufferedImage image = ImageIO.read(new File(imagePath));

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", byteArrayOutputStream);

                byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
                outputStream.write(size);
                outputStream.write(byteArrayOutputStream.toByteArray());
                outputStream.flush();

                System.out.println("Imagem enviada para o Receive.");

                previousModifiedTimestamp = currentModifiedTimestamp;
            }
        }
    }

    private static long getModifiedTimestamp(String imagePath) {
        File file = new File(imagePath);
        return file.lastModified();
    }
}
