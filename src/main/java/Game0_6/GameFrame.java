package Game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// 游戏主框架
public class GameFrame extends Frame {
    private Tank player;
    private List<RedPacket> redPackets = new ArrayList<>();
    private GameTimer gameTimer;
    private int score = 0;
    private boolean isGameOver = false;

    // 游戏配置
    public static final int WIDTH = 600;
    public static final int HEIGHT = 600;
    public static final int GAME_TIME = 30000; // 30秒游戏时间

    public GameFrame() {
        initGame();
        initFrame();
        startGame();
    }

    private void initGame() {
        // 初始化玩家坦克
        player = new Tank(WIDTH / 2 - 50, HEIGHT - 100, 100, 50, 5);

        // 初始化计时器
        gameTimer = new GameTimer(GAME_TIME, () -> isGameOver = true);
    }

    private void initFrame() {
        setTitle("红包收集游戏");
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        // 窗口关闭处理
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // 键盘控制
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
        // 启动游戏循环
        new PaintThread().start();
        // 启动红包生成器
        new RedPacketSpawner().start();
        // 启动计时器
        gameTimer.start();
    }

    // 游戏主循环
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

    // 红包生成器
    class RedPacketSpawner extends Thread {
        private Random random = new Random();

        @Override
        public void run() {
            while (!isGameOver) {
                // 随机生成红包
                int x = random.nextInt(WIDTH - 30);
                redPackets.add(new RedPacket(x, 0, 30, 30, 3 + random.nextInt(4)));

                try {
                    // 随机间隔生成红包
                    Thread.sleep(500 + random.nextInt(1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateGame() {
        // 更新玩家位置
        player.updatePosition();

        // 更新红包位置并检测碰撞
        List<RedPacket> toRemove = new ArrayList<>();
        for (RedPacket rp : redPackets) {
            rp.updatePosition();

            // 检测是否超出屏幕
            if (rp.getY() > HEIGHT) {
                toRemove.add(rp);
            }
            // 检测碰撞
            else if (player.collidesWith(rp)) {
                toRemove.add(rp);
                score++;
            }
        }
        redPackets.removeAll(toRemove);
    }

    @Override
    public void paint(Graphics g) {
        // 绘制背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 绘制玩家
        g.setColor(Color.BLUE);
        player.draw(g);

        // 绘制红包
        g.setColor(Color.RED);
        for (RedPacket rp : redPackets) {
            rp.draw(g);
        }

        // 绘制分数和剩余时间
        g.setColor(Color.BLACK);
        g.drawString("分数: " + score, 20, 30);
        g.drawString("剩余时间: " + gameTimer.getRemainingTime() / 1000 + "秒", 20, 50);

        // 游戏结束显示
        if (isGameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("宋体", Font.BOLD, 40));
            g.drawString("游戏结束", WIDTH / 2 - 120, HEIGHT / 2 - 40);
            g.drawString("最终得分: " + score, WIDTH / 2 - 140, HEIGHT / 2 + 20);
        }
    }

    // 双缓冲解决闪烁
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

// 游戏物体基类
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

    // 检测碰撞
    public boolean collidesWith(GameObject other) {
        return x < other.x + other.width &&
                x + width > other.x &&
                y < other.y + other.height &&
                y + height > other.y;
    }

    public abstract void draw(Graphics g);
    public abstract void updatePosition();
}

// 玩家坦克类
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
    public void draw(Graphics g) {
        g.fillRect(x, y, width, height);
        // 绘制炮管
        g.fillRect(x + width/2 - 5, y - 10, 10, 20);
    }
}

// 红包类
class RedPacket extends GameObject {
    private int speed;

    public RedPacket(int x, int y, int width, int height, int speed) {
        super(x, y, width, height);
        this.speed = speed;
    }

    @Override
    public void updatePosition() {
        // 竖直下落
        y += speed;
    }

    @Override
    public void draw(Graphics g) {
        // 绘制红包形状
        g.fillRect(x, y, width, height);
        g.setColor(Color.YELLOW);
        g.drawString("¥", x + 10, y + 20);
        g.setColor(Color.RED);
    }
}

// 游戏计时器
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