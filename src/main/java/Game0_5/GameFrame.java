package Game0_5;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.stream.IntStream;

public class GameFrame extends Frame {

    Image bgImg = loadImage("/images/bg.webp");
    Image tankImg = loadImage("/images/tank.jpg");
    int nCount = 0;
    int x = 275, y = 275;
    boolean left, right, up, down;
    // 移动步长
    int step = 5;

    int x_shell=300,y_shell=300;
    double degree =Math.random()*Math.PI*2;

    int[] x_array = new int[50];
    int[] y_array = new int[50];
    double[] degree_array = new double[50];

    public static void main(String[] args) {
        GameFrame gameFrame = new GameFrame();
        gameFrame.initialFrame();
    }



    class paintThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                repaint();
                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    class KeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            super.keyPressed(e);

            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    up = true;
                    break;
                case KeyEvent.VK_DOWN:
                    down = true;
                    break;
                case KeyEvent.VK_LEFT:
                    left = true;
                    break;
                case KeyEvent.VK_RIGHT:
                    right = true;
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            super.keyReleased(e);

            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    up = false;
                    break;
                case KeyEvent.VK_DOWN:
                    down = false;
                    break;
                case KeyEvent.VK_LEFT:
                    left = false;
                    break;
                case KeyEvent.VK_RIGHT:
                    right = false;
                    break;
            }
        }
    }


    private Image offScreenImage = null;

    public void update(Graphics g) {
        if (offScreenImage == null) {
            offScreenImage = this.createImage(600, 600);
        }
        Graphics gOff = offScreenImage.getGraphics();
        paint(gOff);
        g.drawImage(offScreenImage, 0, 0, null);
    }

    private void initialFrame() {
        setVisible(true);

        setTitle("GameFrame0.4");
        setSize(600, 600);
        setLocation(500, 100);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });
        new paintThread().start();

        addKeyListener(new KeyListener());

        for(int n =0; n < 50; n++) {

            x_array[n] = 100;
            y_array[n] = 100;
            degree_array[n] = Math.random()*Math.PI*2;
        }

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(bgImg, 0, 0, 600, 600, null);
        g.drawImage(tankImg, x, y, 100, 50, null);

        // 根据按键方向移动坦克，并添加边界判断
        if (up && y > 0) {
            y -= step;
        }
        if (down && y < 600 - 50) {
            y += step;
        }
        if (left && x > 0) {
            x -= step;
        }
        if (right && x < 600 - 100) {
            x += step;
        }
        Color c=g.getColor();
        g.setColor(Color.BLACK);
        g.fillRect(x_shell, y_shell, 10, 10);
        x_shell+=5*Math.cos(degree);
        y_shell+=5*Math.sin(degree);
        g.setColor(c);

        System.out.println("绘制中... x: " + x + ", y: " + y);

        Color c2 = g.getColor();
        g.setColor(Color.BLACK);
        IntStream.range(0,50).forEach(i -> {
            g.fillOval(x_array[i], y_array[i], 10, 10);
            x_array[i]+=7*Math.cos(degree_array[i]);
            y_array[i]+=7*Math.sin(degree_array[i]);
        }) ;
    }

    public Image loadImage(String imagePath) {
        URL url = getClass().getResource(imagePath);
        if (url != null) {
            try {
                return ImageIO.read(url);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.err.println("Couldn't load image " + imagePath);
        return null;
    }

}