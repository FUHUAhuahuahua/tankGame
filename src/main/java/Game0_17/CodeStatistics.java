package Game0_17;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * 代码统计器 - 能够统计各种编程语言的代码量
 * 从 Game0_10 移植并优化
 */
public class CodeStatistics {
    
    // 语言识别表 - 通过文件后缀名来判断编程语言
    private static final Map<String, String> EXTENSION_TO_LANGUAGE = new HashMap<>();
    
    static {
        // Python语言
        EXTENSION_TO_LANGUAGE.put(".py", "Python");
        EXTENSION_TO_LANGUAGE.put(".pyi", "Python");
        
        // Java语言
        EXTENSION_TO_LANGUAGE.put(".java", "Java");
        
        // JavaScript语言
        EXTENSION_TO_LANGUAGE.put(".js", "JavaScript");
        EXTENSION_TO_LANGUAGE.put(".jsx", "JavaScript");
        EXTENSION_TO_LANGUAGE.put(".ts", "TypeScript");
        EXTENSION_TO_LANGUAGE.put(".tsx", "TypeScript");
        
        // C/C++语言
        EXTENSION_TO_LANGUAGE.put(".cpp", "C++");
        EXTENSION_TO_LANGUAGE.put(".cc", "C++");
        EXTENSION_TO_LANGUAGE.put(".cxx", "C++");
        EXTENSION_TO_LANGUAGE.put(".c", "C");
        EXTENSION_TO_LANGUAGE.put(".h", "C/C++ Header");
        EXTENSION_TO_LANGUAGE.put(".hpp", "C++ Header");
        
        // Web相关
        EXTENSION_TO_LANGUAGE.put(".html", "HTML");
        EXTENSION_TO_LANGUAGE.put(".htm", "HTML");
        EXTENSION_TO_LANGUAGE.put(".css", "CSS");
        EXTENSION_TO_LANGUAGE.put(".scss", "SCSS");
        EXTENSION_TO_LANGUAGE.put(".less", "LESS");
        
        // 数据格式
        EXTENSION_TO_LANGUAGE.put(".xml", "XML");
        EXTENSION_TO_LANGUAGE.put(".json", "JSON");
        EXTENSION_TO_LANGUAGE.put(".yaml", "YAML");
        EXTENSION_TO_LANGUAGE.put(".yml", "YAML");
        
        // 脚本语言
        EXTENSION_TO_LANGUAGE.put(".sh", "Shell");
        EXTENSION_TO_LANGUAGE.put(".bash", "Bash");
        EXTENSION_TO_LANGUAGE.put(".bat", "Batch");
        EXTENSION_TO_LANGUAGE.put(".cmd", "Batch");
        EXTENSION_TO_LANGUAGE.put(".ps1", "PowerShell");
        
        // 其他语言
        EXTENSION_TO_LANGUAGE.put(".go", "Go");
        EXTENSION_TO_LANGUAGE.put(".rs", "Rust");
        EXTENSION_TO_LANGUAGE.put(".php", "PHP");
        EXTENSION_TO_LANGUAGE.put(".rb", "Ruby");
        EXTENSION_TO_LANGUAGE.put(".swift", "Swift");
        EXTENSION_TO_LANGUAGE.put(".kt", "Kotlin");
        EXTENSION_TO_LANGUAGE.put(".cs", "C#");
        EXTENSION_TO_LANGUAGE.put(".vb", "Visual Basic");
        EXTENSION_TO_LANGUAGE.put(".r", "R");
        EXTENSION_TO_LANGUAGE.put(".m", "MATLAB");
        EXTENSION_TO_LANGUAGE.put(".sql", "SQL");
        
        // 文档
        EXTENSION_TO_LANGUAGE.put(".md", "Markdown");
        EXTENSION_TO_LANGUAGE.put(".txt", "Text");
        EXTENSION_TO_LANGUAGE.put(".rst", "reStructuredText");
    }
    
    // 存储每种语言的代码行数
    private Map<String, Integer> languageLineCount = new HashMap<>();
    
    // 存储每种语言的文件数量
    private Map<String, Integer> languageFileCount = new HashMap<>();
    
    // 存储每种语言的空行数
    private Map<String, Integer> languageBlankLines = new HashMap<>();
    
    // 存储每种语言的注释行数
    private Map<String, Integer> languageCommentLines = new HashMap<>();
    
    // 总代码行数（不含空行和注释）
    private int totalCodeLines = 0;
    
    // 总行数（包含所有）
    private int totalLines = 0;
    
    // 总文件数
    private int totalFiles = 0;
    
    // 总空行数
    private int totalBlankLines = 0;
    
    // 总注释行数
    private int totalCommentLines = 0;
    
    /**
     * 扫描指定文件夹，统计所有代码文件
     * @param folderPath 要扫描的文件夹路径
     */
    public void scanFolder(String folderPath) {
        System.out.println("🔍 开始扫描文件夹: " + folderPath);
        
        // 重置统计数据
        reset();
        
        try {
            // 递归扫描所有文件
            Files.walk(Paths.get(folderPath))
                .filter(Files::isRegularFile)  // 只要文件，不要文件夹
                .filter(this::shouldAnalyze)   // 过滤掉不需要分析的文件
                .forEach(this::analyzeFile);   // 分析每个文件
            
            System.out.println("✅ 扫描完成!");
            System.out.println("📊 总共找到 " + totalFiles + " 个文件");
            System.out.println("📝 总共 " + totalLines + " 行（含 " + totalCodeLines + " 行代码）");
            
        } catch (IOException e) {
            System.err.println("❌ 扫描文件夹时出错: " + e.getMessage());
        }
    }
    
