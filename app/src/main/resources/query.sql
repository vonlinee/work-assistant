SELECT
    u.user_id ,
    u.user_name ,
    u.nick_name ,
    u.user_type ,
    d.dept_name ,
    d.order_num ,
    p.post_id ,
    p.post_code ,
    p.post_name
FROM sys_user u
         LEFT JOIN sys_dept d ON u.dept_id = d.dept_id
         LEFT JOIN sys_post p ON p.post_name = u.user_name