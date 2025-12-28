package com.bms;

import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@CrossOrigin(origins = "*")
public class ChatController {

    /* -------------------- DATA STORES -------------------- */
    private static final Map<String, String> KNOWLEDGE_BASE = new HashMap<>();
    private static final Map<String, String> EMOTIONAL_SUPPORT = new HashMap<>();
    private static final Map<String, ScheduledFuture<?>> STUDY_TIMERS = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService timerService = Executors.newScheduledThreadPool(10);
    private static final List<String> STUDY_GROUPS = new ArrayList<>();
    private static final Random random = new Random();
    private static final Map<String, StudySession> ACTIVE_SESSIONS = new ConcurrentHashMap<>();
    
    // BMS College Information
    private static final String BMS_LOGO = "https://th.bing.com/th/id/OIP.jlBzFsGL1j13RuIbg1p65QHaHV?w=159&h=211&c=8&rs=1&qlt=90&o=6&pid=3.1&rm=2";
    private static final String COLLEGE_MOTTO = "ಸತ್ಯಂ ವದ ಧರ್ಮಂ ಚರ (Speak the Truth, Practice Righteousness)";

    /* -------------------- STUDY SESSION CLASS -------------------- */
    private static class StudySession {
        String sessionId;
        int duration; // minutes
        long startTime;
        List<String> subjects;
        double progress; // 0-100%
        
        StudySession(String id, int dur, List<String> subs) {
            this.sessionId = id;
            this.duration = dur;
            this.startTime = System.currentTimeMillis();
            this.subjects = subs != null ? subs : new ArrayList<>();
            this.progress = 0.0;
        }
    }

