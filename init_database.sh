#!/bin/bash

echo "=== 唐老鸭游戏数据库初始化脚本 ==="
echo ""

# 提示输入MySQL root密码
echo "请输入MySQL root用户密码："
read -s MYSQL_PASSWORD

# 创建数据库
echo ""
echo "1. 创建数据库 duck_game..."
sudo mysql -u root -p$MYSQL_PASSWORD -e "CREATE DATABASE IF NOT EXISTS duck_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

if [ $? -eq 0 ]; then
    echo "   ✓ 数据库创建成功"
else
    echo "   ✗ 数据库创建失败"
    exit 1
fi

# 导入SQL文件
echo ""
echo "2. 导入数据表和初始数据..."
sudo mysql -u root -p$MYSQL_PASSWORD duck_game < duck_game.sql

if [ $? -eq 0 ]; then
    echo "   ✓ 数据导入成功"
else
    echo "   ✗ 数据导入失败"
    exit 1
fi

# 创建游戏用户（可选）
echo ""
echo "3. 配置数据库权限..."
sudo mysql -u root -p$MYSQL_PASSWORD -e "
    CREATE USER IF NOT EXISTS 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'thedangerinmyheart';
    GRANT ALL PRIVILEGES ON duck_game.* TO 'root'@'localhost';
    FLUSH PRIVILEGES;
"

if [ $? -eq 0 ]; then
    echo "   ✓ 权限配置成功"
else
    echo "   ✗ 权限配置失败（可能用户已存在）"
fi

# 验证安装
echo ""
echo "4. 验证数据库..."
echo "   数据库中的表："
sudo mysql -u root -p$MYSQL_PASSWORD duck_game -e "SHOW TABLES;"

echo ""
echo "=== 初始化完成！==="
echo ""
echo "数据库信息："
echo "- 数据库名: duck_game"
echo "- 用户名: root"
echo "- 密码: thedangerinmyheart"
echo "- 地址: localhost:3306"
echo ""
echo "现在可以运行游戏了！"
