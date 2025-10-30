package Game0_10;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * 代码统计图形界面 - 用漂亮的图表展示代码统计结果
 * 包含柱状图、饼状图和详细的统计数据
 */
public class CodeStatsFrame extends JFrame {
    
    private CodeStatistics codeStats;
    private PythonFunctionAnalyzer pythonAnalyzer;
    
    // 颜色方案 - 让图表更好看
    private static final Color[] COLORS = {
        new Color(255, 99, 132),   // 粉红色
        new Color(54, 162, 235),   // 蓝色
        new Color(255, 206, 86),   // 黄色
        new Color(75, 192, 192),   // 青色
        new Color(153, 102, 255),  // 紫色
        new Color(255, 159, 64),   // 橙色
        new Color(199, 199, 199),  // 灰色
        new Color(83, 102, 255),   // 靛蓝色
        new Color(255, 99, 255),   // 品红色
        new Color(99, 255, 132)    // 绿色
    };
    
    public CodeStatsFrame(CodeStatistics codeStats, PythonFunctionAnalyzer pythonAnalyzer) {
        this.codeStats = codeStats;
        this.pythonAnalyzer = pythonAnalyzer;
        
        initUI();
    }
    
    private void initUI() {
        setTitle("📊 代码统计分析器");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 使用选项卡面板,可以切换不同的视图
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // 添加各个统计面板
        tabbedPane.addTab("📊 柱状图", createBarChartPanel());
        tabbedPane.addTab("🥧 饼状图", createPieChartPanel());
        tabbedPane.addTab("🐍 Python函数分析", createPythonAnalysisPanel());
        tabbedPane.addTab("📋 详细数据", createDetailPanel());
        
        add(tabbedPane);
    }
    
