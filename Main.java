import javax.swing.*;

public static void main() {
    JFrame frame = new JFrame("Mandelbrot");
    MandelbrotOld mandelbrot = new MandelbrotOld();
    frame.add(mandelbrot);
    frame.setSize(1024, 720);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
}