/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80036
 Source Host           : localhost:3306
 Source Schema         : duck_game

 Target Server Type    : MySQL
 Target Server Version : 80036
 File Encoding         : 65001

 Date: 13/12/2025 21:51:32
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for call_history
-- ----------------------------
DROP TABLE IF EXISTS `call_history`;
CREATE TABLE `call_history`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `duck_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '被点名的唐小鸭',
  `was_used` tinyint(1) NULL DEFAULT 0 COMMENT '是否使用了技能',
  `session_amount` int NULL DEFAULT 0 COMMENT '该局获得金额',
  `call_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点名时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_duck_name`(`duck_name` ASC) USING BTREE,
  INDEX `idx_call_time`(`call_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '点名历史记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of call_history
-- ----------------------------

-- ----------------------------
-- Table structure for dress_inventory
-- ----------------------------
DROP TABLE IF EXISTS `dress_inventory`;
CREATE TABLE `dress_inventory`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dress_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '装扮名称',
  `dress_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '类型: hat/glasses/scarf/tie/watch',
  `cost` int NULL DEFAULT 0 COMMENT '购买价格',
  `is_owned` tinyint(1) NULL DEFAULT 0 COMMENT '是否已拥有',
  `is_equipped` tinyint(1) NULL DEFAULT 0 COMMENT '是否装备中',
  `image_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片路径',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '装扮库存' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dress_inventory
-- ----------------------------
INSERT INTO `dress_inventory` VALUES (1, '礼帽', 'hat', 100, 0, 0, NULL, '2025-12-13 10:05:36');
INSERT INTO `dress_inventory` VALUES (2, '棒球帽', 'hat', 50, 0, 0, NULL, '2025-12-13 10:05:36');
INSERT INTO `dress_inventory` VALUES (3, '墨镜', 'glasses', 80, 0, 0, NULL, '2025-12-13 10:05:36');
INSERT INTO `dress_inventory` VALUES (4, '运动眼镜', 'glasses', 60, 0, 0, NULL, '2025-12-13 10:05:36');
INSERT INTO `dress_inventory` VALUES (5, '丝绸围巾', 'scarf', 120, 0, 0, NULL, '2025-12-13 10:05:36');
INSERT INTO `dress_inventory` VALUES (6, '运动毛巾', 'scarf', 40, 0, 0, NULL, '2025-12-13 10:05:36');
INSERT INTO `dress_inventory` VALUES (7, '领带', 'tie', 90, 0, 0, NULL, '2025-12-13 10:05:36');
INSERT INTO `dress_inventory` VALUES (8, '名表', 'watch', 200, 0, 0, NULL, '2025-12-13 10:05:36');
INSERT INTO `dress_inventory` VALUES (9, '运动手表', 'watch', 70, 0, 0, NULL, '2025-12-13 10:05:36');

-- ----------------------------
-- Table structure for game_stats
-- ----------------------------
DROP TABLE IF EXISTS `game_stats`;
CREATE TABLE `game_stats`  (
  `id` int NOT NULL,
  `total_amount` int NULL DEFAULT 0 COMMENT '累计总金额',
  `games_played` int NULL DEFAULT 0 COMMENT '游戏次数',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '游戏总统计' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of game_stats
-- ----------------------------
INSERT INTO `game_stats` VALUES (1, 228, 0, '2025-12-13 10:05:36', '2025-12-13 13:38:26');

-- ----------------------------
-- Table structure for skill_stats
-- ----------------------------
DROP TABLE IF EXISTS `skill_stats`;
CREATE TABLE `skill_stats`  (
  `duck_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '唐小鸭名称',
  `skill_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '技能描述',
  `called_count` int NULL DEFAULT 0 COMMENT '被点名次数',
  `used_count` int NULL DEFAULT 0 COMMENT '技能使用次数',
  `not_used_count` int NULL DEFAULT 0 COMMENT '技能未使用次数',
  `last_called` timestamp NULL DEFAULT NULL COMMENT '最后点名时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`duck_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '唐小鸭技能统计' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skill_stats
-- ----------------------------
INSERT INTO `skill_stats` VALUES ('唐小哥', '速度+3', 5, 2, 3, NULL, '2025-12-13 10:05:36');
INSERT INTO `skill_stats` VALUES ('唐小弟', '金额x1.5', 4, 1, 3, NULL, '2025-12-13 10:05:36');
INSERT INTO `skill_stats` VALUES ('唐老二', '体积变大', 1, 0, 1, NULL, '2025-12-13 10:05:36');

-- ----------------------------
-- Table structure for suit_purchases
-- ----------------------------
DROP TABLE IF EXISTS `suit_purchases`;
CREATE TABLE `suit_purchases`  (
  `suit_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '套装名称',
  `is_purchased` tinyint(1) NULL DEFAULT 0 COMMENT '是否已购买',
  `purchase_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '购买时间',
  PRIMARY KEY (`suit_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '套装购买记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of suit_purchases
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
