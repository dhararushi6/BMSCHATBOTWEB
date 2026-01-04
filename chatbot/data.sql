-- Achievo Initial Data
-- Save this as data.sql

-- Insert categories
INSERT OR IGNORE INTO categories (key, name, description, icon, color) VALUES 
('time-management', 'Time Management', 'Balancing classes, study, work and social life', '⏰', '#3B82F6'),
('stress-management', 'Stress Management', 'Coping with anxiety, pressure and overwhelm', '🧠', '#EF4444'),
('study-techniques', 'Study Techniques', 'Effective learning, note-taking and exam prep', '🎓', '#10B981'),
('college-life', 'College Life', 'Adjusting to campus, friends, and living away', '🏠', '#F59E0B'),
('exam-preparation', 'Exam Preparation', 'Strategies for tests, finals and performance', '📝', '#8B5CF6'),
('wellness', 'Health & Wellness', 'Maintaining physical and mental health', '❤️', '#EC4899');

-- Insert sample questions for time-management
INSERT OR IGNORE INTO questions (category_key, question, prompt, tips) VALUES 
('time-management', 'How do I balance my schedule?', 'Help me create a balanced schedule between classes, study time, extracurricular activities, and personal life.', '1. Use a planner or digital calendar\n2. Block time for different activities\n3. Include buffer time\n4. Review and adjust weekly'),
('time-management', 'How can I stop procrastinating?', 'I keep putting off important tasks until the last minute. What strategies can help me overcome procrastination?', '1. Use the Pomodoro technique\n2. Break tasks into smaller steps\n3. Remove distractions\n4. Set specific deadlines'),
('time-management', 'What''s the best way to plan my week?', 'I want to learn effective weekly planning techniques to stay organized and productive.', '1. Plan on Sunday evening\n2. Prioritize tasks\n3. Schedule study sessions\n4. Include self-care time');

-- Insert sample questions for stress-management
INSERT OR IGNORE INTO questions (category_key, question, prompt, tips) VALUES 
('stress-management', 'How do I deal with anxiety before exams?', 'I experience severe anxiety before exams that affects my performance. How can I manage this?', '1. Practice deep breathing\n2. Prepare thoroughly\n3. Get enough sleep\n4. Use positive self-talk'),
('stress-management', 'What are good stress relief techniques?', 'What are effective ways to relieve stress during busy college periods?', '1. Regular exercise\n2. Meditation or mindfulness\n3. Talk to friends\n4. Creative hobbies'),
('stress-management', 'How do I manage multiple deadlines?', 'I have several assignments and exams coming up at the same time. How do I handle this pressure?', '1. Make a priority list\n2. Break projects into steps\n3. Communicate with professors\n4. Take regular breaks');

-- Insert sample questions for study-techniques
INSERT OR IGNORE INTO questions (category_key, question, prompt, tips) VALUES 
('study-techniques', 'What are effective study methods?', 'I want to learn study techniques that actually work and help me retain information better.', '1. Active recall\n2. Spaced repetition\n3. Teach others\n4. Practice testing'),
('study-techniques', 'How do I take better notes?', 'What note-taking methods are most effective for college lectures?', '1. Cornell method\n2. Outline method\n3. Mind mapping\n4. Review within 24 hours'),
('study-techniques', 'How to study for long periods?', 'How can I maintain focus and productivity during long study sessions?', '1. Use Pomodoro technique\n2. Switch subjects\n3. Stay hydrated\n4. Take movement breaks');

-- Insert sample questions for college-life
INSERT OR IGNORE INTO questions (category_key, question, prompt, tips) VALUES 
('college-life', 'How do I make friends in college?', 'I''m having trouble making friends as a new student. What are good ways to connect with people?', '1. Join clubs or organizations\n2. Attend campus events\n3. Talk to classmates\n4. Be approachable'),
('college-life', 'How do I manage finances in college?', 'What are good strategies for budgeting and managing money as a college student?', '1. Track expenses\n2. Create a budget\n3. Look for student discounts\n4. Consider part-time work'),
('college-life', 'How do I deal with homesickness?', 'I''m struggling with being away from home and family. How can I cope with homesickness?', '1. Stay connected with family\n2. Build a support network\n3. Explore your new environment\n4. Establish routines');

-- Insert sample questions for exam-preparation
INSERT OR IGNORE INTO questions (category_key, question, prompt, tips) VALUES 
('exam-preparation', 'How should I prepare for finals week?', 'What''s the best approach to prepare for multiple final exams in one week?', '1. Start early\n2. Create a study schedule\n3. Review past exams\n4. Form study groups'),
('exam-preparation', 'How do I handle exam anxiety?', 'What strategies can help me stay calm and focused during exams?', '1. Practice relaxation techniques\n2. Arrive early\n3. Read instructions carefully\n4. Manage your time'),
('exam-preparation', 'What''s the best way to review for an exam?', 'How can I effectively review material to prepare for an upcoming test?', '1. Create summary sheets\n2. Do practice problems\n3. Teach the material\n4. Get enough sleep');

-- Insert sample questions for wellness
INSERT OR IGNORE INTO questions (category_key, question, prompt, tips) VALUES 
('wellness', 'How do I maintain a healthy lifestyle in college?', 'How can I balance academics with physical health and wellness?', '1. Regular exercise\n2. Balanced diet\n3. Adequate sleep\n4. Stress management'),
('wellness', 'How do I improve my sleep schedule?', 'My sleep schedule is irregular. How can I establish a healthy sleep routine?', '1. Consistent bedtime\n2. Limit screen time before bed\n3. Create a relaxing routine\n4. Avoid caffeine late'),
('wellness', 'How do I practice self-care in college?', 'What are practical self-care activities for busy college students?', '1. Schedule "me time"\n2. Practice mindfulness\n3. Connect with nature\n4. Pursue hobbies');
