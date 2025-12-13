package Game0_17;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 装扮商店对话框
 * 用于展示和购买各种装扮套装
 */
public class DressShopDialog extends JDialog {
    private GameFrame gameFrame;
    private Image shopBackground;
    private Map<String, Boolean> purchasedSuits = new HashMap<>();
    private Map<String, JButton> suitButtons = new HashMap<>();
    
    // 套装图片
    private Map<String, Image> suitImages = new HashMap<>();
    private Image defaultDuckImage; // 默认唐老鸭图片
    
    // 套装定义
    private static final String[] SUIT_NAMES = {"运动风格", "正式风格", "混搭风格"};
    private static final int SUIT_COST = 100;
    
    public DressShopDialog(Frame parent, GameFrame gameFrame) {
        super(parent, "🛍️ 唐老鸭装扮商店", true);
        this.gameFrame = gameFrame;
        loadResources();
        loadPurchaseStatus();
        initUI();
    }
    
    /**
     * 加载资源
     */
    private void loadResources() {
        try {
            // 加载背景图片
            URL bgUrl = getClass().getResource("/images/shopbackground.png");
            if (bgUrl != null) {
                shopBackground = ImageIO.read(bgUrl);
            }
            
            // 加载默认唐老鸭图片
            URL duckUrl = getClass().getResource("/images/duck.jpg");
            if (duckUrl != null) {
                defaultDuckImage = ImageIO.read(duckUrl);
            }
            
            // 加载套装预览图片
            loadSuitImage("运动风格", "/images/yd_style.jpeg");
            loadSuitImage("正式风格", "/images/zs_style.jpeg");
            loadSuitImage("混搭风格", "/images/hd_style.jpeg");
            
        } catch (IOException e) {
            System.err.println("商店资源加载失败: " + e.getMessage());
        }
    }
    
    /**
     * 加载单个套装图片
     */
    private void loadSuitImage(String suitName, String imagePath) {
        try {
            URL url = getClass().getResource(imagePath);
            if (url != null) {
                Image img = ImageIO.read(url);
                // 缩放图片到合适大小
                Image scaledImg = img.getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                suitImages.put(suitName, scaledImg);
            }
        } catch (IOException e) {
            System.err.println("套装图片加载失败 [" + suitName + "]: " + e.getMessage());
        }
    }
    
