package Game0_9;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;

/**
 * 唐老鸭换装游戏 - 使用装饰器模式
 * 游戏特点：
 * 1. 中间显示唐老鸭图片
 * 2. 下方有三个按钮：正式风格、运动风格、混搭风格
 * 3. 五个维度的装饰：帽子、眼镜、围巾、领带、手表
 */
public class GameFrame extends Frame {
    // 游戏窗口尺寸
    public static final int WIDTH = 800;
    public static final int HEIGHT = 700;
    
    // 唐老鸭图片和装饰品图片
    private Image duckImg;
    
    // 当前装扮的唐老鸭（使用装饰器模式）
    private Duck currentDuck;
    
    // 当前选择的风格
    private String currentStyle = "无装扮";
    
    // 三个风格按钮
    private Button formalButton;
    private Button sportButton;
    private Button mixedButton;
    
    public GameFrame() {
        loadResources();
        initFrame();
        initButtons();
        // 初始化为基础唐老鸭（无装扮）
        currentDuck = new DonaldDuck();
    }
    
    /**
     * 加载图片资源
     */
    private void loadResources() {
        try {
            System.out.println("尝试加载唐老鸭图片: /images/duck.jpg");
            duckImg = loadImage("/images/duck.jpg");
            System.out.println("唐老鸭图片加载" + (duckImg != null ? "成功" : "失败"));
        } catch (Exception e) {
            System.err.println("资源加载失败: " + e.getMessage());
        }
    }
    
    /**
     * 加载单个图片
     */
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
    
    /**
     * 初始化窗口
     */
    private void initFrame() {
        setTitle("唐老鸭换装游戏 0.9 - 装饰器模式");
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);  // 使用绝对布局
        
        // 设置背景色
        setBackground(new Color(240, 248, 255));
        
        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
    
    /**
     * 初始化三个风格按钮
     */
    private void initButtons() {
        // 按钮的Y坐标（在窗口底部）
        int buttonY = HEIGHT - 120;
        int buttonWidth = 180;
        int buttonHeight = 50;
        int spacing = 50;
        
        // 计算三个按钮的起始X坐标（居中排列）
        int totalWidth = buttonWidth * 3 + spacing * 2;
        int startX = (WIDTH - totalWidth) / 2;
        
        // 正式风格按钮
        formalButton = new Button("正式风格");
        formalButton.setBounds(startX, buttonY, buttonWidth, buttonHeight);
        formalButton.setFont(new Font("宋体", Font.BOLD, 18));
        formalButton.setBackground(new Color(70, 130, 180));
        formalButton.setForeground(Color.WHITE);
        formalButton.addActionListener(e -> applyFormalStyle());
        add(formalButton);
        
        // 运动风格按钮
        sportButton = new Button("运动风格");
        sportButton.setBounds(startX + buttonWidth + spacing, buttonY, buttonWidth, buttonHeight);
        sportButton.setFont(new Font("宋体", Font.BOLD, 18));
        sportButton.setBackground(new Color(34, 139, 34));
        sportButton.setForeground(Color.WHITE);
        sportButton.addActionListener(e -> applySportStyle());
        add(sportButton);
        
        // 混搭风格按钮
        mixedButton = new Button("混搭风格");
        mixedButton.setBounds(startX + (buttonWidth + spacing) * 2, buttonY, buttonWidth, buttonHeight);
        mixedButton.setFont(new Font("宋体", Font.BOLD, 18));
        mixedButton.setBackground(new Color(255, 140, 0));
        mixedButton.setForeground(Color.WHITE);
        mixedButton.addActionListener(e -> applyMixedStyle());
        add(mixedButton);
    }
    
    /**
     * 应用正式风格
     * 装扮：礼帽 + 墨镜 + 丝绸围巾 + 领带 + 名表
     */
    private void applyFormalStyle() {
        currentDuck = new DonaldDuck();
        currentDuck = new TopHat(currentDuck);           // 礼帽
        currentDuck = new Sunglasses(currentDuck);       // 墨镜
        currentDuck = new SilkScarf(currentDuck);        // 丝绸围巾
        currentDuck = new Tie(currentDuck);              // 领带
        currentDuck = new LuxuryWatch(currentDuck);      // 名表
        currentStyle = "正式风格";
        repaint();
        
        // 显示装扮详情
        showStyleDetails();
    }
    
    /**
     * 应用运动风格
     * 装扮：棒球帽 + 运动眼镜 + 运动毛巾 + 无领带 + 运动手表
     */
    private void applySportStyle() {
        currentDuck = new DonaldDuck();
        currentDuck = new BaseballCap(currentDuck);      // 棒球帽
        currentDuck = new SportGlasses(currentDuck);     // 运动眼镜
        currentDuck = new SportTowel(currentDuck);       // 运动毛巾
        currentDuck = new SportWatch(currentDuck);       // 运动手表
        currentStyle = "运动风格";
        repaint();
        
        // 显示装扮详情
        showStyleDetails();
    }
    
