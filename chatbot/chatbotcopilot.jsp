<%@ page contentType="application/json; charset=UTF-8" language="java" %>
<%
  // Read the question number parameter
  String qParam = request.getParameter("q");
  String agent = "Lexi";
  String brand = "Achievo";

  // Default question and answer
  String question = "";
  String answer = "";
  String category = "General Advice";

  if (qParam == null || qParam.trim().isEmpty()) {
    // If no parameter, return a welcome message
    question = "Welcome";
    answer = "Welcome to Achievo, your campus companion! I'm Lexi, here to help you navigate college challenges. I can provide guidance on time management, stress management, study techniques, college life, exam preparation, and health & wellness. Select a question or type your own!";
  } else {
    try {
      int qNum = Integer.parseInt(qParam.trim());
      
      // Map of all 37 questions with their categories and answers
      // Based on the updated chatbot with 6 categories and 37 total questions
      switch (qNum) {
        // Time Management Questions (1-6)
        case 1:
          category = "Time Management";
          question = "What's the most effective way to balance academics, part-time work, and social life?";
          answer = "Master the art of the 'Time Block': Map your non-negotiables first (classes, work). Use the 2:1 study rule (2 hours study per class hour). Theme your days (e.g., 'Focus Mondays'). Protect recovery time. Communicate proactively with employers during busy periods. Balance is dynamic—review weekly.";
          break;
        case 2:
          category = "Time Management";
          question = "How can I create an effective weekly schedule that includes classes, study, and leisure?";
          answer = "Build a schedule that serves you: 1) Reverse-engineer from sleep/study needs first. 2) Batch similar tasks. 3) Use time boxing (assign specific times). 4) Include 15-min buffer zones. 5) Schedule leisure intentionally. 6) Use color-coded digital calendars for visibility.";
          break;
        case 3:
          category = "Time Management";
          question = "What's the best way to prioritize tasks when everything seems important?";
          answer = "Use the Eisenhower Matrix: 1) Urgent & Important = DO NOW. 2) Not Urgent & Important = SCHEDULE. 3) Urgent & Not Important = DELEGATE/LIMIT. 4) Not Urgent & Not Important = ELIMINATE. Shrink Quadrant 1 by spending more time in Quadrant 2. Pick daily 'Top 3' tasks.";
          break;
        case 4:
          category = "Time Management";
          question = "How do I break down large projects into manageable steps?";
          answer = "Defeat the monster by naming its parts: 1) Start with the end vision. 2) Work backwards from the due date. 3) Chunk into phases (Research→Outline→Draft→Revise→Polish). 4) Break phases into 30-90 min 'micro-tasks'. 5) Assign deadlines with 20% buffer. 6) Focus only on the next small win.";
          break;
        case 5:
          category = "Time Management";
          question = "How can I use technology effectively to manage my time and tasks?";
          answer = "Let tech be your assistant: Use Google Calendar/Notion as central command. Trello/Asana for visual project boards. Todoist for task lists. Forest/Focus Keeper for Pomodoro timers. Cold Turkey to block distractions. OneNote for notes. Zotero for citations. Golden rule: Use minimal apps, master them.";
          break;
        case 6:
          category = "Time Management";
          question = "How do I avoid procrastination when assignments feel overwhelming?";
          answer = "Beat procrastination by making starting effortless: 1) 5-Minute Spark rule. 2) Make tasks obvious, easy, satisfying. 3) Change environment (go to library). 4) Focus on process goals ('write for 25 min') not product goals ('write perfect paper'). 5) Forgive past procrastination and start fresh now.";
          break;
        
        // Stress Management Questions (7-13)
        case 7:
          category = "Stress Management";
          question = "How can I manage exam anxiety without burning out?";
          answer = "Transform anxiety into focused energy: 1) Preparation is best antidote (spaced study plan). 2) Practice under pressure with timed tests. 3) Reframe nerves as excitement. 4) Use 5-4-3-2-1 grounding during exams. 5) Have post-exam decompression ritual. Confidence comes from systematic preparation.";
          break;
        case 8:
          category = "Stress Management";
          question = "What are immediate techniques to calm down during a panic attack before an exam?";
          answer = "Emergency protocols: 1) 4-7-8 breathing (inhale 4s, hold 7s, exhale 8s). 2) Temperature shock with cold water/ice. 3) 5-4-3-2-1 sensory grounding. 4) Repeat calming mantras. If frequent, seek campus counseling for long-term strategies like CBT.";
          break;
        case 9:
          category = "Stress Management";
          question = "How do I handle academic pressure from parents and high expectations?";
          answer = "Navigate pressure with clarity: 1) Shift to shared understanding via calm conversation. 2) Redefine success together (growth vs. just grades). 3) Set informational boundaries (periodic updates). 4) Build internal validation via 'wins journal'. 5) Seek advisor mediation if needed.";
          break;
        case 10:
          category = "Stress Management";
          question = "What are healthy ways to cope with academic failure or low grades?";
          answer = "Treat setback as data, not destiny: 1) Allow 24-48h to feel, then problem-solve. 2) Conduct neutral post-mortem analysis. 3) Seek specific professor feedback. 4) Create 'recovery plan' with actionable steps. 5) Practice self-compassion. Resilience is built through recovery.";
          break;
        case 11:
          category = "Stress Management";
          question = "How can I reduce stress when I have multiple deadlines approaching?";
          answer = "Strategic deadline management: 1) Create visual timeline with mini-deadlines. 2) Prioritize by due date and weight. 3) Break into 30-60 min micro-tasks. 4) Communicate early if extensions needed. 5) Use Pomodoro method. 6) Practice strategic neglect of non-essentials. 7) Maintain minimum self-care.";
          break;
        case 12:
          category = "Stress Management";
          question = "How do I prevent burnout while juggling multiple responsibilities?";
          answer = "Proactive burnout prevention: 1) Set clear work-free time boundaries. 2) Practice 80/20 rule (focus on high-impact efforts). 3) Schedule regular short/long breaks. 4) Monitor energy cycles. 5) Develop non-academic hobbies. 6) Learn to delegate. 7) Create consistent wind-down routine.";
          break;
        case 13:
          category = "Stress Management";
          question = "What are signs that I should seek professional help for my stress levels?";
          answer = "Seek help if symptoms persist 2+ weeks and interfere with functioning: Emotional (persistent overwhelm, numbness, irritability). Physical (sleep/appetite changes, constant fatigue, frequent illness). Cognitive (concentration issues, negative thinking). Behavioral (withdrawal, substance use, avoidance). Campus counseling offers confidential, often free services.";
          break;
        
        // Study Techniques Questions (14-19)
        case 14:
          category = "Study Techniques";
          question = "How do I stay consistent with studying across the semester?";
          answer = "Build sustainable study habits: 1) Establish consistent time/location routine. 2) Start with small daily goals (30 min > 5 hours weekly). 3) Use active recall and spaced repetition. 4) Conduct weekly reviews. 5) Track progress visually. 6) Match difficult subjects with peak energy times.";
          break;
        case 15:
          category = "Study Techniques";
          question = "What are effective note-taking strategies for different subjects?";
          answer = "Tailor notes to subjects: Conceptual (Math/Physics): Cornell Method with formulas+examples. Content-heavy (History/Bio): Outline Method with color coding. Discussion-based (Lit/Philosophy): Mapping Method for connections. Always review within 24h, summarize each page, consider digital tools like Notion.";
          break;
        case 16:
          category = "Study Techniques";
          question = "How can I improve my concentration and focus during long study sessions?";
          answer = "Develop concentration as a skill: 1) Optimize environment (consistent, minimal distractions). 2) Use Pomodoro/time blocking. 3) Practice single-tasking. 4) Incorporate movement breaks every 45-60 min. 5) Try focus-enhancing sounds. 6) Regular mindfulness meditation. 7) Stay hydrated and nourished.";
          break;
        case 17:
          category = "Study Techniques";
          question = "What are the best methods for memorizing complex information?";
          answer = "Effective memorization strategies: 1) Spaced Repetition System (Anki flashcards). 2) Mnemonics and memory palaces. 3) Active recall (self-testing). 4) Interleaving different subjects. 5) Teaching concepts to others. 6) Creating visual mind maps. 7) Connecting new info to existing knowledge.";
          break;
        case 18:
          category = "Study Techniques";
          question = "How do I study effectively for subjects I find difficult or boring?";
          answer = "Tackle difficult subjects: 1) Find real-world applications to spark interest. 2) Use the Pomodoro technique (25 min focused, 5 min break). 3) Study in different locations. 4) Form study groups for accountability. 5) Gamify learning with rewards. 6) Connect material to personal goals. 7) Seek professor/TA help early.";
          break;
        case 19:
          category = "Study Techniques";
          question = "What's the difference between active and passive learning, and which is better?";
          answer = "Active learning (retrieval practice, teaching, problem-solving) engages you with material, leading to ~50-75% better retention. Passive learning (rereading, highlighting, listening) feels easier but has lower retention. Prioritize active methods: flashcards, practice problems, summarizing without notes, teaching concepts aloud.";
          break;
        
        // College Life Questions (20-25)
        case 20:
          category = "College Life";
          question = "How do I cope with homesickness and being away from family?";
          answer = "Manage homesickness by: 1) Creating home-away-from-home with familiar items. 2) Establishing new routines. 3) Staying connected but not over-connected. 4) Getting involved on campus. 5) Finding 'family' among peers/mentors. 6) Exploring new city positively. 7) Being patient—adjustment takes time.";
          break;
        case 21:
          category = "College Life";
          question = "How can I make meaningful connections and friends in a new college?";
          answer = "Build college friendships: 1) Attend orientation events. 2) Join interest-based clubs. 3) Participate in class/study groups. 4) Be open and approachable. 5) Use campus social resources. 6) Live on campus if possible. 7) Be patient—deep friendships develop over time. 8) Follow up after meeting someone.";
          break;
        case 22:
          category = "College Life";
          question = "How do I manage finances as a college student living away from home?";
          answer = "Financial management essentials: 1) Create realistic budget tracking income/expenses. 2) Prioritize needs over wants. 3) Use student discounts everywhere. 4) Cook at home vs. eating out. 5) Use free campus resources. 6) Avoid credit card debt. 7) Consider on-campus part-time work. 8) Review subscriptions regularly.";
          break;
        case 23:
          category = "College Life";
          question = "How do I handle conflicts with roommates or classmates?";
          answer = "Conflict resolution steps: 1) Address issues early before resentment builds. 2) Use 'I feel' statements, not accusations. 3) Listen actively to other perspective. 4) Seek compromise/solutions together. 5) Establish clear agreements/rules. 6) Involve RA/mediator if needed. 7) Know when to disengage from toxic situations.";
          break;
        case 24:
          category = "College Life";
          question = "What should I do if I'm feeling lonely or isolated on campus?";
          answer = "Combat loneliness: 1) Join one new activity/club this week. 2) Attend campus events (even alone). 3) Use study groups for social+academic connection. 4) Volunteer—helps others and builds community. 5) Reach out to campus counseling services. 6) Remember many students feel similarly—be brave and initiate.";
          break;
        case 25:
          category = "College Life";
          question = "How do I balance independence with asking for help when needed?";
          answer = "Healthy independence balance: 1) Try solving problems yourself first (builds resilience). 2) Identify when you're stuck—set time limit before seeking help. 3) Know who to ask (professors, TAs, advisors, counseling). 4) Frame requests clearly with what you've tried. 5) Remember asking for help is strength, not weakness.";
          break;
        
        // Exam Preparation Questions (26-31)
        case 26:
          category = "Exam Preparation";
          question = "How do I prepare effectively for final exams in multiple subjects?";
          answer = "Strategic exam prep: 1) Create master schedule allocating time by difficulty/date. 2) Prioritize active recall over passive rereading. 3) Study in different locations for better retention. 4) Form/join study groups. 5) Practice with past exams under timed conditions. 6) Use spaced repetition. 7) Schedule final review days.";
          break;
        case 27:
          category = "Exam Preparation";
          question = "What's the best way to review and revise before exams?";
          answer = "Effective revision: 1) Create summary sheets for each topic. 2) Focus on weak areas identified through self-testing. 3) Use the Feynman Technique (explain simply). 4) Practice with different question types. 5) Review mistakes from past assignments/tests. 6) Teach material to study partner. 7) Get adequate sleep before exam.";
          break;
        case 28:
          category = "Exam Preparation";
          question = "How can I improve my test-taking skills and reduce mistakes?";
          answer = "Test-taking mastery: 1) Skim entire exam first, allocate time. 2) Answer known questions first for momentum. 3) Read questions carefully, underline key terms. 4) Show all work for partial credit. 5) Review answers with fresh perspective. 6) Manage anxiety with breathing techniques. 7) Learn from returned exams to improve next time.";
          break;
        case 29:
          category = "Exam Preparation";
          question = "What should I do the night before and morning of an important exam?";
          answer = "Exam day protocol: Night before—light review only, prepare materials, early bedtime. Morning of—nutritious breakfast, avoid cramming, arrive early. During—use first minutes to jot down formulas/key points. Remember: Last-minute cramming rarely helps; trust your preparation.";
          break;
        case 30:
          category = "Exam Preparation";
          question = "How do I manage time during exams to complete all questions?";
          answer = "Exam time management: 1) Allocate minutes per question/section before starting. 2) Wear a watch to track time. 3) Answer easiest questions first for quick points. 4) Put mark by uncertain questions, return if time. 5) Leave 5-10 minutes for review. 6) If stuck, move on—don't lose time on one question.";
          break;
        case 31:
          category = "Exam Preparation";
          question = "How can I stay motivated during long exam periods?";
          answer = "Maintain exam period motivation: 1) Break study into manageable chunks with rewards. 2) Visualize post-exam freedom/celebrations. 3) Study with motivated peers. 4) Maintain exercise/routine for energy. 5) Remember 'this too shall pass' perspective. 6) Celebrate small victories. 7) Schedule something enjoyable after last exam.";
          break;
        
        // Health & Wellness Questions (32-37)
        case 32:
          category = "Health & Wellness";
          question = "How can I maintain a healthy lifestyle with a busy college schedule?";
          answer = "Prioritize health amidst busyness: 1) Schedule exercise like a class (3-4x weekly, even 20 min). 2) Make smart dining hall choices (protein+veggies+whole grains). 3) Protect 7-8 hour sleep window with consistent routine. 4) Use campus counseling for stress. 5) Carry water bottle. 6) Practice moderation in social events. 7) Listen to body's signals.";
          break;
        case 33:
          category = "Health & Wellness";
          question = "What are quick and healthy meal options for busy students?";
          answer = "Quick healthy meals: Overnight oats, yogurt parfaits, whole grain wraps with veggies/protein, microwaved sweet potatoes with toppings, canned tuna/salmon salads, frozen veggie stir-fries, protein smoothies. Prep basics on weekends. Use dining hall salad bars creatively. Stay hydrated—carry water always.";
          break;
        case 34:
          category = "Health & Wellness";
          question = "How much sleep do I really need, and how can I improve my sleep quality?";
          answer = "Sleep essentials: Need 7-9 hours nightly for cognitive function. Improve quality: 1) Consistent bedtime/waketime (even weekends). 2) 1-hour pre-bed screen-free wind down. 3) Cool, dark, quiet room. 4) Limit caffeine after 2 PM. 5) Avoid heavy meals/alcohol before bed. 6) Use bed only for sleep (no studying). 7) Manage stress through daytime habits.";
          break;
        case 35:
          category = "Health & Wellness";
          question = "How can I incorporate exercise into my routine without it feeling like a chore?";
          answer = "Make exercise enjoyable: 1) Find activities you like (dancing, sports, hiking). 2) Exercise with friends for accountability/fun. 3) Try 'exercise snacks' (10-min bursts). 4) Walk/bike to classes. 5) Use campus gym classes (often free). 6) Listen to podcasts/music while moving. 7) Focus on how exercise makes you feel, not just calories burned.";
          break;
        case 36:
          category = "Health & Wellness";
          question = "What are signs of mental health issues I should watch for in myself and friends?";
          answer = "Mental health red flags: Persistent sadness/irritability (2+ weeks). Social withdrawal/isolation. Drastic sleep/appetite changes. Loss of interest in usual activities. Difficulty concentrating. Talking about hopelessness/being burden. Increased substance use. Expressing thoughts of self-harm. If concerned for friend, express care, listen without judgment, encourage professional help.";
          break;
        case 37:
          category = "Health & Wellness";
          question = "How do I establish healthy boundaries with friends and social commitments?";
          answer = "Boundaries protect your energy: 1) Know your non-negotiables (sleep, key study blocks). 2) Use clear, kind language ('Sounds fun, but I've got a prior commitment'). 3) Offer alternative times. 4) Use 'Do Not Disturb' during focus time. 5) Remember: People who respect you will respect your boundaries. True friends want you to succeed.";
          break;
        
        default:
          category = "Error";
          question = "Invalid Question Number";
          answer = "Please provide a valid question number between 1 and 37. Or select a category to see available questions.";
          break;
      }
    } catch (NumberFormatException e) {
      // If not a number, treat as custom question
      category = "Custom Question";
      question = qParam;
      answer = "Thanks for your question! While I'm designed for academic guidance, I recommend: 1) Breaking challenges into small steps. 2) Seeking campus resources (advising, counseling). 3) Connecting with peers facing similar issues. 4) Prioritizing self-care. For specific advice, try selecting a numbered question from the categories.";
    }
  }

  // Build JSON response matching the frontend's expected format
  // The updated frontend expects: category, question, answer
  // (The old format had agent and brand which we're keeping for compatibility)
  String json = "{"
    + "\"agent\":\"" + escapeJson(agent) + "\","
    + "\"brand\":\"" + escapeJson(brand) + "\","
    + "\"category\":\"" + escapeJson(category) + "\","
    + "\"question\":\"" + escapeJson(question) + "\","
    + "\"answer\":\"" + escapeJson(answer) + "\""
    + "}";
  out.print(json);
%>

<%!
  // Helper method to escape JSON strings
  private String escapeJson(String input) {
    if (input == null) return "";
    return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
  }
%>
