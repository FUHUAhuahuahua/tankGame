package Game0_11;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * 多语言代码分析器 - 支持 C/C++/Java/Python/C# 的综合分析
 * 统计：源文件数、代码行数、空行数、注释行数、函数个数、函数长度统计
 */
public class MultiLanguageAnalyzer {
    
    // 支持的语言类型
    public enum Language {
        C(".c", ".h"),
        CPP(".cpp", ".cc", ".cxx", ".hpp", ".hxx"),
        JAVA(".java"),
        PYTHON(".py", ".pyi"),
        CSHARP(".cs");
        
        private final Set<String> extensions;
        
        Language(String... exts) {
            this.extensions = new HashSet<>(Arrays.asList(exts));
        }
        
        public boolean matches(String extension) {
            return extensions.contains(extension.toLowerCase());
        }
        
        public static Language fromExtension(String extension) {
            for (Language lang : values()) {
                if (lang.matches(extension)) {
                    return lang;
                }
            }
            return null;
        }
    }
    
    // 语言特定的函数匹配模式
    private static final Map<Language, Pattern> FUNCTION_PATTERNS = new HashMap<>();
    
    static {
        // C/C++ 函数模式
        FUNCTION_PATTERNS.put(Language.C, Pattern.compile(
            "^\\s*(?:extern\\s+)?(?:static\\s+)?(?:inline\\s+)?(?:const\\s+)?(?:unsigned\\s+|signed\\s+)?" +
            "(?:(?:long\\s+long|long\\s+double)|long|short|void|int|char|float|double|bool|size_t|u?int\\d+_t|struct\\s+\\w+|[A-Za-z_][A-Za-z0-9_]*\\s*(?:\\*+)?)\\s+" +
            "([A-Za-z_][A-Za-z0-9_]*)\\s*\\([^;{}]*\\)\\s*\\{",
            Pattern.MULTILINE
        ));
        
        FUNCTION_PATTERNS.put(Language.CPP, Pattern.compile(
            "^\\s*(?:virtual\\s+)?(?:static\\s+)?(?:inline\\s+)?(?:const\\s+)?(?:unsigned\\s+|signed\\s+)?" +
            "(?:(?:long\\s+long|long\\s+double)|long|short|void|int|char|float|double|bool|size_t|u?int\\d+_t|std::\\w+|[A-Za-z_][A-Za-z0-9_:]*\\s*(?:[<>\\w\\s,]*>)?\\s*(?:\\*+|&+)?)\\s+" +
            "([A-Za-z_][A-Za-z0-9_]*)\\s*\\([^;{}]*\\)\\s*(?:const\\s+)?\\{",
            Pattern.MULTILINE
        ));
        
        // Java 方法模式
        FUNCTION_PATTERNS.put(Language.JAVA, Pattern.compile(
            "^\\s*(?:public|private|protected)?\\s*(?:static\\s+)?(?:final\\s+)?(?:abstract\\s+)?(?:synchronized\\s+)?" +
            "(?:void|boolean|byte|short|int|long|float|double|char|String|[A-Z][A-Za-z0-9_]*(?:<[^>]*>)?)\\s+" +
            "([A-Za-z_][A-Za-z0-9_]*)\\s*\\([^;{}]*\\)\\s*(?:throws\\s+[A-Za-z0-9_,\\s]+)?\\s*\\{",
            Pattern.MULTILINE
        ));
        
        // Python 函数模式
        FUNCTION_PATTERNS.put(Language.PYTHON, Pattern.compile(
            "^\\s*def\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\([^)]*\\)\\s*:",
            Pattern.MULTILINE
        ));
        
        // C# 方法模式
        FUNCTION_PATTERNS.put(Language.CSHARP, Pattern.compile(
            "^\\s*(?:public|private|protected|internal)?\\s*(?:static\\s+)?(?:virtual\\s+)?(?:override\\s+)?(?:abstract\\s+)?(?:async\\s+)?" +
            "(?:void|bool|byte|sbyte|short|ushort|int|uint|long|ulong|float|double|decimal|char|string|object|[A-Z][A-Za-z0-9_]*(?:<[^>]*>)?)\\s+" +
            "([A-Za-z_][A-Za-z0-9_]*)\\s*\\([^;{}]*\\)\\s*\\{",
            Pattern.MULTILINE
        ));
    }
    
    // 语言特定的注释模式
    private static final Map<Language, List<Pattern>> COMMENT_PATTERNS = new HashMap<>();
    
    static {
        // C/C++/Java/C# 注释模式
        List<Pattern> cStyleComments = Arrays.asList(
            Pattern.compile("//.*$", Pattern.MULTILINE),  // 单行注释
            Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL)   // 多行注释
        );
        COMMENT_PATTERNS.put(Language.C, cStyleComments);
        COMMENT_PATTERNS.put(Language.CPP, cStyleComments);
        COMMENT_PATTERNS.put(Language.JAVA, cStyleComments);
        COMMENT_PATTERNS.put(Language.CSHARP, cStyleComments);
        
