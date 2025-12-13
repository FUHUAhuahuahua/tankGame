package Game0_17;

import javax.swing.SwingUtilities;

/**
 * Game0_17 主程序入口
 * 唐老鸭抢红包游戏 - 包含装扮商店系统
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 创建游戏主窗口
            GameFrame gameFrame = new GameFrame();
            
            // 初始化并设置装扮系统
            DressUpSystem dressUpSystem = new DressUpSystem(gameFrame.getDonaldDuck());
            gameFrame.setDressUpSystem(dressUpSystem);
            
            // 初始化并设置代码统计系统
            CodeStatsSystem codeStatsSystem = new CodeStatsSystem();
            gameFrame.setCodeStatsSystem(codeStatsSystem);
            
            System.out.println("=================================");
            System.out.println("唐老鸭抢红包游戏 v0.17 已启动！");
            System.out.println("新功能：");
            System.out.println("1. 装扮商店系统");
            System.out.println("   - 进入换装系统后可以访问商店");
            System.out.println("   - 每套装扮需要花费100元购买");
            System.out.println("   - 只有购买后才能使用相应装扮");
            System.out.println("2. 代码统计功能");
            System.out.println("   - 查看代码量柱状图和饼图");
            System.out.println("   - 详细的代码统计数据");
            System.out.println("=================================");
        });
    }
}
