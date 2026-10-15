INSERT INTO member_roles (member_id, roles_id)
SELECT seed.member_id, roles.roles_id
FROM (
    VALUES
        ('00000000-0000-0000-0000-000000000001', 'USER'),
        ('00000000-0000-0000-0000-000000000002', 'ADMIN'),
        ('00000000-0000-0000-0000-000000000003', 'DEV')
) AS seed(member_id, role_name)
JOIN roles ON roles.name = seed.role_name
WHERE NOT EXISTS (
    SELECT 1
    FROM member_roles
    WHERE member_roles.member_id = seed.member_id
      AND member_roles.roles_id = roles.roles_id
);