    /**
     * 从数据库加载套装购买状态
     */
    private void loadPurchaseStatus() {
        // 初始化所有套装为未购买
        for (String suit : SUIT_NAMES) {
            purchasedSuits.put(suit, false);
        }
        
        // 从数据库读取购买记录
        try (Connection conn = DriverManager.getConnection(
                GameFrame.DB_URL, GameFrame.DB_USER, GameFrame.DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(
                "SELECT suit_name FROM suit_purchases WHERE is_purchased = 1")) {
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String suitName = rs.getString("suit_name");
                purchasedSuits.put(suitName, true);
            }
        } catch (SQLException e) {
            System.err.println("加载购买记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 初始化UI
     */
    private void initUI() {
        setSize(700, 500);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        // 创建主面板
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 绘制背景
                if (shopBackground != null) {
                    g.drawImage(shopBackground, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // 如果没有背景图，使用渐变色
                    Graphics2D g2d = (Graphics2D) g;
                    GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(255, 220, 180),
                        0, getHeight(), new Color(255, 200, 150)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setLayout(null);
        
        // 标题
        JLabel titleLabel = new JLabel("唐老鸭装扮商店");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 32));
        titleLabel.setForeground(new Color(255, 255, 255));
        titleLabel.setBounds(150, 20, 400, 40);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // 添加文字阴影效果
        titleLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(2, 2, 2, 2),
            null
        ));
        mainPanel.add(titleLabel);
        
        // 显示当前金额
        JLabel moneyLabel = new JLabel("💰 当前金额: " + gameFrame.getTotalAmount() + "元");
        moneyLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        moneyLabel.setForeground(Color.YELLOW);
        moneyLabel.setBounds(250, 70, 200, 30);
        mainPanel.add(moneyLabel);
        
        // 套装展示区域
        int startX = 80;
        int startY = 120;
        int buttonWidth = 180;
        int buttonHeight = 250;
        int spacing = 30;
        
        for (int i = 0; i < SUIT_NAMES.length; i++) {
            String suitName = SUIT_NAMES[i];
            boolean isPurchased = purchasedSuits.get(suitName);
            
            // 创建套装面板
            JPanel suitPanel = createSuitPanel(suitName, isPurchased, i);
            suitPanel.setBounds(startX + i * (buttonWidth + spacing), startY, buttonWidth, buttonHeight);
            mainPanel.add(suitPanel);
        }
        
        // 返回按钮
        JButton backButton = new JButton("返回");
        backButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        backButton.setBounds(300, 400, 100, 40);
        backButton.addActionListener(e -> dispose());
        mainPanel.add(backButton);
        
        setContentPane(mainPanel);
        setVisible(true);
    }
    
    /**
     * 创建套装面板
     */
    private JPanel createSuitPanel(String suitName, boolean isPurchased, int index) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        // 套装图标面板
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制背景框
                if (isPurchased) {
                    g2d.setColor(new Color(100, 200, 100, 50));
                } else {
                    g2d.setColor(new Color(200, 200, 200, 50));
                }
                g2d.fillRoundRect(10, 10, 140, 140, 20, 20);
                
                // 绘制边框
                g2d.setStroke(new BasicStroke(3));
                if (isPurchased) {
                    g2d.setColor(new Color(100, 200, 100));
                } else {
                    g2d.setColor(new Color(150, 150, 150));
                }
                g2d.drawRoundRect(10, 10, 140, 140, 20, 20);
                
                // 绘制套装预览图片
                Image suitImage = suitImages.get(suitName);
                if (suitImage != null) {
                    g2d.drawImage(suitImage, 10, 10, 140, 140, this);
                } else if (defaultDuckImage != null) {
                    // 使用默认唐老鸭图片
                    g2d.drawImage(defaultDuckImage, 10, 10, 140, 140, this);
                } else {
                    // 如果都没有图片，绘制占位符
                    g2d.setColor(new Color(100, 100, 100));
                    g2d.fillOval(50, 50, 60, 60);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("微软雅黑", Font.BOLD, 14));
                    String text = "预览";
                    FontMetrics fm = g2d.getFontMetrics();
                    int textX = (160 - fm.stringWidth(text)) / 2;
                    int textY = 85;
                    g2d.drawString(text, textX, textY);
                }
                
