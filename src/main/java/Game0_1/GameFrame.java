package Game0_1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameFrame extends JFrame {
    public static void main(String[] args) {
        GameFrame gameFrame = new GameFrame();

        gameFrame.setVisible(true);

        gameFrame.setTitle("0.1");
        gameFrame.setSize(600, 600);
        gameFrame.setLocationRelativeTo(null);

        gameFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });
    }
}
