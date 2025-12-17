package Game0_17;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.*;

public class GameFrame extends Frame {
    public static final int WIDTH = 700;
    public static final int HEIGHT = 700;
    public static final int GAME_TIME = 30000;

    private enum GameState { WAITING, PLAYING, GAME_OVER }
    private GameState gameState = GameState.WAITING;

    private DonaldDuck donald;
    private List<LittleDuck> littleDucks = new ArrayList<>();
    private List<RedPacket> redPackets = new ArrayList<>();
    private GameTimer gameTimer;
    private int totalAmount = 0;
    private int sessionAmount = 0;

    private Image bgImg, donaldImg, littleDuckImg;
    private Image redPacketSmallImg, redPacketMiddleImg, redPacketBigImg;

    private SkillType activeSkill = null;
    private LittleDuck selectedDuck = null;

    private ExecutorService executor = Executors.newCachedThreadPool();
    private SpeechService speechService = new SpeechService();

    private DebuffType activeDebuff = null;
    private boolean debuffTriggered = false;
    private double amountMultiplier = 1.0;

    public static final String DB_URL = "jdbc:mysql://localhost:3306/duck_game?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "thedangerinmyheart";

    private Button startBtn, funcBtn;

    public interface DressUpSystem {
        String getCurrentStyle();
        boolean purchaseDress(String dressName, int cost, GameFrame game);
        Image getDonaldImage();
        void showDressUpDialog(GameFrame parent);
    }

    public interface CodeStatsSystem {
        void showStatsDialog(GameFrame parent);
        void triggerRedPacketRain(GameFrame game);
    }

    private DressUpSystem dressUpSystem;
    private CodeStatsSystem codeStatsSystem;

    public void setDressUpSystem(DressUpSystem system) { this.dressUpSystem = system; }
    public void setCodeStatsSystem(CodeStatsSystem system) { this.codeStatsSystem = system; }

    public boolean deductAmount(int cost) {
        if (totalAmount >= cost) {
            totalAmount -= cost;
            updateTotalAmountInDB();
            return true;
        }
        return false;
    }

    public int getTotalAmount() { return totalAmount; }

