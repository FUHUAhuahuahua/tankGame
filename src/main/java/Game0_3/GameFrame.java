package Game0_3;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;

public class GameFrame extends Frame {

    Image bgImg = loadImage("images/bg.webp");
    Image tankImg = loadImage("images/tank.jpg");
    int nCount = 0;
    int x = 275,y=275;

    public static void main(String[] args) {
        Game0_3.GameFrame gameFrame = new Game0_3.GameFrame();
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

    private Image offScreenImage = null;
    public void update(Graphics g) {
        if (offScreenImage == null){
            offScreenImage = this.createImage(600,600);
        }
        Graphics gOff = offScreenImage.getGraphics();
        paint(gOff);
        g.drawImage(offScreenImage,0,0,null);
    }

    public void initialFrame() {
        setVisible(true);

        setTitle("GameFrame0.3");
        setSize(600,600);
        setLocation(500,100);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                super.windowClosing(e);
                System.exit(0);
            }
        });
        new paintThread().start();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(bgImg,0,0,600,600,null);
        g.drawImage(tankImg,x,y,100,50,null);
        x -= 2;
        System.out.println("绘制中...");
    }

    public Image loadImage(String imagePath){
        URL url = getClass().getResource(imagePath);
        if (url != null){
            try {
                return ImageIO.read(url);
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }
        System.err.println("Couldn't load image " + imagePath);
        return null;
    }
}