package Game0_11;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.SwingWorker;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * 统计结果显示窗口 - 用漂亮的图表和表格展示代码分析结果
 * 就像一个"成绩展示板"，让统计数据一目了然
 */
public class StatisticsFrame extends JFrame {
    
    private CFunctionAnalyzer analyzer;
    private CFunctionStatistics statistics;
    private List<CFunctionAnalyzer.FunctionInfo> functions;
    private String exportFormat;  // 导出格式
    
    // 颜色方案
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    
    public StatisticsFrame(CFunctionAnalyzer analyzer, CFunctionStatistics statistics, String exportFormat) {
        this.analyzer = analyzer;
        this.statistics = statistics;
        this.functions = analyzer.getFunctions();
        this.exportFormat = exportFormat;
        
        initUI();
    }
    
    private void initUI() {
        setTitle("📊 C语言函数统计分析结果");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // 使用选项卡展示不同的视图
        JTabbedPane tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("📈 统计概览", createOverviewPanel());
        tabbedPane.addTab("📊 函数分布图", createChartPanel());
        tabbedPane.addTab("📋 函数详情", createDetailPanel());
        tabbedPane.addTab("💡 代码建议", createAdvicePanel());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // 添加导出按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportButton = new JButton("💾 导出统计结果");
        exportButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        exportButton.addActionListener(e -> exportStatistics());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(exportButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    /**
     * 导出统计结果
     */
    private void exportStatistics() {
        // 显示文件保存对话框
        String filePath = ExportUtil.showSaveDialog(this, exportFormat);
        
        if (filePath != null) {
            // 在后台线程中执行导出
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return ExportUtil.exportStatistics(analyzer, statistics, exportFormat, filePath);
                }
                
                @Override
                protected void done() {
                    try {
                        Boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(StatisticsFrame.this,
                                "统计结果已成功导出到:\n" + filePath,
                                "导出成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(StatisticsFrame.this,
                            "导出失败:\n" + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
            
            // 显示进度提示
            JOptionPane.showMessageDialog(this,
                "正在导出统计结果，请稍候...",
                "导出中",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * 创建统计概览面板
     */
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 创建统计卡片面板
        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        
        // 函数总数卡片
        cardsPanel.add(createStatCard("函数总数", 
            String.valueOf(statistics.totalFunctions), 
            "个函数", PRIMARY_COLOR));
        
        // 平均长度卡片
        cardsPanel.add(createStatCard("平均长度", 
            String.format("%.1f", statistics.avgLength), 
            "行/函数", SUCCESS_COLOR));
        
        // 最长函数卡片
        cardsPanel.add(createStatCard("最长函数", 
            String.valueOf(statistics.maxLength), 
            "行", WARNING_COLOR));
        
        // 中位数卡片
        cardsPanel.add(createStatCard("中位数", 
            String.format("%.1f", statistics.medianLength), 
            "行", PRIMARY_COLOR));
        
        panel.add(cardsPanel, BorderLayout.CENTER);
        
        // 添加详细信息面板
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBorder(BorderFactory.createTitledBorder("📝 详细信息"));
        
        if (statistics.longestFunction != null) {
            detailPanel.add(new JLabel(String.format(
                "🔴 最长函数: %s (%d行) - 文件: %s",
                statistics.longestFunction.name,
                statistics.maxLength,
                statistics.longestFunction.fileName
            )));
        }
        
        if (statistics.shortestFunction != null) {
            detailPanel.add(Box.createVerticalStrut(10));
            detailPanel.add(new JLabel(String.format(
                "🟢 最短函数: %s (%d行) - 文件: %s",
                statistics.shortestFunction.name,
                statistics.minLength,
                statistics.shortestFunction.fileName
            )));
        }
        
        panel.add(detailPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 创建统计卡片
     */
    private JPanel createStatCard(String title, String value, String unit, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 36));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel unitLabel = new JLabel(unit);
        unitLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        unitLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(unitLabel);
        
        return card;
    }
    
    /**
     * 创建函数分布图面板
     */
    private JPanel createChartPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawDistributionChart(g);
            }
        };
    }
    
    /**
     * 绘制函数长度分布图
     */
    private void drawDistributionChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int margin = 60;
        
        // 绘制标题
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 20));
        String title = "函数长度分布直方图";
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, 40);
        
        if (functions.isEmpty()) {
            g2d.drawString("没有数据", width / 2 - 30, height / 2);
            return;
        }
        
