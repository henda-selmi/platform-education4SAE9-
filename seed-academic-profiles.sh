#!/bin/bash
# Seed academic profiles for all students who don't have one yet.

echo "Waiting for MySQL to be ready..."
until docker exec mysql mysqladmin ping -uroot -proot --silent 2>/dev/null; do
  sleep 1
done

echo "Seeding student_academic_profile..."

docker exec mysql mysql -uroot -proot auth_db 2>/dev/null <<'SQL'
INSERT INTO student_academic_profile (user_id, age, g1, g2, failures, absences, studytime)
SELECT id, 20, 12, 13, 0, 4, 2
FROM users
WHERE role = 'STUDENT'
  AND id NOT IN (SELECT user_id FROM student_academic_profile);
SQL

COUNT=$(docker exec mysql mysql -uroot -proot auth_db -se \
  "SELECT COUNT(*) FROM student_academic_profile;" 2>/dev/null)

echo "Done. Total profiles in DB: $COUNT"