    /**
     * 判断是否应该分析这个文件
     */
    private boolean shouldAnalyze(Path path) {
        String pathStr = path.toString();
        // 跳过隐藏文件和目录
        if (pathStr.contains("/.") || pathStr.contains("\\.")) {
            return false;
        }
        // 跳过常见的非代码目录
        if (pathStr.contains("node_modules") || 
            pathStr.contains("target") ||
            pathStr.contains("build") ||
            pathStr.contains("dist") ||
            pathStr.contains(".git") ||
            pathStr.contains("__pycache__")) {
            return false;
        }
        return true;
    }
    
    /**
     * 分析单个文件
     */
    private void analyzeFile(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String extension = getFileExtension(fileName);
        String language = EXTENSION_TO_LANGUAGE.get(extension);
        
        if (language != null) {
            FileStats stats = analyzeFileContent(filePath, language);
            
            if (stats.totalLines > 0) {
                // 更新统计数据
                languageLineCount.put(language, 
                    languageLineCount.getOrDefault(language, 0) + stats.totalLines);
                languageFileCount.put(language, 
                    languageFileCount.getOrDefault(language, 0) + 1);
                languageBlankLines.put(language,
                    languageBlankLines.getOrDefault(language, 0) + stats.blankLines);
                languageCommentLines.put(language,
                    languageCommentLines.getOrDefault(language, 0) + stats.commentLines);
                
                totalLines += stats.totalLines;
                totalCodeLines += stats.codeLines;
                totalBlankLines += stats.blankLines;
                totalCommentLines += stats.commentLines;
                totalFiles++;
                
                System.out.println("  📄 " + fileName + " (" + language + "): " + 
                    stats.codeLines + " 行代码, " + stats.commentLines + " 行注释");
            }
        }
    }
    
    /**
     * 分析文件内容，统计代码行、注释行、空行
     */
    private FileStats analyzeFileContent(Path filePath, String language) {
        FileStats stats = new FileStats();
        boolean inMultiLineComment = false;
        
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                stats.totalLines++;
                String trimmed = line.trim();
                
                if (trimmed.isEmpty()) {
                    stats.blankLines++;
                } else if (isComment(trimmed, language, inMultiLineComment)) {
                    stats.commentLines++;
                    // 检查多行注释的开始和结束
                    if (language.contains("Java") || language.contains("C") || 
                        language.equals("JavaScript") || language.equals("TypeScript")) {
                        if (trimmed.contains("/*") && !trimmed.contains("*/")) {
                            inMultiLineComment = true;
                        } else if (trimmed.contains("*/")) {
                            inMultiLineComment = false;
                        }
                    }
                } else {
                    stats.codeLines++;
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️ 无法读取文件: " + filePath);
        }
        
        return stats;
    }
    
    /**
     * 判断是否是注释行
     */
    private boolean isComment(String line, String language, boolean inMultiLineComment) {
        if (inMultiLineComment) {
            return true;
        }
        
        // 单行注释
        if (language.equals("Python") || language.equals("Shell") || 
            language.equals("Bash") || language.equals("YAML")) {
            return line.startsWith("#");
        } else if (language.contains("Java") || language.contains("C") || 
                   language.equals("JavaScript") || language.equals("TypeScript") ||
                   language.equals("Go") || language.equals("Rust") || 
                   language.equals("Swift") || language.equals("Kotlin") ||
                   language.equals("C#") || language.equals("PHP")) {
            return line.startsWith("//") || line.startsWith("/*") || line.startsWith("*");
        } else if (language.equals("SQL")) {
            return line.startsWith("--");
        } else if (language.equals("HTML") || language.equals("XML")) {
            return line.startsWith("<!--");
        } else if (language.equals("CSS") || language.equals("SCSS") || language.equals("LESS")) {
            return line.startsWith("/*") || line.startsWith("*");
        }
        
        return false;
    }
    
    /**
     * 获取文件的后缀名
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot).toLowerCase();
        }
        return "";
    }
    
    /**
     * 重置所有统计数据
     */
    private void reset() {
        languageLineCount.clear();
        languageFileCount.clear();
        languageBlankLines.clear();
        languageCommentLines.clear();
        totalCodeLines = 0;
        totalLines = 0;
        totalFiles = 0;
        totalBlankLines = 0;
        totalCommentLines = 0;
    }
    
    // Getter方法
    public Map<String, Integer> getLanguageLineCount() {
        return new HashMap<>(languageLineCount);
    }
    
    public Map<String, Integer> getLanguageFileCount() {
        return new HashMap<>(languageFileCount);
    }
    
    public Map<String, Integer> getLanguageCodeLines() {
        Map<String, Integer> codeLines = new HashMap<>();
        for (String language : languageLineCount.keySet()) {
            int total = languageLineCount.get(language);
            int blank = languageBlankLines.getOrDefault(language, 0);
            int comment = languageCommentLines.getOrDefault(language, 0);
            codeLines.put(language, total - blank - comment);
        }
        return codeLines;
    }
    
    public int getTotalLines() { return totalLines; }
    public int getTotalCodeLines() { return totalCodeLines; }
    public int getTotalFiles() { return totalFiles; }
    public int getTotalBlankLines() { return totalBlankLines; }
    public int getTotalCommentLines() { return totalCommentLines; }
    
    /**
     * 文件统计信息
     */
    private static class FileStats {
        int totalLines = 0;
        int codeLines = 0;
        int blankLines = 0;
        int commentLines = 0;
    }
}