        // Python 注释模式
        COMMENT_PATTERNS.put(Language.PYTHON, Arrays.asList(
            Pattern.compile("#.*$", Pattern.MULTILINE),     // 单行注释
            Pattern.compile("\"\"\".*?\"\"\"", Pattern.DOTALL), // 三引号注释
            Pattern.compile("'''.*?'''", Pattern.DOTALL)     // 三单引号注释
        ));
    }
    
    // 分析结果存储
    private Map<Language, LanguageStatistics> languageStats = new HashMap<>();
    private boolean countEmptyLines = true;
    private boolean countCommentLines = true;
    
    /**
     * 语言统计信息类
     */
    public static class LanguageStatistics {
        public Language language;
        public int sourceFiles = 0;
        public int totalLines = 0;
        public int codeLines = 0;
        public int emptyLines = 0;
        public int commentLines = 0;
        public List<FunctionInfo> functions = new ArrayList<>();
        
        // 函数长度统计
        public int functionCount = 0;
        public int maxFunctionLength = 0;
        public int minFunctionLength = Integer.MAX_VALUE;
        public double avgFunctionLength = 0.0;
        public double medianFunctionLength = 0.0;
        public FunctionInfo longestFunction = null;
        public FunctionInfo shortestFunction = null;
        
        public LanguageStatistics(Language language) {
            this.language = language;
        }
        
        public void calculateFunctionStatistics() {
            if (functions.isEmpty()) {
                functionCount = 0;
                maxFunctionLength = 0;
                minFunctionLength = 0;
                avgFunctionLength = 0.0;
                medianFunctionLength = 0.0;
                return;
            }
            
            functionCount = functions.size();
            
            // 计算最大值、最小值
            for (FunctionInfo func : functions) {
                int length = func.totalLines;
                if (length > maxFunctionLength) {
                    maxFunctionLength = length;
                    longestFunction = func;
                }
                if (length < minFunctionLength) {
                    minFunctionLength = length;
                    shortestFunction = func;
                }
            }
            
            // 计算平均值
            double sum = functions.stream().mapToInt(f -> f.totalLines).sum();
            avgFunctionLength = sum / functionCount;
            
            // 计算中位数
            List<Integer> lengths = functions.stream()
                .map(f -> f.totalLines)
                .sorted()
                .collect(java.util.stream.Collectors.toList());
            
            if (lengths.size() % 2 == 0) {
                medianFunctionLength = (lengths.get(lengths.size() / 2 - 1) + 
                                      lengths.get(lengths.size() / 2)) / 2.0;
            } else {
                medianFunctionLength = lengths.get(lengths.size() / 2);
            }
        }
    }
    
    /**
     * 函数信息类
     */
    public static class FunctionInfo {
        public String name;
        public String fileName;
        public Language language;
        public int startLine;
        public int endLine;
        public int totalLines;
        public int codeLines;
        public int emptyLines;
        public int commentLines;
        
        public FunctionInfo(String name, String fileName, Language language, int startLine) {
            this.name = name;
            this.fileName = fileName;
            this.language = language;
            this.startLine = startLine;
        }
        
        public void calculateLength() {
            this.totalLines = endLine - startLine + 1;
        }
    }
    
    /**
     * 设置统计选项
     */
    public void setCountOptions(boolean countEmptyLines, boolean countCommentLines) {
        this.countEmptyLines = countEmptyLines;
        this.countCommentLines = countCommentLines;
    }
    
    /**
     * 分析指定文件夹
     */
    public void analyzeFolder(String folderPath) throws IOException {
        System.out.println("🔍 开始多语言代码分析: " + folderPath);
        
        Files.walk(Paths.get(folderPath))
            .filter(Files::isRegularFile)
            .forEach(this::analyzeFile);
        
        // 计算各语言的函数统计
        for (LanguageStatistics stats : languageStats.values()) {
            stats.calculateFunctionStatistics();
        }
        
        System.out.println("✅ 多语言分析完成!");
    }
    
    /**
     * 分析单个文件
     */
    private void analyzeFile(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String extension = getFileExtension(fileName);
        Language language = Language.fromExtension(extension);
        
        if (language == null) {
            return; // 不支持的文件类型
        }
        
        try {
            String content = readFileWithFallback(filePath);
            LanguageStatistics stats = languageStats.computeIfAbsent(language, LanguageStatistics::new);
            
            stats.sourceFiles++;
            analyzeFileContent(content, fileName, language, stats);
            
            System.out.println("📄 分析文件: " + fileName + " (" + language + ")");
            
        } catch (Exception e) {
            System.err.println("❌ 分析文件失败: " + fileName + " - " + e.getMessage());
        }
    }
    
    /**
     * 分析文件内容
     */
    private void analyzeFileContent(String content, String fileName, Language language, LanguageStatistics stats) {
        String[] lines = content.split("\\R");
        stats.totalLines += lines.length;
        
        // 统计空行和注释行
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                stats.emptyLines++;
            } else if (isCommentLine(trimmed, language)) {
                stats.commentLines++;
            } else {
                stats.codeLines++;
            }
        }
        
        // 分析函数
        analyzeFunctions(content, fileName, language, stats);
    }
    
    /**
     * 分析函数
     */
    private void analyzeFunctions(String content, String fileName, Language language, LanguageStatistics stats) {
        Pattern pattern = FUNCTION_PATTERNS.get(language);
        if (pattern == null) return;
        
        Matcher matcher = pattern.matcher(content);
        String[] lines = content.split("\\R");
        
        while (matcher.find()) {
            String functionName = matcher.group(1);
            int startPos = matcher.start();
            int startLine = getLineNumber(content, startPos);
            
            FunctionInfo funcInfo = new FunctionInfo(functionName, fileName, language, startLine);
            
            // 找到函数结束位置
            int endLine = findFunctionEnd(lines, startLine - 1, language);
            funcInfo.endLine = endLine;
            funcInfo.calculateLength();
            
            // 分析函数内容
            analyzeFunctionContent(lines, funcInfo, language);
            
            stats.functions.add(funcInfo);
            System.out.println("  🔧 找到函数: " + functionName + " (" + funcInfo.totalLines + " 行)");
        }
    }
    
    /**
     * 分析函数内容的详细统计
     */
    private void analyzeFunctionContent(String[] lines, FunctionInfo funcInfo, Language language) {
        for (int i = funcInfo.startLine - 1; i < funcInfo.endLine && i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                funcInfo.emptyLines++;
            } else if (isCommentLine(line, language)) {
                funcInfo.commentLines++;
            } else {
                funcInfo.codeLines++;
            }
        }
    }
    
    /**
     * 判断是否为注释行
     */
    private boolean isCommentLine(String line, Language language) {
        List<Pattern> patterns = COMMENT_PATTERNS.get(language);
        if (patterns == null) return false;
        
        for (Pattern pattern : patterns) {
            if (pattern.matcher(line).find()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 找到函数结束行
     */
    private int findFunctionEnd(String[] lines, int startLine, Language language) {
        if (language == Language.PYTHON) {
            return findPythonFunctionEnd(lines, startLine);
        } else {
            return findBraceFunctionEnd(lines, startLine);
        }
    }
    
    /**
     * 找到Python函数结束（基于缩进）
     */
    private int findPythonFunctionEnd(String[] lines, int startLine) {
        if (startLine >= lines.length) return startLine;
        
        String defLine = lines[startLine];
        int baseIndent = defLine.length() - defLine.replaceAll("^\\s+", "").length();
        
        for (int i = startLine + 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.trim().isEmpty()) continue;
            
            int currentIndent = line.length() - line.replaceAll("^\\s+", "").length();
            if (currentIndent <= baseIndent) {
                return i - 1;
            }
        }
        return lines.length - 1;
    }
    
    /**
     * 找到大括号函数结束
     */
    private int findBraceFunctionEnd(String[] lines, int startLine) {
        int braceCount = 0;
        boolean foundFirstBrace = false;
        
        for (int i = startLine; i < lines.length; i++) {
            String line = lines[i];
            for (char c : line.toCharArray()) {
                if (c == '{') {
                    braceCount++;
                    foundFirstBrace = true;
                } else if (c == '}') {
                    braceCount--;
                    if (foundFirstBrace && braceCount == 0) {
                        return i + 1;
                    }
                }
            }
        }
        return lines.length - 1;
    }
    
    /**
     * 获取行号
     */
    private int getLineNumber(String content, int position) {
        return content.substring(0, position).split("\\R").length;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
    }
    
    /**
     * 带编码回退的文件读取
     */
    private String readFileWithFallback(Path filePath) throws IOException {
        try {
            return Files.readString(filePath);
        } catch (java.nio.charset.MalformedInputException e) {
            try {
                return Files.readString(filePath, java.nio.charset.Charset.defaultCharset());
            } catch (java.nio.charset.MalformedInputException e2) {
                return Files.readString(filePath, java.nio.charset.Charset.forName("GBK"));
            }
        }
    }
    
    /**
     * 获取分析结果
     */
    public Map<Language, LanguageStatistics> getLanguageStatistics() {
        return new HashMap<>(languageStats);
    }
    
    /**
     * 获取总体统计
     */
    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> overall = new HashMap<>();
        
        int totalSourceFiles = languageStats.values().stream().mapToInt(s -> s.sourceFiles).sum();
        int totalLines = languageStats.values().stream().mapToInt(s -> s.totalLines).sum();
        int totalCodeLines = languageStats.values().stream().mapToInt(s -> s.codeLines).sum();
        int totalEmptyLines = languageStats.values().stream().mapToInt(s -> s.emptyLines).sum();
        int totalCommentLines = languageStats.values().stream().mapToInt(s -> s.commentLines).sum();
        int totalFunctions = languageStats.values().stream().mapToInt(s -> s.functionCount).sum();
        
        overall.put("totalSourceFiles", totalSourceFiles);
        overall.put("totalLines", totalLines);
        overall.put("totalCodeLines", totalCodeLines);
        overall.put("totalEmptyLines", totalEmptyLines);
        overall.put("totalCommentLines", totalCommentLines);
        overall.put("totalFunctions", totalFunctions);
        overall.put("supportedLanguages", languageStats.keySet().size());
        
        return overall;
    }
}