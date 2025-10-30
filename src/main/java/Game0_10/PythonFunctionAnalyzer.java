package Game0_10;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Python函数分析器 - 专门分析Python代码中的函数
 * 它能告诉你每个函数有多长,并计算出平均值、最大值、最小值等统计数据
 */
public class PythonFunctionAnalyzer {
    
    // 存储所有找到的函数长度
    private List<Integer> functionLengths = new ArrayList<>();
    
    // 存储函数信息(用于调试和展示)
    private List<FunctionInfo> functions = new ArrayList<>();
    
    /**
     * 函数信息类 - 记录一个函数的详细信息
     */
    public static class FunctionInfo {
        String fileName;      // 文件名
        String functionName;  // 函数名
        int startLine;        // 开始行号
        int length;           // 函数长度(行数)
        
        public FunctionInfo(String fileName, String functionName, int startLine, int length) {
            this.fileName = fileName;
            this.functionName = functionName;
            this.startLine = startLine;
            this.length = length;
        }
        
        @Override
        public String toString() {
            return String.format("%s:%d - %s() [%d行]", 
                fileName, startLine, functionName, length);
        }
    }
    
    /**
     * 扫描文件夹中的所有Python文件
     * @param folderPath 要扫描的文件夹路径
     */
    public void scanPythonFiles(String folderPath) {
        System.out.println("\n🐍 开始分析Python函数...");
        
        try {
            Files.walk(Paths.get(folderPath))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".py"))
                .forEach(this::analyzePythonFile);
            
            System.out.println("✅ Python函数分析完成!");
            System.out.println("📊 总共找到 " + functions.size() + " 个函数");
            
        } catch (IOException e) {
            System.err.println("❌ 扫描Python文件时出错: " + e.getMessage());
        }
    }
    
    /**
     * 分析单个Python文件
     * @param filePath 文件路径
     */
    private void analyzePythonFile(Path filePath) {
        try {
            List<String> lines = Files.readAllLines(filePath);
            String fileName = filePath.getFileName().toString();
            
            // 当前正在分析的函数
            String currentFunction = null;
            int functionStartLine = 0;
            int functionStartIndent = 0;
            
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String trimmedLine = line.trim();
                
                // 检查是否是函数定义(以 "def " 开头)
                if (trimmedLine.startsWith("def ") && trimmedLine.contains("(")) {
                    // 如果之前有函数正在分析,先保存它
                    if (currentFunction != null) {
                        int length = i - functionStartLine;
                        saveFunctionInfo(fileName, currentFunction, functionStartLine, length);
                    }
                    
                    // 开始分析新函数
                    currentFunction = extractFunctionName(trimmedLine);
                    functionStartLine = i + 1;  // 行号从1开始
                    functionStartIndent = getIndentLevel(line);
                }
                // 检查函数是否结束(遇到同级或更低缩进的非空行)
                else if (currentFunction != null && !trimmedLine.isEmpty()) {
                    int currentIndent = getIndentLevel(line);
                    
                    // 如果缩进回到函数定义级别或更少,说明函数结束了
                    if (currentIndent <= functionStartIndent && 
                        !trimmedLine.startsWith("#")) {  // 忽略注释
                        int length = i - functionStartLine;
                        saveFunctionInfo(fileName, currentFunction, functionStartLine, length);
                        currentFunction = null;
                    }
                }
            }
            
            // 处理文件末尾的函数
            if (currentFunction != null) {
                int length = lines.size() - functionStartLine;
                saveFunctionInfo(fileName, currentFunction, functionStartLine, length);
            }
            
        } catch (IOException e) {
            System.err.println("⚠️ 无法读取文件: " + filePath);
        }
    }
    
    /**
     * 从函数定义行中提取函数名
     * 例如: "def hello_world(name):" -> "hello_world"
     */
    private String extractFunctionName(String line) {
        int defPos = line.indexOf("def ");
        int parenPos = line.indexOf("(", defPos);
        
        if (defPos >= 0 && parenPos > defPos) {
            return line.substring(defPos + 4, parenPos).trim();
        }
        return "unknown";
    }
    
    /**
     * 获取代码行的缩进级别(有多少个空格或tab)
     */
    private int getIndentLevel(String line) {
        int indent = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                indent++;
            } else if (c == '\t') {
                indent += 4;  // 一个tab算4个空格
            } else {
                break;
            }
        }
        return indent;
    }
    
    /**
     * 保存函数信息
     */
    private void saveFunctionInfo(String fileName, String functionName, 
                                  int startLine, int length) {
        if (length > 0) {  // 只保存有内容的函数
            FunctionInfo info = new FunctionInfo(fileName, functionName, startLine, length);
            functions.add(info);
            functionLengths.add(length);
        }
    }
    
    /**
     * 计算平均值
     */
    public double getAverage() {
        if (functionLengths.isEmpty()) return 0;
        return functionLengths.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
    }
    
    /**
     * 获取最大值
     */
    public int getMax() {
        if (functionLengths.isEmpty()) return 0;
        return Collections.max(functionLengths);
    }
    
    /**
     * 获取最小值
     */
    public int getMin() {
        if (functionLengths.isEmpty()) return 0;
        return Collections.min(functionLengths);
    }
    
    /**
     * 计算中位数
     */
    public double getMedian() {
        if (functionLengths.isEmpty()) return 0;
        
        List<Integer> sorted = new ArrayList<>(functionLengths);
        Collections.sort(sorted);
        
        int size = sorted.size();
        if (size % 2 == 0) {
            // 偶数个数据,取中间两个的平均值
            return (sorted.get(size/2 - 1) + sorted.get(size/2)) / 2.0;
        } else {
            // 奇数个数据,取中间那个
            return sorted.get(size/2);
        }
    }
    
    /**
     * 获取所有函数长度数据
     */
    public List<Integer> getFunctionLengths() {
        return new ArrayList<>(functionLengths);
    }
    
    /**
     * 获取函数总数
     */
    public int getFunctionCount() {
        return functions.size();
    }
    
    /**
     * 打印统计结果
     */
    public void printStatistics() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🐍 Python函数长度统计");
        System.out.println("=".repeat(60));
        
        if (functions.isEmpty()) {
            System.out.println("没有找到Python函数");
            return;
        }
        
        System.out.printf("函数总数: %d\n", getFunctionCount());
        System.out.printf("平均长度: %.2f 行\n", getAverage());
        System.out.printf("最大长度: %d 行\n", getMax());
        System.out.printf("最小长度: %d 行\n", getMin());
        System.out.printf("中位数: %.2f 行\n", getMedian());
        
        System.out.println("\n📋 最长的5个函数:");
        functions.stream()
            .sorted((f1, f2) -> Integer.compare(f2.length, f1.length))
            .limit(5)
            .forEach(f -> System.out.println("  " + f));
        
        System.out.println("\n📋 最短的5个函数:");
        functions.stream()
            .sorted((f1, f2) -> Integer.compare(f1.length, f2.length))
            .limit(5)
            .forEach(f -> System.out.println("  " + f));
        
        System.out.println("=".repeat(60));
    }
}