    /* -------------------- STATIC DATA INIT -------------------- */
    static {
        // 🌟 COMPREHENSIVE EMOTIONAL SUPPORT & STUDENT CONCERNS
        EMOTIONAL_SUPPORT.put("keep up assignments deadlines burnout", 
            "🎯 <b>Burnout Prevention Strategy:</b><br>" +
            "1. <b>Chunk Tasks:</b> Break assignments into 30-min chunks<br>" +
            "2. <b>Priority Matrix:</b> Urgent+Important tasks first<br>" +
            "3. <b>Two-Minute Rule:</b> If it takes <2 mins, do it now<br>" +
            "4. <b>Weekly Buffer:</b> Leave 20% time for unexpected<br>" +
            "5. <b>Pomodoro:</b> 25min work, 5min break ×4<br>" +
            "💡 <i>Perfect is the enemy of done. Submit at 80% quality.</i>");
        
        EMOTIONAL_SUPPORT.put("stress anxiety exams pressure",
            "🧘 <b>Exam Stress Management:</b><br>" +
            "1. <b>Study Plan:</b> 2 hours/day, 6 weeks before exams<br>" +
            "2. <b>Active Recall:</b> Test yourself daily<br>" +
            "3. <b>Sleep:</b> 7-8 hours, no all-nighters<br>" +
            "4. <b>Breathing:</b> 4-7-8 technique before study<br>" +
            "5. <b>Group Study:</b> 2-3 serious students only<br>" +
            "💭 <i>Remember: Your worth ≠ your marks</i>");
        
        EMOTIONAL_SUPPORT.put("homesick lonely adjustment",
            "🏡 <b>Coping with Homesickness:</b><br>" +
            "• <b>Connect:</b> Video call family daily<br>" +
            "• <b>Explore:</b> Join 1 club + 1 sport<br>" +
            "• <b>Routine:</b> Keep meal/sleep times consistent<br>" +
            "• <b>Roommate:</b> Share feelings openly<br>" +
            "• <b>Professional Help:</b> Free counselling at Student Wellness Center<br>" +
            "🌱 <i>Adjustment takes 6-8 weeks - be patient</i>");
        
        EMOTIONAL_SUPPORT.put("procrastination motivation study",
            "🚀 <b>Overcoming Procrastination:</b><br>" +
            "• <b>5-Minute Rule:</b> Just start for 5 minutes<br>" +
            "• <b>Environment:</b> Library > Room > Canteen<br>" +
            "• <b>Accountability:</b> Study buddy system<br>" +
            "• <b>Reward:</b> 1 episode after 2 hours study<br>" +
            "• <b>Phone:</b> Forest app or silent mode<br>" +
            "🎯 <i>Motivation follows action, not vice versa</i>");

        // 📚 ACADEMICS - EXPANDED SECTION
        KNOWLEDGE_BASE.put("syllabus", "📘 VTU syllabus: portal.vtu.ac.in<br>📍 CBCS scheme at dept notice board<br>📥 Download syllabus from dept website");
        KNOWLEDGE_BASE.put("notes", "📚 Sources:<br>• Library (1st floor)<br>• Class Representatives<br>• Google Drive (seniors)<br>• Dept WhatsApp groups<br>• Subject-wise notes at CSE block");
        KNOWLEDGE_BASE.put("internals", "📝 Internal Assessment (40 marks):<br>• Test 1: 20 marks<br>• Test 2: 20 marks<br>• Attendance: 5 marks bonus<br>• Assignments: Mandatory submission");
        KNOWLEDGE_BASE.put("semester", "🎯 Semester Pattern:<br>• Theory: 60 marks<br>• Internals: 40 marks<br>• Passing: 40% aggregate<br>• Results: results.vtu.ac.in");
        KNOWLEDGE_BASE.put("backlog", "⚠️ Backlog Clearance:<br>• Clear within 4 attempts<br>• Photocopy: ₹500/subject<br>• Supplementary exams: Jan/Jul<br>• Revaluation within 30 days");
        
        KNOWLEDGE_BASE.put("attendance requirements",
            "📊 <b>VTU Attendance Rules:</b><br>" +
            "• <b>Minimum:</b> 75% aggregate (theory + lab)<br>" +
            "• <b>Condonation:</b> 65-74% with medical/fee<br>" +
            "• <b>Detention:</b> Below 65% - repeat semester<br>" +
            "• <b>Defaulter List:</b> Published every 3 weeks<br>" +
            "📍 <i>Check attendance on dept portal weekly</i>");
        
        KNOWLEDGE_BASE.put("library hours resources",
            "🏫 <b>Central Library Timings:</b><br>" +
            "• <b>Weekdays:</b> 8:30 AM - 8:30 PM<br>" +
            "• <b>Saturdays:</b> 9:00 AM - 5:00 PM<br>" +
            "• <b>Digital Section:</b> 24/7 access via campus VPN<br>" +
            "📚 <b>Resources:</b><br>" +
            "• 1,25,000+ books<br>" +
            "• IEEE, Springer, Elsevier e-journals<br>" +
            "• NPTEL video lectures access<br>" +
            "• Book bank for SC/ST students");
        
        KNOWLEDGE_BASE.put("lab sessions submission guidelines",
            "🔬 <b>Lab Guidelines:</b><br>" +
            "• <b>Timing:</b> As per timetable (2-4 hours/session)<br>" +
            "• <b>Submission:</b> Within 1 week of experiment<br>" +
            "• <b>Format:</b> A4 sheets with observation + viva<br>" +
            "• <b>Marks Split:</b> 40% record + 40% test + 20% viva<br>" +
            "⚠️ <i>Missing >2 labs leads to detention</i>");

        // 🏛️ ADMINISTRATIVE QUERIES
        KNOWLEDGE_BASE.put("fee structure payment deadlines",
            "💰 <b>Fee Structure (2024-25):</b><br>" +
            "• <b>Tuition Fee:</b> ₹87,500/sem (GM), ₹35,000 (SC/ST)<br>" +
            "• <b>Exam Fee:</b> ₹1,850/sem<br>" +
            "• <b>Miscellaneous:</b> ₹12,500 (one-time)<br>" +
            "• <b>Hostel:</b> ₹65,000/year<br>" +
            "⏰ <b>Deadlines:</b><br>" +
            "• Semester 1: Aug 31<br>" +
            "• Semester 2: Jan 31<br>" +
            "📍 Late fee: ₹50/day after due date");
        
        KNOWLEDGE_BASE.put("certificates bonafide transcript",
            "📄 <b>Certificate Issuance:</b><br>" +
            "• <b>Bonafide Certificate:</b> 1 working day<br>" +
            "• <b>Transcript:</b> 15 working days<br>" +
            "• <b>Migration Certificate:</b> After course completion<br>" +
            "• <b>Caste Certificate:</b> 3 working days<br>" +
            "📍 Apply at Admin Block, Ground Floor<br>" +
            "📞 Contact: 080-26614345");
        
        KNOWLEDGE_BASE.put("hostel facilities rules",
            "🏠 <b>Hostel Facilities:</b><br>" +
            "• Wi-Fi: 24/7 with 2GB daily limit<br>" +
            "• Mess: Veg/Non-veg options<br>" +
            "• Curfew: 9 PM (1st year), 10 PM (others)<br>" +
            "• Laundry: Weekly service<br>" +
            "• Gym: 6-8 AM, 5-7 PM<br>" +
            "⚠️ <b>Strict Rules:</b> No ragging, alcohol, or opposite gender in rooms");

        // 💼 CAREER & PLACEMENTS
        KNOWLEDGE_BASE.put("placement training companies",
            "🎯 <b>Placement Statistics:</b><br>" +
            "• <b>2023-24:</b> 92% placement rate<br>" +
            "• <b>Top Recruiters:</b> Microsoft, Amazon, Goldman Sachs<br>" +
            "• <b>Avg Package:</b> ₹8.5 LPA<br>" +
            "• <b>Highest Package:</b> ₹45 LPA<br>" +
            "🏆 <b>Training Schedule:</b><br>" +
            "• Aptitude: Mon-Wed, 4-6 PM<br>" +
            "• Coding: Thu-Fri, 4-6 PM<br>" +
            "• Soft Skills: Saturday, 10-12 PM<br>" +
            "📍 T&P Cell: Main Building, 3rd Floor");
        
        KNOWLEDGE_BASE.put("internship opportunities procedure",
            "🔍 <b>Internship Process:</b><br>" +
            "1. <b>Eligibility:</b> 7.5+ CGPA, no backlogs<br>" +
            "2. <b>Apply:</b> Through T&P portal monthly<br>" +
            "3. <b>Duration:</b> 6 weeks (summer), 6 months (final year)<br>" +
            "4. <b>Stipend:</b> ₹5,000-25,000/month<br>" +
            "5. <b>Credit:</b> 4 credits for approved internships<br>" +
            "🌐 Check: bmsce.ac.in/training-placements");

        // 🏀 CAMPUS LIFE & FACILITIES
        KNOWLEDGE_BASE.put("clubs associations join",
            "🎭 <b>Student Clubs:</b><br>" +
            "• <b>ACM:</b> Programming contests<br>" +
            "• <b>IEEE:</b> Technical workshops<br>" +
            "• <b>SAE:</b> Automotive projects<br>" +
            "• <b>NSS:</b> Social service<br>" +
            "• <b>Music/Dance:</b> Cultural events<br>" +
            "• <b>Sports:</b> Gymkhana activities<br>" +
            "📍 Registrations open in August");
        
        KNOWLEDGE_BASE.put("canteen food court timings",
            "🍔 <b>Food Courts:</b><br>" +
            "• <b>Main Canteen:</b> 8 AM - 8 PM<br>" +
            "• <b>Food Court 1:</b> 9 AM - 6 PM<br>" +
            "• <b>Food Court 2:</b> 9 AM - 5 PM<br>" +
            "• <b>Juice Center:</b> 10 AM - 4 PM<br>" +
            "💰 <b>Average Cost:</b> ₹50-120 per meal");
        
        KNOWLEDGE_BASE.put("transport bus routes timings",
            "🚌 <b>College Bus Service:</b><br>" +
            "• <b>Routes:</b> 15 routes covering Bangalore<br>" +
            "• <b>Timings:</b> Pickup 7-8 AM, Drop 4-5:30 PM<br>" +
            "• <b>Fee:</b> ₹15,000/semester<br>" +
            "• <b>App:</b> BMS Transport for live tracking<br>" +
            "📍 Transport Office: Near Main Gate");

        // 📅 IMPORTANT DATES & DEADLINES
        KNOWLEDGE_BASE.put("academic calendar events",
            "📅 <b>Academic Calendar 2024-25:</b><br>" +
            "• <b>Semester Start:</b> Aug 1, 2024<br>" +
            "• <b>Test 1:</b> Sep 15-30<br>" +
            "• <b>Test 2:</b> Nov 1-15<br>" +
            "• <b>Project Submission:</b> Dec 1<br>" +
            "• <b>Prep Holidays:</b> Dec 20-31<br>" +
            "• <b>Exams:</b> Jan 2-25, 2025<br>" +
            "• <b>Results:</b> Feb 15, 2025<br>" +
            "📱 Download calendar: bmsce.ac.in/academic");
        
        KNOWLEDGE_BASE.put("fest events bms engage",
            "🎉 <b>College Fests:</b><br>" +
            "• <b>Engage:</b> Tech fest (March)<br>" +
            "• <b>Utsav:</b> Cultural fest (September)<br>" +
            "• <b>Sports Fest:</b> November<br>" +
            "• <b>Department Fests:</b> Throughout year<br>" +
            "📍 Follow @bmsce_engage on Instagram");

        // 🆘 EMERGENCY & SUPPORT
        KNOWLEDGE_BASE.put("emergency contacts crisis support",
            "🚨 <b>Emergency Contacts:</b><br>" +
            "• <b>Campus Security:</b> 080-26614345 ext 111<br>" +
            "• <b>Medical Room:</b> ext 222 (24/7 nurse)<br>" +
            "• <b>Ambulance:</b> ext 333<br>" +
            "• <b>Women's Cell:</b> 080-26614345 ext 444<br>" +
            "• <b>Anti-Ragging:</b> 1800-180-5522<br>" +
            "• <b>Counselling:</b> Mon-Fri 10 AM-4 PM");

        // ✅ NEW PRECISE ACADEMIC RESPONSES
        KNOWLEDGE_BASE.put("academics",
            "📚 <b>BMS Academic Overview:</b><br>" +
            "• <b>Programs:</b> 13 UG (BE in CSE, ECE, etc.) + 13 PG (MTech, MBA, MCA) under VTU CBCS scheme<br>" +
            "• <b>Support Resources:</b> Central Library (e-books, journals), Proctor system for mentoring, Academic Calendar (Odd: Jul-Dec, Even: Jan-Jun)<br>" +
            "• <b>Grading:</b> SGPA/CGPA on 10-point scale; 40% min to pass<br>" +
            "• <b>Key Dates (2025):</b> Odd Sem Starts: Jul 15; Exams: Nov-Dec; Results: Jan<br>" +
            "<i>Check VTU portal for updates. Need syllabus or internals help?</i>");
        
        KNOWLEDGE_BASE.put("how do i manage assignment deadlines",
            "📝 <b>Assignment Management at BMS:</b><br>" +
            "• <b>Deadlines:</b> Submit via Google Classroom/Dept Portal by 5 PM (late: 10% deduction/day)<br>" +
            "• <b>Resources:</b> Library (1st floor, assignment templates), CRs for group work, Proctor for extensions (apply 3 days early)<br>" +
            "• <b>Tips:</b> Use planner app (Notion/OneNote); Break into chunks (1 hr/day); Form study groups via WhatsApp<br>" +
            "• <b>Weightage:</b> 20 marks in Internals – mandatory for eligibility<br>" +
            "<i>Pro Tip: Track via VTU app notifications!</i>");
        
        KNOWLEDGE_BASE.put("exam preparation tips",
            "🎯 <b>Exam Prep at BMS (VTU Pattern):</b><br>" +
            "• <b>Strategy:</b> Past 5 years papers (library/VTU site); Focus 60% theory + 40% problems; Daily 4-6 hrs study<br>" +
            "• <b>Resources:</b> Model papers from dept, Online mocks via NPTEL, Group revisions (CSE Block, weekends)<br>" +
            "• <b>Schedule:</b> 1 month pre-exam: 70% revision + 30% practice; Use Pomodoro (25 min study/5 min break)<br>" +
            "• <b>Passing:</b> 40% aggregate; Reval within 30 days (₹500/subject)<br>" +
            "<i>Attend faculty doubt sessions – high success rate!</i>");
        
        KNOWLEDGE_BASE.put("study schedule for 5 subjects",
            "📅 <b>Sample Study Schedule for 5 Subjects (BMS Weekly Plan):</b><br>" +
            "• <b>Mon-Wed:</b> 2 hrs/subject (Math/Physics morning; DSA/ECE afternoon)<br>" +
            "• <b>Thu-Sat:</b> Revision + Practice (1 hr each; include 1 mock test/day)<br>" +
            "• <b>Sun:</b> Full review + weak areas (4 hrs total)<br>" +
            "• <b>Total:</b> 25-30 hrs/week; Include 1 hr breaks + sleep 7 hrs<br>" +
            "• <b>Tools:</b> Google Calendar sync with VTU dates; Track via Proctor meetings<br>" +
            "<i>Adjust based on internals (Tests: Sep/Feb). Need timetable generator?</i>");
        
        KNOWLEDGE_BASE.put("how to improve concentration",
            "🧠 <b>Focus Tips for BMS Students:</b><br>" +
            "• <b>Techniques:</b> Pomodoro (25 min focus/5 min break); Mindfulness (Headspace app, 10 min/day)<br>" +
            "• <b>Environment:</b> Library quiet zones; No-phone policy during study (use Forest app)<br>" +
            "• <b>Habits:</b> 7-8 hrs sleep, 30 min walk (campus garden); Hydrate + healthy snacks (canteen fruits)<br>" +
            "• <b>BMS Resources:</b> Yoga sessions (Wed 5PM, Sports Complex); Counselor for ADHD screening<br>" +
            "<i>Start small: 1 focus session/day. Track progress in journal!</i>");

        // Study Groups
        STUDY_GROUPS.addAll(Arrays.asList(
            "DSA Study Group (Mon/Wed 4PM, Library Room 3)",
            "VTU Papers Solving (Tue/Thu 5PM, CSE Block)",
            "Placement Prep Group (Daily 6PM, Placement Cell)",
            "Project Collaboration (Sat 10AM, ECE Lab)",
            "Stress Support Group (Fri 4PM, Admin Room 201)"
        ));
    }

