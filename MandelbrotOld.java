import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MandelbrotOld extends JPanel {
    private final int MAX_ITER = 3000;
    private double ZOOM = 300;
    private double offsetX = 0; // Смещение по оси X
    private double offsetY = 0; // Смещение по оси Y

    public MandelbrotOld() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) { //приближение
                    double x = e.getX();
                    double y = e.getY();
                    offsetX += (x - getWidth() / 2) / ZOOM;
                    offsetY += (y - getHeight() / 2) / ZOOM;
                    ZOOM *= 1.4;
                    repaint();
                } else if (SwingUtilities.isRightMouseButton(e)) { //отдаление
                    double x = e.getX();
                    double y = e.getY();
                    offsetX += (x - getWidth() / 2) / ZOOM;
                    offsetY += (y - getHeight() / 2) / ZOOM;
                    ZOOM *= 0.8;
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                double zx = 0, zy = 0;
                double cX = (x - getWidth() / 2) / ZOOM + offsetX;
                double cY = (y - getHeight() / 2) / ZOOM + offsetY;
                int i = MAX_ITER;
                while (zx * zx + zy * zy < 4 && i > 0) {
                    double tmp = zx * zx - zy * zy + cX;
                    zy = 2.0 * zx * zy + cY;
                    zx = tmp;
                    i--;
                }
                int color = i | (i << 10);
                image.setRGB(x, y, i > 0 ? color : 0);
            }
        }
        g.drawImage(image, 0, 0, null);
    }
}