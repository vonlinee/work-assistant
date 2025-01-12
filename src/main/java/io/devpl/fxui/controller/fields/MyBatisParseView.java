package io.devpl.fxui.controller.fields;

import io.devpl.common.interfaces.FieldParser;
import io.devpl.fxui.editor.CodeEditor;
import io.devpl.fxui.editor.LanguageMode;
import javafx.scene.Node;

public class MyBatisParseView extends FieldParseView {

    CodeEditor area;

    @Override
    String getName() {
        return "MyBatis";
    }

    @Override
    Node createRootNode() {
        area = CodeEditor.newInstance(LanguageMode.XML);
        return area.getView();
    }

    @Override
    public String getParseableText() {
        return area.getText();
    }

    @Override
    public void fillSampleText() {
        area.setText(getSampleText(), false);
    }

    @Override
    protected FieldParser getFieldParser() {
        return super.getFieldParser();
    }

    @Override
    public String getSampleText() {
        return """
            <select id="listCourseByCondition" resultType="com.lancoo.examuniv.entity.ExamCourse"
                    parameterType="com.lancoo.examuniv.domain.param.CourseReportParam">
                SELECT course.id, course.exam_id, course.course_no, course.course_name, course.course_type, course.is_pub AS
                pub, course.course_number, course.school_id, course.course_name, course.college_id, course.college_name, course.allow_delay,
                course.allow_resit, course.room_type, course.duration, course.create_time, course.update_time, course.is_deleted
                AS deleted,
                IFNULL(stu.student_count, 0) AS student_count, ecls.teacher_name
                FROM exam_course course
                LEFT JOIN
                (SELECT es.exam_course_id, count(*) AS student_count
                 FROM exam_student es WHERE es.exam_id = #{examId}
                <choose>
                    <when test="confirm == 1 and deleted == 0">  <!-- 注释信息 -->
                        AND es.confirmed != 7 AND es.reported != 3
                    </when>
                    <when test="confirm == 1 and deleted == 1">  <!-- 注释信息 -->
                        AND es.confirmed = 7
                    </when>
                    <when test="confirm != 1 and deleted == 0">  <!-- 注释信息 -->
                        AND es.reported != 3
                    </when>
                    <when test="confirm != 1 and deleted == 1">  <!-- 注释信息 -->
                        AND es.reported = 3
                    </when>
                </choose>
                GROUP BY es.exam_course_id) stu
                ON course.id = stu.exam_course_id
                LEFT JOIN
                (SELECT exam_course_id, GROUP_CONCAT(DISTINCT teacher_name SEPARATOR '、') AS teacher_name FROM
                exam_class WHERE exam_id = #{examId} GROUP BY exam_course_id) ecls
                ON course.id = ecls.exam_course_id
                WHERE course.exam_id = #{examId}
                <if test="deleted != null">
                    <choose>
                        <when test="confirm == 1 and deleted == 0">  <!-- 注释信息 -->
                            AND course.confirmed != 7 AND course.reported != 3
                        </when>
                        <when test="confirm == 1 and deleted == 1">  <!-- 注释信息 -->
                            AND course.confirmed = 7
                        </when>
                        <when test="confirm != 1 and deleted == 0">  <!-- 注释信息 -->
                            AND course.reported != 3
                        </when>
                        <when test="confirm != 1 and deleted == 1">  <!-- 注释信息 -->
                            AND course.reported = 3
                        </when>
                    </choose>
                </if>
                <if test="collegeId != null and collegeId != ''">
                    AND course.college_id = #{collegeId}
                </if>
                <if test="assignedTo != null and assignedTo != ''">
                    AND course.assigned_to = #{assignedTo}
                </if>
                <if test="pub != null">
                    AND course.is_pub = #{pub}
                </if>
                <if test="keyword != null and keyword != ''">
                    AND course.course_name LIKE concat('%', #{keyword}, '%')
                </if>
                <if test="reportStates != null and reportStates.size() > 0">
                    AND course.reported IN
                    <foreach item="item" index="index" collection="reportStates" open="(" close=")" separator=",">
                        #{item}
                    </foreach>
                </if>
                <if test="confirmStates != null and confirmStates.size() > 0">
                    AND course.confirmed IN
                    <foreach item="item" index="index" collection="confirmStates" open="(" close=")" separator=",">
                        #{item}
                    </foreach>
                </if>
            </select>
            """;
    }
}