    public void spawnRedPacketRain(int count) {
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            int x = random.nextInt(WIDTH - 50);
            RedPacket.Size size = RedPacket.Size.values()[random.nextInt(3)];
            int sizeValue = size == RedPacket.Size.SMALL ? 20 : size == RedPacket.Size.MEDIUM ? 30 : 40;
            redPackets.add(new RedPacket(x, 0, sizeValue, sizeValue, 3 + random.nextInt(4), size));
        }
    }

    public GameFrame() {
        loadResources();
        initDatabase();
        loadTotalAmountFromDB();
        initFrame();
        initLittleDucks();
        initDonald();
        setVisible(true);
        new PaintThread().start();
    }

    private void loadResources() {
        try {
            bgImg = loadImage("/images/R-C.jpg");
            donaldImg = loadImage("/images/duck.jpg");
            littleDuckImg = loadImage("/images/little_duck.png");
            redPacketSmallImg = loadImage("/images/redpacket.png");
            redPacketMiddleImg = loadImage("/images/redpacket-middle.png");
            redPacketBigImg = loadImage("/images/redpacket-big.png");
        } catch (Exception e) {
            System.err.println("资源加载失败: " + e.getMessage());
        }
    }

    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url != null) {
            try { return ImageIO.read(url); } catch (IOException e) { throw new RuntimeException(e); }
        }
        return null;
    }

    private void initFrame() {
        setTitle("唐老鸭抢红包 v0.17 - AI对话版");
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);

        funcBtn = new Button("FUNCTION");
        funcBtn.setBounds(10, 30, 80, 30);
        funcBtn.addActionListener(e -> {
            showFunctionDialog();
            requestFocusForGame();
        });
        add(funcBtn);

        startBtn = new Button("BEGIN");
        startBtn.setBounds(100, 30, 80, 30);
        startBtn.addActionListener(e -> startGame());
        add(startBtn);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                executor.shutdownNow();
                System.exit(0);
            }
        });

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (gameState == GameState.PLAYING && donald != null) {
                    handleKeyWithDebuff(e.getKeyCode(), true);
                }
                if (e.getKeyCode() == KeyEvent.VK_Z) {
                    triggerRandomDuckVoice();
                }
            }
            public void keyReleased(KeyEvent e) {
                if (gameState == GameState.PLAYING && donald != null) {
                    handleKeyWithDebuff(e.getKeyCode(), false);
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                requestFocusForGame();
                if (gameState == GameState.GAME_OVER) {
                    if (isInButton(e.getX(), e.getY(), WIDTH/2 - 100, HEIGHT/2 + 100, 200, 50)) {
                        resetToWaiting();
                    }
                }
            }
        });

        addWindowFocusListener(new WindowFocusListener() {
            public void windowGainedFocus(WindowEvent e) { requestFocusForGame(); }
            public void windowLostFocus(WindowEvent e) {}
        });
    }

    private void handleKeyWithDebuff(int keyCode, boolean pressed) {
        int actualKey = keyCode;
        if (activeDebuff == DebuffType.REVERSE_WORLD) {
            if (keyCode == KeyEvent.VK_LEFT) actualKey = KeyEvent.VK_RIGHT;
            else if (keyCode == KeyEvent.VK_RIGHT) actualKey = KeyEvent.VK_LEFT;
        } else if (activeDebuff == DebuffType.NO_FLY) {
            if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN) return;
        }
        if (pressed) donald.handleKeyPress(actualKey);
        else donald.handleKeyRelease(actualKey);
    }

    private void requestFocusForGame() {
        this.requestFocus();
        this.requestFocusInWindow();
    }

    private void initDonald() {
        donald = new DonaldDuck(WIDTH / 2 - 30, HEIGHT - 150, 60, 60, 5);
    }

    private void initLittleDucks() {
        littleDucks.clear();
        String[] names = {"唐小哥", "唐老二", "唐小弟"};
        SkillType[] skills = {SkillType.SPEED_UP, SkillType.SIZE_UP, SkillType.AMOUNT_UP};
        int baseY = HEIGHT - 80;
        int spacing = WIDTH / 4;
        for (int i = 0; i < 3; i++) {
            LittleDuck duck = new LittleDuck(spacing * (i + 1) - 20, baseY, 50, 50, names[i], skills[i]);
            littleDucks.add(duck);
        }
    }

    private void startGame() {
        if (gameState != GameState.WAITING) return;

        Random rand = new Random();
        int idx = rand.nextInt(3);
        selectedDuck = littleDucks.get(idx);

        speakSkillCallout(selectedDuck.getName(), selectedDuck.getSkill());
        recordDuckCalled(selectedDuck.getName());

        int choice = JOptionPane.showConfirmDialog(this,
                "点到了【" + selectedDuck.getName() + "】！\n技能：" + selectedDuck.getSkill().getDescription() +
                        "\n\n是否花费50元使用该技能？", "技能选择", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            if (totalAmount >= 50) {
                totalAmount -= 50;
                updateTotalAmountInDB();
                activeSkill = selectedDuck.getSkill();
                applySkill(activeSkill);
                recordSkillUsed(selectedDuck.getName(), true);
                speechService.speak("技能已激活，" + activeSkill.getDescription() + "，消耗50元");
            } else {
                JOptionPane.showMessageDialog(this,
                        "金额不足，请积累足够基金再使用该技能",
                        "提示", JOptionPane.WARNING_MESSAGE);
                activeSkill = null;
                recordSkillUsed(selectedDuck.getName(), false);
                speechService.speak("金额不足，无法使用技能");
            }
        } else {
            activeSkill = null;
            recordSkillUsed(selectedDuck.getName(), false);
            speechService.speak("技能未使用");
        }

        activeDebuff = null;
        debuffTriggered = false;
        amountMultiplier = 1.0;

        gameState = GameState.PLAYING;
        sessionAmount = 0;
        redPackets.clear();
        spawnInitialRedPackets();
        startBtn.setEnabled(false);

        gameTimer = new GameTimer(GAME_TIME, () -> endGame(), this);
        gameTimer.start();
        new RedPacketSpawner().start();
        requestFocusForGame();
    }

    public void triggerRandomDebuff() {
        if (debuffTriggered) return;
        debuffTriggered = true;

        Random rand = new Random();
        DebuffType[] debuffs = DebuffType.values();
        activeDebuff = debuffs[rand.nextInt(debuffs.length)];

        speechService.speak("警告！" + activeDebuff.getDescription() + "！");

        if (activeDebuff == DebuffType.EIGHT_GATES) {
            amountMultiplier = 2.0;
            if (gameTimer != null) gameTimer.setRemainingTime(5000);
        }
    }

    private void speakSkillCallout(String duckName, SkillType skill) {
        String skillText;
        switch (skill) {
            case SPEED_UP: skillText = "速度加三"; break;
            case SIZE_UP: skillText = "体积变大"; break;
            case AMOUNT_UP: skillText = "金额乘以一点五"; break;
            default: skillText = "未知技能";
        }
        speechService.speak("点名，" + duckName + "，技能，" + skillText);
    }

    private void applySkill(SkillType skill) {
        switch (skill) {
            case SPEED_UP: donald.setSpeed(donald.getSpeed() + 3); break;
            case SIZE_UP: donald.setWidth(80); donald.setHeight(80); break;
            case AMOUNT_UP: break;
        }
    }

    private void endGame() {
        gameState = GameState.GAME_OVER;
        int finalAmount = (int)(sessionAmount * amountMultiplier);
        if (amountMultiplier > 1.0) {
            speechService.speak("八门齐开！收益翻倍！获得" + finalAmount + "元");
        } else {
            speechService.speak("游戏结束，本局获得" + finalAmount + "元");
        }
        sessionAmount = finalAmount;
        totalAmount += sessionAmount;
        updateTotalAmountInDB();
        donald.setSpeed(5);
        donald.setWidth(60);
        donald.setHeight(60);
        activeSkill = null;
        activeDebuff = null;
    }

    private void resetToWaiting() {
        gameState = GameState.WAITING;
        sessionAmount = 0;
        redPackets.clear();
        initDonald();
        startBtn.setEnabled(true);
        activeDebuff = null;
        debuffTriggered = false;
        amountMultiplier = 1.0;
        requestFocusForGame();
    }

    private void spawnInitialRedPackets() {
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            int x = random.nextInt(WIDTH - 50);
            RedPacket.Size size = RedPacket.Size.values()[random.nextInt(3)];
            int sizeValue = size == RedPacket.Size.SMALL ? 20 : size == RedPacket.Size.MEDIUM ? 30 : 40;
            redPackets.add(new RedPacket(x, random.nextInt(200), sizeValue, sizeValue, 2 + random.nextInt(3), size));
        }
    }

    private void showFunctionDialog() {
        String[] options = {"抢红包(当前)", "换装系统", "代码统计", "技能统计", "技能点名系统", "🤖 AI对话"};
        int choice = JOptionPane.showOptionDialog(this, "选择功能模块", "功能菜单",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0: break;
            case 1:
                if (dressUpSystem != null) dressUpSystem.showDressUpDialog(this);
                else JOptionPane.showMessageDialog(this, "换装系统未加载");
                break;
            case 2:
                if (codeStatsSystem != null) codeStatsSystem.showStatsDialog(this);
                else JOptionPane.showMessageDialog(this, "代码统计系统未加载");
                break;
            case 3: new SkillStatsDialog(this); break;
            case 4: openSkillSystem(); break;
            case 5: openAIChatDialog(); break;
        }
    }

    private void openSkillSystem() {
        this.setVisible(false);
        new SkillSystem(this);
    }

    private void openAIChatDialog() {
        new AIChatDialog(this, speechService);
    }

    private void triggerRandomDuckVoice() {
        Random rand = new Random();
        int idx = rand.nextInt(3);
        LittleDuck duck = littleDucks.get(idx);
        String[] phrases = {"恭喜你发财", "恭喜你精彩", "新年快乐"};
        speechService.speak(phrases[idx]);
    }

    private boolean isInButton(int x, int y, int bx, int by, int bw, int bh) {
        return x >= bx && x <= bx + bw && y >= by && y <= by + bh;
    }

    private Image getRedPacketImage(RedPacket.Size size) {
        switch (size) {
            case SMALL: return redPacketSmallImg;
            case MEDIUM: return redPacketMiddleImg;
            case LARGE: return redPacketBigImg;
            default: return redPacketSmallImg;
        }
    }


    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS game_stats (id INT PRIMARY KEY, total_amount INT DEFAULT 0)");
            stmt.execute("INSERT IGNORE INTO game_stats (id, total_amount) VALUES (1, 0)");
            stmt.execute("CREATE TABLE IF NOT EXISTS skill_stats (" +
                    "duck_name VARCHAR(20) PRIMARY KEY, called_count INT DEFAULT 0, " +
                    "used_count INT DEFAULT 0, not_used_count INT DEFAULT 0)");
            for (String name : new String[]{"唐小哥", "唐老二", "唐小弟"}) {
                stmt.execute("INSERT IGNORE INTO skill_stats (duck_name) VALUES ('" + name + "')");
            }
            stmt.execute("CREATE TABLE IF NOT EXISTS suit_purchases (" +
                    "suit_name VARCHAR(50) PRIMARY KEY, " +
                    "is_purchased BOOLEAN DEFAULT FALSE, " +
                    "purchase_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        } catch (SQLException e) { System.err.println("数据库初始化失败: " + e.getMessage()); }
    }

    private void loadTotalAmountFromDB() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT total_amount FROM game_stats WHERE id=1")) {
            if (rs.next()) totalAmount = rs.getInt("total_amount");
        } catch (SQLException e) { System.err.println("读取金额失败: " + e.getMessage()); }
    }

    private void updateTotalAmountInDB() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement("UPDATE game_stats SET total_amount=? WHERE id=1")) {
            ps.setInt(1, totalAmount);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("更新金额失败: " + e.getMessage()); }
    }

    private void recordDuckCalled(String duckName) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE skill_stats SET called_count = called_count + 1 WHERE duck_name = ?")) {
            ps.setString(1, duckName);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void recordSkillUsed(String duckName, boolean used) {
        String col = used ? "used_count" : "not_used_count";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE skill_stats SET " + col + " = " + col + " + 1 WHERE duck_name = ?")) {
            ps.setString(1, duckName);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    class PaintThread extends Thread {
        public void run() {
            while (true) {
                if (gameState == GameState.PLAYING) updateGame();
                repaint();
                try { Thread.sleep(40); } catch (InterruptedException e) {}
            }
        }
    }

    class RedPacketSpawner extends Thread {
        public void run() {
            Random random = new Random();
            while (gameState == GameState.PLAYING) {
                int x = random.nextInt(WIDTH - 50);
                RedPacket.Size size = RedPacket.Size.values()[random.nextInt(3)];
                int sizeValue = size == RedPacket.Size.SMALL ? 20 : size == RedPacket.Size.MEDIUM ? 30 : 40;
                redPackets.add(new RedPacket(x, 0, sizeValue, sizeValue, 2 + random.nextInt(3), size));
                try { Thread.sleep(800 + random.nextInt(1000)); } catch (InterruptedException e) {}
            }
        }
    }

    private void updateGame() {
        if (donald == null) return;
        if (activeDebuff == DebuffType.NO_FLY) donald.applyGravity(HEIGHT);
        donald.updatePosition(WIDTH, HEIGHT);

        List<RedPacket> toRemove = new ArrayList<>();
        for (RedPacket rp : redPackets) {
            rp.updatePosition();
            if (rp.getY() > HEIGHT) toRemove.add(rp);
            else if (donald.collidesWith(rp)) {
                int amt = rp.getAmount();
                if (activeSkill == SkillType.AMOUNT_UP) amt = (int)(amt * 1.5);
                sessionAmount += amt;
                toRemove.add(rp);
            }
        }
        redPackets.removeAll(toRemove);
    }

    @Override
    public void paint(Graphics g) {
        if (bgImg != null) g.drawImage(bgImg, 0, 0, WIDTH, HEIGHT, null);
        else { g.setColor(Color.WHITE); g.fillRect(0, 0, WIDTH, HEIGHT); }

        if (activeDebuff != null && gameState == GameState.PLAYING) {
            g.setColor(activeDebuff.getOverlayColor());
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }

        if (donald != null) {
            Image img = (dressUpSystem != null) ? dressUpSystem.getDonaldImage() : donaldImg;
            if (img != null) g.drawImage(img, donald.getX(), donald.getY(), donald.getWidth(), donald.getHeight(), null);
            else { g.setColor(Color.YELLOW); g.fillOval(donald.getX(), donald.getY(), donald.getWidth(), donald.getHeight()); }
        }

        for (LittleDuck duck : littleDucks) {
            if (littleDuckImg != null) g.drawImage(littleDuckImg, duck.getX(), duck.getY(), duck.getWidth(), duck.getHeight(), null);
            else { g.setColor(Color.ORANGE); g.fillOval(duck.getX(), duck.getY(), duck.getWidth(), duck.getHeight()); }
            g.setColor(Color.BLACK);
            g.setFont(new Font("宋体", Font.PLAIN, 12));
            g.drawString(duck.getName(), duck.getX(), duck.getY() + duck.getHeight() + 15);
        }

        for (RedPacket rp : redPackets) {
            Image img = getRedPacketImage(rp.getSize());
            rp.draw(g, img);
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font("宋体", Font.BOLD, 16));
        g.drawString("总金额: " + totalAmount + "元", 200, 45);
        String sessionText = "本局: " + sessionAmount + "元";
        if (amountMultiplier > 1.0) sessionText += " (x" + amountMultiplier + ")";
        g.drawString(sessionText, 350, 45);

        if (gameTimer != null && gameState == GameState.PLAYING) {
            int remaining = gameTimer.getRemainingTime() / 1000;
            if (remaining <= 10) { g.setColor(Color.RED); g.setFont(new Font("宋体", Font.BOLD, 18)); }
            g.drawString("剩余: " + remaining + "秒", 480, 45);
            g.setColor(Color.BLACK); g.setFont(new Font("宋体", Font.BOLD, 16));
        }

        if (activeSkill != null) g.drawString("技能: " + activeSkill.getDescription(), 200, 65);
        if (activeDebuff != null && gameState == GameState.PLAYING) {
            g.setColor(Color.RED); g.setFont(new Font("宋体", Font.BOLD, 16));
            g.drawString("⚠ " + activeDebuff.getDescription(), 350, 65);
        }

        g.setColor(Color.GRAY); g.setFont(new Font("宋体", Font.PLAIN, 12));
        g.drawString("按Z键：小鸭说话", 580, 45);

        if (gameState == GameState.GAME_OVER) {
            g.setColor(new Color(255, 0, 0, 180)); g.fillRect(0, 0, WIDTH, HEIGHT);
            g.setColor(Color.WHITE); g.setFont(new Font("宋体", Font.BOLD, 36));
            g.drawString("游戏结束", WIDTH/2 - 80, HEIGHT/2 - 20);
            String resultText = "本局获得: " + sessionAmount + "元";
            if (amountMultiplier > 1.0) resultText += " (翻倍!)";
            g.drawString(resultText, WIDTH/2 - 140, HEIGHT/2 + 30);
            g.setColor(new Color(255, 215, 0));
            g.fillRoundRect(WIDTH/2 - 100, HEIGHT/2 + 100, 200, 50, 15, 15);
            g.setColor(Color.RED); g.setFont(new Font("宋体", Font.BOLD, 20));
            g.drawString("再来一次", WIDTH/2 - 45, HEIGHT/2 + 132);
        }
    }

    private Image offScreenImage;
    @Override
    public void update(Graphics g) {
        if (offScreenImage == null) offScreenImage = createImage(WIDTH, HEIGHT);
        Graphics gOff = offScreenImage.getGraphics();
        paint(gOff);
        g.drawImage(offScreenImage, 0, 0, null);
    }

    public DonaldDuck getDonaldDuck() { return donald; }
    public boolean isPlaying() { return gameState == GameState.PLAYING; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameFrame::new);
    }
}

