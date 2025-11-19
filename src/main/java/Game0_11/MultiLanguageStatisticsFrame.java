package Game0_11;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

/**
 * 多语言统计结果显示窗口
 */
public class MultiLanguageStatisticsFrame extends JFrame {

    private MultiLanguageAnalyzer analyzer;
    private Map<MultiLanguageAnalyzer.Language, MultiLanguageAnalyzer.LanguageStatistics> languageStats;
    private Map<String, Object> overallStats;
    private String exportFormat;

    public MultiLanguageStatisticsFrame(MultiLanguageAnalyzer analyzer,
                                        Map<MultiLanguageAnalyzer.Language, MultiLanguageAnalyzer.LanguageStatistics> languageStats,
                                        Map<String, Object> overallStats,
                                        String exportFormat) {
        this.analyzer = analyzer;
        this.languageStats = languageStats;
        this.overallStats = overallStats;
        this.exportFormat = exportFormat;

        initializeFrame();
        createComponents();
    }

    private void initializeFrame() {
        setTitle("📊 多语言代码统计结果");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void createComponents() {
        // 创建选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();

        // 概览页签
        tabbedPane.addTab("📊 概览", createOverviewPanel());

        // 语言统计页签
        tabbedPane.addTab("📝 语言统计", createLanguageStatsPanel());

        // 函数统计页签
        tabbedPane.addTab("🔧 函数统计", createFunctionStatsPanel());

        // 详细数据页签
        tabbedPane.addTab("📄 详细数据", createDetailPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // 底部按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportButton = new JButton("💾 导出统计结果");
        exportButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        exportButton.addActionListener(this::exportStatistics);
        buttonPanel.add(exportButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 创建概览面板
     */
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 标题
        JLabel titleLabel = new JLabel("📊 多语言代码分析概览", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 概览卡片面板
        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 20, 20));

        // 总体统计卡片
        cardsPanel.add(createStatCard("支持语言数",
                String.valueOf(overallStats.get("supportedLanguages")),
                new Color(52, 152, 219)));

        cardsPanel.add(createStatCard("源文件总数",
                String.valueOf(overallStats.get("totalSourceFiles")),
                new Color(46, 204, 113)));

        cardsPanel.add(createStatCard("代码总行数",
                String.format("%,d", (Integer) overallStats.get("totalLines")),
                new Color(155, 89, 182)));

        cardsPanel.add(createStatCard("函数总数",
                String.valueOf(overallStats.get("totalFunctions")),
                new Color(230, 126, 34)));

        cardsPanel.add(createStatCard("代码行数",
                String.format("%,d", (Integer) overallStats.get("totalCodeLines")),
                new Color(231, 76, 60)));

        cardsPanel.add(createStatCard("注释行数",
                String.format("%,d", (Integer) overallStats.get("totalCommentLines")),
                new Color(52, 73, 94)));

        panel.add(cardsPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建语言统计面板
     */
    private JPanel createLanguageStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 表格数据
        String[] columns = {"语言", "源文件数", "代码行数", "空行数", "注释行数", "函数个数", "占比"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        int totalLines = (Integer) overallStats.get("totalLines");

        // 按代码行数排序
        languageStats.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().totalLines, e1.getValue().totalLines))
                .forEach(entry -> {
                    MultiLanguageAnalyzer.LanguageStatistics stats = entry.getValue();
                    double percentage = totalLines > 0 ? (stats.totalLines * 100.0 / totalLines) : 0;

                    model.addRow(new Object[]{
                            stats.language.toString(),
                            stats.sourceFiles,
                            String.format("%,d", stats.totalLines),
                            String.format("%,d", stats.emptyLines),
                            String.format("%,d", stats.commentLines),
                            stats.functionCount,
                            String.format("%.2f%%", percentage)
                    });
                });

        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建函数统计面板
     */
    private JPanel createFunctionStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 表格数据
        String[] columns = {"语言", "函数个数", "最大值", "最小值", "平均值", "中位数", "最长函数", "最短函数"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        // 按函数数量排序
        languageStats.entrySet().stream()
                .filter(entry -> entry.getValue().functionCount > 0)
                .sorted((e1, e2) -> Integer.compare(e2.getValue().functionCount, e1.getValue().functionCount))
                .forEach(entry -> {
                    MultiLanguageAnalyzer.LanguageStatistics stats = entry.getValue();

                    String longestFunc = stats.longestFunction != null ?
                            stats.longestFunction.name + " (" + stats.longestFunction.fileName + ")" : "-";
                    String shortestFunc = stats.shortestFunction != null ?
                            stats.shortestFunction.name + " (" + stats.shortestFunction.fileName + ")" : "-";

                    model.addRow(new Object[]{
                            stats.language.toString(),
                            stats.functionCount,
                            stats.maxFunctionLength,
                            stats.minFunctionLength == Integer.MAX_VALUE ? 0 : stats.minFunctionLength,
                            String.format("%.2f", stats.avgFunctionLength),
                            String.format("%.2f", stats.medianFunctionLength),
                            longestFunc,
                            shortestFunc
                    });
                });

        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        table.setRowHeight(25);

        // 设置列宽
        table.getColumnModel().getColumn(6).setPreferredWidth(200);
        table.getColumnModel().getColumn(7).setPreferredWidth(200);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建详细数据面板
     */
    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 表格数据
        String[] columns = {"语言", "文件名", "函数名", "起始行", "结束行", "总行数", "代码行", "空行", "注释行"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        // 添加所有函数详情
        List<MultiLanguageAnalyzer.FunctionInfo> allFunctions = new ArrayList<>();
        for (MultiLanguageAnalyzer.LanguageStatistics stats : languageStats.values()) {
            allFunctions.addAll(stats.functions);
        }

        // 按语言和函数名排序
        allFunctions.sort((f1, f2) -> {
            int langCompare = f1.language.toString().compareTo(f2.language.toString());
            if (langCompare != 0) return langCompare;
            return f1.name.compareTo(f2.name);
        });

        for (MultiLanguageAnalyzer.FunctionInfo func : allFunctions) {
            model.addRow(new Object[]{
                    func.language.toString(),
                    func.fileName,
                    func.name,
                    func.startLine,
                    func.endLine,
                    func.totalLines,
                    func.codeLines,
                    func.emptyLines,
                    func.commentLines
            });
        }

        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 11));
        table.setRowHeight(22);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建统计卡片
     */
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(value, JLabel.CENTER);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    /**
     * 导出统计结果
     */
    private void exportStatistics(ActionEvent e) {
        // 让用户在导出时选择格式，确保 CSV/JSON/XLSX 均可用
        String[] options = {"CSV", "JSON", "XLSX"};
        int choice = JOptionPane.showOptionDialog(this,
                "选择导出格式",
                "导出多语言统计结果",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                exportFormat != null ? exportFormat.toUpperCase() : options[0]);
        if (choice < 0) return;
        String chosenFormat = options[choice].toLowerCase();

        // 显示文件保存对话框（按所选格式过滤）
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存多语言统计结果");

        String extension = chosenFormat.equals("xlsx") ? "xlsx" : chosenFormat;
        fileChooser.setSelectedFile(new java.io.File("multi_language_stats." + extension));

        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        String filePath = fileChooser.getSelectedFile().getAbsolutePath();

        // 在后台线程中执行导出
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                String fmt = chosenFormat.toLowerCase();
                java.util.List<java.util.Map.Entry<MultiLanguageAnalyzer.Language, MultiLanguageAnalyzer.LanguageStatistics>> entries = new java.util.ArrayList<>(languageStats.entrySet());
                entries.sort((a, b) -> a.getKey().toString().compareTo(b.getKey().toString()));

                if ("csv".equals(fmt)) {
                    java.io.PrintWriter writer = null;
                    try {
                        writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                                new java.io.FileOutputStream(filePath), java.nio.charset.StandardCharsets.UTF_8));
                        writer.write('\ufeff');
                        writer.println("语言,源文件数,代码行数,空行数,注释行数,函数个数,最大值,最小值,均值,中位数");
                        for (java.util.Map.Entry<MultiLanguageAnalyzer.Language, MultiLanguageAnalyzer.LanguageStatistics> entry : entries) {
                            MultiLanguageAnalyzer.LanguageStatistics s = entry.getValue();
                            int minLen = (s.minFunctionLength == Integer.MAX_VALUE) ? 0 : s.minFunctionLength;
                            String avg = String.format(java.util.Locale.ROOT, "%.2f", s.avgFunctionLength);
                            String median = String.format(java.util.Locale.ROOT, "%.2f", s.medianFunctionLength);
                            writer.printf(java.util.Locale.ROOT,
                                    "%s,%d,%d,%d,%d,%d,%d,%d,%s,%s%n",
                                    s.language.toString(),
                                    s.sourceFiles,
                                    s.codeLines,
                                    s.emptyLines,
                                    s.commentLines,
                                    s.functionCount,
                                    s.maxFunctionLength,
                                    minLen,
                                    avg,
                                    median);
                        }
                    } finally {
                        if (writer != null) writer.close();
                    }
                    return true;
                } else if ("json".equals(fmt)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("{\n");
                    sb.append("  \"languages\": [\n");
                    for (int i = 0; i < entries.size(); i++) {
                        MultiLanguageAnalyzer.LanguageStatistics s = entries.get(i).getValue();
                        int minLen = (s.minFunctionLength == Integer.MAX_VALUE) ? 0 : s.minFunctionLength;
                        sb.append("    {\n");
                        sb.append("      \"language\": \"").append(s.language.toString()).append("\",\n");
                        sb.append("      \"sourceFiles\": ").append(s.sourceFiles).append(",\n");
                        sb.append("      \"codeLines\": ").append(s.codeLines).append(",\n");
                        sb.append("      \"emptyLines\": ").append(s.emptyLines).append(",\n");
                        sb.append("      \"commentLines\": ").append(s.commentLines).append(",\n");
                        sb.append("      \"functionCount\": ").append(s.functionCount).append(",\n");
                        sb.append("      \"max\": ").append(s.maxFunctionLength).append(",\n");
                        sb.append("      \"min\": ").append(minLen).append(",\n");
                        sb.append("      \"avg\": ").append(String.format(java.util.Locale.ROOT, "%.2f", s.avgFunctionLength)).append(",\n");
                        sb.append("      \"median\": ").append(String.format(java.util.Locale.ROOT, "%.2f", s.medianFunctionLength)).append("\n");
                        sb.append("    }");
                        if (i < entries.size() - 1) sb.append(",");
                        sb.append("\n");
                    }
                    sb.append("  ]\n");
                    sb.append("}\n");
                    java.io.PrintWriter writer = null;
                    try {
                        writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                                new java.io.FileOutputStream(filePath), java.nio.charset.StandardCharsets.UTF_8));
                        writer.print(sb.toString());
                    } finally {
                        if (writer != null) writer.close();
                    }
                    return true;
                } else { // xlsx
                    org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                    org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("语言统计");
                    org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
                    String[] headers = new String[]{"语言", "源文件数", "代码行数", "空行数", "注释行数", "函数个数", "最大值", "最小值", "均值", "中位数"};
                    for (int c = 0; c < headers.length; c++) {
                        org.apache.poi.ss.usermodel.Cell cell = header.createCell(c);
                        cell.setCellValue(headers[c]);
                    }
                    int rowIdx = 1;
                    for (java.util.Map.Entry<MultiLanguageAnalyzer.Language, MultiLanguageAnalyzer.LanguageStatistics> entry : entries) {
                        MultiLanguageAnalyzer.LanguageStatistics s = entry.getValue();
                        int minLen = (s.minFunctionLength == Integer.MAX_VALUE) ? 0 : s.minFunctionLength;
                        org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(s.language.toString());
                        row.createCell(1).setCellValue(s.sourceFiles);
                        row.createCell(2).setCellValue(s.codeLines);
                        row.createCell(3).setCellValue(s.emptyLines);
                        row.createCell(4).setCellValue(s.commentLines);
                        row.createCell(5).setCellValue(s.functionCount);
                        row.createCell(6).setCellValue(s.maxFunctionLength);
                        row.createCell(7).setCellValue(minLen);
                        row.createCell(8).setCellValue(s.avgFunctionLength);
                        row.createCell(9).setCellValue(s.medianFunctionLength);
                    }
                    for (int c = 0; c < headers.length; c++) sheet.autoSizeColumn(c);
                    java.io.FileOutputStream out = null;
                    try {
                        out = new java.io.FileOutputStream(filePath);
                        workbook.write(out);
                    } finally {
                        if (out != null) out.close();
                        workbook.close();
                    }
                    return true;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(MultiLanguageStatisticsFrame.this,
                                "导出成功",
                                "导出结果",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(MultiLanguageStatisticsFrame.this,
                                "导出失败",
                                "导出结果",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MultiLanguageStatisticsFrame.this,
                            "导出失败: " + e.getMessage(),
                            "导出结果",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        // 启动后台任务
        worker.execute();

        // 显示进度提示
        JOptionPane.showMessageDialog(this,
                "正在导出多语言统计结果，请稍候...",
                "导出中",
                JOptionPane.INFORMATION_MESSAGE);
    }
}