package Game0_17;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * 代码统计系统 - 管理代码统计和竞猜功能
 */
public class CodeStatsSystem implements GameFrame.CodeStatsSystem {
    
    private CodeStatistics codeStats;
    private String lastScannedPath;
    
    @Override
    public void showStatsDialog(GameFrame parent) {
        // 如果还没有扫描过，先让用户选择文件夹
        if (codeStats == null) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setDialogTitle("选择要统计的代码文件夹");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            
            int result = chooser.showOpenDialog(parent);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFolder = chooser.getSelectedFile();
                lastScannedPath = selectedFolder.getAbsolutePath();
                
                // 显示进度对话框
                JDialog progressDialog = new JDialog(parent, "扫描中...", true);
                JProgressBar progressBar = new JProgressBar();
                progressBar.setIndeterminate(true);
                progressBar.setString("正在扫描代码文件...");
                progressBar.setStringPainted(true);
                
                progressDialog.add(progressBar);
                progressDialog.setSize(300, 100);
                progressDialog.setLocationRelativeTo(parent);
                
                // 在后台线程中扫描
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        codeStats = new CodeStatistics();
                        codeStats.scanFolder(lastScannedPath);
                        return null;
                    }
                    
                    @Override
                    protected void done() {
                        progressDialog.dispose();
                        
                        // 检查是否有数据，如果有则显示竞猜窗口
                        if (codeStats.getTotalFiles() > 0) {
                            showLanguageGuessDialog(parent, codeStats, lastScannedPath);
                        } else {
                            // 空文件夹，直接显示统计界面
                            new CodeStatsFrame(parent, codeStats, lastScannedPath);
                        }
                    }
                };
                
                worker.execute();
                progressDialog.setVisible(true);
            }
        } else {
            // 已经扫描过，直接显示结果
            new CodeStatsFrame(parent, codeStats, lastScannedPath);
        }
    }
    
    @Override
    public void triggerRedPacketRain(GameFrame game) {
        // 触发红包雨功能
        game.spawnRedPacketRain(20);
        JOptionPane.showMessageDialog(game, "红包雨来啦！快去抢红包！", "红包雨", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 显示语言竞猜对话框
     */
    private void showLanguageGuessDialog(GameFrame parent, CodeStatistics codeStats, String scannedPath) {
        // 获取语言统计数据
        Map<String, Integer> languageLines = codeStats.getLanguageLineCount();
        if (languageLines.isEmpty()) {
            new CodeStatsFrame(parent, codeStats, scannedPath);
            return;
        }
        
        // 找出代码量最多的语言
        String topLanguage = "";
        int tempMaxLines = 0;
        for (Map.Entry<String, Integer> entry : languageLines.entrySet()) {
            if (entry.getValue() > tempMaxLines) {
                tempMaxLines = entry.getValue();
                topLanguage = entry.getKey();
            }
        }
        final int maxLines = tempMaxLines;
        
        // 创建竞猜对话框
        JDialog guessDialog = new JDialog(parent, "🎯 有奖竞猜", true);
        guessDialog.setSize(500, 400);
        guessDialog.setLocationRelativeTo(parent);
        guessDialog.setLayout(new BorderLayout());
        
        // 顶部标题面板
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(52, 152, 219));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("猜一猜：哪种编程语言的代码最多？");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        // 中间选项面板
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(0, 2, 10, 10));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 获取所有语言并排序
        List<String> languages = new ArrayList<>(languageLines.keySet());
        languages.sort((a, b) -> languageLines.get(b).compareTo(languageLines.get(a)));
        
        // 创建选项按钮（最多显示8个）
        ButtonGroup group = new ButtonGroup();
        List<JRadioButton> buttons = new ArrayList<>();
        int optionCount = Math.min(languages.size(), 8);
        
        for (int i = 0; i < optionCount; i++) {
            String lang = languages.get(i);
            JRadioButton button = new JRadioButton(lang);
            button.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            button.setActionCommand(lang);
            group.add(button);
            buttons.add(button);
            optionsPanel.add(button);
        }
        
        // 底部按钮面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        
        JButton submitBtn = new JButton("提交答案");
        submitBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        submitBtn.setBackground(new Color(46, 204, 113));
        submitBtn.setForeground(Color.WHITE);
        
        JButton skipBtn = new JButton("跳过竞猜");
        skipBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        final String correctAnswer = topLanguage;
        
        submitBtn.addActionListener(e -> {
            String selected = group.getSelection() != null ? group.getSelection().getActionCommand() : null;
            
            if (selected == null) {
                JOptionPane.showMessageDialog(guessDialog, "请先选择一个答案！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            guessDialog.dispose();
            
            // 显示结果
            if (selected.equals(correctAnswer)) {
                // 猜对了
                JOptionPane.showMessageDialog(parent, 
                    "🎉 恭喜你猜对了！\n\n" + correctAnswer + " 确实是代码量最多的语言！\n" +
                    "共有 " + String.format("%,d", maxLines) + " 行代码\n\n" +
                    "奖励：触发一次红包雨！", 
                    "猜对啦！", JOptionPane.INFORMATION_MESSAGE);
                
                // 触发红包雨作为奖励
                parent.spawnRedPacketRain(30);
            } else {
                // 猜错了
                JOptionPane.showMessageDialog(parent, 
                    "😅 很遗憾，猜错了！\n\n正确答案是：" + correctAnswer + 
                    "\n共有 " + String.format("%,d", maxLines) + " 行代码\n\n" +
                    "你选择的 " + selected + " 有 " + String.format("%,d", languageLines.get(selected)) + " 行代码", 
                    "再接再厉", JOptionPane.INFORMATION_MESSAGE);
            }
            
            // 显示统计界面
            new CodeStatsFrame(parent, codeStats, scannedPath);
        });
        
        skipBtn.addActionListener(e -> {
            guessDialog.dispose();
            new CodeStatsFrame(parent, codeStats, scannedPath);
        });
        
        bottomPanel.add(submitBtn);
        bottomPanel.add(skipBtn);
        
        // 添加提示信息
        JPanel hintPanel = new JPanel();
        hintPanel.setLayout(new BoxLayout(hintPanel, BoxLayout.Y_AXIS));
        hintPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel hintLabel1 = new JLabel("💡 提示：猜对有红包雨奖励哦！");
        hintLabel1.setFont(new Font("微软雅黑", Font.ITALIC, 14));
        hintLabel1.setForeground(new Color(155, 89, 182));
        hintLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 添加一个有趣的统计信息作为提示
        String funFact = "";
        if (languages.size() > 5) {
            funFact = "🔍 这个项目包含了 " + languages.size() + " 种不同的编程语言！";
        } else if (maxLines > 10000) {
            funFact = "🚀 这是一个大型项目，代码总量超过万行！";
        } else if (maxLines < 1000) {
            funFact = "🌱 这是一个精简的项目，代码简洁高效！";
        } else {
            funFact = "📊 总共扫描了 " + codeStats.getTotalFiles() + " 个文件！";
        }
        
        JLabel hintLabel2 = new JLabel(funFact);
        hintLabel2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        hintLabel2.setForeground(new Color(52, 73, 94));
        hintLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        hintPanel.add(hintLabel1);
        hintPanel.add(Box.createVerticalStrut(5));
        hintPanel.add(hintLabel2);
        
        // 组装对话框
        guessDialog.add(titlePanel, BorderLayout.NORTH);
        guessDialog.add(new JScrollPane(optionsPanel), BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(hintPanel, BorderLayout.NORTH);
        southPanel.add(bottomPanel, BorderLayout.SOUTH);
        guessDialog.add(southPanel, BorderLayout.SOUTH);
        
        guessDialog.setVisible(true);
    }
}
