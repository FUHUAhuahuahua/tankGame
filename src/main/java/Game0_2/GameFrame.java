package Game0_2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameFrame extends JFrame {
    // 游戏面板，负责绘制和游戏逻辑
    private GamePanel gamePanel;

    public GameFrame() {
        // 初始化窗口设置
        initFrame();
        // 初始化游戏面板
        gamePanel = new GamePanel();
        this.add(gamePanel);
        // 添加键盘监听
        this.addKeyListener(new KeyMonitor());
    }

    // 窗口初始化设置
    private void initFrame() {
        setTitle("坦克大战 0.2");
        setSize(600, 600);
        setLocation(500, 100);
        // 窗口大小不可变
        setResizable(false);
        // 关闭窗口时退出程序
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        GameFrame gameFrame = new GameFrame();
        gameFrame.setVisible(true);
    }

    class GamePanel extends JPanel {
        Image bg;
        int tankX = 270;
        int tankY = 500;
        int tankSize = 60;

        public GamePanel() {
            bg = Toolkit.getDefaultToolkit().getImage("images/bg.jpg");
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bg, 0, 0, 600, 600, this);
            g.setColor(Color.RED);
            g.fillRect(tankX, tankY, tankSize, tankSize);
            g.setColor(Color.BLACK);
            g.fillRect(tankX + 25, tankY - 10, 10, 20);
        }
    }

    // 键盘监听器，控制坦克移动
    class KeyMonitor implements KeyListener {
        // 移动步长
        int step = 5;

        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            // 根据按键调整坦克位置
            switch (keyCode) {
                case KeyEvent.VK_UP:
                    if (gamePanel.tankY > 0) {
                        gamePanel.tankY -= step;
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (gamePanel.tankY < 600 - gamePanel.tankSize) {
                        gamePanel.tankY += step;
                    }
                    break;
                case KeyEvent.VK_LEFT:
                    if (gamePanel.tankX > 0) {
                        gamePanel.tankX -= step;
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (gamePanel.tankX < 600 - gamePanel.tankSize) {
                        gamePanel.tankX += step;
                    }
                    break;
            }
            // 重绘面板，更新显示
            gamePanel.repaint();
        }

        @Override
        public void keyReleased(KeyEvent e) {}

        @Override
        public void keyTyped(KeyEvent e) {}
    }
}