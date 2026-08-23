import java.applet.Applet;
import java.awt.Graphics;

public class Q2_AppletLifecycle extends Applet {

    String message = "";

    public void init() {
        message = "init() is invoked";
    }

    public void start() {
        message = "start() is invoked";
        repaint();
    }

    public void paint(Graphics g) {
        g.drawString("Applet Lifecycle Demonstration", 80, 80);
        g.drawString(message, 80, 120);
    }

    public void stop() {
        message = "stop() is invoked";
        repaint();
    }

    public void destroy() {
        message = "destroy() is invoked";
    }
}
