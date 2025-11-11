package Game0_11;

/**
 * C函数统计数据类 - 就像一个"成绩单"，记录所有函数的统计信息
 * 包括最大值、最小值、平均值、中位数等
 */
public class CFunctionStatistics {
    
    // 函数总数
    public int totalFunctions = 0;
    
    // 函数长度的统计数据
    public int minLength = 0;           // 最短函数的行数
    public int maxLength = 0;           // 最长函数的行数
    public double avgLength = 0.0;      // 平均函数长度
    public double medianLength = 0.0;   // 中位数函数长度
    
    // 记录最长和最短的函数信息
    public CFunctionAnalyzer.FunctionInfo longestFunction = null;
    public CFunctionAnalyzer.FunctionInfo shortestFunction = null;
    
    /**
     * 获取统计摘要字符串
     */
    public String getSummary() {
        if (totalFunctions == 0) {
            return "没有找到任何C语言函数";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("📊 C语言函数统计结果\n");
        sb.append("=".repeat(50)).append("\n");
        sb.append(String.format("函数总数: %d 个\n", totalFunctions));
        sb.append(String.format("平均长度: %.2f 行\n", avgLength));
        sb.append(String.format("中位数长度: %.2f 行\n", medianLength));
        sb.append(String.format("最短函数: %d 行", minLength));
        
        if (shortestFunction != null) {
            sb.append(String.format(" (%s 在 %s)", 
                shortestFunction.name, shortestFunction.fileName));
        }
        sb.append("\n");
        
        sb.append(String.format("最长函数: %d 行", maxLength));
        if (longestFunction != null) {
            sb.append(String.format(" (%s 在 %s)", 
                longestFunction.name, longestFunction.fileName));
        }
        sb.append("\n");
        sb.append("=".repeat(50));
        
        return sb.toString();
    }
    
    /**
     * 判断函数长度是否合理
     * 一般认为函数不应该超过50行
     */
    public String getHealthAdvice() {
        StringBuilder advice = new StringBuilder();
        advice.append("\n💡 代码健康建议:\n");
        
        if (avgLength > 50) {
            advice.append("⚠️ 平均函数长度超过50行，建议将大函数拆分成更小的函数\n");
        } else if (avgLength < 10) {
            advice.append("✅ 函数长度控制得很好，保持简洁是好习惯！\n");
        } else {
            advice.append("✅ 函数长度适中，代码结构良好\n");
        }
        
        if (maxLength > 100) {
            advice.append(String.format("⚠️ 最长的函数 '%s' 有 %d 行，强烈建议重构\n", 
                longestFunction != null ? longestFunction.name : "未知", maxLength));
        }
        
        return advice.toString();
    }
}
