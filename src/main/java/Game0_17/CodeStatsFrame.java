package Game0_17;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * 代码统计界面 - 显示代码统计结果
 */
public class CodeStatsFrame extends JFrame {
    
    private CodeStatistics codeStats;
    private String scannedPath;
    
    // 专业配色方案
    private static final Color[] CHART_COLORS = {
        new Color(52, 152, 219),   // 蓝色
        new Color(231, 76, 60),    // 红色
        new Color(46, 204, 113),   // 绿色
        new Color(241, 196, 15),   // 黄色
        new Color(155, 89, 182),   // 紫色
        new Color(230, 126, 34),   // 橙色
        new Color(149, 165, 166),  // 灰色
        new Color(26, 188, 156),   // 青色
        new Color(52, 73, 94),     // 深蓝
        new Color(192, 57, 43)     // 深红
    };

    public CodeStatsFrame(GameFrame gameFrame, CodeStatistics codeStats, String scannedPath) {
        this.codeStats = codeStats;
        this.scannedPath = scannedPath;
        
        setTitle("📊 代码统计分析器 - " + new File(scannedPath).getName());
        setSize(1000, 700);
        setLocationRelativeTo(gameFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 使用更专业的选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 添加各个统计面板
        tabbedPane.addTab("📊 总览", createOverviewPanel());
        tabbedPane.addTab("📈 柱状图", createBarChartPanel());
        tabbedPane.addTab("🥧 饼图", createPieChartPanel());
        tabbedPane.addTab("📋 详细数据", createDetailPanel());
        tabbedPane.addTab("💻 代码质量", createCodeQualityPanel());

        // 底部按钮面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        JButton rescanBtn = new JButton("🔄 重新扫描");
        rescanBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        rescanBtn.addActionListener(e -> {
            dispose();
            // 清空缓存，强制重新扫描
            CodeStatsSystem system = new CodeStatsSystem();
            system.showStatsDialog(gameFrame);
        });
        
        JButton backBtn = new JButton("🎮 返回游戏");
        backBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        backBtn.addActionListener(e -> dispose());
        
        bottomPanel.add(rescanBtn);
        bottomPanel.add(backBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    /**
     * 创建总览面板
     */
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 标题
        JLabel titleLabel = new JLabel("📊 代码统计总览");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(titleLabel, gbc);
        
        // 扫描路径
        JLabel pathLabel = new JLabel("扫描路径: " + scannedPath);
        pathLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridy = 1;
        panel.add(pathLabel, gbc);
        
        // 统计卡片
        gbc.gridwidth = 1;
        gbc.gridy = 2;
        
        // 总文件数卡片
        gbc.gridx = 0;
        panel.add(createStatCard("📁 总文件数", 
            String.format("%,d", codeStats.getTotalFiles()), 
            new Color(52, 152, 219)), gbc);
        
        // 总代码行数卡片
        gbc.gridx = 1;
        panel.add(createStatCard("💻 总代码行数", 
            String.format("%,d", codeStats.getTotalCodeLines()), 
            new Color(46, 204, 113)), gbc);
        
        gbc.gridy = 3;
        
        // 总行数卡片
        gbc.gridx = 0;
        panel.add(createStatCard("📝 总行数", 
            String.format("%,d", codeStats.getTotalLines()), 
            new Color(241, 196, 15)), gbc);
        
        // 空行和注释卡片
        gbc.gridx = 1;
        panel.add(createStatCard("💬 注释行数", 
            String.format("%,d", codeStats.getTotalCommentLines()), 
            new Color(155, 89, 182)), gbc);
        
        return panel;
    }
    
    /**
     * 创建统计卡片
     */
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        card.setPreferredSize(new Dimension(200, 100));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        
        return card;
    }
    
    /**
     * 创建柱状图面板
     */
    private JPanel createBarChartPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setBackground(Color.WHITE);
                drawEnhancedBarChart((Graphics2D) g);
            }
        };
    }
    
    /**
     * 创建饼图面板
     */
    private JPanel createPieChartPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setBackground(Color.WHITE);
                drawEnhancedPieChart((Graphics2D) g);
            }
        };
    }
    
    /**
     * 创建详细数据面板
     */
    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // 创建表格数据
        String[] columnNames = {"编程语言", "文件数", "总行数", "代码行数", "注释行数", "占比"};
        Map<String, Integer> languageLines = codeStats.getLanguageLineCount();
        Map<String, Integer> languageFiles = codeStats.getLanguageFileCount();
        Map<String, Integer> languageCode = codeStats.getLanguageCodeLines();
        
        // 排序语言列表
        List<String> sortedLanguages = new ArrayList<>(languageLines.keySet());
        sortedLanguages.sort((a, b) -> languageLines.get(b).compareTo(languageLines.get(a)));
        
        Object[][] data = new Object[sortedLanguages.size()][6];
        int totalLines = codeStats.getTotalLines();
        
        for (int i = 0; i < sortedLanguages.size(); i++) {
            String lang = sortedLanguages.get(i);
            int lines = languageLines.get(lang);
            int files = languageFiles.get(lang);
            int codeLines = languageCode.getOrDefault(lang, lines);
            double percentage = (lines * 100.0) / totalLines;
            
            data[i] = new Object[]{
                lang,
                files,
                String.format("%,d", lines),
                String.format("%,d", codeLines),
                String.format("%,d", lines - codeLines),
                String.format("%.2f%%", percentage)
            };
        }
        
        JTable table = new JTable(data, columnNames);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.setRowHeight(30);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建代码质量面板
     */
    private JPanel createCodeQualityPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 标题
        JLabel titleLabel = new JLabel("💻 代码质量分析");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));
        
        // 代码质量指标
        double commentRatio = (codeStats.getTotalCommentLines() * 100.0) / codeStats.getTotalLines();
        double codeRatio = (codeStats.getTotalCodeLines() * 100.0) / codeStats.getTotalLines();
        double avgLinesPerFile = codeStats.getTotalFiles() > 0 ? 
            (double) codeStats.getTotalLines() / codeStats.getTotalFiles() : 0;
        
        addQualityMetric(panel, "📊 代码密度", String.format("%.1f%%", codeRatio), 
            "纯代码行占总行数的比例");
        addQualityMetric(panel, "💬 注释率", String.format("%.1f%%", commentRatio), 
            "注释行占总行数的比例");
        addQualityMetric(panel, "📄 平均文件大小", String.format("%.1f 行", avgLinesPerFile), 
            "每个文件的平均行数");
        
        // 建议
        panel.add(Box.createVerticalStrut(30));
        JLabel suggestionTitle = new JLabel("💡 代码质量建议");
        suggestionTitle.setFont(new Font("微软雅黑", Font.BOLD, 16));
        panel.add(suggestionTitle);
        panel.add(Box.createVerticalStrut(10));
        
        if (commentRatio < 10) {
            addSuggestion(panel, "⚠️ 注释率偏低，建议增加代码注释以提高可维护性");
        } else if (commentRatio > 30) {
            addSuggestion(panel, "💭 注释率较高，确保注释内容有价值且及时更新");
        } else {
            addSuggestion(panel, "✅ 注释率适中，保持良好的注释习惯");
        }
        
        if (avgLinesPerFile > 500) {
            addSuggestion(panel, "📏 平均文件较大，考虑将大文件拆分以提高可读性");
        } else {
            addSuggestion(panel, "✅ 文件大小适中，模块划分合理");
        }
        
        return panel;
    }
    
    private void addQualityMetric(JPanel panel, String name, String value, String description) {
        JPanel metricPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        metricPanel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(name + ": ");
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        valueLabel.setForeground(new Color(52, 152, 219));
        
        JLabel descLabel = new JLabel(" - " + description);
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        descLabel.setForeground(Color.GRAY);
        
        metricPanel.add(nameLabel);
        metricPanel.add(valueLabel);
        metricPanel.add(descLabel);
        
        panel.add(metricPanel);
    }
    
    private void addSuggestion(JPanel panel, String suggestion) {
        JLabel label = new JLabel(suggestion);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        label.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        panel.add(label);
    }
    
    /**
     * 绘制增强版柱状图
     */
    private void drawEnhancedBarChart(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 获取真实数据
        Map<String, Integer> languageLines = codeStats.getLanguageLineCount();
        if (languageLines.isEmpty()) {
            g.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            g.drawString("暂无数据", getWidth() / 2 - 40, getHeight() / 2);
            return;
        }
        
        // 排序并限制显示前10个
        List<Map.Entry<String, Integer>> sortedData = new ArrayList<>(languageLines.entrySet());
        sortedData.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        if (sortedData.size() > 10) {
            sortedData = sortedData.subList(0, 10);
        }
        
        int x = 100;
        int y = 400;
        int barWidth = 80;
        int maxHeight = 300;

        // 找出最大值用于缩放
        int maxValue = sortedData.get(0).getValue();
        
        // 绘制标题
        g.setFont(new Font("微软雅黑", Font.BOLD, 20));
        g.setColor(Color.BLACK);
        g.drawString("各编程语言代码量统计", getWidth() / 2 - 120, 40);
        
        // 绘制坐标轴
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawLine(80, y, getWidth() - 80, y);  // X轴
        g.drawLine(80, 100, 80, y); // Y轴
        
        // 绘制Y轴刻度
        g.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        for (int i = 0; i <= 5; i++) {
            int yPos = y - (maxHeight * i / 5);
            int value = maxValue * i / 5;
            g.drawLine(75, yPos, 80, yPos);
            g.drawString(String.format("%,d", value), 20, yPos + 5);
        }

        // 绘制柱状图
        int colorIndex = 0;
        for (Map.Entry<String, Integer> entry : sortedData) {
            String language = entry.getKey();
            int lines = entry.getValue();
            int barHeight = (int)((double)lines / maxValue * maxHeight);
            
            // 使用预定义的颜色
            g.setColor(CHART_COLORS[colorIndex % CHART_COLORS.length]);
            g.fillRect(x, y - barHeight, barWidth, barHeight);
            
            // 绘制边框
            g.setColor(Color.BLACK);
            g.drawRect(x, y - barHeight, barWidth, barHeight);
            
            // 绘制语言名称（旋转45度）
            Graphics2D g2 = (Graphics2D) g.create();
            g2.rotate(-Math.PI / 4, x + barWidth / 2, y + 10);
            g2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            g2.drawString(language, x + barWidth / 2 - 20, y + 10);
            g2.dispose();
            
            // 绘制数值
            g.setFont(new Font("微软雅黑", Font.BOLD, 11));
            String valueStr = String.format("%,d", lines);
            int strWidth = g.getFontMetrics().stringWidth(valueStr);
            g.drawString(valueStr, x + (barWidth - strWidth) / 2, y - barHeight - 5);
            
            x += barWidth + 20;
            colorIndex++;
        }
    }
    
    /**
     * 绘制增强版饼图
     */
    private void drawEnhancedPieChart(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 获取真实数据
        Map<String, Integer> languageLines = codeStats.getLanguageLineCount();
        if (languageLines.isEmpty()) {
            g.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            g.drawString("暂无数据", getWidth() / 2 - 40, getHeight() / 2);
            return;
        }
        
        // 排序数据
        List<Map.Entry<String, Integer>> sortedData = new ArrayList<>(languageLines.entrySet());
        sortedData.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        // 计算总量
        int total = codeStats.getTotalLines();
        
        // 绘制标题
        g.setFont(new Font("微软雅黑", Font.BOLD, 20));
        g.setColor(Color.BLACK);
        g.drawString("各编程语言占比分布", getWidth() / 2 - 100, 40);
        
        // 饼图参数
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2 + 20;
        int radius = 150;
        
        // 绘制饼图
        float startAngle = 0;
        int colorIndex = 0;
        List<String> legendLabels = new ArrayList<>();
        List<Color> legendColors = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : sortedData) {
            String language = entry.getKey();
            int lines = entry.getValue();
            float percentage = (lines * 100.0f) / total;
            float angle = percentage * 360 / 100;
            
            // 只显示占比大于1%的语言
            if (percentage >= 1.0) {
                Color color = CHART_COLORS[colorIndex % CHART_COLORS.length];
                
                // 绘制扇形
                g.setColor(color);
                g.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2,
                        (int) startAngle, (int) angle);
                
                // 绘制边框
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(1));
                g.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2,
                        (int) startAngle, (int) angle);
                
                // 添加到图例
                legendLabels.add(String.format("%s %.1f%%", language, percentage));
                legendColors.add(color);
                
                startAngle += angle;
                colorIndex++;
            }
        }
        
        // 绘制图例
        int legendX = 50;
        int legendY = 100;
        g.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        for (int i = 0; i < legendLabels.size() && i < 10; i++) {
            g.setColor(legendColors.get(i));
            g.fillRect(legendX, legendY + i * 25, 15, 15);
            g.setColor(Color.BLACK);
            g.drawRect(legendX, legendY + i * 25, 15, 15);
            g.drawString(legendLabels.get(i), legendX + 20, legendY + i * 25 + 12);
        }
    }
}
