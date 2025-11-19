package Game0_11;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * C语言函数分析器 - 就像一个"函数侦探"，能够找出C语言代码中的所有函数
 * 并统计每个函数的长度（行数）
 */
public class CFunctionAnalyzer {
    
    // 用于匹配C语言函数定义的正则表达式
    // 这个表达式能识别类似 "int main()" 或 "void func(int a, int b)" 这样的函数定义
    private static final Pattern FUNCTION_PATTERN = Pattern.compile(
        "^\\s*(?:extern\\s+)?(?:static\\s+)?(?:inline\\s+)?(?:const\\s+)?(?:unsigned\\s+|signed\\s+)?"
        + "(?:(?:long\\s+long|long\\s+double)|long|short|void|int|char|float|double|bool|size_t|u?int\\d+_t|struct\\s+\\w+|[A-Za-z_][A-Za-z0-9_]*\\s*(?:\\*+)?)\\s+"
        + "([A-Za-z_][A-Za-z0-9_]*)\\s*\\([^;{}]*\\)\\s*\\{",
        Pattern.MULTILINE
    );
    
    // 存储所有找到的函数信息
    private List<FunctionInfo> functions = new ArrayList<>();
    
    // 统计选项
    private boolean countEmptyLines = true;
    private boolean countCommentLines = true;
    
    /**
     * 函数信息类 - 存储每个函数的详细信息
     */
    public static class FunctionInfo {
        public String name;           // 函数名
        public String fileName;       // 所在文件名
        public int startLine;         // 开始行号
        public int endLine;          // 结束行号
        public int totalLines;       // 总行数
        public int codeLines;        // 代码行数（不含空行和注释）
        public int emptyLines;       // 空行数
        public int commentLines;     // 注释行数
        
        public FunctionInfo(String name, String fileName, int startLine) {
            this.name = name;
            this.fileName = fileName;
            this.startLine = startLine;
        }
        
        // 计算函数长度
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
     * 分析指定文件夹中的所有C语言文件
     */
    public void analyzeFolder(String folderPath) throws IOException {
        functions.clear();
        
        // 遍历文件夹，找出所有的.c和.h文件
        Files.walk(Paths.get(folderPath))
            .filter(Files::isRegularFile)
            .filter(path -> {
                String name = path.getFileName().toString().toLowerCase();
                return name.endsWith(".c") || name.endsWith(".h");
            })
            .forEach(path -> {
                try {
                    analyzeFile(path);
                } catch (IOException e) {
                    System.err.println("分析文件出错: " + path + " - " + e.getMessage());
                }
            });
    }
    
    /**
     * 分析单个C语言文件
     */
    private void analyzeFile(Path filePath) throws IOException {
        String content;
        try {
            // 优先按UTF-8读取（Java默认），便于跨平台
            content = Files.readString(filePath);
        } catch (java.nio.charset.MalformedInputException e) {
            // 若文件为本地系统编码（如Windows中文环境下的GBK），则回退到系统默认编码
            try {
                content = Files.readString(filePath, java.nio.charset.Charset.defaultCharset());
            } catch (java.nio.charset.MalformedInputException e2) {
                // 再次回退尝试GBK（常见于中文Windows）
                try {
                    content = Files.readString(filePath, java.nio.charset.Charset.forName("GBK"));
                } catch (Exception e3) {
                    // 仍失败则抛出最初的异常信息
                    throw e;
                }
            }
        }
        String fileName = filePath.getFileName().toString();
        // 使用通用换行分隔符匹配，兼容\r\n/\n
        String[] lines = content.split("\\R");
        
        // 使用正则表达式找出所有函数
        Matcher matcher = FUNCTION_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String functionName = matcher.group(1);
            int startPos = matcher.start();
            
            // 计算函数开始的行号
            int startLine = getLineNumber(content, startPos);
            
            // 创建函数信息对象
            FunctionInfo funcInfo = new FunctionInfo(functionName, fileName, startLine);
            
            // 找到函数的结束位置（匹配大括号）
            int endLine = findFunctionEnd(lines, startLine - 1);
            funcInfo.endLine = endLine;
            funcInfo.calculateLength();
            
            // 分析函数内容，统计空行和注释行
            analyzeFunctionContent(lines, funcInfo);
            
            functions.add(funcInfo);
        }
    }
    
