import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class MandelbrotAnalyzer {

    private static final int ARRAY_SIZE = 1000;
    private static final double DEFAULT_ZX = 3000.0;
    private static final double DEFAULT_ZY = 300.0;
    private static final double DEFAULT_CX = 0.0;
    private static final double DEFAULT_CY = 0.0;

    public static void main(String[] args) {
        //analyzeImages();
        compareImagesPairwise();
    }

    public static void analyzeImages() {
        double[] zX_input = new double[ARRAY_SIZE];
        double[] zY_input = new double[ARRAY_SIZE];
        double[] cX_input = new double[ARRAY_SIZE];
        double[] cY_input = new double[ARRAY_SIZE];

        // Массивы для хранения правильных предсказаний по SSIM
        int[] correctZX_ssim = new int[ARRAY_SIZE];
        int[] correctZY_ssim = new int[ARRAY_SIZE];
        int[] correctCX_ssim = new int[ARRAY_SIZE];
        int[] correctCY_ssim = new int[ARRAY_SIZE];

        // Массивы для хранения правильных предсказаний по коэффициенту корреляции Пирсона
        int[] correctZX_pearson = new int[ARRAY_SIZE];
        int[] correctZY_pearson = new int[ARRAY_SIZE];
        int[] correctCX_pearson = new int[ARRAY_SIZE];
        int[] correctCY_pearson = new int[ARRAY_SIZE];

        // Массивы для хранения правильных предсказаний по проценту совпадения пикселей
        int[] correctZX_matching = new int[ARRAY_SIZE];
        int[] correctZY_matching = new int[ARRAY_SIZE];
        int[] correctCX_matching = new int[ARRAY_SIZE];
        int[] correctCY_matching = new int[ARRAY_SIZE];

        BufferedImage defaultImage = loadImage("default.png");

        File dir = new File("sv");
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(".png"));

        if (files != null) {
            int currentIteration = 0;
            for (File file : files) {
                BufferedImage image = loadImage(file.getPath());
                System.out.println("Trying to load file:");
                System.out.println(file.getName());

                if (image != null) {
                    double[] params = extractParameters(file.getName());
                    int iteration = (int) params[0]; // Номер итерации
                    double zX = params[1];
                    double zY = params[2];
                    double cX = params[3];
                    double cY = params[4];

                    zX_input[iteration] = zX;
                    zY_input[iteration] = zY;
                    cX_input[iteration] = cX;
                    cY_input[iteration] = cY;

                    // Получаем результаты сравнения по всем метрикам
                    double[] predictedParams = compareImages(defaultImage, image, zX, zY, cX, cY);

                    // Результаты по SSIM
                    double zX_ssim = predictedParams[0];
                    double zY_ssim = predictedParams[1];
                    double cX_ssim = predictedParams[2];
                    double cY_ssim = predictedParams[3];

                    // Результаты по коэффициенту корреляции Пирсона
                    double zX_pearson = predictedParams[4];
                    double zY_pearson = predictedParams[5];
                    double cX_pearson = predictedParams[6];
                    double cY_pearson = predictedParams[7];

                    // Результаты по проценту совпадения пикселей
                    double zX_matching = predictedParams[8];
                    double zY_matching = predictedParams[9];
                    double cX_matching = predictedParams[10];
                    double cY_matching = predictedParams[11];

                    // Оценка правильности по SSIM
                    correctZX_ssim[iteration] = Math.abs(zX_ssim - zX) <= 10 ? 1 : 0;
                    correctZY_ssim[iteration] = Math.abs(zY_ssim - zY) <= 1 ? 1 : 0;
                    correctCX_ssim[iteration] = Math.abs(cX_ssim - cX) <= 0.01 ? 1 : 0;
                    correctCY_ssim[iteration] = Math.abs(cY_ssim - cY) <= 0.01 ? 1 : 0;

                    // Оценка правильности по коэффициенту корреляции Пирсона
                    correctZX_pearson[iteration] = Math.abs(zX_pearson - zX) <= 10 ? 1 : 0;
                    correctZY_pearson[iteration] = Math.abs(zY_pearson - zY) <= 1 ? 1 : 0;
                    correctCX_pearson[iteration] = Math.abs(cX_pearson - cX) <= 0.01 ? 1 : 0;
                    correctCY_pearson[iteration] = Math.abs(cY_pearson - cY) <= 0.01 ? 1 : 0;

                    // Оценка правильности по проценту совпадения пикселей
                    correctZX_matching[iteration] = Math.abs(zX_matching - zX) <= 10 ? 1 : 0;
                    correctZY_matching[iteration] = Math.abs(zY_matching - zY) <= 1 ? 1 : 0;
                    correctCX_matching[iteration] = Math.abs(cX_matching - cX) <= 0.01 ? 1 : 0;
                    correctCY_matching[iteration] = Math.abs(cY_matching - cY) <= 0.01 ? 1 : 0;

                    // Вывод результатов
                    System.out.println("Iteration i = " + iteration + ":");
                    System.out.println("  SSIM: Correct ZX: " + correctZX_ssim[iteration] + ", Correct ZY: " + correctZY_ssim[iteration] + ", Correct CX: " + correctCX_ssim[iteration] + ", Correct CY: " + correctCY_ssim[iteration]);
                    System.out.println("  Pearson: Correct ZX: " + correctZX_pearson[iteration] + ", Correct ZY: " + correctZY_pearson[iteration] + ", Correct CX: " + correctCX_pearson[iteration] + ", Correct CY: " + correctCY_pearson[iteration]);
                    System.out.println("  Matching Pixels: Correct ZX: " + correctZX_matching[iteration] + ", Correct ZY: " + correctZY_matching[iteration] + ", Correct CX: " + correctCX_matching[iteration] + ", Correct CY: " + correctCY_matching[iteration]);

                    currentIteration++;
                } else {
                    System.out.println("Couldn't parse the file!");
                }
            }
        } else {
            System.out.println("Not enough matching files!");
        }

        // Вывод процента правильных предсказаний по каждой метрике
        double percentCorrectZX_ssim = calculatePercentage(correctZX_ssim);
        double percentCorrectZY_ssim = calculatePercentage(correctZY_ssim);
        double percentCorrectCX_ssim = calculatePercentage(correctCX_ssim);
        double percentCorrectCY_ssim = calculatePercentage(correctCY_ssim);

        double percentCorrectZX_pearson = calculatePercentage(correctZX_pearson);
        double percentCorrectZY_pearson = calculatePercentage(correctZY_pearson);
        double percentCorrectCX_pearson = calculatePercentage(correctCX_pearson);
        double percentCorrectCY_pearson = calculatePercentage(correctCY_pearson);

        double percentCorrectZX_matching = calculatePercentage(correctZX_matching);
        double percentCorrectZY_matching = calculatePercentage(correctZY_matching);
        double percentCorrectCX_matching = calculatePercentage(correctCX_matching);
        double percentCorrectCY_matching = calculatePercentage(correctCY_matching);

        System.out.println("SSIM:");
        System.out.println("  Percentage of correct ZX: " + percentCorrectZX_ssim + "%");
        System.out.println("  Percentage of correct ZY: " + percentCorrectZY_ssim + "%");
        System.out.println("  Percentage of correct CX: " + percentCorrectCX_ssim + "%");
        System.out.println("  Percentage of correct CY: " + percentCorrectCY_ssim + "%");

        System.out.println("Pearson Correlation:");
        System.out.println("  Percentage of correct ZX: " + percentCorrectZX_pearson + "%");
        System.out.println("  Percentage of correct ZY: " + percentCorrectZY_pearson + "%");
        System.out.println("  Percentage of correct CX: " + percentCorrectCX_pearson + "%");
        System.out.println("  Percentage of correct CY: " + percentCorrectCY_pearson + "%");

        System.out.println("Matching Pixels:");
        System.out.println("  Percentage of correct ZX: " + percentCorrectZX_matching + "%");
        System.out.println("  Percentage of correct ZY: " + percentCorrectZY_matching + "%");
        System.out.println("  Percentage of correct CX: " + percentCorrectCX_matching + "%");
        System.out.println("  Percentage of correct CY: " + percentCorrectCY_matching + "%");
    }

    public static void compareImagesPairwise() {
        File dir = new File("sv");
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(".png"));

        if (files != null && files.length > 0) {
            int totalComparisons = 0;
            double totalSSIM = 0;
            double totalPearson = 0;
            double totalMatchingPixels = 0;

            for (int i = 0; i < files.length; i += 20) { // было 50
                for (int j = i + 1; j < files.length; j += 20) { // было 50
                    BufferedImage image1 = loadImage(files[i].getPath());
                    BufferedImage image2 = loadImage(files[j].getPath());

                    if (image1 != null && image2 != null) {
                        // Вычисляем все три метрики
                        double ssim = calculateSSIM(image1, image2);
                        double pearson = calculatePearsonCorrelation(image1, image2);
                        double matchingPixels = calculateMatchingPixelsPercentage(image1, image2);

                        // Выводим результаты для каждой пары изображений
                        System.out.println("Comparing " + files[i].getName() + " and " + files[j].getName() + ":");
                        System.out.println("  SSIM = " + ssim);
                        System.out.println("  Pearson Correlation = " + pearson);
                        System.out.println("  Matching Pixels Percentage = " + matchingPixels + "%");

                        // Суммируем результаты для вычисления средних значений
                        totalSSIM += ssim;
                        totalPearson += pearson;
                        totalMatchingPixels += matchingPixels;
                        totalComparisons++;
                    } else {
                        System.out.println("Couldn't load one of the images!");
                    }
                }
            }

            // Вычисляем средние значения для каждой метрики
            if (totalComparisons > 0) {
                double averageSSIM = totalSSIM / totalComparisons;
                double averagePearson = totalPearson / totalComparisons;
                double averageMatchingPixels = totalMatchingPixels / totalComparisons;

                // Выводим средние значения
                System.out.println("Average SSIM: " + averageSSIM);
                System.out.println("Average Pearson Correlation: " + averagePearson);
                System.out.println("Average Matching Pixels Percentage: " + averageMatchingPixels + "%");
            } else {
                System.out.println("No valid comparisons were made.");
            }
        } else {
            System.out.println("No matching files!");
        }
    }

    private static BufferedImage loadImage(String fileName) {
        try {
            return ImageIO.read(new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static double[] compareImages(BufferedImage defaultImage, BufferedImage image, double zX, double zY, double cX, double cY) {
        int width = defaultImage.getWidth();
        int height = defaultImage.getHeight();

        int[] defaultPixels = new int[width * height];
        int[] imagePixels = new int[width * height];

        defaultImage.getRGB(0, 0, width, height, defaultPixels, 0, width);
        image.getRGB(0, 0, width, height, imagePixels, 0, width);

        // 1. Сравнение по SSIM
        double ssim = calculateSSIM(defaultPixels, imagePixels);
        double zX_ssim = DEFAULT_ZX;
        double zY_ssim = DEFAULT_ZY;
        double cX_ssim = DEFAULT_CX;
        double cY_ssim = DEFAULT_CY;

        double thresholdSSIM = 0.90; // Порог SSIM для определения близости
        if (ssim > thresholdSSIM) {
            zX_ssim = DEFAULT_ZX;
            zY_ssim = DEFAULT_ZY;
            cX_ssim = DEFAULT_CX;
            cY_ssim = DEFAULT_CY;
        } else {
            // Здесь можно добавить логику для определения параметров на основе SSIM
        }

        // 2. Сравнение по коэффициенту корреляции Пирсона
        double pearsonCorrelation = calculatePearsonCorrelation(defaultPixels, imagePixels);
        double zX_pearson = DEFAULT_ZX;
        double zY_pearson = DEFAULT_ZY;
        double cX_pearson = DEFAULT_CX;
        double cY_pearson = DEFAULT_CY;

        double thresholdPearson = 0.90; // Порог коэффициента корреляции Пирсона
        if (pearsonCorrelation > thresholdPearson) {
            zX_pearson = DEFAULT_ZX;
            zY_pearson = DEFAULT_ZY;
            cX_pearson = DEFAULT_CX;
            cY_pearson = DEFAULT_CY;
        } else {
            // Здесь можно добавить логику для определения параметров на основе коэффициента корреляции
        }

        // 3. Сравнение по проценту совпадающих пикселей
        double matchingPixelsPercentage = calculateMatchingPixelsPercentage(defaultPixels, imagePixels);
        double zX_matching = DEFAULT_ZX;
        double zY_matching = DEFAULT_ZY;
        double cX_matching = DEFAULT_CX;
        double cY_matching = DEFAULT_CY;

        double thresholdMatchingPixels = 90.0; // Порог процента совпадающих пикселей
        if (matchingPixelsPercentage > thresholdMatchingPixels) {
            zX_matching = DEFAULT_ZX;
            zY_matching = DEFAULT_ZY;
            cX_matching = DEFAULT_CX;
            cY_matching = DEFAULT_CY;
        } else {
            // Здесь можно добавить логику для определения параметров на основе процента совпадения пикселей
        }

        // Возвращаем три набора параметров: по SSIM, по коэффициенту корреляции и по проценту совпадения пикселей
        return new double[]{
                zX_ssim, zY_ssim, cX_ssim, cY_ssim, // Результат по SSIM
                zX_pearson, zY_pearson, cX_pearson, cY_pearson, // Результат по коэффициенту корреляции
                zX_matching, zY_matching, cX_matching, cY_matching // Результат по проценту совпадения пикселей
        };
    }

    private static double calculatePercentage(int[] results) {
        int sum = 0;
        for (int result : results) {
            sum += result;
        }
        return (double) sum / results.length * 100;
    }

    private static double[] extractParameters(String fileName) {
        double[] params = new double[5];
        Pattern pattern = Pattern.compile("i=(\\d+)_ZOOM=(-?\\d+,\\d+)_MAX_ITER=(-?\\d+,\\d+)_offsetX=(-?\\d+,\\d+)_offsetY=(-?\\d+,\\d+)");
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            params[0] = Double.parseDouble(matcher.group(1));
            params[1] = Double.parseDouble(matcher.group(2).replace(',', '.'));
            params[2] = Double.parseDouble(matcher.group(3).replace(',', '.'));
            params[3] = Double.parseDouble(matcher.group(4).replace(',', '.'));
            params[4] = Double.parseDouble(matcher.group(5).replace(',', '.'));
        }

        return params;
    }

    // Перегруженный метод для вычисления коэффициента корреляции Пирсона
    private static double calculatePearsonCorrelation(BufferedImage image1, BufferedImage image2) {
        int width = image1.getWidth();
        int height = image1.getHeight();

        int[] pixels1 = new int[width * height];
        int[] pixels2 = new int[width * height];

        image1.getRGB(0, 0, width, height, pixels1, 0, width);
        image2.getRGB(0, 0, width, height, pixels2, 0, width);

        return calculatePearsonCorrelation(pixels1, pixels2);
    }

    // Перегруженный метод для вычисления процента совпадения пикселей
    private static double calculateMatchingPixelsPercentage(BufferedImage image1, BufferedImage image2) {
        int width = image1.getWidth();
        int height = image1.getHeight();

        int[] pixels1 = new int[width * height];
        int[] pixels2 = new int[width * height];

        image1.getRGB(0, 0, width, height, pixels1, 0, width);
        image2.getRGB(0, 0, width, height, pixels2, 0, width);

        return calculateMatchingPixelsPercentage(pixels1, pixels2);
    }

    private static double calculateSSIM(BufferedImage image1, BufferedImage image2) {
        int width = image1.getWidth();
        int height = image1.getHeight();

        int[] pixels1 = new int[width * height];
        int[] pixels2 = new int[width * height];

        image1.getRGB(0, 0, width, height, pixels1, 0, width);
        image2.getRGB(0, 0, width, height, pixels2, 0, width);

        return calculateSSIM(pixels1, pixels2);
    }

    private static double calculateSSIM(int[] x, int[] y) {
        int n = x.length;

        double meanX = 0, meanY = 0;
        for (int i = 0; i < n; i++) {
            meanX += x[i];
            meanY += y[i];
        }
        meanX /= n;
        meanY /= n;

        double varX = 0, varY = 0, covXY = 0;
        for (int i = 0; i < n; i++) {
            double diffX = x[i] - meanX;
            double diffY = y[i] - meanY;
            varX += diffX * diffX;
            varY += diffY * diffY;
            covXY += diffX * diffY;
        }
        varX /= n;
        varY /= n;
        covXY /= n;

        double C1 = 0.01 * 255 * 0.01 * 255;
        double C2 = 0.03 * 255 * 0.03 * 255;

        double numerator = (2 * meanX * meanY + C1) * (2 * covXY + C2);
        double denominator = (meanX * meanX + meanY * meanY + C1) * (varX + varY + C2);

        return numerator / denominator;
    }

    // Функция для вычисления коэффициента корреляции Пирсона
    private static double calculatePearsonCorrelation(int[] x, int[] y) {
        int n = x.length;

        double meanX = 0, meanY = 0;
        for (int i = 0; i < n; i++) {
            meanX += x[i];
            meanY += y[i];
        }
        meanX /= n;
        meanY /= n;

        double varX = 0, varY = 0, covXY = 0;
        for (int i = 0; i < n; i++) {
            double diffX = x[i] - meanX;
            double diffY = y[i] - meanY;
            varX += diffX * diffX;
            varY += diffY * diffY;
            covXY += diffX * diffY;
        }
        varX /= n;
        varY /= n;
        covXY /= n;

        if (varX == 0 || varY == 0) {
            return 0; // Если дисперсия равна нулю, корреляция не определена
        }

        return covXY / Math.sqrt(varX * varY);
    }

    // Функция для вычисления процента совпадающих пикселей
    private static double calculateMatchingPixelsPercentage(int[] x, int[] y) {
        int n = x.length;
        int matchingPixels = 0;

        for (int i = 0; i < n; i++) {
            if (x[i] == y[i]) {
                matchingPixels++;
            }
        }

        return (double) matchingPixels / n * 100;
    }
}