import Model.ImageDecrypt;
import Model.ImageEncrypt;
import Model.Mandelbrot;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AutoEncryptorDecryptor {

    private static final String IMAGE_FOLDER = "images/"; // Папка с изображениями
    private static final String ENCRYPTED_FOLDER = "encrypted/"; // Папка для сохранения зашифрованных изображений
    private static final String DECRYPTED_FOLDER = "decrypted/"; // Папка для сохранения расшифрованных изображений
    private static final String MANDELBROT_FOLDER = "mandelbrot/"; // Папка для сохранения изображений Мандельброта

    public static void main(String[] args) {
        // Создаем папки, если их нет
        createFolders();

        long totalEncryptionTime = 0; // Общее время шифрования
        long totalDecryptionTime = 0; // Общее время расшифрования
        int imageCount = 5; // Количество изображений

        // Автоматически шифруем и расшифровываем 30 изображений
        for (int i = 1; i <= imageCount; i++) {
            String imagePath = IMAGE_FOLDER + i + ".png";
            String mandelbrotPath = MANDELBROT_FOLDER + "mandelbrot_" + i + ".png";
            String encryptedImagePath = ENCRYPTED_FOLDER + i + "_encrypted.png";
            String decryptedImagePath = DECRYPTED_FOLDER + i + "_decrypted.png";

            // Загружаем или генерируем изображение Мандельброта
            long encryptionStartTime = System.nanoTime(); // Начало шифрования
            BufferedImage mandelbrotImage = loadOrGenerateMandelbrot(mandelbrotPath);
            if (mandelbrotImage == null) {
                System.err.println("Ошибка генерации или загрузки изображения Мандельброта: " + mandelbrotPath);
                continue;
            }

            // Загружаем изображение
            BufferedImage image = loadImage(imagePath);
            if (image == null) {
                System.err.println("Ошибка загрузки изображения: " + imagePath);
                continue;
            }

            // Шифруем изображение
            //long encryptionStartTime = System.nanoTime(); // Начало шифрования
            BufferedImage encryptedImage = encryptImage(image, mandelbrotImage);
            long encryptionEndTime = System.nanoTime(); // Конец шифрования
            long encryptionTime = encryptionEndTime - encryptionStartTime; // Время шифрования
            totalEncryptionTime += encryptionTime; // Добавляем к общему времени шифрования

            if (encryptedImage == null) {
                System.err.println("Ошибка шифрования изображения: " + imagePath);
                continue;
            }

            // Сохраняем зашифрованное изображение
            saveImage(encryptedImage, encryptedImagePath);
            System.out.println("Изображение " + imagePath + " зашифровано и сохранено в " + encryptedImagePath);
            System.out.println("Время шифрования изображения " + i + ": " + encryptionTime / 1_000_000 + " мс");

            // Расшифровываем изображение
            long decryptionStartTime = System.nanoTime(); // Начало расшифрования
            BufferedImage decryptedImage = decryptImage(encryptedImage, mandelbrotImage);
            long decryptionEndTime = System.nanoTime(); // Конец расшифрования
            long decryptionTime = decryptionEndTime - decryptionStartTime; // Время расшифрования
            totalDecryptionTime += decryptionTime; // Добавляем к общему времени расшифрования

            if (decryptedImage == null) {
                System.err.println("Ошибка расшифрования изображения: " + encryptedImagePath);
                continue;
            }

            // Сохраняем расшифрованное изображение
            saveImage(decryptedImage, decryptedImagePath);
            System.out.println("Изображение " + encryptedImagePath + " расшифровано и сохранено в " + decryptedImagePath);
            System.out.println("Время расшифрования изображения " + i + ": " + decryptionTime / 1_000_000 + " мс");
        }

        // Выводим статистику
        System.out.println("Общее время шифрования: " + totalEncryptionTime / 1_000_000 + " мс");
        System.out.println("Среднее время шифрования: " + (totalEncryptionTime / imageCount) / 1_000_000 + " мс");
        System.out.println("Общее время расшифрования: " + totalDecryptionTime / 1_000_000 + " мс");
        System.out.println("Среднее время расшифрования: " + (totalDecryptionTime / imageCount) / 1_000_000 + " мс");
    }

    /**
     * Создает папки, если их нет.
     */
    private static void createFolders() {
        new File(IMAGE_FOLDER).mkdirs();
        new File(ENCRYPTED_FOLDER).mkdirs();
        new File(DECRYPTED_FOLDER).mkdirs();
        new File(MANDELBROT_FOLDER).mkdirs();
    }

    /**
     * Загружает изображение по указанному пути.
     *
     * @param path Путь к изображению.
     * @return Загруженное изображение или null, если произошла ошибка.
     */
    private static BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Ошибка загрузки изображения: " + e.getMessage());
            return null;
        }
    }

    /**
     * Сохраняет изображение по указанному пути.
     *
     * @param image Изображение для сохранения.
     * @param path  Путь для сохранения.
     */
    private static void saveImage(BufferedImage image, String path) {
        try {
            ImageIO.write(image, "png", new File(path));
        } catch (IOException e) {
            System.err.println("Ошибка сохранения изображения: " + e.getMessage());
        }
    }

    /**
     * Загружает или генерирует изображение Мандельброта.
     *
     * @param mandelbrotPath Путь к изображению Мандельброта.
     * @return Изображение Мандельброта или null, если произошла ошибка.
     */
    private static BufferedImage loadOrGenerateMandelbrot(String mandelbrotPath) {
        File mandelbrotFile = new File(mandelbrotPath);
        if (mandelbrotFile.exists()) {
            // Загружаем существующее изображение Мандельброта
            return loadImage(mandelbrotPath);
        } else {
            // Генерируем новое изображение Мандельброта
            Mandelbrot mandelbrot = new Mandelbrot();
            BufferedImage mandelbrotImage = mandelbrot.generateImage();
            if (mandelbrotImage != null) {
                // Сохраняем сгенерированное изображение
                saveImage(mandelbrotImage, mandelbrotPath);
                return mandelbrotImage;
            } else {
                System.err.println("Ошибка генерации изображения Мандельброта.");
                return null;
            }
        }
    }

    /**
     * Шифрует изображение с использованием изображения Мандельброта.
     *
     * @param image          Изображение для шифрования.
     * @param mandelbrotImage Изображение Мандельброта.
     * @return Зашифрованное изображение или null, если произошла ошибка.
     */
    private static BufferedImage encryptImage(BufferedImage image, BufferedImage mandelbrotImage) {
        try {
            // Используем методы из вашего интерфейса для шифрования
            ImageEncrypt imageEncrypt = new ImageEncrypt();
            imageEncrypt.encryptWholeImage(image);
            return imageEncrypt.getEncryptedImage();
        } catch (Exception e) {
            System.err.println("Ошибка шифрования изображения: " + e.getMessage());
            return null;
        }
    }

    /**
     * Расшифровывает изображение с использованием изображения Мандельброта.
     *
     * @param encryptedImage  Зашифрованное изображение.
     * @param mandelbrotImage Изображение Мандельброта.
     * @return Расшифрованное изображение или null, если произошла ошибка.
     */
    private static BufferedImage decryptImage(BufferedImage encryptedImage, BufferedImage mandelbrotImage) {
        try {
            // Сохраняем зашифрованное изображение в ресурсы
            saveImage(encryptedImage, ImageDecrypt.getResourcesPath() + "encrypted_image.bmp");

            // Используем методы из вашего интерфейса для расшифрования
            ImageDecrypt.decryptImage();

            // Загружаем расшифрованное изображение
            return ImageDecrypt.loadDecryptedImage();
        } catch (Exception e) {
            System.err.println("Ошибка расшифрования изображения: " + e.getMessage());
            return null;
        }
    }
}