    /**
     * 应用混搭风格
     * 装扮：棒球帽 + 墨镜 + 运动毛巾 + 领带 + 名表
     */
    private void applyMixedStyle() {
        currentDuck = new DonaldDuck();
        currentDuck = new BaseballCap(currentDuck);      // 棒球帽（运动）
        currentDuck = new Sunglasses(currentDuck);       // 墨镜（正式）
        currentDuck = new SportTowel(currentDuck);       // 运动毛巾（运动）
        currentDuck = new Tie(currentDuck);              // 领带（正式）
        currentDuck = new LuxuryWatch(currentDuck);      // 名表（正式）
        currentStyle = "混搭风格";
        repaint();
        
        // 显示装扮详情
        showStyleDetails();
    }
    
    /**
     * 显示当前装扮的详细信息
     */
    private void showStyleDetails() {
        String details = currentDuck.getDescription();
        JOptionPane.showMessageDialog(this, 
            details, 
            "当前装扮 - " + currentStyle, 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    @Override
    public void paint(Graphics g) {
        // 先清空整个画布，防止文字残留
        g.setColor(new Color(240, 248, 255));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 绘制标题
        g.setColor(new Color(25, 25, 112));
        g.setFont(new Font("宋体", Font.BOLD, 32));
        g.drawString("🦆 唐老鸭换装秀 🦆", WIDTH / 2 - 180, 60);
        
        // 绘制当前风格提示
        g.setFont(new Font("宋体", Font.PLAIN, 20));
        g.setColor(new Color(220, 20, 60));
        g.drawString("当前风格：" + currentStyle, WIDTH / 2 - 100, 100);
        
        // 绘制唐老鸭图片（居中显示）
        int duckX = WIDTH / 2 - 150;
        int duckY = 150;
        int duckWidth = 300;
        int duckHeight = 300;
        
        if (duckImg != null) {
            g.drawImage(duckImg, duckX, duckY, duckWidth, duckHeight, null);
        } else {
            // 如果没有图片，用黄色椭圆代替
            g.setColor(Color.YELLOW);
            g.fillOval(duckX, duckY, duckWidth, duckHeight);
            g.setColor(Color.ORANGE);
            g.drawOval(duckX, duckY, duckWidth, duckHeight);
        }
        
        // 绘制装饰品图形（在唐老鸭身上）
        drawAccessoriesGraphics(g, duckX, duckY, duckWidth, duckHeight);
        
        // 绘制装扮文字说明（在鸭子图片周围）
        drawAccessoriesText(g, duckX, duckY, duckWidth, duckHeight);
        
        // 绘制提示信息
        g.setFont(new Font("宋体", Font.PLAIN, 16));
        g.setColor(Color.DARK_GRAY);
        g.drawString("💡 点击下方按钮为唐老鸭换装吧！", WIDTH / 2 - 150, HEIGHT - 150);
    }
    
    /**
     * 绘制装饰品图形（在唐老鸭身上画出装饰品）
     */
    private void drawAccessoriesGraphics(Graphics g, int duckX, int duckY, int duckWidth, int duckHeight) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        String description = currentDuck.getDescription();
        
        // 根据唐老鸭图片的实际特征计算关键位置
        // 唐老鸭张开双手的姿势，头部在上方，身体在中间
        int centerX = duckX + duckWidth / 2;
        int headY = duckY + duckHeight / 6;      // 头部位置（更靠上）
        int eyeY = duckY + duckHeight / 4;       // 眼睛位置（在脸部中央）
        int neckY = duckY + duckHeight * 2 / 5;  // 脖子/领结位置
        int rightHandX = duckX + duckWidth - 30; // 右手位置（举起的手）
        int rightHandY = duckY + duckHeight / 3; // 右手高度
        
        // 绘制帽子（在头顶上方，再高一点）
        if (description.contains("礼帽")) {
            drawTopHat(g2d, centerX, headY - 60);
        } else if (description.contains("棒球帽")) {
            drawBaseballCap(g2d, centerX, headY - 50);
        }
        
        // 绘制眼镜（在眼睛位置，更精确）
        if (description.contains("墨镜")) {
            drawSunglasses(g2d, centerX, eyeY - 20);
        } else if (description.contains("运动眼镜")) {
            drawSportGlasses(g2d, centerX, eyeY - 20);
        }
        
        // 绘制围巾/毛巾（在领结上方一点）
        if (description.contains("丝绸围巾")) {
            drawSilkScarf(g2d, centerX, neckY + 10);
        } else if (description.contains("运动毛巾")) {
            drawSportTowel(g2d, centerX, neckY + 10);
        }
        
        // 绘制领带（在领结位置）
        if (description.contains("领带")) {
            drawTie(g2d, centerX, neckY + 20);
        }
        
        // 绘制手表（在右上方举起的手上）
        if (description.contains("名表")) {
            drawLuxuryWatch(g2d, rightHandX - 40, rightHandY - 30);
        } else if (description.contains("运动手表")) {
            drawSportWatch(g2d, rightHandX - 40, rightHandY - 30);
        }
    }
    
    // ========== 各种装饰品的绘制方法 ==========
    
    /** 绘制礼帽 */
    private void drawTopHat(Graphics2D g, int x, int y) {
        g.setColor(Color.BLACK);
        g.fillRect(x - 30, y, 60, 40);  // 帽身
        g.fillOval(x - 45, y + 35, 90, 15);  // 帽檐
        g.setColor(Color.WHITE);
        g.fillRect(x - 25, y + 15, 50, 8);  // 装饰带
    }
    
    /** 绘制棒球帽 */
    private void drawBaseballCap(Graphics2D g, int x, int y) {
        g.setColor(new Color(255, 69, 0));  // 橙红色
        g.fillArc(x - 40, y, 80, 60, 0, 180);  // 帽顶
        g.fillRect(x - 50, y + 30, 60, 8);  // 帽檐
        g.setColor(Color.WHITE);
        g.fillOval(x - 10, y + 15, 20, 20);  // 装饰圆
    }
    
    /** 绘制墨镜 */
    private void drawSunglasses(Graphics2D g, int x, int y) {
        g.setColor(Color.BLACK);
        g.fillOval(x - 45, y, 35, 25);  // 左镜片
        g.fillOval(x + 10, y, 35, 25);  // 右镜片
        g.setStroke(new BasicStroke(3));
        g.drawLine(x - 10, y + 12, x + 10, y + 12);  // 鼻梁
        g.setStroke(new BasicStroke(1));
    }
    
    /** 绘制运动眼镜 */
    private void drawSportGlasses(Graphics2D g, int x, int y) {
        g.setColor(new Color(0, 191, 255));  // 天蓝色
        g.setStroke(new BasicStroke(3));
        g.drawOval(x - 45, y, 35, 25);  // 左镜框
        g.drawOval(x + 10, y, 35, 25);  // 右镜框
        g.drawLine(x - 10, y + 12, x + 10, y + 12);  // 鼻梁
        g.setStroke(new BasicStroke(1));
    }
    
    /** 绘制丝绸围巾 */
    private void drawSilkScarf(Graphics2D g, int x, int y) {
        g.setColor(new Color(255, 20, 147));  // 深粉色
        g.setStroke(new BasicStroke(8));
        g.drawArc(x - 50, y - 20, 100, 40, 0, 180);  // 围巾主体
        // 围巾两端
        g.drawLine(x - 50, y, x - 60, y + 30);
        g.drawLine(x + 50, y, x + 60, y + 30);
        g.setStroke(new BasicStroke(1));
    }
    
    /** 绘制运动毛巾 */
    private void drawSportTowel(Graphics2D g, int x, int y) {
        g.setColor(new Color(50, 205, 50));  // 绿色
        g.setStroke(new BasicStroke(10));
        g.drawArc(x - 50, y - 20, 100, 40, 0, 180);  // 毛巾主体
        g.setStroke(new BasicStroke(1));
    }
    
    /** 绘制领带 */
    private void drawTie(Graphics2D g, int x, int y) {
        g.setColor(new Color(139, 0, 0));  // 深红色
        int[] xPoints = {x, x - 15, x - 10, x - 15, x + 15, x + 10, x + 15};
        int[] yPoints = {y, y + 10, y + 20, y + 60, y + 60, y + 20, y + 10};
        g.fillPolygon(xPoints, yPoints, 7);
        // 领结
        g.setColor(new Color(178, 34, 34));
        g.fillRect(x - 20, y - 5, 40, 10);
    }
    
    /** 绘制名表 */
    private void drawLuxuryWatch(Graphics2D g, int x, int y) {
        g.setColor(new Color(218, 165, 32));  // 金色
        g.fillOval(x - 15, y - 15, 30, 30);  // 表盘
        g.setColor(Color.BLACK);
        g.drawOval(x - 15, y - 15, 30, 30);  // 表盘边框
        // 表带
        g.setColor(new Color(139, 69, 19));
        g.fillRect(x - 20, y - 3, 10, 6);
        g.fillRect(x + 10, y - 3, 10, 6);
        // 时针分针
        g.setColor(Color.BLACK);
        g.drawLine(x, y, x + 5, y - 8);
        g.drawLine(x, y, x + 8, y + 3);
    }
    
    /** 绘制运动手表 */
    private void drawSportWatch(Graphics2D g, int x, int y) {
        g.setColor(Color.BLACK);  // 黑色表盘
        g.fillRoundRect(x - 18, y - 12, 36, 24, 5, 5);
        g.setColor(new Color(0, 255, 0));  // 绿色显示屏
        g.fillRect(x - 15, y - 9, 30, 18);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.drawString("12:34", x - 12, y + 3);
        // 表带
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x - 22, y - 3, 8, 6);
        g.fillRect(x + 14, y - 3, 8, 6);
    }
    
