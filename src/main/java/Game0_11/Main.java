package Game0_11;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import javax.swing.SwingWorker;
import javax.swing.SwingUtilities;
import java.util.HashMap;
import javax.swing.JOptionPane;

/**
 * 主程序入口 - 代码统计分析器的启动器
 * 就像一个"总指挥"，负责启动整个程序
 */
public class Main {
    
    public static void main(String[] args) {
        // 设置系统外观，让界面更美观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // 如果设置失败也没关系，使用默认外观
        }
        
        // 在事件调度线程中启动GUI
        SwingUtilities.invokeLater(() -> {
            startApplication();
        });
    }
    
    /**
     * 启动应用程序
     */
    private static void startApplication() {
        // 创建一个临时的主窗口（用于显示对话框）
        JFrame tempFrame = new JFrame();
        tempFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 显示参数设置对话框
        CodeAnalysisDialog dialog = new CodeAnalysisDialog(tempFrame);
        dialog.setVisible(true);
        
        // 检查用户是否确认
        if (!dialog.isConfirmed()) {
            System.out.println("用户取消了操作");
            System.exit(0);
            return;
        }
        
        // 获取用户的选择
        String folderPath = dialog.getSelectedFolder();
        String language = dialog.getSelectedLanguage();
        boolean countEmptyLines = dialog.isCountEmptyLines();
        boolean countCommentLines = dialog.isCountCommentLines();
        String exportFormat = dialog.getExportFormat();
        
        // 显示进度对话框
        JDialog progressDialog = new JDialog(tempFrame, "分析中...", true);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(tempFrame);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        JLabel progressLabel = new JLabel("正在分析多语言代码，请稍候...", JLabel.CENTER);
        progressDialog.add(progressLabel);
        
        // 在后台线程中执行分析
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private MultiLanguageAnalyzer analyzer;
            private Map<MultiLanguageAnalyzer.Language, MultiLanguageAnalyzer.LanguageStatistics> languageStats;
            private Map<String, Object> overallStats;
            
            @Override
            protected Void doInBackground() throws Exception {
                analyzer = new MultiLanguageAnalyzer();
                analyzer.setCountOptions(countEmptyLines, countCommentLines);
                analyzer.analyzeFolder(folderPath);
                languageStats = analyzer.getLanguageStatistics();
                overallStats = analyzer.getOverallStatistics();
                return null;
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                
                try {
                    get(); // 检查是否有异常
                    
                    int totalFunctions = (Integer) overallStats.get("totalFunctions");
                    int totalSourceFiles = (Integer) overallStats.get("totalSourceFiles");
                    
                    if (totalSourceFiles == 0) {
                        int choice = JOptionPane.showConfirmDialog(
                            null,
                            "没有在指定文件夹中找到任何支持的源代码文件。\n\n" +
                            "支持的语言：C/C++/Java/Python/C#\n" +
                            "支持的文件扩展名：.c .h .cpp .java .py .cs 等\n\n" +
                            "可能的原因：\n" +
                            "1. 文件夹中没有支持的源代码文件\n" +
                            "2. 文件编码问题导致无法读取\n\n" +
                            "是否退出程序？",
                            "未找到源代码文件",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );
                        
                        if (choice == JOptionPane.YES_OPTION) {
                            System.exit(0);
                        } else {
                            // 重新开始
                            startApplication();
                        }
                    } else {
                        // 显示多语言统计结果
                        MultiLanguageStatisticsFrame frame = new MultiLanguageStatisticsFrame(
                            analyzer, languageStats, overallStats, exportFormat);
                        frame.setVisible(true);
                    }
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        null,
                        "分析过程中发生错误：\n" + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE
                    );
                    e.printStackTrace();
                }
                // 已在上面处理结果显示
                
                // 在控制台输出总体统计摘要
                if (overallStats != null) {
                    System.out.println("\n================ 总体统计 ================");
                    System.out.println("源文件: " + overallStats.get("totalSourceFiles")
                        + ", 总行: " + overallStats.get("totalLines")
                        + ", 代码: " + overallStats.get("totalCodeLines")
                        + ", 空行: " + overallStats.get("totalEmptyLines")
                        + ", 注释: " + overallStats.get("totalCommentLines")
                        + ", 函数: " + overallStats.get("totalFunctions"));
                    System.out.println("========================================\n");
                }
            }
        };
        
        // 启动后台任务
        worker.execute();
        
        // 显示进度对话框（模态）
        progressDialog.setVisible(true);
    }
}
