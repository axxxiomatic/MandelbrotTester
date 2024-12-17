import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImageMetricsCalculator {

    // Метод для вычисления коэффициента корреляции Пирсона
    public static double calculatePearsonCorrelation(BufferedImage img1, BufferedImage img2, int index) {
        System.out.println("Вычисление коэффициента корелляции между исходным и зашифрованным изображением номер:");
        System.out.println(index);
        int width = img1.getWidth();
        int height = img1.getHeight();
        double sum1 = 0, sum2 = 0, sum1Sq = 0, sum2Sq = 0, pSum = 0;
        int n = width * height;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                int r1 = (rgb1 >> 16) & 0xFF;
                int r2 = (rgb2 >> 16) & 0xFF;

                sum1 += r1;
                sum2 += r2;
                sum1Sq += r1 * r1;
                sum2Sq += r2 * r2;
                pSum += r1 * r2;
            }
        }

        double num = pSum - (sum1 * sum2 / n);
        double den = Math.sqrt((sum1Sq - sum1 * sum1 / n) * (sum2Sq - sum2 * sum2 / n));

        if (den == 0) return 0;
        return num / den;
    }

    // Метод для вычисления SSIM
    public static double calculateSSIM(BufferedImage img1, BufferedImage img2, int index) {
        // Реализация SSIM может быть сложной, здесь используем упрощенный вариант
        System.out.println("Вычисление SSIM между исходным и зашифрованным изображением номер:");
        System.out.println(index);
        int width = img1.getWidth();
        int height = img1.getHeight();
        double avg1 = 0, avg2 = 0, var1 = 0, var2 = 0, cov = 0;
        int n = width * height;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                int r1 = (rgb1 >> 16) & 0xFF;
                int r2 = (rgb2 >> 16) & 0xFF;

                avg1 += r1;
                avg2 += r2;
            }
        }

        avg1 /= n;
        avg2 /= n;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                int r1 = (rgb1 >> 16) & 0xFF;
                int r2 = (rgb2 >> 16) & 0xFF;

                var1 += (r1 - avg1) * (r1 - avg1);
                var2 += (r2 - avg2) * (r2 - avg2);
                cov += (r1 - avg1) * (r2 - avg2);
            }
        }

        var1 /= n;
        var2 /= n;
        cov /= n;

        double c1 = 6.5025, c2 = 58.5225;
        double ssim = ((2 * avg1 * avg2 + c1) * (2 * cov + c2)) / ((avg1 * avg1 + avg2 * avg2 + c1) * (var1 + var2 + c2));
        return ssim;
    }

    // Метод для вычисления процента соответствующих пикселей
    public static double calculatePixelMatchPercentage(BufferedImage img1, BufferedImage img2, int index) {
        System.out.println("Вычисление совпадения по пикселям между исходным и зашифрованным изображениями номер:");
        System.out.println(index);
        int width = img1.getWidth();
        int height = img1.getHeight();
        int matchCount = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (img1.getRGB(x, y) == img2.getRGB(x, y)) {
                    matchCount++;
                }
            }
        }

        return (double) matchCount / (width * height) * 100;
    }

    // Метод для вычисления коэффициентов корреляции между соседними элементами
    public static double[] calculateNeighborCorrelation(BufferedImage img) {
        System.out.println("Вычисление корелляции между соседними пикселями");
        int width = img.getWidth();
        int height = img.getHeight();
        double sumH = 0, sumV = 0, sumD = 0;
        int n = 0;

        for (int x = 0; x < width - 1; x++) {
            for (int y = 0; y < height - 1; y++) {
                int rgb1 = img.getRGB(x, y);
                int rgb2 = img.getRGB(x + 1, y);
                int rgb3 = img.getRGB(x, y + 1);
                int rgb4 = img.getRGB(x + 1, y + 1);
                int r1 = (rgb1 >> 16) & 0xFF;
                int r2 = (rgb2 >> 16) & 0xFF;
                int r3 = (rgb3 >> 16) & 0xFF;
                int r4 = (rgb4 >> 16) & 0xFF;

                sumH += r1 * r2;
                sumV += r1 * r3;
                sumD += r1 * r4;
                n++;
            }
        }

        double mean = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                mean += (img.getRGB(x, y) >> 16) & 0xFF;
            }
        }
        mean /= width * height;

        double var = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                var += Math.pow(((img.getRGB(x, y) >> 16) & 0xFF) - mean, 2);
            }
        }
        var /= width * height;

        double rh = sumH / n - mean * mean;
        double rv = sumV / n - mean * mean;
        double rd = sumD / n - mean * mean;

        return new double[]{rh / var, rv / var, rd / var};
    }

    // Метод для вычисления дисперсии гистограммы распределения элементов изображения по значениям яркости
    public static double calculateHistogramVariance(BufferedImage img) {
        System.out.println("Вычисление гистограммы отклонения");
        int width = img.getWidth();
        int height = img.getHeight();
        Map<Integer, Integer> colorCount = new HashMap<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int color = img.getRGB(x, y);
                colorCount.put(color, colorCount.getOrDefault(color, 0) + 1);
            }
        }

        int totalPixels = width * height;
        double mean = colorCount.values().stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = colorCount.values().stream().mapToDouble(count -> Math.pow(count - mean, 2)).sum() / totalPixels;

        return variance;
    }

    // Метод для вычисления UACI
    public static double calculateUACI(BufferedImage img1, BufferedImage img2, int index1, int index2) {
        System.out.println("Вычисление UACI между двумя зашифрованными изображениями с номерами:");
        System.out.println(index1);
        System.out.println(index2);
        int width = img1.getWidth();
        int height = img1.getHeight();
        double sum = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                int r1 = (rgb1 >> 16) & 0xFF;
                int r2 = (rgb2 >> 16) & 0xFF;

                sum += Math.abs(r1 - r2);
            }
        }

        return sum / (255 * width * height) * 100;
    }

    // Метод для вычисления NPCR
    public static double calculateNPCR(BufferedImage img1, BufferedImage img2, int index1, int index2) {
        System.out.println("Вычисление NPCR между двумя зашифрованными изображениями с номерами:");
        System.out.println(index1);
        System.out.println(index2);
        int width = img1.getWidth();
        int height = img1.getHeight();
        int diffCount = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                    diffCount++;
                }
            }
        }

        return (double) diffCount / (width * height) * 100;
    }

    // Метод для вычисления NFC
    public static double calculateNFC(BufferedImage img1, BufferedImage img2, int index1, int index2) {
        System.out.println("Вычисление NFC между двумя зашифрованными изображениями с номерами");
        System.out.println(index1);
        System.out.println(index2);
        int width = img1.getWidth();
        int height = img1.getHeight();
        int diffBits = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

                for (int i = 0; i < 32; i++) {
                    if (((rgb1 >> i) & 1) != ((rgb2 >> i) & 1)) {
                        diffBits++;
                    }
                }
            }
        }

        return (double) diffBits / (32 * width * height) * 100;
    }

    // Метод для вычисления средних значений метрик по массиву изображений
    public static double[] calculateAverageMetrics(BufferedImage[] images1, BufferedImage[] images2, int metric) {
        System.out.println("Вычисление средних метрик");
        double sum = 0;
        int n = images1.length;
        if (images1.length >= images2.length) n = images2.length;

        if (metric < 4) {
            for (int i = 0; i < n; i++) {
                    switch (metric) {
                        case 1:
                            sum += calculatePearsonCorrelation(images1[i], images2[i], i);
                            break;
                        case 2:
                            sum += calculateSSIM(images1[i], images2[i], i);
                            break;
                        case 3:
                            sum += calculatePixelMatchPercentage(images1[i], images2[i], i);
                            break;
                    }
                }
            }
        else {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    switch (metric) {
                        case 4:
                            sum += calculateUACI(images2[i], images2[j], i, j);
                            break;
                        case 5:
                            sum += calculateNPCR(images2[i], images2[j], i, j);
                            break;
                        case 6:
                            sum += calculateNFC(images2[i], images2[j], i, j);
                            break;
                    }
                }
            }
        }

        return new double[]{sum / (n * (n - 1) / 2)};
    }

    // Метод для вычисления средних значений Rh, Rl, Rd, D по массиву изображений
    public static double[] calculateAverageNeighborCorrelationAndVariance(BufferedImage[] images) {
        System.out.println("Вычисление средних отклонений и корелляций");
        double sumRh = 0, sumRl = 0, sumRd = 0, sumD = 0;
        int n = images.length;

        for (BufferedImage img : images) {
            double[] correlations = calculateNeighborCorrelation(img);
            sumRh += correlations[0];
            sumRl += correlations[1];
            sumRd += correlations[2];
            sumD += calculateHistogramVariance(img);
        }

        return new double[]{sumRh / n, sumRl / n, sumRd / n, sumD / n};
    }

    public static boolean areImagesIdentical(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        String inputFolder = "C:\\Users\\Илья\\IdeaProjects\\MandelbrotTests\\input";
        String outputFolder = "C:\\Users\\Илья\\IdeaProjects\\MandelbrotTests\\output";

        List<BufferedImage> inputImages = new ArrayList<>();
        List<BufferedImage> outputImages = new ArrayList<>();

        try (Stream<Path> inputPaths = Files.walk(Paths.get(inputFolder));
             Stream<Path> outputPaths = Files.walk(Paths.get(outputFolder))) {

            inputImages = inputPaths
                    .filter(Files::isRegularFile)
                    .sorted((path1, path2) -> path1.toFile().getName().compareTo(path2.toFile().getName()))
                    .map(Path::toFile)
                    .map(file -> {
                        try {
                            System.out.println(file.getPath());
                            return ImageIO.read(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.out.println("Ошибка загрузки входного изображения!");
                            return null;
                        }
                    })
                    .filter(img -> img != null)
                    .collect(Collectors.toList());
            System.out.println("Входное изображение загружено успешно!");

            outputImages = outputPaths
                    .filter(Files::isRegularFile)
                    .sorted((path1, path2) -> path1.toFile().getName().compareTo(path2.toFile().getName()))
                    .map(Path::toFile)
                    .map(file -> {
                        try {
                            System.out.println(file.getPath());
                            return ImageIO.read(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.out.println("Ошибка загрузки шифрованного изображения!");
                            return null;
                        }
                    })
                    .filter(img -> img != null)
                    .collect(Collectors.toList());
            System.out.println("Шифрованное изображение загружено успешно!");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка!");
        }

        BufferedImage[] inputImageArray = inputImages.toArray(new BufferedImage[0]);
        BufferedImage[] outputImageArray = outputImages.toArray(new BufferedImage[0]);

//        double totalR = 0;
//        double totalSSIM = 0;
//        double totalPixelMatch = 0;
//        int count = inputImageArray.length;
//
//        for (int i = 0; i < count; i++) {
//            BufferedImage inputImage = inputImageArray[i];
//            BufferedImage outputImage = outputImageArray[i];
//
//            if (areImagesIdentical(inputImage, outputImage)) {
//                System.out.println("Изображения " + (i + 1) + " идентичны.");
//            } else {
//                System.out.println("Изображения " + (i + 1) + " не идентичны.");
//            }
//
//            totalR += calculatePearsonCorrelation(inputImage, outputImage, i + 1);
//            totalSSIM += calculateSSIM(inputImage, outputImage, i + 1);
//            totalPixelMatch += calculatePixelMatchPercentage(inputImage, outputImage, i + 1);
//        }
//
//        double avgR = totalR / count;
//        double avgSSIM = totalSSIM / count;
//        double avgPixelMatch = totalPixelMatch / count;

        // Вычисление средних значений метрик для input и output изображений
        double[] avgR = calculateAverageMetrics(inputImageArray, outputImageArray, 1);
        double[] avgSSIM = calculateAverageMetrics(inputImageArray, outputImageArray, 2);
        double[] avgPixelMatch = calculateAverageMetrics(inputImageArray, outputImageArray, 3);

        // Вычисление средних значений Rh, Rl, Rd, D для output изображений
        double[] avgNeighborCorrelationAndVariance = calculateAverageNeighborCorrelationAndVariance(outputImageArray);

        // Вычисление средних значений UACI, NPCR, NFC для попарных сравнений output изображений
        double[] avgUACI = calculateAverageMetrics(inputImageArray, outputImageArray, 4);
        double[] avgNPCR = calculateAverageMetrics(inputImageArray, outputImageArray, 5);
        double[] avgNFC = calculateAverageMetrics(inputImageArray, outputImageArray, 6);

        // Вывод результатов
        System.out.println("Среднее R между исходным и зашифрованным изображением: " + avgR[0]);
        System.out.println("Среднее SSIM между исходным и зашифрованным изображением: " + avgSSIM[0]);
        System.out.println("Средний процент соответствующих пикселей у исходного и зашифрованного изображения: " + avgPixelMatch[0]);
        System.out.println("Среднее Rh для зашифрованных изображений: " + avgNeighborCorrelationAndVariance[0]);
        System.out.println("Среднее Rl для зашифрованных изображений: " + avgNeighborCorrelationAndVariance[1]);
        System.out.println("Среднее Rd для зашифрованных изображений: " + avgNeighborCorrelationAndVariance[2]);
        System.out.println("Среднее D для зашифрованных изображений: " + avgNeighborCorrelationAndVariance[3]);
        System.out.println("Среднее UACI для зашифрованных изображений: " + avgUACI[0]);
        System.out.println("Среднее NPCR для зашифрованных изображений: " + avgNPCR[0]);
        System.out.println("Среднее NFC для зашифрованных изображений: " + avgNFC[0]);
//        System.out.println("Среднее R между исходным и расшифрованным изображением: " + avgR);
//        System.out.println("Среднее SSIM между исходным и расшифрованным изображением: " + avgSSIM);
//        System.out.println("Средний процент соответствующих пикселей у исходного и расшифрованного изображения: " + avgPixelMatch);
    }
}