    /**
     * 绘制装饰品文字说明
     */
    private void drawAccessoriesText(Graphics g, int duckX, int duckY, int duckWidth, int duckHeight) {
        g.setFont(new Font("宋体", Font.BOLD, 16));
        
        // 获取当前装扮列表
        String description = currentDuck.getDescription();
        String[] items = description.split("\n");
        
        // 在鸭子图片右侧显示装扮列表
        int textX = duckX + duckWidth + 30;
        int textY = duckY + 50;
        
        g.setColor(new Color(0, 100, 0));
        for (int i = 0; i < items.length; i++) {
            if (!items[i].trim().isEmpty()) {
                g.drawString(items[i], textX, textY + i * 30);
            }
        }
    }
    
    // 双缓冲，防止闪烁
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
        GameFrame game = new GameFrame();
        game.setVisible(true);
    }
}

// ==================== 装饰器模式实现 ====================

/**
 * 鸭子抽象类（被装饰对象的基类）
 */
abstract class Duck {
    /**
     * 获取装扮描述
     */
    public abstract String getDescription();
}

/**
 * 唐老鸭（基础对象，没有任何装扮）
 */
class DonaldDuck extends Duck {
    @Override
    public String getDescription() {
        return "🦆 唐老鸭";
    }
}

/**
 * 装饰器基类（所有装饰品的父类）
 */