    /* -------------------- MAIN CHAT ENDPOINT -------------------- */
    @GetMapping("/chat")
    public String getChatResponse(@RequestParam String message) {
        String msg = message.toLowerCase().trim();
        
        // Emotional support check
        for (String key : EMOTIONAL_SUPPORT.keySet()) {
            String[] keywords = key.split(" ");
            boolean allMatch = true;
            for (String keyword : keywords) {
                if (!msg.contains(keyword)) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) return EMOTIONAL_SUPPORT.get(key);
        }
        
        // Specific emotional checks
        if (msg.contains("overwhelm") || msg.contains("too much") || msg.contains("can't handle")) {
            return EMOTIONAL_SUPPORT.get("keep up assignments deadlines burnout");
        }
        
        if (msg.contains("alone") || msg.contains("lonely") || msg.contains("isolated")) {
            return "🧡 <b>Feeling Isolated?</b><br>It's common to feel alone in a crowd. Remember:<br>" +
                   "• Join clubs (IEEE, Music, Sports)<br>" +
                   "• Attend college events and fests<br>" +
                   "• Form study groups with classmates<br>" +
                   "• Visit counseling center (Room 201)<br>" +
                   "• Everyone feels this way sometimes";
        }
        
        if (msg.contains("procrastinat")) {
            return "⏰ <b>Beat Procrastination:</b><br>" +
                   "1. <b>2-Minute Rule:</b> Start for just 2 minutes<br>" +
                   "2. <b>Remove Distractions:</b> Phone in another room<br>" +
                   "3. <b>Break Tasks:</b> Small, manageable chunks<br>" +
                   "4. <b>Reward System:</b> Complete task → 15-min break<br>" +
                   "5. <b>Accountability:</b> Study with a friend<br>" +
                   "<i>Action creates motivation, not the other way around.</i>";
        }
        
        // Timetable generator
        if (msg.contains("timetable") || msg.contains("schedule") || msg.contains("time table")) {
            Pattern pattern = Pattern.compile("\\b(math|physics|chemistry|dsa|java|python|c\\+\\+|calculus|algebra|circuits|digital|mechanics)\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(msg);
            List<String> subjects = new ArrayList<>();
            while (matcher.find()) subjects.add(matcher.group().toLowerCase());
            if (subjects.size() >= 2) return generatePersonalizedTimetable(subjects);
            else return "📅 <b>Timetable Generator</b><br>Please specify 3-6 subjects. Example:<br>" +
                        "• 'Create timetable for Math Physics Chemistry'<br>" +
                        "• 'Generate schedule for DSA Java Python'<br>" +
                        "• 'Timetable for calculus, circuits, digital'";
        }
        
        // Knowledge base lookup
        for (String key : KNOWLEDGE_BASE.keySet()) {
            if (msg.contains(key)) return KNOWLEDGE_BASE.get(key);
        }
        
        // Department-specific queries
        String[] departments = {"cse", "ise", "ece", "mech", "civil"};
        for (String dept : departments) {
            if (msg.contains(dept)) {
                return getDepartmentSpecificInfo(dept);
            }
        }
        
        // Semester-specific queries
        Pattern semPattern = Pattern.compile("semester\\s*(\\d+)");
        Matcher semMatcher = semPattern.matcher(msg);
        if (semMatcher.find()) {
            int sem = Integer.parseInt(semMatcher.group(1));
            if (sem >= 1 && sem <= 8) {
                return getSemesterSpecificInfo(msg, sem);
            }
        }
        
        // General responses
        if (msg.contains("hello") || msg.contains("hi ") || msg.contains("hey")) {
            return "Hello! 👋 Welcome to BMS College support. How can I help you today?";
        }
        
        if (msg.contains("thank")) {
            return "You're welcome! 😊 Remember to take care of yourself. Need anything else?";
        }
        
        if (msg.contains("how are you")) {
            return "I'm here and ready to support you! 💪 How's your day going?";
        }
        
        // Default empathetic responses
        List<String> defaultResponses = Arrays.asList(
            "Sounds tough—BMS Proctors can help with personalized plans (meet yours weekly). Try 'assignment help' for deadlines or 'exam preparation' for VTU mocks. What's the main hurdle?",
            "Many CSE students hit this wall mid-sem. Check VTU portal for extensions (results.vtu.ac.in). For quick wins, say 'study schedule' to generate a 5-subject plan.",
            "Let's zero in: 'internals' for Test 1/2 tips (40 marks total), or 'notes' for library e-resources. Proctor office (Room 105) is open Mon-Fri 2-4 PM—book via email?",
            "BMS Placement Cell has free resume reviews (Fridays, CSE Block). If it's stress-related, click 'Stress Relief' for grounding exercises. Need career guidance now?",
            "Start here: Library for past papers (1st floor, open 8AM-10PM), or join a study group via 'Peer Study Groups'. VTU app notifications keep you on track—what's your top priority?"
        );
        
        return defaultResponses.get(random.nextInt(defaultResponses.size()));
    }

    /* -------------------- PEER STUDY ENDPOINTS -------------------- */
    @GetMapping("/peer-study")
    public String startPeerStudy(@RequestParam int minutes) {
        String sessionId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        
        // Simulate subjects if from timetable
        List<String> subjects = Arrays.asList("Math", "Physics", "DSA");
        StudySession session = new StudySession(sessionId, minutes, subjects);
        ACTIVE_SESSIONS.put(sessionId, session);
        
        // Timer to update progress every minute
        ScheduledFuture<?> timer = timerService.scheduleAtFixedRate(() -> {
            long elapsed = (System.currentTimeMillis() - session.startTime) / 60000;
            session.progress = Math.min(100, (elapsed / (double) minutes) * 100);
            System.out.println("👥 PEER STUDY [" + sessionId + "] - Progress: " + session.progress + "%");
        }, 0, 1, TimeUnit.MINUTES);
        
        STUDY_TIMERS.put(sessionId, timer);
        
        // Schedule cleanup after session ends
        timerService.schedule(() -> {
            timer.cancel(false);
            STUDY_TIMERS.remove(sessionId);
            ACTIVE_SESSIONS.remove(sessionId);
            System.out.println("✅ Session " + sessionId + " completed! Report ready.");
        }, minutes, TimeUnit.MINUTES);
        
        return "<div style='background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%); padding: 20px; border-radius: 15px; border-left: 5px solid #10b981;'>" +
               "<h3 style='color: #065f46; margin-bottom: 10px;'>👥 Peer Study Session Started!</h3>" +
               "<p><b>Duration:</b> " + minutes + " minutes</p>" +
               "<p><b>Session ID:</b> " + sessionId + "</p>" +
               "<p><b>Subjects:</b> " + String.join(", ", subjects) + "</p>" +
               "<p><b>Tips:</b></p><ul style='margin-left: 20px;'><li>Keep cameras on for accountability</li><li>Take 5-minute breaks every 25 minutes</li><li>Share screen for problem-solving</li><li>Stay hydrated and take stretch breaks</li></ul>" +
               "<p style='margin-top: 10px; color: #065f46;'><i>Stay focused and support each other! 🌟 Report auto-generates at end.</i></p>" +
               "<script>startStudyTimer('" + sessionId + "', " + minutes + ");</script>" +
               "</div>";
    }

    @GetMapping("/study-progress")
    public Map<String, Object> getStudyProgress(@RequestParam String sessionId) {
        StudySession session = ACTIVE_SESSIONS.get(sessionId);
        if (session == null) {
            return Map.of("error", "Session not found");
        }
        
        long elapsed = (System.currentTimeMillis() - session.startTime) / 1000;
        int remaining = Math.max(0, (session.duration * 60) - (int) elapsed);
        
        return Map.of(
            "sessionId", session.sessionId,
            "progress", session.progress,
            "remainingSeconds", remaining,
            "subjects", session.subjects
        );
    }

    @GetMapping("/generate-report")
    public String generateProgressReport(@RequestParam String sessionId) {
        StudySession session = ACTIVE_SESSIONS.remove(sessionId);
        if (session == null) {
            return "<div style='color: red;'>Session not found. Start a new one!</div>";
        }
        
        // Cancel timer
        ScheduledFuture<?> timer = STUDY_TIMERS.remove(sessionId);
        if (timer != null) {
            timer.cancel(false);
        }
        
        double completion = session.progress;
        String grade = completion >= 90 ? "Excellent! 🎉" : completion >= 70 ? "Good Job! 👍" : "Keep Going! 💪";
        String tips = completion < 70 ? 
            "<li>Next time, minimize distractions (phone off)</li><li>Pair with a study buddy</li>" : 
            "<li>Great pace—try adding 10 min review</li>";
        
        return "<div style='background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%); padding: 20px; border-radius: 15px; border-left: 5px solid #d97706;'>" +
               "<h3 style='color: #92400e; margin-bottom: 10px;'>📊 Study Progress Report</h3>" +
               "<p><b>Session ID:</b> " + session.sessionId + "</p>" +
               "<p><b>Duration Planned:</b> " + session.duration + " min | <b>Completed:</b> " + String.format("%.0f", completion) + "%</p>" +
               "<p><b>Subjects Covered:</b> " + String.join(", ", session.subjects) + "</p>" +
               "<p><b>Grade:</b> " + grade + "</p>" +
               "<h4 style='color: #b45309; margin: 10px 0;'>Quick Tips:</h4>" +
               "<ul style='margin-left: 20px;'>" + tips + "</ul>" +
               "<p style='margin-top: 10px; color: #92400e;'><i>Log this for Proctor review. Start another session?</i></p>" +
               "</div>";
    }

    /* -------------------- TIMETABLE ENDPOINTS -------------------- */
    @GetMapping("/generate-timetable")
    public String generateTimetable(@RequestParam String subjects) {
        String[] subjectArray = subjects.split(",");
        List<String> subjectList = Arrays.asList(subjectArray);
        return generatePersonalizedTimetable(subjectList);
    }

    /* -------------------- HELPER METHODS -------------------- */
    private String generatePersonalizedTimetable(List<String> subjects) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%); padding: 25px; border-radius: 20px; border: 2px solid #0ea5e9; box-shadow: 0 8px 30px rgba(14, 165, 233, 0.15);'>");
        sb.append("<h3 style='color: #0369a1; margin-bottom: 20px; display: flex; align-items: center; gap: 10px;'>📅 Personalized Study Timetable</h3>");
        
