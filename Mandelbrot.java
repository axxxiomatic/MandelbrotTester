import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Класс Mandelbrot представляет собой графический компонент Swing, который генерирует изображение множества Мандельброта.
 * Он позволяет пользователю сохранять сгенерированные изображения на рабочий стол и использует многопоточность для ускорения
 * генерации изображения и проверки его разнообразия.
 */
public class Mandelbrot extends JPanel {
    private int startMandelbrotWidth; // Ширина изображения
    private int startMandelbrotHeight; // Высота изображения
    private double ZOOM; // Уровень масштабирования
    private int MAX_ITER; // Максимальное количество итераций
    private double offsetX; // Смещение по оси X
    private double offsetY; // Смещение по оси Y
    private BufferedImage image; // Изображение для записи результатов
    private int segmentWidthSize; // Ширина сегмента
    private int segmentHeightSize; // Высота сегмента
    private int[] segmentIndices; // Индексы сегментов изображения
    private int numberSave = 0;

    private static final int ARRAY_SIZE = 500;
    double[] zX_vals = new double[ARRAY_SIZE];
    double[] zY_vals = new double[ARRAY_SIZE];
    double[] cX_vals = new double[ARRAY_SIZE];
    double[] cY_vals = new double[ARRAY_SIZE];

    private static final String PROJECT_PATH = "C:/Users/Danil/ideaProjects/mandelbrot_for_cipher/";

    /**
     * Конструктор класса Mandelbrot.
     * Инициализирует компонент и добавляет обработчик событий мыши для повторной генерации изображения.
     */
    public Mandelbrot() {
        this.startMandelbrotWidth = 1024; // Устанавливаем начальные значения ширины и высоты
        this.startMandelbrotHeight = 720;

        for (int i = 0; i < ARRAY_SIZE; i++) {
            zX_vals[i] = 5 * (2 * Math.random() - 1); // от -5 до 5
            zY_vals[i] = 5 * (2 * Math.random() - 1); // от -5 до 5
            cX_vals[i] = 1024 * (Math.random() - 0.5); // от -512 до 512
            cY_vals[i] = 768 * (Math.random() - 0.5); // от -384 до 384
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isRightMouseButton(e)) { //Повторная генерация
                    generateImage();
                }
            }
        });
    }

    /**
     * Переопределяет метод paintComponent для отрисовки сгенерированного изображения множества Мандельброта.
     *
     * @param g Графический контекст для рисования.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, null); // Рисуем сохраненное изображение
        }
    }

    /**
     * Генерирует случайные значения для параметров MAX_ITER, offsetX, offsetY и ZOOM.
     */
    public void randomPositionOnPlenty() {
        //MAX_ITER = 3000;
        //ZOOM = 300;
        //offsetX = 0; // Смещение по оси X
        //offsetY = 0; // Смещение по оси Y
        Random random = new Random();
        MAX_ITER = 500 + (random.nextInt(91) * 8); // 91 для диапазона от 0 до 90, чтобы получить 300, 310 и до 1200
        offsetX = -0.9998 + (random.nextDouble() * (0.9998 - -0.9998));
        offsetY = -0.9998 + (random.nextDouble() * (0.9998 - -0.9998));
        ZOOM = 100000 + (random.nextInt(44) * 1000);
        repaint();
    }

    /**
     * Генерирует изображение множества Мандельброта и проверяет его разнообразие.
     * Если изображение удовлетворяет условиям разнообразия, оно отображается и предлагается пользователю сохранить его.
     * Если пользователь отказывается, генерируется новое изображение.
     */
    public void generateImage() {
        boolean validImage = false;
        int attempt = 0;

        for (int j = 0; j < ARRAY_SIZE; j++) {
            validImage = false; // Устанавливаем validImage в false в начале каждой новой итерации

            while (!validImage) {
                System.out.println("Iteration i = " + j); // Вывод значения i в консоль
                attempt++;
                randomPositionOnPlenty();
                image = new BufferedImage(startMandelbrotWidth, startMandelbrotHeight, BufferedImage.TYPE_INT_RGB);
                ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                for (int x = 0; x < startMandelbrotWidth; x++) {
                    executor.submit(new MandelbrotThread(x, startMandelbrotWidth, startMandelbrotHeight, ZOOM, MAX_ITER, offsetX,
                            offsetY, image, zX_vals[j], zY_vals[j], cX_vals[j], cY_vals[j]));
                }

                executor.shutdown();
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                validImage = checkImageDiversity(image);
                if (!validImage) {
                    System.out.println("Попытка №" + attempt + ". Изображение не удовлетворяет условиям, повторная рандомизация...");
                }
            }

            repaint();

            String fileName = String.format("sv/i=%d_ZOOM=%.2f_MAX_ITER=%d_offsetX=%.2f_offsetY=%.2f.png", j, ZOOM, MAX_ITER, offsetX, offsetY);
            File outputFile = new File(fileName);
            try {
                ImageIO.write(image, "png", outputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Проверяет разнообразие пикселей в изображении.
     *
     * @param image Изображение для проверки.
     * @return true, если изображение удовлетворяет условиям разнообразия, иначе false.
     */
    private boolean checkImageDiversity(BufferedImage image) {
        int totalPixels = image.getWidth() * image.getHeight();
        Map<Integer, Integer> colorCount = new HashMap<>();

        if (isImageBlackPercentageAboveThreshold(image, 0.075)) {
            return false;
        }

        int[] pixels = new int[totalPixels];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        for (int pixel : pixels) {
            colorCount.put(pixel, colorCount.getOrDefault(pixel, 0) + 1);
        }

        int uniqueColors = colorCount.size();
        int maxCount = colorCount.values().stream().max(Integer::compare).orElse(0);
        double percentage = (double) maxCount / totalPixels;

        return (uniqueColors > 250 && percentage < 0.2);
    }

    public static boolean isImageBlackPercentageAboveThreshold(BufferedImage image, double threshold) {
        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;
        int blackPixelCount = 0;

        int[] pixels = new int[totalPixels];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        for (int pixel : pixels) {
            if (isBlackPixel(pixel)) {
                blackPixelCount++;
            }
        }

        // Вычисление процента черных пикселей
        double percentageBlack = (double) blackPixelCount / (width * height);

        // Сравнение с порогом
        return percentageBlack > threshold;
    }

    public static boolean isBlackPixel(int pixel) {
        // Проверка, является ли пиксель черным
        return (pixel & 0xFFFFFF) == 0;
    }

    /**
     * Основной метод для запуска приложения.
     *
     * @param args Аргументы командной строки.
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Mandelbrot Set");
        Mandelbrot mandelbrot = new Mandelbrot();
        frame.add(mandelbrot);
        frame.setSize(1024, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        mandelbrot.generateImage();
    }
}