    /**
     * 创建柱状图面板
     */
    private JPanel createBarChartPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBarChart(g);
            }
        };
    }
    
    /**
     * 绘制柱状图
     */
    private void drawBarChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Map<String, Integer> data = codeStats.getLanguageLineCount();
        if (data.isEmpty()) {
            g2d.drawString("没有数据", 50, 50);
            return;
        }
        
        // 获取数据并排序
        List<Map.Entry<String, Integer>> sortedData = new ArrayList<>(data.entrySet());
        sortedData.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        
        // 计算图表区域
        int width = getWidth();
        int height = getHeight();
        int margin = 80;
        int chartWidth = width - 2 * margin;
        int chartHeight = height - 2 * margin;
        
        // 绘制标题
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 24));
        g2d.setColor(Color.BLACK);
        String title = "各编程语言代码量统计 (柱状图)";
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, 40);
        
        // 找出最大值,用于缩放
        int maxValue = sortedData.get(0).getValue();
        
        // 计算每个柱子的宽度
        int barCount = sortedData.size();
        int barWidth = Math.max(30, chartWidth / (barCount * 2));
        int spacing = barWidth / 2;
        
        // 绘制坐标轴
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(margin, margin, margin, margin + chartHeight);  // Y轴
        g2d.drawLine(margin, margin + chartHeight, margin + chartWidth, margin + chartHeight);  // X轴
        
        // 绘制Y轴刻度
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        for (int i = 0; i <= 5; i++) {
            int y = margin + chartHeight - (chartHeight * i / 5);
            int value = maxValue * i / 5;
            g2d.drawLine(margin - 5, y, margin, y);
            String label = String.format("%,d", value);
            g2d.drawString(label, margin - 60, y + 5);
        }
        
        // 绘制柱子
        int x = margin + spacing;
        int colorIndex = 0;
        
        for (Map.Entry<String, Integer> entry : sortedData) {
            String language = entry.getKey();
            int lines = entry.getValue();
            
            // 计算柱子高度
            int barHeight = (int) ((double) lines / maxValue * chartHeight);
            int barY = margin + chartHeight - barHeight;
            
            // 绘制柱子
            Color barColor = COLORS[colorIndex % COLORS.length];
            g2d.setColor(barColor);
            g2d.fillRect(x, barY, barWidth, barHeight);
            
            // 绘制柱子边框
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, barY, barWidth, barHeight);
            
            // 绘制数值标签
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 11));
            String valueLabel = String.format("%,d", lines);
            int labelWidth = g2d.getFontMetrics().stringWidth(valueLabel);
            g2d.drawString(valueLabel, x + (barWidth - labelWidth) / 2, barY - 5);
            
            // 绘制语言名称
            g2d.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            Graphics2D g2dRotated = (Graphics2D) g2d.create();
            g2dRotated.rotate(-Math.PI / 4, x + barWidth / 2, margin + chartHeight + 10);
            g2dRotated.drawString(language, x + barWidth / 2, margin + chartHeight + 10);
            g2dRotated.dispose();
            
            x += barWidth + spacing;
            colorIndex++;
        }
        
        // 绘制图例
        drawLegend(g2d, sortedData, width - 200, 100);
    }
    
    /**
     * 创建饼状图面板
     */
    private JPanel createPieChartPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPieChart(g);
            }
        };
    }
    
    /**
     * 绘制饼状图
     */
    private void drawPieChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Map<String, Integer> data = codeStats.getLanguageLineCount();
        if (data.isEmpty()) {
            g2d.drawString("没有数据", 50, 50);
            return;
        }
        
        // 获取数据并排序
        List<Map.Entry<String, Integer>> sortedData = new ArrayList<>(data.entrySet());
        sortedData.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        
        int width = getWidth();
        int height = getHeight();
        
        // 绘制标题
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 24));
        g2d.setColor(Color.BLACK);
        String title = "各编程语言代码量占比 (饼状图)";
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, 40);
        
        // 计算饼图位置和大小
        int pieSize = Math.min(width, height) - 200;
        int pieX = (width - pieSize) / 2 - 100;
        int pieY = (height - pieSize) / 2 + 20;
        
        // 计算总数
        int total = codeStats.getTotalLines();
        
        // 绘制饼图
        int startAngle = 0;
        int colorIndex = 0;
        
        for (Map.Entry<String, Integer> entry : sortedData) {
            String language = entry.getKey();
            int lines = entry.getValue();
            
            // 计算这个扇形的角度
            int angle = (int) Math.round(360.0 * lines / total);
            
            // 绘制扇形
            Color color = COLORS[colorIndex % COLORS.length];
            g2d.setColor(color);
            g2d.fillArc(pieX, pieY, pieSize, pieSize, startAngle, angle);
            
            // 绘制扇形边框
            g2d.setColor(Color.BLACK);
            g2d.drawArc(pieX, pieY, pieSize, pieSize, startAngle, angle);
            
            // 计算百分比标签的位置
            double midAngle = Math.toRadians(startAngle + angle / 2.0);
            int labelRadius = pieSize / 2 + 30;
            int labelX = pieX + pieSize / 2 + (int) (labelRadius * Math.cos(-midAngle));
            int labelY = pieY + pieSize / 2 + (int) (labelRadius * Math.sin(-midAngle));
            
            // 绘制百分比标签
            double percentage = (lines * 100.0) / total;
            if (percentage >= 1.0) {  // 只显示占比大于1%的标签
                g2d.setFont(new Font("微软雅黑", Font.BOLD, 12));
                String label = String.format("%.1f%%", percentage);
                int labelWidth = g2d.getFontMetrics().stringWidth(label);
                g2d.setColor(Color.BLACK);
                g2d.drawString(label, labelX - labelWidth / 2, labelY);
            }
            
            startAngle += angle;
            colorIndex++;
        }
        
        // 绘制图例
        drawLegend(g2d, sortedData, width - 250, 100);
    }
    
    /**
     * 绘制图例
     */
    private void drawLegend(Graphics2D g2d, List<Map.Entry<String, Integer>> data, int x, int y) {
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        int colorIndex = 0;
        int lineHeight = 25;
        
        for (Map.Entry<String, Integer> entry : data) {
            String language = entry.getKey();
            int lines = entry.getValue();
            
            // 绘制颜色方块
            Color color = COLORS[colorIndex % COLORS.length];
            g2d.setColor(color);
            g2d.fillRect(x, y, 15, 15);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, 15, 15);
            
            // 绘制文字
            String label = String.format("%s: %,d 行", language, lines);
            g2d.drawString(label, x + 20, y + 12);
            
            y += lineHeight;
            colorIndex++;
        }
    }
    
    /**
     * 创建Python函数分析面板
     */
    private JPanel createPythonAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建统计信息面板
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 添加标题
        JLabel titleLabel = new JLabel("🐍 Python函数长度统计");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(titleLabel);
        statsPanel.add(Box.createVerticalStrut(20));
        
        // 添加统计数据
        if (pythonAnalyzer.getFunctionCount() > 0) {
            addStatLabel(statsPanel, "函数总数", String.format("%d 个", pythonAnalyzer.getFunctionCount()));
            addStatLabel(statsPanel, "平均长度", String.format("%.2f 行", pythonAnalyzer.getAverage()));
            addStatLabel(statsPanel, "最大长度", String.format("%d 行", pythonAnalyzer.getMax()));
            addStatLabel(statsPanel, "最小长度", String.format("%d 行", pythonAnalyzer.getMin()));
            addStatLabel(statsPanel, "中位数", String.format("%.2f 行", pythonAnalyzer.getMedian()));
        } else {
            JLabel noDataLabel = new JLabel("没有找到Python函数");
            noDataLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            noDataLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            statsPanel.add(noDataLabel);
        }
        
        panel.add(statsPanel, BorderLayout.NORTH);
        
        // 添加函数长度分布图
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawFunctionLengthDistribution(g);
            }
        };
        panel.add(chartPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 添加统计标签
     */
    private void addStatLabel(JPanel panel, String label, String value) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel labelComponent = new JLabel(label + ": ");
        labelComponent.setFont(new Font("微软雅黑", Font.BOLD, 16));
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        valueComponent.setForeground(new Color(0, 102, 204));
        
        rowPanel.add(labelComponent);
        rowPanel.add(valueComponent);
        
        panel.add(rowPanel);
        panel.add(Box.createVerticalStrut(10));
    }
    
    /**
     * 绘制函数长度分布图
     */
    private void drawFunctionLengthDistribution(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        List<Integer> lengths = pythonAnalyzer.getFunctionLengths();
        if (lengths.isEmpty()) {
            return;
        }
        
        int width = getWidth();
        int height = getHeight();
        int margin = 60;
        int chartWidth = width - 2 * margin;
        int chartHeight = height - 2 * margin;
        
        // 创建长度区间统计
        Map<String, Integer> distribution = new TreeMap<>();
        for (int length : lengths) {
            String range;
            if (length <= 10) range = "1-10行";
            else if (length <= 20) range = "11-20行";
            else if (length <= 50) range = "21-50行";
            else if (length <= 100) range = "51-100行";
            else range = "100行以上";
            
            distribution.put(range, distribution.getOrDefault(range, 0) + 1);
        }
        
        // 绘制标题
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 18));
        g2d.setColor(Color.BLACK);
        String title = "函数长度分布";
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, 30);
        
        // 找出最大值
        int maxCount = distribution.values().stream().max(Integer::compare).orElse(1);
        
        // 绘制柱状图
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(distribution.entrySet());
        int barWidth = chartWidth / (entries.size() * 2);
        int x = margin + barWidth / 2;
        
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            String range = entry.getKey();
            int count = entry.getValue();
            
            int barHeight = (int) ((double) count / maxCount * chartHeight);
            int barY = margin + chartHeight - barHeight;
            
            // 绘制柱子
            Color color = COLORS[i % COLORS.length];
            g2d.setColor(color);
            g2d.fillRect(x, barY, barWidth, barHeight);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, barY, barWidth, barHeight);
            
            // 绘制数值
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 12));
            String countStr = String.valueOf(count);
            int labelWidth = g2d.getFontMetrics().stringWidth(countStr);
            g2d.drawString(countStr, x + (barWidth - labelWidth) / 2, barY - 5);
            
            // 绘制范围标签
            g2d.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            labelWidth = g2d.getFontMetrics().stringWidth(range);
            g2d.drawString(range, x + (barWidth - labelWidth) / 2, margin + chartHeight + 20);
            
            x += barWidth * 2;
        }
    }
    
    /**
     * 创建详细数据面板
     */
    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        StringBuilder sb = new StringBuilder();
        sb.append("=" .repeat(80)).append("\n");
        sb.append("📊 代码统计详细报告\n");
        sb.append("=".repeat(80)).append("\n\n");
        
        // 语言统计
        sb.append("【各编程语言代码量统计】\n");
        sb.append("-".repeat(80)).append("\n");
        
        Map<String, Integer> lineCount = codeStats.getLanguageLineCount();
        Map<String, Integer> fileCount = codeStats.getLanguageFileCount();
        
        lineCount.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .forEach(entry -> {
                String language = entry.getKey();
                int lines = entry.getValue();
                int files = fileCount.get(language);
                double percentage = (lines * 100.0) / codeStats.getTotalLines();
                
                sb.append(String.format("%-15s: %,8d 行 (%3d 个文件) - %5.2f%%\n",
                    language, lines, files, percentage));
            });
        
        sb.append("-".repeat(80)).append("\n");
        sb.append(String.format("总计: %,d 行代码,分布在 %d 个文件中\n",
            codeStats.getTotalLines(), codeStats.getTotalFiles()));
        sb.append("\n\n");
        
        // Python函数统计
        if (pythonAnalyzer.getFunctionCount() > 0) {
            sb.append("【Python函数长度统计】\n");
            sb.append("-".repeat(80)).append("\n");
            sb.append(String.format("函数总数: %d\n", pythonAnalyzer.getFunctionCount()));
            sb.append(String.format("平均长度: %.2f 行\n", pythonAnalyzer.getAverage()));
            sb.append(String.format("最大长度: %d 行\n", pythonAnalyzer.getMax()));
            sb.append(String.format("最小长度: %d 行\n", pythonAnalyzer.getMin()));
            sb.append(String.format("中位数: %.2f 行\n", pythonAnalyzer.getMedian()));
        }
        
        sb.append("=".repeat(80)).append("\n");
        
        textArea.setText(sb.toString());
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 主程序入口
     */
    public static void main(String[] args) {
        System.out.println("🚀 代码统计分析器启动中...\n");
        
        // 要分析的文件夹路径
        String folderPath = "/home/mutsumi/Workspace/tankGame/python-ce";
        
        // 创建统计器
        CodeStatistics codeStats = new CodeStatistics();
        PythonFunctionAnalyzer pythonAnalyzer = new PythonFunctionAnalyzer();
        
        // 执行统计
        codeStats.scanFolder(folderPath);
        codeStats.printStatistics();
        
        pythonAnalyzer.scanPythonFiles(folderPath);
        pythonAnalyzer.printStatistics();
        
        // 显示图形界面
        SwingUtilities.invokeLater(() -> {
            CodeStatsFrame frame = new CodeStatsFrame(codeStats, pythonAnalyzer);
            frame.setVisible(true);
            System.out.println("\n✨ 图形界面已打开!你可以切换不同的选项卡查看各种统计图表。");
        });
    }
}
