package Game0_17;

import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.net.URL;
import java.io.IOException;

/**
 * 装扮系统 - 管理唐老鸭的换装功能
 */
public class DressUpSystem implements GameFrame.DressUpSystem {
    
    private String currentStyle = "默认装扮";
    
    // 换装风格图片
    private Image sportStyleImg;    // 运动风格图片
    private Image formalStyleImg;   // 正式风格图片
    private Image mixedStyleImg;    // 混搭风格图片
    private Image donaldImg;        // 默认唐老鸭图片
    
    public DressUpSystem(DonaldDuck donaldDuck) {
        // donaldDuck 参数保留以备将来使用
        loadDressImages();
    }
    
    /**
     * 加载换装图片资源
     */
    private void loadDressImages() {
        try {
            donaldImg = loadImage("/images/duck.jpg");
            sportStyleImg = loadImage("/images/yd_style.jpeg");
            formalStyleImg = loadImage("/images/zs_style.jpeg");
            mixedStyleImg = loadImage("/images/hd_style.jpeg");
        } catch (Exception e) {
            System.err.println("换装图片加载失败: " + e.getMessage());
        }
    }
    
    private Image loadImage(String imagePath) {
        URL url = getClass().getResource(imagePath);
        if (url != null) {
            try {
                return ImageIO.read(url);
            } catch (IOException e) {
                System.err.println("无法加载图片: " + imagePath);
            }
        }
        return null;
    }
    
    @Override
    public String getCurrentStyle() {
        return currentStyle;
    }
    
    @Override
    public boolean purchaseDress(String dressName, int cost, GameFrame game) {
        // 这个方法暂时不使用，购买逻辑在 DressShopDialog 中处理
        return false;
    }
    
    @Override
    public Image getDonaldImage() {
        // 根据当前风格返回对应的图片
        switch (currentStyle) {
            case "正式风格":
                return formalStyleImg != null ? formalStyleImg : donaldImg;
            case "运动风格":
                return sportStyleImg != null ? sportStyleImg : donaldImg;
            case "混搭风格":
                return mixedStyleImg != null ? mixedStyleImg : donaldImg;
            default:
                return donaldImg;
        }
    }
    
    @Override
    public void showDressUpDialog(GameFrame parent) {
        JDialog dressUpDialog = new JDialog(parent, "唐老鸭换装系统", true);
        dressUpDialog.setSize(500, 450);
        dressUpDialog.setLocationRelativeTo(parent);
        dressUpDialog.setLayout(new BorderLayout());
        
        // 风格选择面板
        JPanel stylePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 60));
        
        Button defaultBtn = new Button("默认装扮");
        Button formalBtn = new Button("正式风格");
        Button sportBtn = new Button("运动风格");
        Button mixedBtn = new Button("混搭风格");
        
        defaultBtn.setPreferredSize(new Dimension(100, 40));
        formalBtn.setPreferredSize(new Dimension(100, 40));
        sportBtn.setPreferredSize(new Dimension(100, 40));
        mixedBtn.setPreferredSize(new Dimension(100, 40));
        
        // 默认装扮
        defaultBtn.addActionListener(e -> {
            currentStyle = "默认装扮";
            JOptionPane.showMessageDialog(parent, "装扮完成：默认装扮", "换装成功", JOptionPane.INFORMATION_MESSAGE);
            dressUpDialog.dispose();
            parent.repaint();
        });
        
        // 正式风格
        formalBtn.addActionListener(e -> {
            if (DressShopDialog.isSuitPurchased("正式风格")) {
                currentStyle = "正式风格";
                JOptionPane.showMessageDialog(parent, "装扮完成：正式风格 - 礼帽+墨镜+丝绸围巾+领带+名表", "换装成功", JOptionPane.INFORMATION_MESSAGE);
                dressUpDialog.dispose();
                parent.repaint();
            } else {
                JOptionPane.showMessageDialog(parent, "你还没有购买这个套装！\n请先去商店购买。", "无法使用", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        // 运动风格
        sportBtn.addActionListener(e -> {
            if (DressShopDialog.isSuitPurchased("运动风格")) {
                currentStyle = "运动风格";
                JOptionPane.showMessageDialog(parent, "装扮完成：运动风格 - 棒球帽+运动眼镜+运动毛巾+运动手表", "换装成功", JOptionPane.INFORMATION_MESSAGE);
                dressUpDialog.dispose();
                parent.repaint();
            } else {
                JOptionPane.showMessageDialog(parent, "你还没有购买这个套装！\n请先去商店购买。", "无法使用", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        // 混搭风格
        mixedBtn.addActionListener(e -> {
            if (DressShopDialog.isSuitPurchased("混搭风格")) {
                currentStyle = "混搭风格";
                JOptionPane.showMessageDialog(parent, "装扮完成：混搭风格 - 棒球帽+墨镜+运动毛巾+领带+名表", "换装成功", JOptionPane.INFORMATION_MESSAGE);
                dressUpDialog.dispose();
                parent.repaint();
            } else {
                JOptionPane.showMessageDialog(parent, "你还没有购买这个套装！\n请先去商店购买。", "无法使用", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        stylePanel.add(defaultBtn);
        stylePanel.add(formalBtn);
        stylePanel.add(sportBtn);
        stylePanel.add(mixedBtn);
        
        // 底部按钮面板 - 添加商店入口
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        
        // 商店按钮 - 放在右下角
        Button shopBtn = new Button("🛍️ 进入商店");
        shopBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        shopBtn.setBackground(new Color(255, 200, 100));
        shopBtn.addActionListener(e -> {
            dressUpDialog.dispose();
            new DressShopDialog(parent, parent);
        });
        
        Button backBtn = new Button("返回游戏");
        backBtn.addActionListener(e -> dressUpDialog.dispose());
        
        bottomPanel.add(backBtn);
        bottomPanel.add(shopBtn);
        
        // 添加当前装扮显示
        JPanel topPanel = new JPanel();
        JLabel currentStyleLabel = new JLabel("当前装扮：" + currentStyle);
        currentStyleLabel.setFont(new Font("宋体", Font.BOLD, 16));
        topPanel.add(currentStyleLabel);
        
        dressUpDialog.add(topPanel, BorderLayout.NORTH);
        dressUpDialog.add(stylePanel, BorderLayout.CENTER);
        dressUpDialog.add(bottomPanel, BorderLayout.SOUTH);
        dressUpDialog.setVisible(true);
    }
}
