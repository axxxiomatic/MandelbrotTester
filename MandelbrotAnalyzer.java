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

        int[] correctZX = new int[ARRAY_SIZE];
        int[] correctZY = new int[ARRAY_SIZE];
        int[] correctCX = new int[ARRAY_SIZE];
        int[] correctCY = new int[ARRAY_SIZE];

        BufferedImage defaultImage = loadImage("default.png");

        File dir = new File("sv");
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(".png"));

        //if (files != null && files.length >= ARRAY_SIZE) {
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

                    //if (iteration >= currentIteration) {
                        zX_input[iteration] = zX;
                        zY_input[iteration] = zY;
                        cX_input[iteration] = cX;
                        cY_input[iteration] = cY;

                        double[] predictedParams = compareImages(defaultImage, image);
                        correctZX[iteration] = Math.abs(predictedParams[0] - zX) <= 0.01 ? 1 : 0;
                        correctZY[iteration] = Math.abs(predictedParams[1] - zY) <= 0.01 ? 1 : 0;
                        correctCX[iteration] = Math.abs(predictedParams[2] - cX) <= 1 ? 1 : 0;
                        correctCY[iteration] = Math.abs(predictedParams[3] - cY) <= 1 ? 1 : 0;

                        System.out.println("Iteration i = " + iteration + " - Correct ZX: " + correctZX[iteration] + ", Correct ZY: " + correctZY[iteration] + ", Correct CX: " + correctCX[iteration] + ", Correct CY: " + correctCY[iteration]);

                        currentIteration++;
                    //}
                } else {
                    System.out.println("Couldn't parse the file!");
                }
            }
        } else {
            System.out.println("Not enough matching files!");
        }

        double percentCorrectZX = calculatePercentage(correctZX);
        double percentCorrectZY = calculatePercentage(correctZY);
        double percentCorrectCX = calculatePercentage(correctCX);
        double percentCorrectCY = calculatePercentage(correctCY);

        System.out.println("Percentage of correct ZX: " + percentCorrectZX + "%");
        System.out.println("Percentage of correct ZY: " + percentCorrectZY + "%");
        System.out.println("Percentage of correct CX: " + percentCorrectCX + "%");
        System.out.println("Percentage of correct CY: " + percentCorrectCY + "%");
    }

    public static void compareImagesPairwise() {
        File dir = new File("sv");
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(".png"));

        if (files != null && files.length > 0) {
            int totalComparisons = 0;
            double totalSSIM = 0;
            for (int i = 0; i < files.length; i += 10) { // было 50
                for (int j = i + 1; j < files.length; j += 10) { // было 50
                    BufferedImage image1 = loadImage(files[i].getPath());
                    BufferedImage image2 = loadImage(files[j].getPath());

                    if (image1 != null && image2 != null) {
                        double ssim = calculateSSIM(image1, image2);
                        System.out.println("Comparing " + files[i].getName() + " and " + files[j].getName() + ": SSIM = " + ssim);
                        totalSSIM += ssim;
                        totalComparisons++;
                    } else {
                        System.out.println("Couldn't load one of the images!");
                    }
                }
            }

            if (totalComparisons > 0) {
                double averageSSIM = totalSSIM / totalComparisons;
                System.out.println("Average SSIM: " + averageSSIM);
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

    private static double[] compareImages(BufferedImage defaultImage, BufferedImage image) {
        int width = defaultImage.getWidth();
        int height = defaultImage.getHeight();

        int[] defaultPixels = new int[width * height];
        int[] imagePixels = new int[width * height];

        defaultImage.getRGB(0, 0, width, height, defaultPixels, 0, width);
        image.getRGB(0, 0, width, height, imagePixels, 0, width);

        double ssim = calculateSSIM(defaultPixels, imagePixels);

        // Предположим, что чем ближе SSIM к 1, тем ближе изображение к default.png
        // В данном примере просто возвращаем значения по умолчанию, но можно использовать более сложную эвристику
        double zX = DEFAULT_ZX;
        double zY = DEFAULT_ZY;
        double cX = DEFAULT_CX;
        double cY = DEFAULT_CY;

        // Пример эвристики: если SSIM больше определенного порога, считаем, что параметры близки к значениям по умолчанию
        double threshold = 0.95; // Порог SSIM для определения близости
        if (ssim > threshold) {
            zX = DEFAULT_ZX;
            zY = DEFAULT_ZY;
            cX = DEFAULT_CX;
            cY = DEFAULT_CY;
        } else {
            // Здесь можно добавить более сложную логику для определения параметров на основе SSIM
            // Например, можно использовать методы машинного обучения для предсказания параметров
        }

        return new double[]{zX, zY, cX, cY};
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
        Pattern pattern = Pattern.compile("i=(\\d+)_zx=(-?\\d+,\\d+)_zy=(-?\\d+,\\d+)_cx=(-?\\d+,\\d+)_cy=(-?\\d+,\\d+)");
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
}