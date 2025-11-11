package Game0_11;

import javax.swing.*;

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
        
        // 显示进度对话框
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setString("正在分析代码，请稍候...");
        progressBar.setStringPainted(true);
        
        JDialog progressDialog = new JDialog(tempFrame, "分析中", true);
        progressDialog.add(progressBar);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(tempFrame);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        // 在后台线程中执行分析
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private CFunctionAnalyzer analyzer;
            private CFunctionStatistics statistics;
            private Exception error;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // 创建分析器
                    analyzer = new CFunctionAnalyzer();
                    analyzer.setCountOptions(countEmptyLines, countCommentLines);
                    
                    // 执行分析
                    System.out.println("开始分析文件夹: " + folderPath);
                    analyzer.analyzeFolder(folderPath);
                    
                    // 计算统计数据
                    statistics = analyzer.calculateStatistics();
                    
                } catch (Exception e) {
                    error = e;
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                // 关闭进度对话框
                progressDialog.dispose();
                tempFrame.dispose();
                
                // 检查是否有错误
                if (error != null) {
                    JOptionPane.showMessageDialog(null,
                        "分析过程中出现错误:\n" + error.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                    return;
                }
                
                // 检查是否找到了函数
                if (statistics.totalFunctions == 0) {
                    int result = JOptionPane.showConfirmDialog(null,
                        "没有在指定文件夹中找到任何C语言函数。\n" +
                        "可能的原因:\n" +
                        "1. 文件夹中没有.c或.h文件\n" +
                        "2. 文件中没有符合C语言规范的函数定义\n\n" +
                        "是否要重新选择文件夹?",
                        "未找到函数",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    
                    if (result == JOptionPane.YES_OPTION) {
                        startApplication(); // 重新开始
                    } else {
                        System.exit(0);
                    }
                    return;
                }
                
                // 显示结果窗口
                StatisticsFrame frame = new StatisticsFrame(analyzer, statistics);
                frame.setVisible(true);
                
                // 在控制台也输出一份统计结果
                System.out.println("\n" + statistics.getSummary());
                System.out.println(statistics.getHealthAdvice());
            }
        };
        
        // 启动后台任务
        worker.execute();
        
        // 显示进度对话框（模态）
        progressDialog.setVisible(true);
    }
}
