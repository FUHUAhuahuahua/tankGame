# Game0_17 代码结构说明

## 核心文件

### 1. Main.java
- 程序主入口
- 负责初始化游戏窗口和各个系统
- 设置系统之间的依赖关系

### 2. GameFrame.java
- 游戏主窗口类
- 包含游戏核心逻辑
- 定义了各个系统的接口
- 管理游戏状态和UI

## 系统模块（统一命名为 "...System"）

### 1. DressUpSystem.java
- 装扮系统实现
- 管理唐老鸭的换装功能
- 加载和切换不同风格的图片

### 2. CodeStatsSystem.java
- 代码统计系统实现
- 提供代码扫描功能
- 包含语言竞猜游戏
- 触发红包雨奖励

### 3. SkillSystem.java
- 技能系统实现
- 管理小鸭子的技能
- 处理技能触发和效果

## 辅助类

### 1. CodeStatistics.java
- 代码统计引擎
- 扫描文件夹并统计各种编程语言
- 区分代码行、注释行、空行

### 2. CodeStatsFrame.java
- 代码统计结果展示界面
- 包含总览、柱状图、饼图、详细数据、代码质量等面板
- 支持重新扫描功能

### 3. DressShopDialog.java
- 装扮商店对话框
- 处理套装购买逻辑
- 更新数据库中的购买记录

## 其他类

### 1. DonaldDuck.java (在 GameFrame.java 中定义)
- 唐老鸭角色类
- 管理位置、移动、碰撞检测

### 2. LittleDuck.java (在 GameFrame.java 中定义)
- 小鸭子类
- 包含技能信息

### 3. RedPacket.java (在 GameFrame.java 中定义)
- 红包类
- 管理红包的位置和价值

## 设计模式

1. **接口分离**: 每个系统都通过接口与 GameFrame 交互
2. **依赖注入**: 在 Main.java 中创建系统实例并注入到 GameFrame
3. **模块化设计**: 每个系统独立实现，便于维护和扩展

## 命名规范

- 所有系统类统一命名为 "...System"
- 接口定义在 GameFrame 中
- 辅助类根据功能命名
- 对话框类以 "...Dialog" 结尾
- 框架类以 "...Frame" 结尾
