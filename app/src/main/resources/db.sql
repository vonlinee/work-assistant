-- 数据库需要有以下信息：统一32位UUID作为ID，16唯一编号
-- 学生：学号、姓名、性别、年龄、选修课程名、院系名称
-- 课程：课程编号、课程名、开课院系、课程时长、任课教师编号
-- 教师：教师号、姓名、性别、职称、所在院系
-- 院系：院系名称、院系联系电话
-- 
-- 公寓：
-- 上述实体中存在如下联系：
-- （1）一个学生可选修多门课程，一门课程可被多个学生选修。
-- （2）一个教师可讲授多门课程，一门课程可由多个教师讲授
-- （3）一个单位可有多个教师，一个教师只能属于一个院系

SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- TableTreeItem structure for t_class
-- ----------------------------
DROP TABLE IF EXISTS `t_class`;
CREATE TABLE `t_class`
(
    `CLASS_ID`    varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `CLASS_NO`    varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `CLASS_NAME`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '未知班级',
    `DEPART_NO`   varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
    `DEPART_NAME` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
    PRIMARY KEY (`CLASS_ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_class
-- ----------------------------
INSERT INTO `t_class`
VALUES ('51055b86-7419-11ec-9b88-38142830461b', '02111709', '9班', '', '');
INSERT INTO `t_class`
VALUES ('800c1fcd-7419-11ec-9b88-38142830461b', '02111708', '8班', '', '');

-- ----------------------------
-- TableTreeItem structure for t_cource
-- ----------------------------
DROP TABLE IF EXISTS `t_cource`;
CREATE TABLE `t_cource`
(
    `COURSE_ID`        varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `COURSE_NO`        varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
    `COURSE_NAME`      varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `COURSE_TIME_LONG` float NULL DEFAULT NULL COMMENT '课程时长',
    `TEACHER_NO`       varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `DEPART_NO`        varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '课程所开设院系编号',
    PRIMARY KEY (`COURSE_ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- TableTreeItem structure for t_department
-- ----------------------------
DROP TABLE IF EXISTS `t_department`;
CREATE TABLE `t_department`
(
    `DEPART_ID`   varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `DEPART_NO`   varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `DEPART_NAME` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `TELE_PHONE`  varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    PRIMARY KEY (`DEPART_ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_department
-- ----------------------------
INSERT INTO `t_department`
VALUES ('05f2cf64-741f-11ec-9b88-38142830461b', 'D0001', '光电工程学院', '020-1213212');
INSERT INTO `t_department`
VALUES ('05f33973-741f-11ec-9b88-38142830461b', 'D0002', '传媒学院', '020-1213212');
INSERT INTO `t_department`
VALUES ('05f38605-741f-11ec-9b88-38142830461b', 'D0003', '理学院', '020-1213212');
INSERT INTO `t_department`
VALUES ('05f3d35f-741f-11ec-9b88-38142830461b', 'D0004', '网络与信息安全学院', '020-1213212');

-- ----------------------------
-- TableTreeItem structure for t_score
-- ----------------------------
DROP TABLE IF EXISTS `t_score`;
CREATE TABLE `t_score`
(
    `SCORE_ID`    varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL,
    `SCORE_LEVEL` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL,
    `COURSE_NO`   varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `COURSE_NAME` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `SCORE_VALUE` float NULL DEFAULT NULL,
    `TEACHER_NO`  varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    PRIMARY KEY (`SCORE_ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- TableTreeItem structure for t_student
-- ----------------------------
DROP TABLE IF EXISTS `t_student`;
CREATE TABLE `t_student`
(
    `STU_ID`        varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `STU_NO`        varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `STU_SEX`       char(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '男',
    `STU_NAME`      varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
    `NATIVE_PLACE`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '籍贯',
    `STU_DEPART_NO` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '院系ID',
    `STU_CLASS_NO`  varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    PRIMARY KEY (`STU_ID`) USING BTREE,
    UNIQUE INDEX `UNK_STU_NO`(`STU_NO`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_student
-- ----------------------------
INSERT INTO `t_student`
VALUES ('64880d9d-741a-11ec-9b88-38142830461b', 'S001', '男', '李彤', '中国', 'D1', 'C001');
INSERT INTO `t_student`
VALUES ('7b82b625-741a-11ec-9b88-38142830461b', 'S002', '女', '许茗', '中国', 'D2', 'C002');
INSERT INTO `t_student`
VALUES ('7b838d7a-741a-11ec-9b88-38142830461b', 'S003', '女', '张梦华', '中国', 'D3', 'C003');
INSERT INTO `t_student`
VALUES ('7b83d2b7-741a-11ec-9b88-38142830461b', 'S004', '女', '林雪', '中国', 'D4', 'C004');
INSERT INTO `t_student`
VALUES ('7b841298-741a-11ec-9b88-38142830461b', 'S005', '女', '孟圆圆', '中国', 'D5', 'C005');
INSERT INTO `t_student`
VALUES ('7b844e92-741a-11ec-9b88-38142830461b', 'S006', '男', '罗城', '中国', 'D6', 'C006');

-- ----------------------------
-- TableTreeItem structure for t_teacher
-- ----------------------------
DROP TABLE IF EXISTS `t_teacher`;
CREATE TABLE `t_teacher`
(
    `TEACHER_ID`   varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `TEACHER_NO`   varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
    `TEACHER_SEX`  char(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '男',
    `NATIVE_PLACE` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '籍贯',
    `TEACHER_NAME` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
    `DEPART_NO`    varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '院系ID',
    `DEPART_NAME`  varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `TITLE`        varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '职称',
    `TELE_PHONE`   varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
    PRIMARY KEY (`TEACHER_ID`) USING BTREE,
    UNIQUE INDEX `UNK_TEACHER_NO`(`TEACHER_NO`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_teacher
-- ----------------------------
INSERT INTO `t_teacher`
VALUES ('8836be69-741e-11ec-9b88-38142830461b', 'T0001', '男', '广东省广州市', '杨凌', '1', '计算机网络学院', '讲师',
        '17347898125');
INSERT INTO `t_teacher`
VALUES ('8837b7a2-741e-11ec-9b88-38142830461b', 'T0002', '女', '广东省广州市', '林春梅', '1', '网络与信息安全学院',
        '教授', '17347898125');
INSERT INTO `t_teacher`
VALUES ('883801ba-741e-11ec-9b88-38142830461b', 'T0003', '女', '广东省广州市', '张霞', '1', '理学院', '讲师',
        '17347898125');
INSERT INTO `t_teacher`
VALUES ('883853b1-741e-11ec-9b88-38142830461b', 'T0004', '女', '广东省广州市', '李玉梅', '1', '传媒学院', '副教授',
        '17347898125');

SET
FOREIGN_KEY_CHECKS = 1;
