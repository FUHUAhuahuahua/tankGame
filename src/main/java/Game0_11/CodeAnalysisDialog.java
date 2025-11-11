package Game0_11;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * 代码分析参数设置对话框 - 就像一个"控制面板"
 * 让用户可以选择要分析的文件夹和各种统计选项
 */
public class CodeAnalysisDialog extends JDialog {
    
    // 用户的选择结果
    private String selectedFolder = null;
    private String selectedLanguage = "C";
    private boolean countEmptyLines = true;
    private boolean countCommentLines = true;
    private boolean confirmed = false;
    
    // UI组件
    private JTextField folderField;
    private JComboBox<String> languageCombo;
    private JCheckBox emptyLinesCheckBox;
    private JCheckBox commentLinesCheckBox;
    
    public CodeAnalysisDialog(JFrame parent) {
        super(parent, "代码分析设置", true);
        initUI();
        setSize(500, 350);
        setLocationRelativeTo(parent);
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        
        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 标题
        JLabel titleLabel = new JLabel("🔧 代码分析参数设置");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(titleLabel, gbc);
        
        // 文件夹选择
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        mainPanel.add(new JLabel("📁 选择文件夹:"), gbc);
        
        folderField = new JTextField();
        folderField.setEditable(false);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(folderField, gbc);
        
        JButton browseButton = new JButton("浏览...");
        browseButton.addActionListener(e -> chooseFolder());
        gbc.gridx = 2;
        gbc.weightx = 0;
        mainPanel.add(browseButton, gbc);
        
        // 语言选择
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("💻 编程语言:"), gbc);
        
        languageCombo = new JComboBox<>(new String[]{"C", "C++", "Java", "Python", "所有语言"});
        languageCombo.setSelectedItem("C");
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        mainPanel.add(languageCombo, gbc);
        
        // 分隔线
        JSeparator separator = new JSeparator();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 0, 15, 0);
        mainPanel.add(separator, gbc);
        
        // 统计选项标题
        JLabel optionsLabel = new JLabel("📊 统计选项:");
        optionsLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 5, 10, 5);
        mainPanel.add(optionsLabel, gbc);
        
        // 空行选项
        emptyLinesCheckBox = new JCheckBox("统计空行数", true);
        emptyLinesCheckBox.setToolTipText("勾选后会把空行也算入函数长度");
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        mainPanel.add(emptyLinesCheckBox, gbc);
        
        // 注释行选项
        commentLinesCheckBox = new JCheckBox("统计注释行数", true);
        commentLinesCheckBox.setToolTipText("勾选后会把注释行也算入函数长度");
        gbc.gridy = 6;
        mainPanel.add(commentLinesCheckBox, gbc);
        
        // 提示信息
        JLabel tipLabel = new JLabel("💡 提示: 取消勾选可以只统计纯代码行数");
        tipLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tipLabel.setForeground(Color.GRAY);
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 5, 5, 5);
        mainPanel.add(tipLabel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton startButton = new JButton("开始分析");
        JButton cancelButton = new JButton("取消");
        
        startButton.addActionListener(e -> {
            if (validateInput()) {
                saveSettings();
                confirmed = true;
                dispose();
            }
        });
        
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        
        buttonPanel.add(startButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 设置默认按钮
        getRootPane().setDefaultButton(startButton);
    }
    
    /**
     * 选择文件夹
     */
    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("选择要分析的代码文件夹");
        
        // 设置默认路径为当前项目目录
        chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            folderField.setText(selectedFile.getAbsolutePath());
        }
    }
    
    /**
     * 验证输入
     */
    private boolean validateInput() {
        if (folderField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "请先选择要分析的文件夹！", 
                "提示", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        File folder = new File(folderField.getText());
        if (!folder.exists() || !folder.isDirectory()) {
            JOptionPane.showMessageDialog(this, 
                "选择的文件夹不存在或不是有效的文件夹！", 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * 保存设置
     */
    private void saveSettings() {
        selectedFolder = folderField.getText();
        selectedLanguage = (String) languageCombo.getSelectedItem();
        countEmptyLines = emptyLinesCheckBox.isSelected();
        countCommentLines = commentLinesCheckBox.isSelected();
    }
    
    // Getter方法
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public String getSelectedFolder() {
        return selectedFolder;
    }
    
    public String getSelectedLanguage() {
        return selectedLanguage;
    }
    
    public boolean isCountEmptyLines() {
        return countEmptyLines;
    }
    
    public boolean isCountCommentLines() {
        return countCommentLines;
    }
}