        // Subject chips
        sb.append("<div style='margin-bottom: 20px;'>");
        sb.append("<p><b>Subjects:</b></p>");
        sb.append("<div style='display: flex; flex-wrap: wrap; gap: 8px; margin-top: 10px;'>");
        for (String subject : subjects) {
            String color = getSubjectColor(subject);
            sb.append("<span style='background:").append(color)
              .append("; padding: 8px 16px; border-radius: 20px; font-weight: 500; color: #1e3a8a;'>")
              .append(subject.substring(0, 1).toUpperCase()).append(subject.substring(1))
              .append("</span>");
        }
        sb.append("</div></div>");
        
        // Weekly schedule table
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String[][] schedule = createBalancedSchedule(subjects);
        
        sb.append("<table style='width: 100%; border-collapse: collapse; background: white; border-radius: 15px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05);'>");
        sb.append("<thead><tr style='background: linear-gradient(135deg, #0a2e8a 0%, #3b82f6 100%); color: white;'>");
        sb.append("<th style='padding: 15px; text-align: left;'>Day</th>");
        sb.append("<th style='padding: 15px; text-align: center;'>Morning<br>(8-10 AM)</th>");
        sb.append("<th style='padding: 15px; text-align: center;'>Late Morning<br>(10:30-12:30)</th>");
        sb.append("<th style='padding: 15px; text-align: center;'>Afternoon<br>(2-4 PM)</th>");
        sb.append("<th style='padding: 15px; text-align: center;'>Evening<br>(4:30-6:30)</th>");
        sb.append("<th style='padding: 15px; text-align: center;'>Night<br>(7:30-9:30)</th>");
        sb.append("</tr></thead><tbody>");
        