    /**
     * 获取字符位置对应的行号
     */
    private int getLineNumber(String content, int position) {
        int lineNumber = 1;
        for (int i = 0; i < position && i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                lineNumber++;
            }
        }
        return lineNumber;
    }
    
    /**
     * 找到函数的结束位置（通过匹配大括号）
     */
    private int findFunctionEnd(String[] lines, int startLineIndex) {
        int braceCount = 0;
        boolean inFunction = false;
        
        for (int i = startLineIndex; i < lines.length; i++) {
            String line = lines[i];
            
            // 计算这一行中的大括号数量
            for (char c : line.toCharArray()) {
                if (c == '{') {
                    braceCount++;
                    inFunction = true;
                } else if (c == '}') {
                    braceCount--;
                }
            }
            
            // 当大括号匹配完成时，函数结束
            if (inFunction && braceCount == 0) {
                return i + 1; // 返回行号（从1开始）
            }
        }
        
        // 如果没找到匹配的结束括号，返回文件末尾
        return lines.length;
    }
    
    /**
     * 分析函数内容，统计空行和注释行
     */
    private void analyzeFunctionContent(String[] lines, FunctionInfo funcInfo) {
        int emptyLines = 0;
        int commentLines = 0;
        boolean inMultiLineComment = false;
        
        for (int i = funcInfo.startLine - 1; i < funcInfo.endLine && i < lines.length; i++) {
            String line = lines[i].trim();
            
            // 检查多行注释的开始
            if (line.contains("/*")) {
                inMultiLineComment = true;
            }
            
            // 统计空行
            if (line.isEmpty()) {
                emptyLines++;
            }
            // 统计注释行
            else if (inMultiLineComment || line.startsWith("//") || 
                     (line.startsWith("/*") && line.endsWith("*/"))) {
                commentLines++;
            }
            
            // 检查多行注释的结束
            if (line.contains("*/")) {
                inMultiLineComment = false;
            }
        }
        
        funcInfo.emptyLines = emptyLines;
        funcInfo.commentLines = commentLines;
        funcInfo.codeLines = funcInfo.totalLines - emptyLines - commentLines;
    }
    
    /**
     * 获取所有函数信息
     */
    public List<FunctionInfo> getFunctions() {
        return new ArrayList<>(functions);
    }
    
    /**
     * 计算函数长度的统计数据
     */
    public CFunctionStatistics calculateStatistics() {
        CFunctionStatistics stats = new CFunctionStatistics();
        
        if (functions.isEmpty()) {
            return stats;
        }
        
        // 收集所有函数的长度
        List<Integer> lengths = new ArrayList<>();
        for (FunctionInfo func : functions) {
            int length = func.totalLines;
            if (!countEmptyLines) {
                length -= func.emptyLines;
            }
            if (!countCommentLines) {
                length -= func.commentLines;
            }
            lengths.add(length);
        }
        
        // 排序，用于计算中位数
        Collections.sort(lengths);
        
        // 计算统计数据
        stats.totalFunctions = functions.size();
        stats.minLength = lengths.get(0);
        stats.maxLength = lengths.get(lengths.size() - 1);
        
        // 计算平均值
        int sum = 0;
        for (int length : lengths) {
            sum += length;
        }
        stats.avgLength = (double) sum / lengths.size();
        
        // 计算中位数
        if (lengths.size() % 2 == 0) {
            stats.medianLength = (lengths.get(lengths.size() / 2 - 1) + 
                                 lengths.get(lengths.size() / 2)) / 2.0;
        } else {
            stats.medianLength = lengths.get(lengths.size() / 2);
        }
        
        // 找出最长和最短的函数
        for (FunctionInfo func : functions) {
            int length = func.totalLines;
            if (!countEmptyLines) length -= func.emptyLines;
            if (!countCommentLines) length -= func.commentLines;
            
            if (length == stats.minLength && stats.shortestFunction == null) {
                stats.shortestFunction = func;
            }
            if (length == stats.maxLength && stats.longestFunction == null) {
                stats.longestFunction = func;
            }
        }
        
        return stats;
    }
}