abstract class Accessory extends Duck {
    protected Duck duck;  // 被装饰的鸭子
    
    public Accessory(Duck duck) {
        this.duck = duck;
    }
    
    @Override
    public String getDescription() {
        return duck.getDescription();
    }
}

// ==================== 帽子类装饰器 ====================

/**
 * 礼帽
 */
class TopHat extends Accessory {
    public TopHat(Duck duck) {
        super(duck);
    }
    
    @Override
    public String getDescription() {
        return duck.getDescription() + "\n🎩 戴着礼帽";
    }
}

/**
 * 棒球帽
 */
class BaseballCap extends Accessory {
    public BaseballCap(Duck duck) {
        super(duck);
    }
    
    @Override
    public String getDescription() {
        return duck.getDescription() + "\n🧢 戴着棒球帽";
    }
}

// ==================== 眼镜类装饰器 ====================

/**
 * 墨镜
 */
class Sunglasses extends Accessory {
    public Sunglasses(Duck duck) {
        super(duck);
    }
    
    @Override
    public String getDescription() {
        return duck.getDescription() + "\n🕶️ 戴着墨镜";
    }
}

/**
 * 运动眼镜
 */
class SportGlasses extends Accessory {
    public SportGlasses(Duck duck) {
        super(duck);
    }
    
    @Override
    public String getDescription() {
        return duck.getDescription() + "\n👓 戴着运动眼镜";
    }
}

// ==================== 围巾类装饰器 ====================

/**
 * 丝绸围巾
 */
class SilkScarf extends Accessory {
    public SilkScarf(Duck duck) {
        super(duck);
    }
    
    @Override
    public String getDescription() {
        return duck.getDescription() + "\n🧣 围着丝绸围巾";
    }
}

/**
 * 运动毛巾
 */
class SportTowel extends Accessory {
    public SportTowel(Duck duck) {
        super(duck);
    }
    
    @Override
    public String getDescription() {
        return duck.getDescription() + "\n🏃 围着运动毛巾";
    }
}

// ==================== 领带类装饰器 ====================

/**
 * 领带
 */
class Tie extends Accessory {
    public Tie(Duck duck) {
        super(duck);
    }
    
    @Override
    public String getDescription() {
        return duck.getDescription() + "\n👔 戴着领带";
    }
}

// ==================== 手表类装饰器 ====================

/**
 * 名表
 */
class LuxuryWatch extends Accessory {
    public LuxuryWatch(Duck duck) {
        super(duck);
    }
    
    @Override
    public String getDescription() {
        return duck.getDescription() + "\n⌚ 戴着名表";
    }
}

/**
 * 运动手表
 */
class SportWatch extends Accessory {
    public SportWatch(Duck duck) {
        super(duck);
    }
    
    @Override
    public String getDescription() {
        return duck.getDescription() + "\n⏱️ 戴着运动手表";
    }
}