class AIChatDialog extends JDialog {
    private static final String DASHSCOPE_API_KEY = "sk-80093b52b4124e43bac0e5e18188560b";
    private static final String DASHSCOPE_API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String QWEN_MODEL = "qwen-turbo";

    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendBtn, speakBtn, clearBtn;
    private SpeechService speechService;
    private String lastResponse = "";

    public AIChatDialog(Frame parent, SpeechService speechService) {
        super(parent, "🤖 唐老鸭AI助手", false);
        this.speechService = speechService;
        initUI();
        setVisible(true);
    }

    private void initUI() {
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        JLabel titleLabel = new JLabel("🦆 唐老鸭AI助手 - 通义千问");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        chatArea.setBackground(new Color(245, 245, 245));
        chatArea.setText("🦆 唐老鸭AI助手已上线！\n嘎嘎，我是唐老鸭只能AI助手，有什么可以帮你的吗？\n\n");

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        inputField = new JTextField();
        inputField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        inputField.addActionListener(e -> sendMessage());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        sendBtn = new JButton("发送");
        sendBtn.setFont(new Font("微软雅黑", Font.BOLD, 12));
        sendBtn.setBackground(new Color(76, 175, 80));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.addActionListener(e -> sendMessage());

        speakBtn = new JButton("🔊 朗读");
        speakBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        speakBtn.addActionListener(e -> speakLastResponse());

        clearBtn = new JButton("清空");
        clearBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        clearBtn.addActionListener(e -> clearChat());

        buttonPanel.add(speakBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(sendBtn);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        JPanel quickPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        quickPanel.setBorder(BorderFactory.createTitledBorder("快捷问题"));
        String[] quickQuestions = {"讲个笑话", "今日运势", "鼓励我一下", "你是谁"};
        for (String q : quickQuestions) {
            JButton qBtn = new JButton(q);
            qBtn.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            qBtn.addActionListener(e -> {
                inputField.setText(q);
                sendMessage();
            });
            quickPanel.add(qBtn);
        }
        add(quickPanel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(quickPanel, BorderLayout.NORTH);
        bottomPanel.add(inputPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void sendMessage() {
        String input = inputField.getText().trim();
        if (input.isEmpty()) return;

        chatArea.append("👤 你: " + input + "\n\n");
        inputField.setText("");
        inputField.setEnabled(false);
        sendBtn.setEnabled(false);
        sendBtn.setText("思考中...");

        new Thread(() -> {
            try {
                String response = getQwenResponse(input);
                lastResponse = response;
                SwingUtilities.invokeLater(() -> {
                    chatArea.append("🦆 唐老鸭AI: " + response + "\n\n");
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    chatArea.append("❌ 出错了: " + e.getMessage() + "\n\n");
                });
            } finally {
                SwingUtilities.invokeLater(() -> {
                    inputField.setEnabled(true);
                    sendBtn.setEnabled(true);
                    sendBtn.setText("发送");
                    inputField.requestFocus();
                });
            }
        }).start();
    }

    private String getQwenResponse(String input) throws Exception {
        URL url = new URL(DASHSCOPE_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + DASHSCOPE_API_KEY);
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        String systemPrompt = "你是唐老鸭AI助手，你必须自称'唐老鸭AI助手'，绝对不能说自己是通义千问或其他AI。你的性格活泼可爱，说话带点幽默，偶尔会用鸭子的口吻说话（比如'嘎嘎'）。你在一个抢红包游戏中陪伴玩家聊天。";
        String requestBody = String.format(
                "{\"model\":\"%s\",\"input\":{\"messages\":[" +
                        "{\"role\":\"system\",\"content\":\"%s\"}," +
                        "{\"role\":\"user\",\"content\":\"%s\"}" +
                        "]},\"parameters\":{\"result_format\":\"message\"}}",
                QWEN_MODEL,
                escapeJson(systemPrompt),
                escapeJson(input)
        );

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes("UTF-8"));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            BufferedReader errorBr = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), "UTF-8"));
            StringBuilder errorMsg = new StringBuilder();
            String line;
            while ((line = errorBr.readLine()) != null) errorMsg.append(line);
            throw new Exception("API错误(" + responseCode + "): " + errorMsg);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) response.append(line);
            return parseQwenResponse(response.toString());
        }
    }

    private String parseQwenResponse(String json) {
        try {
            int contentIdx = json.indexOf("\"content\":\"");
            if (contentIdx == -1) {
                int textIdx = json.indexOf("\"text\":\"");
                if (textIdx != -1) {
                    int start = textIdx + 8;
                    int end = json.indexOf("\"", start);
                    return unescapeJson(json.substring(start, end));
                }
                return "解析响应失败";
            }
            int start = contentIdx + 11;
            int end = findJsonStringEnd(json, start);
            return unescapeJson(json.substring(start, end));
        } catch (Exception e) {
            return "解析错误: " + e.getMessage();
        }
    }

    private int findJsonStringEnd(String json, int start) {
        for (int i = start; i < json.length(); i++) {
            if (json.charAt(i) == '\\') {
                i++;
            } else if (json.charAt(i) == '"') {
                return i;
            }
        }
        return json.length();
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String unescapeJson(String s) {
        return s.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private void speakLastResponse() {
        if (!lastResponse.isEmpty()) {
            String toSpeak = lastResponse.length() > 100 ?
                    lastResponse.substring(0, 100) + "..." : lastResponse;
            speechService.speak(toSpeak);
        }
    }

    private void clearChat() {
        chatArea.setText("🦆 唐老鸭AI助手已上线！\n聊天已清空，有什么新问题吗？\n\n");
        lastResponse = "";
    }
}

enum DebuffType {
    REVERSE_WORLD("颠倒世界", "左右颠倒", new Color(128, 0, 128, 50)),
    NO_FLY("飞行无力", "无法飞行", new Color(100, 100, 100, 50)),
    EIGHT_GATES("八门齐开", "5秒翻倍", new Color(255, 215, 0, 50));

    private String name, shortDesc;
    private Color overlayColor;

    DebuffType(String name, String shortDesc, Color color) {
        this.name = name; this.shortDesc = shortDesc; this.overlayColor = color;
    }

    public String getDescription() { return name + " - " + shortDesc; }
    public Color getOverlayColor() { return overlayColor; }
}

class SpeechService {
    public void speak(String text) {
        new Thread(() -> {
            try {
                String cmd = String.format(
                        "Add-Type -AssemblyName System.Speech; " +
                                "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                                "$synth.Rate = 2; $synth.Volume = 100; $synth.Speak('%s')",
                        text.replace("'", "''")
                );
                new ProcessBuilder("powershell", "-Command", cmd)
                        .redirectErrorStream(true).start();
            } catch (Exception e) {
                Toolkit.getDefaultToolkit().beep();
            }
        }).start();
    }
}

enum SkillType {
    SPEED_UP("速度+3"), SIZE_UP("体积变大"), AMOUNT_UP("金额x1.5");
    private String desc;
    SkillType(String desc) { this.desc = desc; }
    public String getDescription() { return desc; }
}

class DonaldDuck {
    private int x, y, width, height, speed, gravity = 2;
    private boolean left, right, up, down;

    public DonaldDuck(int x, int y, int w, int h, int speed) {
        this.x = x; this.y = y; this.width = w; this.height = h; this.speed = speed;
    }

    public void handleKeyPress(int key) {
        switch (key) {
            case KeyEvent.VK_LEFT: left = true; break;
            case KeyEvent.VK_RIGHT: right = true; break;
            case KeyEvent.VK_UP: up = true; break;
            case KeyEvent.VK_DOWN: down = true; break;
        }
    }

    public void handleKeyRelease(int key) {
        switch (key) {
            case KeyEvent.VK_LEFT: left = false; break;
            case KeyEvent.VK_RIGHT: right = false; break;
            case KeyEvent.VK_UP: up = false; break;
            case KeyEvent.VK_DOWN: down = false; break;
        }
    }

    public void applyGravity(int maxH) { if (y < maxH - height) y += gravity; }

    public void updatePosition(int maxW, int maxH) {
        if (left && x > 0) x -= speed;
        if (right && x < maxW - width) x += speed;
        if (up && y > 60) y -= speed;
        if (down && y < maxH - height) y += speed;
    }

    public boolean collidesWith(RedPacket rp) {
        return x < rp.getX() + rp.getWidth() && x + width > rp.getX() &&
                y < rp.getY() + rp.getHeight() && y + height > rp.getY();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getSpeed() { return speed; }
    public void setSpeed(int s) { speed = s; }
    public void setWidth(int w) { width = w; }
    public void setHeight(int h) { height = h; }
}

class LittleDuck {
    private int x, y, width, height;
    private String name;
    private SkillType skill;

    public LittleDuck(int x, int y, int w, int h, String name, SkillType skill) {
        this.x = x; this.y = y; this.width = w; this.height = h;
        this.name = name; this.skill = skill;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getName() { return name; }
    public SkillType getSkill() { return skill; }
}

class RedPacket {
    public enum Size { SMALL, MEDIUM, LARGE }

    private int x, y, width, height, speed, amount;
    private Size size;

    public RedPacket(int x, int y, int w, int h, int speed, Size size) {
        this.x = x; this.y = y; this.width = w; this.height = h;
        this.speed = speed; this.size = size;
        this.amount = calcAmount();
    }

    private int calcAmount() {
        switch (size) {
            case SMALL: return 1 + new Random().nextInt(4);
            case MEDIUM: return 5 + new Random().nextInt(10);
            case LARGE: return 15 + new Random().nextInt(15);
        }
        return 1;
    }

    public void updatePosition() { y += speed; }

    public void draw(Graphics g, Image img) {
        if (img != null) {
            g.drawImage(img, x, y, width, height, null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height);
        }
        g.setColor(Color.YELLOW);
        g.drawString(String.valueOf(amount), x + width/2 - 5, y + height/2 + 5);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getAmount() { return amount; }
    public Size getSize() { return size; }
}

class GameTimer extends Thread {
    private int remaining;
    private Runnable onFinish;
    private GameFrame gameFrame;
    private boolean debuffNotified = false;

    public GameTimer(int total, Runnable onFinish, GameFrame gameFrame) {
        this.remaining = total; this.onFinish = onFinish; this.gameFrame = gameFrame;
    }

    public int getRemainingTime() { return remaining; }
    public void setRemainingTime(int time) { this.remaining = time; }

    public void run() {
        try {
            while (remaining > 0) {
                Thread.sleep(1000);
                remaining -= 1000;
                if (remaining <= 10000 && remaining > 9000 && !debuffNotified) {
                    debuffNotified = true;
                    gameFrame.triggerRandomDebuff();
                }
            }
            onFinish.run();
        } catch (InterruptedException e) {}
    }
}

class SoundUtils {
    private static final AudioFormat FORMAT = new AudioFormat(44100, 16, 2, true, false);

    public static void playTone(int freq, int ms, float vol) throws Exception {
        SourceDataLine line = AudioSystem.getSourceDataLine(FORMAT);
        line.open(FORMAT, 44100); line.start();
        int samples = 44100 * ms / 1000;
        byte[] buf = new byte[samples * 4];
        for (int i = 0; i < samples; i++) {
            short v = (short)(Math.sin(2 * Math.PI * freq * i / 44100) * 32767 * vol);
            buf[4*i] = buf[4*i+2] = (byte)(v & 0xFF);
            buf[4*i+1] = buf[4*i+3] = (byte)((v >> 8) & 0xFF);
        }
        line.write(buf, 0, buf.length);
        line.drain(); line.stop(); line.close();
    }
}

class SkillStatsDialog extends JDialog {
    public SkillStatsDialog(Frame parent) {
        super(parent, "技能统计", true);
        setSize(400, 250);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(4, 4, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("唐小鸭")); panel.add(new JLabel("点名次数"));
        panel.add(new JLabel("使用次数")); panel.add(new JLabel("未使用次数"));

        try (Connection conn = DriverManager.getConnection(
                GameFrame.DB_URL, GameFrame.DB_USER, GameFrame.DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM skill_stats")) {
            while (rs.next()) {
                panel.add(new JLabel(rs.getString("duck_name")));
                panel.add(new JLabel(String.valueOf(rs.getInt("called_count"))));
                panel.add(new JLabel(String.valueOf(rs.getInt("used_count"))));
                panel.add(new JLabel(String.valueOf(rs.getInt("not_used_count"))));
            }
        } catch (SQLException e) {
            panel.add(new JLabel("数据库连接失败"));
        }
        add(panel);
        setVisible(true);
    }
}
