package Game0_6;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class GameFrame extends Frame {
    private Tank player;
    private List<RedPacket> redPackets = new ArrayList<>();
    private GameTimer gameTimer;
    private int score = 0;
    private boolean isGameOver = false;


    private Image bgImg;
    private Image tankImg;
    private Image redPacketImg;


    public static final int WIDTH = 600;
    public static final int HEIGHT = 600;
    public static final int GAME_TIME = 30000;

    public GameFrame() {
        loadResources();
        initGame();
        initFrame();
        startGame();
    }

    private void loadResources() {
        try {
            System.out.println("尝试加载背景图片: /images/R-C.jpg");
            bgImg = loadImage("/images/R-C.jpg");
            System.out.println("背景图片加载" + (bgImg != null ? "成功" : "失败"));

            System.out.println("尝试加载坦克图片: /images/tank.jpg");
            tankImg = loadImage("/images/tank.jpg");
            System.out.println("坦克图片加载" + (tankImg != null ? "成功" : "失败"));

            System.out.println("尝试加载红包图片: /images/redpacket.png");
            redPacketImg = loadImage("/images/redpacket.png");
            System.out.println("红包图片加载" + (redPacketImg != null ? "成功" : "失败"));
        } catch (Exception e) {
            System.err.println("资源加载失败: " + e.getMessage());
        }
    }

    private Image loadImage(String imagePath) {
        URL url = getClass().getResource(imagePath);
        if (url != null) {
            try {
                return ImageIO.read(url);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.err.println("无法加载图片: " + imagePath + "（资源路径不存在）");
        return null;
    }

    private void initGame() {
        player = new Tank(WIDTH / 2 - 50, HEIGHT - 100, 100, 50, 5);
        System.out.println("初始化坦克位置: (" + player.getX() + ", " + player.getY() + ")");

        gameTimer = new GameTimer(GAME_TIME, () -> isGameOver = true);
    }

    private void initFrame() {
        setTitle("红包收集游戏");
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isGameOver) return;
                player.handleKeyPress(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                player.handleKeyRelease(e.getKeyCode());
            }
        });
    }

    private void startGame() {
        new PaintThread().start();
        new RedPacketSpawner().start();
        gameTimer.start();
    }

    class PaintThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (!isGameOver) {
                    updateGame();
                }
                repaint();
                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class RedPacketSpawner extends Thread {
        private Random random = new Random();

        @Override
        public void run() {
            while (!isGameOver) {
                int x = random.nextInt(WIDTH - 30);
                redPackets.add(new RedPacket(x, 0, 30, 30, 3 + random.nextInt(4)));

                try {
                    Thread.sleep(500 + random.nextInt(1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateGame() {
        player.updatePosition();
        System.out.println("坦克当前位置: (" + player.getX() + ", " + player.getY() + ")");

        List<RedPacket> toRemove = new ArrayList<>();
        for (RedPacket rp : redPackets) {
            rp.updatePosition();

            if (rp.getY() > HEIGHT) {
                toRemove.add(rp);
            } else if (player.collidesWith(rp)) {
                toRemove.add(rp);
                score++;
                System.out.println("收集到红包！当前分数: " + score);
            }
        }
        redPackets.removeAll(toRemove);
    }

    @Override
    public void paint(Graphics g) {
        if (bgImg != null) {
            g.drawImage(bgImg, 0, 0, WIDTH, HEIGHT, null);
        } else {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }

        if (tankImg != null) {
            player.draw(g, tankImg);
        } else {
            g.setColor(Color.BLUE);
            player.draw(g, null);
        }

        for (RedPacket rp : redPackets) {
            if (redPacketImg != null) {
                rp.draw(g, redPacketImg);
            } else {
                g.setColor(Color.RED);
                rp.draw(g, null);
            }
        }

        g.setColor(Color.BLACK);
        g.drawString("分数: " + score, 20, 30);
        g.drawString("剩余时间: " + gameTimer.getRemainingTime() / 1000 + "秒", 20, 50);

        if (isGameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("宋体", Font.BOLD, 40));
            g.drawString("游戏结束", WIDTH / 2 - 120, HEIGHT / 2 - 40);
            g.drawString("最终得分: " + score, WIDTH / 2 - 140, HEIGHT / 2 + 20);
        }
    }

    private Image offScreenImage;
    @Override
    public void update(Graphics g) {
        if (offScreenImage == null) {
            offScreenImage = createImage(WIDTH, HEIGHT);
        }
        Graphics gOff = offScreenImage.getGraphics();
        paint(gOff);
        g.drawImage(offScreenImage, 0, 0, null);
    }

    public static void main(String[] args) {
        new GameFrame().setVisible(true);
    }
}

abstract class GameObject {
    protected int x, y;
    protected int width, height;

    public GameObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public boolean collidesWith(GameObject other) {
        return x < other.x + other.width &&
                x + width > other.x &&
                y < other.y + other.height &&
                y + height > other.y;
    }

    public abstract void draw(Graphics g, Image img);
    public abstract void updatePosition();
}

class Tank extends GameObject {
    private int speed;
    private boolean left, right, up, down;

    public Tank(int x, int y, int width, int height, int speed) {
        super(x, y, width, height);
        this.speed = speed;
    }

    public void handleKeyPress(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                left = true;
                break;
            case KeyEvent.VK_RIGHT:
                right = true;
                break;
            case KeyEvent.VK_UP:
                up = true;
                break;
            case KeyEvent.VK_DOWN:
                down = true;
                break;
        }
    }

    public void handleKeyRelease(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                left = false;
                break;
            case KeyEvent.VK_RIGHT:
                right = false;
                break;
            case KeyEvent.VK_UP:
                up = false;
                break;
            case KeyEvent.VK_DOWN:
                down = false;
                break;
        }
    }

    @Override
    public void updatePosition() {
        if (left && x > 0) {
            x -= speed;
        }
        if (right && x < GameFrame.WIDTH - width) {
            x += speed;
        }
        if (up && y > 0) {
            y -= speed;
        }
        if (down && y < GameFrame.HEIGHT - height) {
            y += speed;
        }
    }

    @Override
    public void draw(Graphics g, Image img) {
        if (img != null) {
            g.drawImage(img, x, y, width, height, null);
        } else {
            g.fillRect(x, y, width, height);
            g.fillRect(x + width/2 - 5, y - 10, 10, 20);
        }
    }
}

class RedPacket extends GameObject {
    private int speed;

    public RedPacket(int x, int y, int width, int height, int speed) {
        super(x, y, width, height);
        this.speed = speed;
    }

    @Override
    public void updatePosition() {
        y += speed;
    }

    @Override
    public void draw(Graphics g, Image img) {
        if (img != null) {
            g.drawImage(img, x, y, width, height, null);
        } else {
            g.fillRect(x, y, width, height);
            g.setColor(Color.YELLOW);
            g.drawString("¥", x + 10, y + 20);
            g.setColor(Color.RED);
        }
    }
}

class GameTimer extends Thread {
    private long totalTime;
    private long remainingTime;
    private Runnable onFinish;
    private boolean isRunning = true;

    public GameTimer(long totalTime, Runnable onFinish) {
        this.totalTime = totalTime;
        this.remainingTime = totalTime;
        this.onFinish = onFinish;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        while (isRunning && remainingTime > 0) {
            remainingTime = totalTime - (System.currentTimeMillis() - startTime);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (onFinish != null) {
            onFinish.run();
        }
    }

    public long getRemainingTime() {
        return Math.max(remainingTime, 0);
    }

    public void stopTimer() {
        isRunning = false;
    }
}
