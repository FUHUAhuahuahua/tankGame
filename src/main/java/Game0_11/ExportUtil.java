package Game0_11;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 导出工具类 - 负责将统计结果导出为不同格式的文件
 * 支持 CSV、JSON、XLSX 格式
 */
public class ExportUtil {
    
    /**
     * 导出统计结果到文件
     * @param analyzer 函数分析器
     * @param statistics 统计数据
     * @param format 文件格式（csv, json, xlsx）
     * @param filePath 保存的文件路径
     * @return 是否导出成功
     */
    public static boolean exportStatistics(
            CFunctionAnalyzer analyzer,
            CFunctionStatistics statistics,
            String format,
            String filePath) {
        
        try {
            switch (format.toLowerCase()) {
                case "csv":
                    return exportToCSV(analyzer, statistics, filePath);
                case "json":
                    return exportToJSON(analyzer, statistics, filePath);
                case "xlsx":
                    return exportToXLSX(analyzer, statistics, filePath);
                default:
                    JOptionPane.showMessageDialog(null,
                        "不支持的导出格式: " + format,
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                    return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "导出文件时出错:\n" + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 导出为CSV格式
     */
    private static boolean exportToCSV(CFunctionAnalyzer analyzer,
                                      CFunctionStatistics statistics,
                                      String filePath) throws IOException {
        List<CFunctionAnalyzer.FunctionInfo> functions = analyzer.getFunctions();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, false))) {
            // 写入BOM，确保Excel正确识别UTF-8
            writer.write('\ufeff');
            
            // 写入统计摘要
            writer.println("C语言函数统计分析结果");
            writer.println("=".repeat(50));
            writer.println("函数总数," + statistics.totalFunctions);
            writer.println("平均长度," + String.format("%.2f", statistics.avgLength));
            writer.println("中位数长度," + String.format("%.2f", statistics.medianLength));
            writer.println("最短函数," + statistics.minLength);
            writer.println("最长函数," + statistics.maxLength);
            writer.println();
            
            // 写入函数详情表头
            writer.println("函数名,文件名,总行数,代码行,空行,注释行,开始行,结束行");
            
            // 写入函数详情数据
            for (CFunctionAnalyzer.FunctionInfo func : functions) {
                writer.printf("%s,%s,%d,%d,%d,%d,%d,%d%n",
                    escapeCSV(func.name),
                    escapeCSV(func.fileName),
                    func.totalLines,
                    func.codeLines,
                    func.emptyLines,
                    func.commentLines,
                    func.startLine,
                    func.endLine
                );
            }
        }
        
        return true;
    }
    
    /**
     * 导出为JSON格式
     */
    private static boolean exportToJSON(CFunctionAnalyzer analyzer,
                                       CFunctionStatistics statistics,
                                       String filePath) throws IOException {
        List<CFunctionAnalyzer.FunctionInfo> functions = analyzer.getFunctions();
        
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        // 统计摘要
        json.append("  \"statistics\": {\n");
        json.append("    \"totalFunctions\": ").append(statistics.totalFunctions).append(",\n");
        json.append("    \"averageLength\": ").append(String.format("%.2f", statistics.avgLength)).append(",\n");
        json.append("    \"medianLength\": ").append(String.format("%.2f", statistics.medianLength)).append(",\n");
        json.append("    \"minLength\": ").append(statistics.minLength).append(",\n");
        json.append("    \"maxLength\": ").append(statistics.maxLength).append(",\n");
        
        if (statistics.longestFunction != null) {
            json.append("    \"longestFunction\": {\n");
            json.append("      \"name\": \"").append(escapeJSON(statistics.longestFunction.name)).append("\",\n");
            json.append("      \"fileName\": \"").append(escapeJSON(statistics.longestFunction.fileName)).append("\",\n");
            json.append("      \"length\": ").append(statistics.maxLength).append("\n");
            json.append("    },\n");
        }
        
        if (statistics.shortestFunction != null) {
            json.append("    \"shortestFunction\": {\n");
            json.append("      \"name\": \"").append(escapeJSON(statistics.shortestFunction.name)).append("\",\n");
            json.append("      \"fileName\": \"").append(escapeJSON(statistics.shortestFunction.fileName)).append("\",\n");
            json.append("      \"length\": ").append(statistics.minLength).append("\n");
            json.append("    },\n");
        }
        
        json.deleteCharAt(json.length() - 2); // 删除最后一个逗号
        json.append("  },\n");
        
        // 函数列表
        json.append("  \"functions\": [\n");
        for (int i = 0; i < functions.size(); i++) {
            CFunctionAnalyzer.FunctionInfo func = functions.get(i);
            json.append("    {\n");
            json.append("      \"name\": \"").append(escapeJSON(func.name)).append("\",\n");
            json.append("      \"fileName\": \"").append(escapeJSON(func.fileName)).append("\",\n");
            json.append("      \"totalLines\": ").append(func.totalLines).append(",\n");
            json.append("      \"codeLines\": ").append(func.codeLines).append(",\n");
            json.append("      \"emptyLines\": ").append(func.emptyLines).append(",\n");
            json.append("      \"commentLines\": ").append(func.commentLines).append(",\n");
            json.append("      \"startLine\": ").append(func.startLine).append(",\n");
            json.append("      \"endLine\": ").append(func.endLine).append("\n");
            json.append("    }");
            if (i < functions.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ]\n");
        json.append("}\n");
        
        Files.write(Paths.get(filePath), json.toString().getBytes("UTF-8"));
        return true;
    }
    
    /**
     * 导出为XLSX格式（Excel）
     */
    private static boolean exportToXLSX(CFunctionAnalyzer analyzer,
                                       CFunctionStatistics statistics,
                                       String filePath) throws IOException {
        List<CFunctionAnalyzer.FunctionInfo> functions = analyzer.getFunctions();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("0"));
            
            // 创建统计摘要工作表
            Sheet summarySheet = workbook.createSheet("统计摘要");
            int rowNum = 0;
            
            Row titleRow = summarySheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("C语言函数统计分析结果");
            titleCell.setCellStyle(headerStyle);
            
            rowNum++; // 空行
            
            String[] summaryLabels = {
                "函数总数", "平均长度", "中位数长度", "最短函数", "最长函数"
            };
            String[] summaryValues = {
                String.valueOf(statistics.totalFunctions),
                String.format("%.2f", statistics.avgLength),
                String.format("%.2f", statistics.medianLength),
                String.valueOf(statistics.minLength),
                String.valueOf(statistics.maxLength)
            };
            
            for (int i = 0; i < summaryLabels.length; i++) {
                Row row = summarySheet.createRow(rowNum++);
                Cell labelCell = row.createCell(0);
                labelCell.setCellValue(summaryLabels[i]);
                labelCell.setCellStyle(headerStyle);
                
                Cell valueCell = row.createCell(1);
                valueCell.setCellValue(summaryValues[i]);
            }
            
            // 自动调整列宽
            summarySheet.autoSizeColumn(0);
            summarySheet.autoSizeColumn(1);
            
            // 创建函数详情工作表
            Sheet detailSheet = workbook.createSheet("函数详情");
            rowNum = 0;
            
            // 创建表头
            Row headerRow = detailSheet.createRow(rowNum++);
            String[] headers = {"函数名", "文件名", "总行数", "代码行", "空行", "注释行", "开始行", "结束行"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            for (CFunctionAnalyzer.FunctionInfo func : functions) {
                Row row = detailSheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(func.name);
                row.createCell(1).setCellValue(func.fileName);
                
                Cell totalLinesCell = row.createCell(2);
                totalLinesCell.setCellValue(func.totalLines);
                totalLinesCell.setCellStyle(numberStyle);
                
                Cell codeLinesCell = row.createCell(3);
                codeLinesCell.setCellValue(func.codeLines);
                codeLinesCell.setCellStyle(numberStyle);
                
                Cell emptyLinesCell = row.createCell(4);
                emptyLinesCell.setCellValue(func.emptyLines);
                emptyLinesCell.setCellStyle(numberStyle);
                
                Cell commentLinesCell = row.createCell(5);
                commentLinesCell.setCellValue(func.commentLines);
                commentLinesCell.setCellStyle(numberStyle);
                
                Cell startLineCell = row.createCell(6);
                startLineCell.setCellValue(func.startLine);
                startLineCell.setCellStyle(numberStyle);
                
                Cell endLineCell = row.createCell(7);
                endLineCell.setCellValue(func.endLine);
                endLineCell.setCellStyle(numberStyle);
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                detailSheet.autoSizeColumn(i);
            }
            
            // 写入文件
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
        }
        
        return true;
    }
    
    /**
     * CSV转义处理
     */
    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    /**
     * JSON转义处理
     */
    private static String escapeJSON(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    /**
     * 显示文件保存对话框
     * @param parent 父窗口
     * @param format 文件格式
     * @return 选择的文件路径，如果取消则返回null
     */
    public static String showSaveDialog(JFrame parent, String format) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存统计结果");
        
        // 设置文件过滤器
        String extension = format.toLowerCase();
        switch (extension) {
            case "csv": {
                final String description = "CSV文件 (*.csv)";
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
                    }
                    
                    @Override
                    public String getDescription() {
                        return description;
                    }
                });
                break;
            }
            case "json": {
                final String description = "JSON文件 (*.json)";
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".json");
                    }
                    
                    @Override
                    public String getDescription() {
                        return description;
                    }
                });
                break;
            }
            case "xlsx": {
                final String description = "Excel文件 (*.xlsx)";
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".xlsx");
                    }
                    
                    @Override
                    public String getDescription() {
                        return description;
                    }
                });
                break;
            }
        }
        
        // 设置默认文件名
        String defaultFileName = "函数统计结果." + extension;
        fileChooser.setSelectedFile(new File(defaultFileName));
        
        int result = fileChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            
            // 确保文件扩展名正确
            if (!filePath.toLowerCase().endsWith("." + extension)) {
                filePath += "." + extension;
            }
            
            return filePath;
        }
        
        return null;
    }
}

