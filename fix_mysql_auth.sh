#!/bin/bash

echo "=== 修复MySQL认证问题 ==="
echo ""

# 提示输入MySQL root密码
echo "请输入MySQL root用户密码（用于sudo mysql）："
read -s MYSQL_PASSWORD

echo ""
echo "1. 检查当前用户认证方式..."
sudo mysql -u root -p$MYSQL_PASSWORD -e "SELECT user, host, plugin FROM mysql.user WHERE user='root';"

echo ""
echo "2. 创建游戏专用用户..."
# 创建一个新用户来避免root权限问题
sudo mysql -u root -p$MYSQL_PASSWORD << EOF
-- 删除已存在的游戏用户（如果有）
DROP USER IF EXISTS 'duckgame'@'localhost';

-- 创建新用户，使用更强的密码
CREATE USER 'duckgame'@'localhost' IDENTIFIED BY 'DuckGame@2024!';

-- 授予duck_game数据库的所有权限
GRANT ALL PRIVILEGES ON duck_game.* TO 'duckgame'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;

-- 显示创建的用户
SELECT user, host FROM mysql.user WHERE user='duckgame';
EOF

if [ $? -eq 0 ]; then
    echo "   ✓ 游戏用户创建成功"
else
    echo "   ✗ 用户创建失败"
    exit 1
fi

echo ""
echo "3. 测试新用户连接..."
mysql -u duckgame -p'DuckGame@2024!' duck_game -e "SHOW TABLES;"

if [ $? -eq 0 ]; then
    echo "   ✓ 连接测试成功"
else
    echo "   ✗ 连接测试失败"
fi

echo ""
echo "=== 修复完成！==="
echo ""
echo "新的数据库连接信息："
echo "- 数据库名: duck_game"
echo "- 用户名: duckgame"
echo "- 密码: DuckGame@2024!"
echo "- 地址: localhost:3306"
echo ""
echo "请更新游戏代码中的数据库配置！"