        for (int i = 0; i < days.length; i++) {
            sb.append("<tr style='border-bottom: 1px solid #e5e7eb;'>");
            sb.append("<td style='padding: 15px; font-weight: 600; background: #f8fafc;'>").append(days[i]).append("</td>");
            for (int j = 0; j < 5; j++) {
                String subject = schedule[i][j];
                String color = getSubjectColor(subject);
                sb.append("<td style='padding: 12px; text-align: center; background:").append(color)
                  .append("; border-radius: 8px; margin: 2px; font-weight: 500;'>")
                  .append(subject.isEmpty() ? "-" : subject)
                  .append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");
        
        // Study tips
        sb.append("<div style='margin-top: 25px; padding: 20px; background: white; border-radius: 15px; border-left: 5px solid #d4af37;'>");
        sb.append("<h4 style='color: #b45309; margin-bottom: 15px;'>💡 Weekly Study Strategy</h4>");
        sb.append("<ul style='margin-left: 20px;'>");
        sb.append("<li><b>Sunday:</b> Plan your week + review previous topics</li>");
        sb.append("<li><b>Active Learning:</b> Teach concepts to someone else</li>");
        sb.append("<li><b>Breaks:</b> 5-10 minutes every 50 minutes</li>");
        sb.append("<li><b>Weekends:</b> Focus on projects and revision</li>");
        sb.append("<li><b>Self-care:</b> 7-8 hours sleep + 30-min exercise daily</li>");
        sb.append("</ul></div></div>");
        
        return sb.toString();
    }

    private String getSemesterSpecificInfo(String query, int semester) {
        Map<Integer, String> semInfo = new HashMap<>();
        semInfo.put(1, "📘 <b>Semester 1 Focus:</b><br>" +
                      "• Build fundamentals in Maths & Physics<br>" +
                      "• Join 2 clubs for exploration<br>" +
                      "• CGPA target: 8.0+<br>" +
                      "• Attend all orientation sessions");
        semInfo.put(2, "📘 <b>Semester 2 Focus:</b><br>" +
                      "• Start coding basics<br>" +
                      "• Build project portfolio<br>" +
                      "• Target internships for summer<br>" +
                      "• Network with seniors");
        semInfo.put(3, "📘 <b>Semester 3 Focus:</b><br>" +
                      "• Core subjects specialization<br>" +
                      "• Participate in hackathons<br>" +
                      "• Start preparation for GATE/GRE<br>" +
                      "• Leadership roles in clubs");
        semInfo.put(4, "📘 <b>Semester 4 Focus:</b><br>" +
                      "• Internship applications<br>" +
                      "• Research project initiation<br>" +
                      "• Competitive coding practice<br>" +
                      "• Certification courses");
        semInfo.put(5, "📘 <b>Semester 5 Focus:</b><br>" +
                      "• Placement preparation begins<br>" +
                      "• Major project selection<br>" +
                      "• Mock interviews weekly<br>" +
                      "• Industry visits");
        semInfo.put(6, "📘 <b>Semester 6 Focus:</b><br>" +
                      "• Finalize job/internship<br>" +
                      "• Complete major project<br>" +
                      "• Higher education applications<br>" +
                      "• Alumni networking");
        semInfo.put(7, "📘 <b>Semester 7 Focus:</b><br>" +
                      "• Placement interviews<br>" +
                      "• Project implementation<br>" +
                      "• Skill enhancement courses<br>" +
                      "• Industry training");
        semInfo.put(8, "📘 <b>Semester 8 Focus:</b><br>" +
                      "• Final project submission<br>" +
                      "• Placement completion<br>" +
                      "• Graduation requirements<br>" +
                      "• Alumni transition");
        
        return semInfo.getOrDefault(semester, "Please specify semester (1-8)");
    }

    private String getDepartmentSpecificInfo(String dept) {
        Map<String, String> deptMap = new HashMap<>();
        deptMap.put("cse", "💻 <b>Computer Science:</b><br>" +
                      "• HOD: Dr. S. R. Ramesh<br>" +
                      "• Lab: Open 8 AM - 8 PM<br>" +
                      "• Clubs: ACM, CodeChef, Google DSC<br>" +
                      "• Placement: 95% with avg ₹9 LPA");
        deptMap.put("ise", "💻 <b>Information Science:</b><br>" +
                      "• HOD: Dr. M. K. Ramesh<br>" +
                      "• Lab: Open 8:30 AM - 7:30 PM<br>" +
                      "• Focus: Database, Networking<br>" +
                      "• Placement: 92% with avg ₹8.5 LPA");
        deptMap.put("ece", "📡 <b>Electronics & Communication:</b><br>" +
                      "• HOD: Dr. R. N. Sharma<br>" +
                      "• Lab: VLSI, Embedded Systems<br>" +
                      "• Projects: IoT, Signal Processing<br>" +
                      "• Placement: 90% with avg ₹8 LPA");
        deptMap.put("mech", "⚙️ <b>Mechanical Engineering:</b><br>" +
                      "• HOD: Dr. K. S. Kumar<br>" +
                      "• Workshops: CNC, CAD/CAM<br>" +
                      "• Club: SAE<br>" +
                      "• Placement: 88% with avg ₹7.5 LPA");
        deptMap.put("civil", "🏗️ <b>Civil Engineering:</b><br>" +
                      "• HOD: Dr. P. T. Rao<br>" +
                      "• Lab: Concrete, Surveying<br>" +
                      "• Projects: Structural Design<br>" +
                      "• Placement: 85% with avg ₹7 LPA");
        
        return deptMap.getOrDefault(dept.toLowerCase(), 
            "Department info available for: CSE, ISE, ECE, Mech, Civil");
    }

    private String[][] createBalancedSchedule(List<String> subjects) {
        String[][] schedule = new String[6][5];
        for (int i = 0; i < 6; i++) Arrays.fill(schedule[i], "");
        
        Map<String, Integer> subjectCount = new HashMap<>();
        for (String subject : subjects) subjectCount.put(subject, 3);
        
        List<String> allSlots = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : subjectCount.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) allSlots.add(entry.getKey());
        }
        
