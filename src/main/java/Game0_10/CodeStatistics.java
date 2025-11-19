package Game0_10;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * 代码统计器 - 就像一个"代码侦探",能够统计各种编程语言的代码量
 * 它会扫描指定文件夹,找出所有代码文件,并统计每种语言有多少行代码
 */
public class CodeStatistics {
    
    // 这是一个"语言识别表" - 通过文件后缀名来判断是什么编程语言
    private static final Map<String, String> EXTENSION_TO_LANGUAGE = new HashMap<>();
    
    static {
        // Python语言
        EXTENSION_TO_LANGUAGE.put(".py", "Python");
        EXTENSION_TO_LANGUAGE.put(".pyi", "Python");
        
        // Java语言
        EXTENSION_TO_LANGUAGE.put(".java", "Java");
        
        // JavaScript语言
        EXTENSION_TO_LANGUAGE.put(".js", "JavaScript");
        
        // 其他常见语言
        EXTENSION_TO_LANGUAGE.put(".cpp", "C++");
        EXTENSION_TO_LANGUAGE.put(".c", "C");
        EXTENSION_TO_LANGUAGE.put(".h", "C/C++ Header");
        EXTENSION_TO_LANGUAGE.put(".html", "HTML");
        EXTENSION_TO_LANGUAGE.put(".css", "CSS");
        EXTENSION_TO_LANGUAGE.put(".xml", "XML");
        EXTENSION_TO_LANGUAGE.put(".json", "JSON");
        EXTENSION_TO_LANGUAGE.put(".md", "Markdown");
        EXTENSION_TO_LANGUAGE.put(".txt", "Text");
    }
    
    // 存储每种语言的代码行数
    private Map<String, Integer> languageLineCount = new HashMap<>();
    
    // 存储每种语言的文件数量
    private Map<String, Integer> languageFileCount = new HashMap<>();
    
    // 总代码行数
    private int totalLines = 0;
    
    // 总文件数
    private int totalFiles = 0;
    
    /**
     * 扫描指定文件夹,统计所有代码文件
     * @param folderPath 要扫描的文件夹路径
     */
    public void scanFolder(String folderPath) {
        System.out.println("🔍 开始扫描文件夹: " + folderPath);
        
        try {
            // 使用Java的文件遍历工具,递归扫描所有文件
            Files.walk(Paths.get(folderPath))
                .filter(Files::isRegularFile)  // 只要文件,不要文件夹
                .forEach(this::analyzeFile);   // 分析每个文件
            
            System.out.println("✅ 扫描完成!");
            System.out.println("📊 总共找到 " + totalFiles + " 个文件");
            System.out.println("📝 总共 " + totalLines + " 行代码");
            
        } catch (IOException e) {
            System.err.println("❌ 扫描文件夹时出错: " + e.getMessage());
        }
    }
    
    /**
     * 分析单个文件
     * @param filePath 文件路径
     */
    private void analyzeFile(Path filePath) {
        String fileName = filePath.getFileName().toString();
        
        // 获取文件后缀名(比如 .py, .java)
        String extension = getFileExtension(fileName);
        
        // 判断这是什么语言的文件
        String language = EXTENSION_TO_LANGUAGE.get(extension);
        
        if (language != null) {
            // 统计这个文件有多少行代码
            int lineCount = countLines(filePath);
            
            if (lineCount > 0) {
                // 更新统计数据
                languageLineCount.put(language, 
                    languageLineCount.getOrDefault(language, 0) + lineCount);
                languageFileCount.put(language, 
                    languageFileCount.getOrDefault(language, 0) + 1);
                
                totalLines += lineCount;
                totalFiles++;
                
                System.out.println("  📄 " + fileName + " (" + language + "): " + lineCount + " 行");
            }
        }
    }
    
    /**
     * 获取文件的后缀名
     * @param fileName 文件名
     * @return 后缀名(包括点号,比如 ".py")
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot).toLowerCase();
        }
        return "";
    }
    
    /**
     * 统计文件的行数
     * @param filePath 文件路径
     * @return 行数
     */
    private int countLines(Path filePath) {
        try {
            return (int) Files.lines(filePath).count();
        } catch (IOException e) {
            System.err.println("⚠️ 无法读取文件: " + filePath);
            return 0;
        }
    }
    
    /**
     * 获取每种语言的代码行数统计结果
     */
    public Map<String, Integer> getLanguageLineCount() {
        return new HashMap<>(languageLineCount);
    }
    
    /**
     * 获取每种语言的文件数量统计结果
     */
    public Map<String, Integer> getLanguageFileCount() {
        return new HashMap<>(languageFileCount);
    }
    
    /**
     * 获取总代码行数
     */
    public int getTotalLines() {
        return totalLines;
    }
    
    /**
     * 获取总文件数
     */
    public int getTotalFiles() {
        return totalFiles;
    }
    
    /**
     * 打印统计结果
     */
    public void printStatistics() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📊 代码统计结果");
        System.out.println("=".repeat(60));
        
        // 按代码行数从多到少排序
        languageLineCount.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .forEach(entry -> {
                String language = entry.getKey();
                int lines = entry.getValue();
                int files = languageFileCount.get(language);
                double percentage = (lines * 100.0) / totalLines;
                
                System.out.printf("%-15s: %,8d 行 (%d 个文件) - %.2f%%\n", 
                    language, lines, files, percentage);
            });
        
        System.out.println("=".repeat(60));
        System.out.printf("总计: %,d 行代码,分布在 %d 个文件中\n", totalLines, totalFiles);
        System.out.println("=".repeat(60));
    }
}