                // 如果未购买，添加半透明遮罩
                if (!isPurchased) {
                    g2d.setColor(new Color(0, 0, 0, 100));
                    g2d.fillRoundRect(10, 10, 140, 140, 20, 20);
                }
            }
        };
        iconPanel.setPreferredSize(new Dimension(160, 160));
        iconPanel.setMaximumSize(new Dimension(160, 160));
        iconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(iconPanel);
        
        // 套装名称
        JLabel nameLabel = new JLabel(suitName);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(5));
        panel.add(nameLabel);
        
        // 套装描述
        String description = getDescription(suitName);
        JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>");
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        descLabel.setForeground(new Color(255, 255, 200));
        descLabel.setPreferredSize(new Dimension(160, 50));
        descLabel.setMaximumSize(new Dimension(160, 50));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(5));
        panel.add(descLabel);
        
        // 购买/使用按钮
        JButton actionButton = new JButton();
        if (isPurchased) {
            actionButton.setText("✓ 已拥有");
            actionButton.setBackground(new Color(100, 200, 100));
            actionButton.setEnabled(false);
        } else {
            actionButton.setText("💰 购买 (" + SUIT_COST + "元)");
            actionButton.setBackground(new Color(255, 200, 100));
            actionButton.addActionListener(e -> purchaseSuit(suitName));
        }
        actionButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        actionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(10));
        panel.add(actionButton);
        
        suitButtons.put(suitName, actionButton);
        
        return panel;
    }
    
    /**
     * 获取套装描述
     */
    private String getDescription(String suitName) {
        switch (suitName) {
            case "运动风格":
                return "活力四射的运动装扮<br>棒球帽+运动眼镜+运动毛巾";
            case "正式风格":
                return "优雅绅士的正装<br>礼帽+墨镜+丝绸围巾+领带";
            case "混搭风格":
                return "个性十足的混搭风<br>棒球帽+墨镜+运动毛巾+领带";
            default:
                return "";
        }
    }
    
    /**
     * 购买套装
     */
    private void purchaseSuit(String suitName) {
        // 检查金额是否足够
        if (gameFrame.getTotalAmount() < SUIT_COST) {
            JOptionPane.showMessageDialog(this, 
                "金额不足！\n需要: " + SUIT_COST + "元\n当前: " + gameFrame.getTotalAmount() + "元",
                "购买失败", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 确认购买
        int choice = JOptionPane.showConfirmDialog(this,
            "确定要花费 " + SUIT_COST + "元 购买【" + suitName + "】吗？",
            "确认购买", JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            // 扣除金额
            if (gameFrame.deductAmount(SUIT_COST)) {
                // 更新数据库
                if (updatePurchaseStatus(suitName)) {
                    // 更新UI
                    purchasedSuits.put(suitName, true);
                    JButton button = suitButtons.get(suitName);
                    button.setText("✓ 已拥有");
                    button.setBackground(new Color(100, 200, 100));
                    button.setEnabled(false);
                    
                    // 刷新金额显示
                    Component[] components = getContentPane().getComponents();
                    for (Component comp : components) {
                        if (comp instanceof JPanel) {
                            for (Component child : ((JPanel) comp).getComponents()) {
                                if (child instanceof JLabel && ((JLabel) child).getText().contains("当前金额")) {
                                    ((JLabel) child).setText("💰 当前金额: " + gameFrame.getTotalAmount() + "元");
                                    break;
                                }
                            }
                        }
                    }
                    
                    JOptionPane.showMessageDialog(this,
                        "购买成功！\n你现在拥有了【" + suitName + "】！",
                        "购买成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // 如果数据库更新失败，退还金额
                    gameFrame.deductAmount(-SUIT_COST);
                    JOptionPane.showMessageDialog(this,
                        "购买失败，请稍后再试！",
                        "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    /**
     * 更新数据库中的购买状态
     */
    private boolean updatePurchaseStatus(String suitName) {
        try (Connection conn = DriverManager.getConnection(
                GameFrame.DB_URL, GameFrame.DB_USER, GameFrame.DB_PASSWORD)) {
            
            // 检查记录是否存在
            PreparedStatement checkPs = conn.prepareStatement(
                "SELECT COUNT(*) FROM suit_purchases WHERE suit_name = ?");
            checkPs.setString(1, suitName);
            ResultSet rs = checkPs.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            
            if (count == 0) {
                // 插入新记录
                PreparedStatement insertPs = conn.prepareStatement(
                    "INSERT INTO suit_purchases (suit_name, is_purchased, purchase_time) VALUES (?, 1, NOW())");
                insertPs.setString(1, suitName);
                insertPs.executeUpdate();
            } else {
                // 更新现有记录
                PreparedStatement updatePs = conn.prepareStatement(
                    "UPDATE suit_purchases SET is_purchased = 1, purchase_time = NOW() WHERE suit_name = ?");
                updatePs.setString(1, suitName);
                updatePs.executeUpdate();
            }
            
            return true;
        } catch (SQLException e) {
            System.err.println("更新购买状态失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查套装是否已购买
     */
    public static boolean isSuitPurchased(String suitName) {
        try (Connection conn = DriverManager.getConnection(
                GameFrame.DB_URL, GameFrame.DB_USER, GameFrame.DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(
                "SELECT is_purchased FROM suit_purchases WHERE suit_name = ?")) {
            
            ps.setString(1, suitName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("is_purchased");
            }
        } catch (SQLException e) {
            System.err.println("检查购买状态失败: " + e.getMessage());
        }
        return false;
    }
}
