-- Camps (ON CONFLICT DO NOTHING = skip if the row already exists, since this file runs on every start)
INSERT INTO health_camp (id, name, camp_type, village, venue, camp_date, start_time, end_time, centre_id, target_count, status) VALUES
 (1, 'NCD Screening Camp',  'NCD',     'Kaniyambadi', 'Panchayat Union School', '2026-10-05', '08:00', '14:00', 1, 60, 'COMPLETED'),
 (2, 'General Health Camp', 'GENERAL', 'Pennathur',   'Community Hall',         '2026-10-11', '09:00', '15:00', 1, 80, 'ONGOING'),
 (3, 'Eye Screening Camp',  'EYE',     'Arcot',       'Govt Higher Sec School', '2026-10-18', '09:00', '13:00', 2, 50, 'PLANNED')
ON CONFLICT (id) DO NOTHING;

-- Staff on duty
INSERT INTO camp_staff (id, camp_id, worker_id, worker_name, role, duty) VALUES
 (1, 1, 1, 'Dr. Meena K', 'DOCTOR', 'Consultation'),
 (2, 1, 3, 'Selvi R',     'ASHA',   'Registration'),
 (3, 2, 2, 'Dr. Arun P',  'DOCTOR', 'Consultation'),
 (4, 2, 4, 'Kavitha S',   'NURSE',  'BP station')
ON CONFLICT (id) DO NOTHING;

-- Registered residents
INSERT INTO camp_beneficiary (id, camp_id, resident_id, resident_name, village, age, gender, attendance, registered_at) VALUES
 (1, 1, 1, 'Lakshmi R', 'Kaniyambadi', 52, 'F', 'ATTENDED',   '2026-10-05 08:15'),
 (2, 1, 2, 'Murugan S', 'Kaniyambadi', 61, 'M', 'ATTENDED',   '2026-10-05 08:30'),
 (3, 1, 3, 'Revathi M', 'Kaniyambadi', 45, 'F', 'ATTENDED',   '2026-10-05 09:00'),
 (4, 2, 4, 'Ganesan V', 'Pennathur',   58, 'M', 'REGISTERED', '2026-10-10 18:00')
ON CONFLICT (id) DO NOTHING;

-- Screening results
INSERT INTO screening (id, beneficiary_id, screening_type, reading, result, screened_by, remarks, screened_at) VALUES
 (1, 1, 'BP',          '152/96 mmHg', 'ABNORMAL', 'Kavitha S',   NULL,                 '2026-10-05 09:00'),
 (2, 2, 'BLOOD_SUGAR', '268 mg/dL',   'CRITICAL', 'Dr. Meena K', 'Random blood sugar', '2026-10-05 09:20'),
 (3, 3, 'BP',          '118/78 mmHg', 'NORMAL',   'Kavitha S',   NULL,                 '2026-10-05 09:40')
ON CONFLICT (id) DO NOTHING;

-- Follow-ups for the two non-normal results (id 2 is already overdue, good for the demo)
INSERT INTO follow_up (id, screening_id, resident_id, resident_name, village, action, priority, due_date, status, outcome, closed_on) VALUES
 (1, 1, 1, 'Lakshmi R', 'Kaniyambadi', 'RECHECK',         'MEDIUM', '2026-10-19', 'PENDING', NULL, NULL),
 (2, 2, 2, 'Murugan S', 'Kaniyambadi', 'REFER_TO_CENTRE', 'HIGH',   '2026-10-07', 'PENDING', NULL, NULL)
ON CONFLICT (id) DO NOTHING;

-- Move each id counter past the inserted ids, so new rows from the app don't clash (PostgreSQL)
SELECT setval(pg_get_serial_sequence('health_camp', 'id'),      (SELECT MAX(id) FROM health_camp));
SELECT setval(pg_get_serial_sequence('camp_staff', 'id'),       (SELECT MAX(id) FROM camp_staff));
SELECT setval(pg_get_serial_sequence('camp_beneficiary', 'id'), (SELECT MAX(id) FROM camp_beneficiary));
SELECT setval(pg_get_serial_sequence('screening', 'id'),        (SELECT MAX(id) FROM screening));
SELECT setval(pg_get_serial_sequence('follow_up', 'id'),        (SELECT MAX(id) FROM follow_up));