        allSlots.add("Revision");
        allSlots.add("Practice");
        allSlots.add("Projects");
        Collections.shuffle(allSlots);
        
        int index = 0;
        for (int day = 0; day < 6; day++) {
            for (int slot = 0; slot < 5; slot++) {
                if (index < allSlots.size()) {
                    schedule[day][slot] = allSlots.get(index++);
                }
            }
        }
        
        return schedule;
    }

    private String getSubjectColor(String subject) {
        subject = subject.toLowerCase();
        if (subject.contains("math") || subject.contains("calculus") || subject.contains("algebra")) 
            return "#fef2f2";
        else if (subject.contains("physics") || subject.contains("mechanics")) 
            return "#f0f9ff";
        else if (subject.contains("chemistry")) 
            return "#f0fdf4";
        else if (subject.contains("dsa") || subject.contains("algorithm")) 
            return "#faf5ff";
        else if (subject.contains("java") || subject.contains("python") || subject.contains("programming")) 
            return "#eff6ff";
        else if (subject.contains("circuit") || subject.contains("digital")) 
            return "#fffbeb";
        else if (subject.contains("revision")) 
            return "#fef3c7";
        else if (subject.contains("practice")) 
            return "#dcfce7";
        else if (subject.contains("project")) 
            return "#e0e7ff";
        else 
            return "#f8fafc";
    }

    /* -------------------- HEALTH CHECK -------------------- */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Backend healthy! ✅ All features (Chat, Peer Study, Timetable, Progress Tracking) ready.");
    }

    /* -------------------- COLLEGE LOGO -------------------- */
    @GetMapping(value = "/college-logo", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getCollegeLogo() throws IOException {
        Resource resource = new ClassPathResource("static/bms-logo.jpg");
        byte[] bytes = StreamUtils.copyToByteArray(resource.getInputStream());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
    }

    /* -------------------- COLLEGE MOTTO -------------------- */
    @GetMapping("/motto")
    public ResponseEntity<Map<String, String>> getMotto() {
        Map<String, String> response = new HashMap<>();
        response.put("motto", COLLEGE_MOTTO);
        response.put("translation", "Speak the Truth, Practice Righteousness");
        response.put("language", "Sanskrit");
        return ResponseEntity.ok(response);
    }
}