package Game0_17;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * 技能点名系统 v0.16
 * 可独立运行，也可从抢红包游戏跳转
 */
public class SkillSystem extends Frame {

    // ★ 使用 GameFrame 的数据库配置（如果可用），否则使用本地配置
    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    private JTable statsTable;
    private DefaultTableModel tableModel;
    private JLabel totalCallLabel;
    private JLabel usageRateLabel;

    // 游戏引用（从游戏跳转时使用）
    private Frame gameFrame;

    // 独立运行时的构造函数
    public SkillSystem() {
        this(null);
    }

    // 从游戏跳转时的构造函数
    public SkillSystem(Frame gameFrame) {
        this.gameFrame = gameFrame;

        // 设置数据库配置
        try {
            // 尝试使用 GameFrame 的配置
            dbUrl = GameFrame.DB_URL;
            dbUser = GameFrame.DB_USER;
            dbPassword = GameFrame.DB_PASSWORD;
        } catch (NoClassDefFoundError e) {
            // 独立运行时使用本地配置
            dbUrl = "jdbc:mysql://localhost:3306/duck_game?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            dbUser = "root";
            dbPassword = "thedangerinmyheart";  // 改成你的密码
        }

        initDatabase();
        initUI();
        loadStatsFromDB();
        setVisible(true);
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS skill_stats (" +
                    "duck_name VARCHAR(20) PRIMARY KEY, " +
                    "skill_name VARCHAR(50), " +
                    "called_count INT DEFAULT 0, " +
                    "used_count INT DEFAULT 0, " +
                    "not_used_count INT DEFAULT 0" +
                    ")");

            String[] names = {"唐小哥", "唐老二", "唐小弟"};
            String[] skills = {"速度+3", "体积变大", "金额x1.5"};

            for (int i = 0; i < names.length; i++) {
                stmt.execute("INSERT IGNORE INTO skill_stats (duck_name, skill_name) VALUES ('"
                        + names[i] + "', '" + skills[i] + "')");
            }

        } catch (SQLException e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
        }
    }

    private void initUI() {
        setTitle("唐小鸭技能点名系统 v0.16");
        setSize(650, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                returnToGame();
            }
        });

        // 顶部面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JLabel titleLabel = new JLabel("唐小鸭技能点名统计", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
        totalCallLabel = new JLabel("总点名次数: 0");
        totalCallLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        usageRateLabel = new JLabel("平均使用率: 0%");
        usageRateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        summaryPanel.add(totalCallLabel);
        summaryPanel.add(usageRateLabel);
        headerPanel.add(summaryPanel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        // 表格
        String[] columns = {"唐小鸭", "技能", "点名次数", "使用次数", "未使用次数", "使用率"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        statsTable = new JTable(tableModel);
        statsTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        statsTable.setRowHeight(35);
        statsTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(statsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("技能使用统计"));
        add(scrollPane, BorderLayout.CENTER);

        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton refreshBtn = new JButton("刷新数据");
        refreshBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        refreshBtn.addActionListener(e -> loadStatsFromDB());

        JButton resetBtn = new JButton("重置统计");
        resetBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        resetBtn.setForeground(Color.RED);
        resetBtn.addActionListener(e -> resetStats());

        JButton backBtn = new JButton("返回游戏");
        backBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        backBtn.setBackground(new Color(76, 175, 80));
        backBtn.addActionListener(e -> returnToGame());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(resetBtn);
        buttonPanel.add(backBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        // 右侧技能说明
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("技能说明"));
        infoPanel.setPreferredSize(new Dimension(150, 0));

        String[] infos = {
                "唐小哥", "速度+3", "",
                "唐老二", "体积变大", "",
                "唐小弟", "金额x1.5", ""
        };
        for (String info : infos) {
            JLabel lbl = new JLabel(info);
            lbl.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            infoPanel.add(lbl);
        }

        add(infoPanel, BorderLayout.EAST);
    }

    private void loadStatsFromDB() {
        tableModel.setRowCount(0);
        int totalCalls = 0;
        int totalUsed = 0;

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM skill_stats ORDER BY duck_name")) {

            while (rs.next()) {
                String name = rs.getString("duck_name");
                String skill = rs.getString("skill_name");
                if (skill == null) skill = "";
                int called = rs.getInt("called_count");
                int used = rs.getInt("used_count");
                int notUsed = rs.getInt("not_used_count");
                double rate = called > 0 ? (used * 100.0 / called) : 0;

                tableModel.addRow(new Object[]{
                        name, skill, called, used, notUsed, String.format("%.1f%%", rate)
                });

                totalCalls += called;
                totalUsed += used;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "读取数据失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }

        totalCallLabel.setText("总点名次数: " + totalCalls);
        double avgRate = totalCalls > 0 ? (totalUsed * 100.0 / totalCalls) : 0;
        usageRateLabel.setText("平均使用率: " + String.format("%.1f%%", avgRate));
    }

    private void resetStats() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要重置所有统计数据吗？\n此操作不可恢复！",
                "确认重置", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                 Statement stmt = conn.createStatement()) {

                stmt.execute("UPDATE skill_stats SET called_count=0, used_count=0, not_used_count=0");

                JOptionPane.showMessageDialog(this, "统计数据已重置", "完成", JOptionPane.INFORMATION_MESSAGE);
                loadStatsFromDB();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "重置失败: " + e.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ★ 返回游戏
    private void returnToGame() {
        this.dispose();  // 关闭当前窗口
        if (gameFrame != null) {
            gameFrame.setVisible(true);  // 显示游戏窗口
        } else {
            System.exit(0);  // 独立运行时退出
        }
    }

    // 独立运行入口
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SkillSystem();
        });
    }
}