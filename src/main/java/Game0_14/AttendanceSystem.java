package Game0_14;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;

// Apache POI 导入 - 使用别名避免冲突
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * 点名系统主类 - Excel版本（适配红包游戏跳转）
 * 已从 JFrame 改为 java.awt.Frame
 * 需要在pom.xml中添加Apache POI依赖
 */
public class AttendanceSystem extends Frame {
    // 红包游戏窗口引用（新增：用于返回游戏）
    private Window redPacketGameFrame;

    private List<Student> students = new ArrayList<>();
    private List<AttendanceRecord> records = new ArrayList<>();
    private AttendanceSession currentSession;
    private Timer lateTimer = new Timer(true);
    private File currentExcelFile = null;

    // UI组件
    private JPanel mainPanel;
    private JLabel photoLabel;
    private JLabel nameLabel;
    private JLabel studentIdLabel;
    private JLabel fileLabel;
    private JButton loadButton;
    private JButton startButton;
    private JButton presentButton;
    private JButton absentButton;
    private JButton summaryButton;
    private JButton saveButton;
    private JButton backToGameButton; // 新增：返回红包游戏按钮

    // 构造函数：接收红包游戏窗口引用
    public AttendanceSystem(Frame redPacketGameFrame) {
        this.redPacketGameFrame = redPacketGameFrame;

        setTitle("点名系统 - Excel版");
        setSize(850, 650);
        // Frame 替换 JFrame 的关闭操作：通过 WindowListener 实现
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose(); // 仅关闭当前Frame，不退出整个程序
            }
        });
        // Frame 居中显示（兼容实现）
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        // Frame 默认布局是 BorderLayout，直接设置主面板
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 顶部文件加载区（新增返回游戏按钮）
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filePanel.setBorder(BorderFactory.createTitledBorder("数据文件"));

        loadButton = new JButton("加载Excel");
        loadButton.setFont(new java.awt.Font("微软雅黑", java.awt.Font.PLAIN, 14));
        loadButton.addActionListener(e -> loadExcelFile());

        fileLabel = new JLabel("未加载文件");
        fileLabel.setFont(new java.awt.Font("微软雅黑", java.awt.Font.PLAIN, 12));
        fileLabel.setForeground(java.awt.Color.GRAY);

        saveButton = new JButton("保存结果");
        saveButton.setFont(new java.awt.Font("微软雅黑", java.awt.Font.PLAIN, 14));
        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> saveToExcel());

        // 新增：返回红包游戏按钮
        backToGameButton = new JButton("返回红包游戏");
        backToGameButton.setFont(new java.awt.Font("微软雅黑", java.awt.Font.PLAIN, 14));
        backToGameButton.addActionListener(e -> {
            // 隐藏点名系统，显示红包游戏
            this.setVisible(false);
            if (redPacketGameFrame != null) {
                redPacketGameFrame.setVisible(true);
            }
        });

        filePanel.add(loadButton);
        filePanel.add(fileLabel);
        filePanel.add(Box.createHorizontalStrut(20));
        filePanel.add(saveButton);
        filePanel.add(Box.createHorizontalStrut(10));
        filePanel.add(backToGameButton); // 添加返回按钮

        // 学生信息显示区
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("当前点名学生"));

        photoLabel = new JLabel();
        photoLabel.setPreferredSize(new Dimension(150, 150));
        photoLabel.setHorizontalAlignment(JLabel.CENTER);
        photoLabel.setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY, 2));
        photoLabel.setOpaque(true);
        photoLabel.setBackground(java.awt.Color.LIGHT_GRAY);
        infoPanel.add(photoLabel, BorderLayout.WEST);

        JPanel studentInfoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        nameLabel = new JLabel("姓名: --", JLabel.CENTER);
        nameLabel.setFont(new java.awt.Font("微软雅黑", java.awt.Font.BOLD, 28));
        studentIdLabel = new JLabel("学号: --", JLabel.CENTER);
        studentIdLabel.setFont(new java.awt.Font("微软雅黑", java.awt.Font.PLAIN, 20));
        studentInfoPanel.add(nameLabel);
        studentInfoPanel.add(studentIdLabel);
        infoPanel.add(studentInfoPanel, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filePanel, BorderLayout.NORTH);
        topPanel.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 中间控制按钮区
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));

        startButton = new JButton("开始点名");
        startButton.setFont(new java.awt.Font("微软雅黑", java.awt.Font.BOLD, 18));
        startButton.setPreferredSize(new Dimension(150, 50));
        startButton.setEnabled(false);
        startButton.addActionListener(e -> showAttendanceDialog());

        presentButton = new JButton("到");
        presentButton.setFont(new java.awt.Font("微软雅黑", java.awt.Font.BOLD, 18));
        presentButton.setPreferredSize(new Dimension(100, 50));
        presentButton.setEnabled(false);
        presentButton.setBackground(new java.awt.Color(76, 175, 80));
        presentButton.setForeground(java.awt.Color.WHITE);
        presentButton.addActionListener(e -> markPresent());

        absentButton = new JButton("无人应答");
        absentButton.setFont(new java.awt.Font("微软雅黑", java.awt.Font.PLAIN, 16));
        absentButton.setPreferredSize(new Dimension(120, 50));
        absentButton.setEnabled(false);
        absentButton.setBackground(new java.awt.Color(244, 67, 54));
        absentButton.setForeground(java.awt.Color.WHITE);
        absentButton.addActionListener(e -> markAbsent());

        summaryButton = new JButton("查看统计");
        summaryButton.setFont(new java.awt.Font("微软雅黑", java.awt.Font.PLAIN, 16));
        summaryButton.setPreferredSize(new Dimension(120, 50));
        summaryButton.addActionListener(e -> showSummary());

        controlPanel.add(startButton);
        controlPanel.add(presentButton);
        controlPanel.add(absentButton);
        controlPanel.add(summaryButton);

        mainPanel.add(controlPanel, BorderLayout.CENTER);

        // 底部提示信息
        JTextArea hintArea = new JTextArea(
                "使用说明：\n" +
                        "1. 点击「加载Excel」选择学生名单文件（支持.xlsx和.xls格式）\n" +
                        "2. Excel格式要求：第一行为表头，包含「姓名」「学号」「备注」三列\n" +
                        "3. 点击「开始点名」选择点名方式和策略\n" +
                        "4. 点名完成后点击「保存结果」将考勤记录导出到Excel\n" +
                        "5. 旷课学生在10分钟内到达可手动修改为迟到"
        );
        hintArea.setEditable(false);
        hintArea.setFont(new java.awt.Font("微软雅黑", java.awt.Font.PLAIN, 12));
        hintArea.setBackground(new java.awt.Color(245, 245, 245));
        hintArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(hintArea, BorderLayout.SOUTH);

        // 将主面板添加到Frame
        add(mainPanel);
        // 确保组件可见
        setVisible(true);
    }

    private void loadExcelFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择学生名单Excel文件");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Excel文件 (*.xlsx, *.xls)", "xlsx", "xls"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentExcelFile = chooser.getSelectedFile();
            try {
                readStudentsFromExcel(currentExcelFile);
                fileLabel.setText("已加载: " + currentExcelFile.getName() +
                        " (" + students.size() + "人)");
                fileLabel.setForeground(new java.awt.Color(0, 128, 0));
                startButton.setEnabled(true);
                saveButton.setEnabled(true);
                JOptionPane.showMessageDialog(this,
                        "成功加载 " + students.size() + " 名学生数据！",
                        "加载成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "读取Excel文件失败：\n" + e.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void readStudentsFromExcel(File file) throws IOException {
        students.clear();
        records.clear();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = file.getName().endsWith(".xlsx")
                     ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            int nameCol = -1, idCol = -1, descCol = -1;
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String header = getCellStringValue(cell).trim();
                    if (header.contains("姓名") || header.equalsIgnoreCase("name")) {
                        nameCol = i;
                    } else if (header.contains("学号") || header.equalsIgnoreCase("id")) {
                        idCol = i;
                    } else if (header.contains("备注") || header.contains("描述") ||
                            header.equalsIgnoreCase("description")) {
                        descCol = i;
                    }
                }
            }

            if (nameCol == -1 || idCol == -1) {
                throw new IOException("Excel格式错误：必须包含「姓名」和「学号」列！");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String name = getCellStringValue(row.getCell(nameCol)).trim();
                String studentId = getCellStringValue(row.getCell(idCol)).trim();
                String desc = descCol >= 0 ?
                        getCellStringValue(row.getCell(descCol)).trim() : "";

                if (!name.isEmpty() && !studentId.isEmpty()) {
                    students.add(new Student(name, studentId, desc));
                }
            }
        }

        if (students.isEmpty()) {
            throw new IOException("未读取到有效的学生数据！");
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        CellType cellType = cell.getCellType();
        if (cellType == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cellType == CellType.NUMERIC) {
            double num = cell.getNumericCellValue();
            if (num == Math.floor(num)) {
                return String.valueOf((long) num);
            }
            return String.valueOf(num);
        } else if (cellType == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return "";
    }

    private void saveToExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("保存考勤结果");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel文件 (*.xlsx)", "xlsx"));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        chooser.setSelectedFile(new File("考勤结果_" + sdf.format(new Date()) + ".xlsx"));

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }

            try {
                writeResultsToExcel(file);
                JOptionPane.showMessageDialog(this,
                        "考勤结果已保存到：\n" + file.getAbsolutePath(),
                        "保存成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "保存Excel文件失败：\n" + e.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void writeResultsToExcel(File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // 创建数据样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Sheet1: 学生统计
            Sheet summarySheet = workbook.createSheet("学生统计");
            String[] summaryHeaders = {"姓名", "学号", "点名次数", "出勤次数",
                    "请假次数", "旷课次数", "迟到次数", "出勤率"};

            Row headerRow = summarySheet.createRow(0);
            for (int i = 0; i < summaryHeaders.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(summaryHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Student student : students) {
                int presentCount = (int) records.stream()
                        .filter(r -> r.student.equals(student) && r.status == AttendanceStatus.PRESENT)
                        .count();
                int leaveCount = (int) records.stream()
                        .filter(r -> r.student.equals(student) && r.status == AttendanceStatus.LEAVE)
                        .count();
                int absentCount = (int) records.stream()
                        .filter(r -> r.student.equals(student) && r.status == AttendanceStatus.ABSENT)
                        .count();
                int lateCount = (int) records.stream()
                        .filter(r -> r.student.equals(student) && r.status == AttendanceStatus.LATE)
                        .count();

                double attendanceRate = student.callCount > 0
                        ? (presentCount + lateCount) * 100.0 / student.callCount : 0;

                Row row = summarySheet.createRow(rowNum++);
                int col = 0;
                createCell(row, col++, student.name, dataStyle);
                createCell(row, col++, student.studentId, dataStyle);
                createCell(row, col++, student.callCount, dataStyle);
                createCell(row, col++, presentCount, dataStyle);
                createCell(row, col++, leaveCount, dataStyle);
                createCell(row, col++, absentCount, dataStyle);
                createCell(row, col++, lateCount, dataStyle);
                createCell(row, col++, String.format("%.1f%%", attendanceRate), dataStyle);
            }

            for (int i = 0; i < summaryHeaders.length; i++) {
                summarySheet.autoSizeColumn(i);
            }

            // Sheet2: 点名记录
            Sheet recordSheet = workbook.createSheet("点名记录");
            String[] recordHeaders = {"日期时间", "姓名", "学号", "考勤状态"};

            Row recordHeaderRow = recordSheet.createRow(0);
            for (int i = 0; i < recordHeaders.length; i++) {
                Cell cell = recordHeaderRow.createCell(i);
                cell.setCellValue(recordHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            rowNum = 1;
            for (AttendanceRecord record : records) {
                Row row = recordSheet.createRow(rowNum++);
                createCell(row, 0, dateFmt.format(record.time), dataStyle);
                createCell(row, 1, record.student.name, dataStyle);
                createCell(row, 2, record.student.studentId, dataStyle);
                createCell(row, 3, record.status.getText(), dataStyle);
            }

            for (int i = 0; i < recordHeaders.length; i++) {
                recordSheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    private void createCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
        cell.setCellStyle(style);
    }

    private void showAttendanceDialog() {
        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先加载学生名单！");
            return;
        }

        JDialog dialog = new JDialog(this, "点名配置", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modePanel.add(new JLabel("点名方式: "));
        ButtonGroup modeGroup = new ButtonGroup();
        JRadioButton fullMode = new JRadioButton("全点", true);
        JRadioButton selectMode = new JRadioButton("抽点");
        modeGroup.add(fullMode);
        modeGroup.add(selectMode);
        modePanel.add(fullMode);
        modePanel.add(selectMode);
        panel.add(modePanel);

        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countPanel.add(new JLabel("点名人数: "));
        JComboBox<String> countCombo = new JComboBox<>(new String[]{"10", "15", "20", "自定义"});
        countCombo.setEnabled(false);
        JTextField customCount = new JTextField(5);
        customCount.setEnabled(false);
        countCombo.addActionListener(e ->
                customCount.setEnabled(countCombo.getSelectedItem().equals("自定义")));
        countPanel.add(countCombo);
        countPanel.add(customCount);
        panel.add(countPanel);

        selectMode.addActionListener(e -> {
            countCombo.setEnabled(true);
            if (countCombo.getSelectedItem().equals("自定义")) {
                customCount.setEnabled(true);
            }
        });
        fullMode.addActionListener(e -> {
            countCombo.setEnabled(false);
            customCount.setEnabled(false);
        });

        JPanel strategyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        strategyPanel.add(new JLabel("点名策略: "));
        JComboBox<String> strategyCombo = new JComboBox<>(new String[]{
                "随机选取", "优先旷课最多", "优先点名最少"
        });
        strategyPanel.add(strategyCombo);
        panel.add(strategyPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton confirmButton = new JButton("开始点名");
        confirmButton.setPreferredSize(new Dimension(120, 35));
        confirmButton.addActionListener(e -> {
            try {
                boolean isFullMode = fullMode.isSelected();
                int count = students.size();

                if (!isFullMode) {
                    if (countCombo.getSelectedItem().equals("自定义")) {
                        String text = customCount.getText().trim();
                        if (text.isEmpty()) {
                            JOptionPane.showMessageDialog(dialog, "请输入自定义人数！");
                            return;
                        }
                        count = Integer.parseInt(text);
                    } else {
                        count = Integer.parseInt((String) countCombo.getSelectedItem());
                    }

                    if (count <= 0 || count > students.size()) {
                        JOptionPane.showMessageDialog(dialog,
                                "人数必须在1到" + students.size() + "之间！");
                        return;
                    }
                }

                String strategy = (String) strategyCombo.getSelectedItem();
                dialog.dispose();
                startAttendance(isFullMode, count, strategy);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "请输入有效的数字！");
            }
        });
        buttonPanel.add(confirmButton);
        panel.add(buttonPanel);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void startAttendance(boolean isFullMode, int count, String strategy) {
        List<Student> selectedStudents;

        if (isFullMode) {
            selectedStudents = new ArrayList<>(students);
            Collections.shuffle(selectedStudents);
        } else {
            selectedStudents = selectStudents(count, strategy);
        }

        currentSession = new AttendanceSession(selectedStudents);
        startButton.setEnabled(false);
        presentButton.setEnabled(true);
        absentButton.setEnabled(true);

        callNextStudent();
    }

    private List<Student> selectStudents(int count, String strategy) {
        List<Student> selected = new ArrayList<>(students);

        switch (strategy) {
            case "随机选取":
                Collections.shuffle(selected);
                break;
            case "优先旷课最多":
                selected.sort((s1, s2) -> Integer.compare(s2.absentCount, s1.absentCount));
                break;
            case "优先点名最少":
                selected.sort((s1, s2) -> Integer.compare(s1.callCount, s2.callCount));
                break;
        }

        return selected.subList(0, Math.min(count, selected.size()));
    }

    private void callNextStudent() {
        if (currentSession.hasNext()) {
            Student student = currentSession.next();
            displayStudent(student);
            speakName(student.name);
        } else {
            endAttendance();
        }
    }

    private void displayStudent(Student student) {
        nameLabel.setText("姓名: " + student.name);
        studentIdLabel.setText("学号: " + student.studentId);

        Random rand = new Random(student.studentId.hashCode());
        java.awt.Color color = new java.awt.Color(
                rand.nextInt(200) + 55, rand.nextInt(200) + 55, rand.nextInt(200) + 55);
        photoLabel.setBackground(color);
        photoLabel.setText(student.name.substring(0, 1));
        photoLabel.setFont(new java.awt.Font("微软雅黑", java.awt.Font.BOLD, 60));
        photoLabel.setForeground(java.awt.Color.WHITE);
    }

    private void speakName(String name) {
        try {
            String cmd = String.format(
                    "Add-Type -AssemblyName System.Speech; " +
                            "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                            "$synth.Rate = 0; $synth.Speak('%s')",
                    name.replace("'", "''")
            );
            ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", cmd);
            pb.redirectErrorStream(true);
            pb.start();
        } catch (Exception e) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private void markPresent() {
        if (currentSession != null && currentSession.currentStudent != null) {
            Student student = currentSession.currentStudent;
            student.callCount++;
            records.add(new AttendanceRecord(student, new Date(), AttendanceStatus.PRESENT));
            JOptionPane.showMessageDialog(this, student.name + " 已标记为出勤",
                    "出勤确认", JOptionPane.INFORMATION_MESSAGE);
            callNextStudent();
        }
    }

    private void markAbsent() {
        if (currentSession != null && currentSession.currentStudent != null) {
            Student student = currentSession.currentStudent;
            student.callCount++;

            int result = JOptionPane.showConfirmDialog(this,
                    "该学生是否提前递交了请假条？", "确认考勤状态", JOptionPane.YES_NO_OPTION);

            AttendanceStatus status = (result == JOptionPane.YES_OPTION)
                    ? AttendanceStatus.LEAVE : AttendanceStatus.ABSENT;

            if (status == AttendanceStatus.ABSENT) {
                student.absentCount++;
            }

            AttendanceRecord record = new AttendanceRecord(student, new Date(), status);
            records.add(record);

            if (status == AttendanceStatus.ABSENT) {
                scheduleLateMark(record);
            }

            String statusText = (status == AttendanceStatus.LEAVE) ? "请假" : "旷课";
            JOptionPane.showMessageDialog(this, student.name + " 已标记为" + statusText,
                    "考勤确认", JOptionPane.WARNING_MESSAGE);

            callNextStudent();
        }
    }

    private void scheduleLateMark(AttendanceRecord record) {
        java.util.TimerTask task = new java.util.TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (record.status == AttendanceStatus.ABSENT) {
                        int result = JOptionPane.showConfirmDialog(AttendanceSystem.this,
                                record.student.name + " 是否已经到达教室？\n（旷课后10分钟内到达可标记为迟到）",
                                "迟到确认", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            record.status = AttendanceStatus.LATE;
                            record.student.absentCount--;
                            JOptionPane.showMessageDialog(AttendanceSystem.this,
                                    record.student.name + " 已修改为迟到", "状态更新",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
            }
        };
        lateTimer.schedule(task, 10 * 60 * 1000);
    }

    private void endAttendance() {
        startButton.setEnabled(true);
        presentButton.setEnabled(false);
        absentButton.setEnabled(false);

        photoLabel.setBackground(java.awt.Color.LIGHT_GRAY);
        photoLabel.setText("");
        nameLabel.setText("姓名: --");
        studentIdLabel.setText("学号: --");

        int result = JOptionPane.showConfirmDialog(this,
                "本次点名已完成！共点名 " + currentSession.students.size() + " 人\n是否立即保存结果？",
                "点名完成", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            saveToExcel();
        }

        currentSession = null;
    }

    private void showSummary() {
        JDialog dialog = new JDialog(this, "考勤统计", true);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel studentPanel = new JPanel(new BorderLayout());
        String[] studentColumns = {"姓名", "学号", "点名次数", "出勤次数",
                "请假次数", "旷课次数", "迟到次数", "出勤率"};
        DefaultTableModel studentModel = new DefaultTableModel(studentColumns, 0);

        for (Student student : students) {
            int presentCount = (int) records.stream()
                    .filter(r -> r.student.equals(student) && r.status == AttendanceStatus.PRESENT)
                    .count();
            int leaveCount = (int) records.stream()
                    .filter(r -> r.student.equals(student) && r.status == AttendanceStatus.LEAVE)
                    .count();
            int absentCount = (int) records.stream()
                    .filter(r -> r.student.equals(student) && r.status == AttendanceStatus.ABSENT)
                    .count();
            int lateCount = (int) records.stream()
                    .filter(r -> r.student.equals(student) && r.status == AttendanceStatus.LATE)
                    .count();

            double rate = student.callCount > 0
                    ? (presentCount + lateCount) * 100.0 / student.callCount : 0;

            studentModel.addRow(new Object[]{
                    student.name, student.studentId, student.callCount,
                    presentCount, leaveCount, absentCount, lateCount,
                    String.format("%.1f%%", rate)
            });
        }

        JTable studentTable = new JTable(studentModel);
        studentTable.setFont(new java.awt.Font("微软雅黑", java.awt.Font.PLAIN, 12));
        studentTable.getTableHeader().setFont(new java.awt.Font("微软雅黑", java.awt.Font.BOLD, 12));
        studentPanel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        JPanel recordPanel = new JPanel(new BorderLayout());
        String[] recordColumns = {"日期时间", "姓名", "学号", "考勤状态"};
        DefaultTableModel recordModel = new DefaultTableModel(recordColumns, 0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (AttendanceRecord record : records) {
            recordModel.addRow(new Object[]{
                    sdf.format(record.time), record.student.name,
                    record.student.studentId, record.status.getText()
            });
        }

        JTable recordTable = new JTable(recordModel);
        recordTable.setFont(new java.awt.Font("微软雅黑", java.awt.Font.PLAIN, 12));
        recordTable.getTableHeader().setFont(new java.awt.Font("微软雅黑", java.awt.Font.BOLD, 12));
        recordPanel.add(new JScrollPane(recordTable), BorderLayout.CENTER);

        tabbedPane.addTab("学生统计", studentPanel);
        tabbedPane.addTab("点名记录", recordPanel);

        dialog.add(tabbedPane);
        dialog.setVisible(true);
    }

    // 内部类
    static class Student {
        String name, studentId, description;
        int callCount = 0, absentCount = 0;

        Student(String name, String studentId, String description) {
            this.name = name;
            this.studentId = studentId;
            this.description = description;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Student)) return false;
            return studentId.equals(((Student) obj).studentId);
        }

        @Override
        public int hashCode() {
            return studentId.hashCode();
        }
    }

    enum AttendanceStatus {
        PRESENT("出勤"), ABSENT("旷课"), LEAVE("请假"), LATE("迟到");
        private String text;
        AttendanceStatus(String text) { this.text = text; }
        public String getText() { return text; }
    }

    static class AttendanceRecord {
        Student student;
        Date time;
        AttendanceStatus status;

        AttendanceRecord(Student student, Date time, AttendanceStatus status) {
            this.student = student;
            this.time = time;
            this.status = status;
        }
    }

    static class AttendanceSession {
        List<Student> students;
        int currentIndex = -1;
        Student currentStudent;

        AttendanceSession(List<Student> students) {
            this.students = students;
        }

        boolean hasNext() { return currentIndex < students.size() - 1; }

        Student next() {
            currentStudent = students.get(++currentIndex);
            return currentStudent;
        }
    }

    // 测试入口（单独运行点名系统时使用）
    public static void main(String[] args) {
        // Frame 需在事件调度线程中创建
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 单独运行时，红包游戏引用为null（不影响功能）
            new AttendanceSystem(null);
        });
    }
}