        // 创建长度分布统计
        Map<String, Integer> distribution = new TreeMap<>();
        for (CFunctionAnalyzer.FunctionInfo func : functions) {
            int length = func.totalLines;
            String range;
            if (length <= 10) range = "1-10行";
            else if (length <= 20) range = "11-20行";
            else if (length <= 30) range = "21-30行";
            else if (length <= 50) range = "31-50行";
            else if (length <= 100) range = "51-100行";
            else range = "100+行";
            
            distribution.put(range, distribution.getOrDefault(range, 0) + 1);
        }
        
        // 绘制直方图
        int chartWidth = width - 2 * margin;
        int chartHeight = height - 2 * margin;
        int barWidth = chartWidth / distribution.size();
        int maxCount = Collections.max(distribution.values());
        
        int x = margin;
        int colorIndex = 0;
        Color[] barColors = {SUCCESS_COLOR, PRIMARY_COLOR, WARNING_COLOR, DANGER_COLOR};
        
        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            int barHeight = (int) ((double) entry.getValue() / maxCount * chartHeight * 0.8);
            int y = height - margin - barHeight;
            
            // 绘制柱子
            g2d.setColor(barColors[colorIndex % barColors.length]);
            g2d.fillRect(x, y, barWidth - 10, barHeight);
            
            // 绘制数值
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 14));
            String count = String.valueOf(entry.getValue());
            int countWidth = g2d.getFontMetrics().stringWidth(count);
            g2d.drawString(count, x + (barWidth - 10 - countWidth) / 2, y - 5);
            
            // 绘制标签
            g2d.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            g2d.drawString(entry.getKey(), x, height - margin + 20);
            
            x += barWidth;
            colorIndex++;
        }
        
        // 绘制坐标轴
        g2d.setColor(Color.BLACK);
        g2d.drawLine(margin, height - margin, width - margin, height - margin); // X轴
        g2d.drawLine(margin, margin, margin, height - margin); // Y轴
    }
    
    /**
     * 创建函数详情面板
     */
    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建表格模型
        String[] columnNames = {"函数名", "文件名", "总行数", "代码行", "空行", "注释行", "开始行", "结束行"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // 填充数据
        for (CFunctionAnalyzer.FunctionInfo func : functions) {
            model.addRow(new Object[]{
                func.name,
                func.fileName,
                func.totalLines,
                func.codeLines,
                func.emptyLines,
                func.commentLines,
                func.startLine,
                func.endLine
            });
        }
        
        // 创建表格
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        
        // 设置列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(150); // 函数名
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // 文件名
        
        // 根据行数设置行颜色
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, 
                    isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    int totalLines = (int) table.getValueAt(row, 2);
                    if (totalLines > 100) {
                        c.setBackground(new Color(255, 230, 230)); // 淡红色
                    } else if (totalLines > 50) {
                        c.setBackground(new Color(255, 250, 230)); // 淡黄色
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 添加提示信息
        JLabel tipLabel = new JLabel("💡 提示: 点击列标题可以排序，红色背景表示函数过长需要重构");
        tipLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(tipLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 创建代码建议面板
     */
    private JPanel createAdvicePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextArea adviceArea = new JTextArea();
        adviceArea.setEditable(false);
        adviceArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        adviceArea.setLineWrap(true);
        adviceArea.setWrapStyleWord(true);
        
        // 生成建议内容
        StringBuilder advice = new StringBuilder();
        advice.append(statistics.getSummary()).append("\n\n");
        advice.append(statistics.getHealthAdvice()).append("\n\n");
        
        // 添加具体的重构建议
        advice.append("📌 具体建议:\n\n");
        
        int longFunctionCount = 0;
        for (CFunctionAnalyzer.FunctionInfo func : functions) {
            if (func.totalLines > 50) {
                longFunctionCount++;
                if (longFunctionCount <= 5) { // 只显示前5个需要重构的函数
                    advice.append(String.format("• 函数 '%s' (文件: %s) 有 %d 行，建议拆分成更小的函数\n",
                        func.name, func.fileName, func.totalLines));
                }
            }
        }
        
        if (longFunctionCount > 5) {
            advice.append(String.format("• ... 还有 %d 个函数需要重构\n", longFunctionCount - 5));
        }
        
        if (longFunctionCount == 0) {
            advice.append("✅ 太棒了！所有函数的长度都控制在合理范围内。\n");
        }
        
        adviceArea.setText(advice.toString());
        adviceArea.setCaretPosition(0);
        
        JScrollPane scrollPane = new JScrollPane(adviceArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
}
