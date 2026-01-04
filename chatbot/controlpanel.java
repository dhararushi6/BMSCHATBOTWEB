import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class CodeController {
    // Database configuration
    private static final String DB_URL = "jdbc:sqlite:achievo.db";
    private Connection connection;
    private ExecutorService executorService;
    
    // UI Components
    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private Map<String, JButton> categoryButtons;
    
    // Data storage
    private Map<String, Category> categories;
    private Map<String, List<Question>> questionsByCategory;
    
    public CodeController() {
        initializeDatabase();
        executorService = Executors.newFixedThreadPool(4);
        initializeUI();
        loadDataAsync();
    }
    
    // ==================== DATABASE METHODS ====================
    
    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            createTables();
            seedInitialData();
        } catch (Exception e) {
            showError("Database Error", "Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createTables() throws SQLException {
        String[] tables = {
            // Categories table
            """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                key TEXT UNIQUE NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                icon TEXT,
                color TEXT
            )
            """,
            
            // Questions table
            """
            CREATE TABLE IF NOT EXISTS questions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category_key TEXT NOT NULL,
                question TEXT NOT NULL,
                prompt TEXT,
                tips TEXT,
                FOREIGN KEY (category_key) REFERENCES categories(key)
            )
            """,
            
            // Chat history table
            """
            CREATE TABLE IF NOT EXISTS chat_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                question_id INTEGER NOT NULL,
                sender TEXT NOT NULL,
                message TEXT NOT NULL,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (question_id) REFERENCES questions(id)
            )
            """,
            
            // User progress table
            """
            CREATE TABLE IF NOT EXISTS user_stats (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category_key TEXT,
                questions_viewed INTEGER DEFAULT 0,
                last_accessed DATETIME,
                rating INTEGER
            )
            """
        };
        
        try (Statement stmt = connection.createStatement()) {
            for (String sql : tables) {
                stmt.execute(sql);
            }
        }
    }
    
    private void seedInitialData() throws SQLException {
        // Check if data already exists
        String checkSql = "SELECT COUNT(*) FROM categories";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.getInt(1) > 0) {
                return; // Data already exists
            }
        }
        
        // Insert categories
        String[][] categoryData = {
            {"time-management", "Time Management", "Balancing classes, study, work and social life", "⏰", "#3B82F6"},
            {"stress-management", "Stress Management", "Coping with anxiety, pressure and overwhelm", "🧠", "#EF4444"},
            {"study-techniques", "Study Techniques", "Effective learning, note-taking and exam prep", "🎓", "#10B981"},
            {"college-life", "College Life", "Adjusting to campus, friends, and living away", "🏠", "#F59E0B"},
            {"exam-preparation", "Exam Preparation", "Strategies for tests, finals and performance", "📝", "#8B5CF6"},
            {"wellness", "Health & Wellness", "Maintaining physical and mental health", "❤️", "#EC4899"}
        };
        
        String insertCategory = "INSERT INTO categories (key, name, description, icon, color) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertCategory)) {
            for (String[] data : categoryData) {
                pstmt.setString(1, data[0]);
                pstmt.setString(2, data[1]);
                pstmt.setString(3, data[2]);
                pstmt.setString(4, data[3]);
                pstmt.setString(5, data[4]);
                pstmt.executeUpdate();
            }
        }
        
        // Insert sample questions
        insertSampleQuestions();
    }
    
    private void insertSampleQuestions() throws SQLException {
        String insertQuestion = "INSERT INTO questions (category_key, question, prompt, tips) VALUES (?, ?, ?, ?)";
        
        Map<String, String[][]> questions = new HashMap<>();
        questions.put("time-management", new String[][]{
            {"How do I balance my schedule?", "Help me create a balanced schedule...", "1. Prioritize tasks\n2. Use time blocks\n3. Schedule breaks"},
            {"How can I stop procrastinating?", "I need strategies to overcome procrastination...", "1. Pomodoro technique\n2. 2-minute rule\n3. Remove distractions"},
            {"What's the best way to plan my week?", "Guide me through weekly planning...", "1. Review syllabus\n2. Block study time\n3. Include self-care"}
        });
        
        questions.put("stress-management", new String[][]{
            {"How do I deal with anxiety before exams?", "Help me manage exam anxiety...", "1. Deep breathing\n2. Positive self-talk\n3. Preparation reduces anxiety"},
            {"What are good stress relief techniques?", "I need stress relief methods...", "1. Exercise\n2. Meditation\n3. Talk to someone"},
            {"How do I manage multiple deadlines?", "I'm overwhelmed with deadlines...", "1. Make a list\n2. Break it down\n3. Ask for extensions if needed"}
        });
        
        questions.put("study-techniques", new String[][]{
            {"What are effective study methods?", "Teach me study techniques...", "1. Active recall\n2. Spaced repetition\n3. Teach others"},
            {"How do I take better notes?", "Improve my note-taking...", "1. Cornell method\n2. Visual notes\n3. Review regularly"},
            {"How to study for long periods?", "Maintain focus during long study sessions...", "1. 50/10 rule\n2. Change subjects\n3. Stay hydrated"}
        });
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuestion)) {
            for (Map.Entry<String, String[][]> entry : questions.entrySet()) {
                String categoryKey = entry.getKey();
                for (String[] q : entry.getValue()) {
                    pstmt.setString(1, categoryKey);
                    pstmt.setString(2, q[0]);
                    pstmt.setString(3, q[1]);
                    pstmt.setString(4, q[2]);
                    pstmt.executeUpdate();
                }
            }
        }
    }
    
    // ==================== ASYNC DATA LOADING ====================
    
    private void loadDataAsync() {
        executorService.execute(() -> {
            try {
                // Load categories
                categories = loadCategoriesFromDB();
                
                // Load questions for each category
                questionsByCategory = new HashMap<>();
                for (String categoryKey : categories.keySet()) {
                    List<Question> questions = loadQuestionsFromDB(categoryKey);
                    questionsByCategory.put(categoryKey, questions);
                }
                
                // Update UI on EDT
                SwingUtilities.invokeLater(() -> {
                    updateCategoryPanel();
                });
                
            } catch (SQLException e) {
                SwingUtilities.invokeLater(() -> {
                    showError("Load Error", "Failed to load data: " + e.getMessage());
                });
            }
        });
    }
    
    private Map<String, Category> loadCategoriesFromDB() throws SQLException {
        Map<String, Category> result = new HashMap<>();
        String sql = "SELECT key, name, description, icon, color FROM categories";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Category cat = new Category(
                    rs.getString("key"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("icon"),
                    Color.decode(rs.getString("color"))
                );
                result.put(cat.getKey(), cat);
            }
        }
        return result;
    }
    
    private List<Question> loadQuestionsFromDB(String categoryKey) throws SQLException {
        List<Question> result = new ArrayList<>();
        String sql = "SELECT id, question, prompt, tips FROM questions WHERE category_key = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, categoryKey);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question(
                        rs.getInt("id"),
                        categoryKey,
                        rs.getString("question"),
                        rs.getString("prompt"),
                        rs.getString("tips")
                    );
                    result.add(q);
                }
            }
        }
        return result;
    }
    
    // ==================== UI INITIALIZATION ====================
    
    private void initializeUI() {
        frame = new JFrame("Achievo – College Companion");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(248, 250, 252));
        
        categoryButtons = new HashMap<>();
        
        // Create panels
        JPanel categoryPanel = createCategoryPanel();
        mainPanel.add(categoryPanel, "CATEGORY");
        
        frame.add(mainPanel);
        frame.setVisible(true);
    }
    
    private JPanel createCategoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 250, 252));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Achievo – Your College Companion");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(79, 70, 229));
        headerPanel.add(titleLabel);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Intro panel
        JPanel introPanel = createIntroPanel();
        panel.add(introPanel, BorderLayout.CENTER);
        
        // Categories panel (initially empty, will be populated async)
        JPanel categoriesContainer = new JPanel(new BorderLayout());
        categoriesContainer.setBackground(new Color(248, 250, 252));
        
        JLabel loadingLabel = new JLabel("Loading categories...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        loadingLabel.setForeground(Color.GRAY);
        categoriesContainer.add(loadingLabel, BorderLayout.CENTER);
        
        panel.add(categoriesContainer, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void updateCategoryPanel() {
        // Find and remove loading label
        Component[] comps = ((JPanel)mainPanel.getComponent(0)).getComponents();
        for (Component comp : comps) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getLayout() instanceof BorderLayout) {
                    Component southComp = ((BorderLayout)panel.getLayout()).getLayoutComponent(panel, BorderLayout.SOUTH);
                    if (southComp != null) {
                        panel.remove(southComp);
                        
                        // Add actual categories panel
                        JPanel categoriesPanel = createCategoriesGrid();
                        JScrollPane scrollPane = new JScrollPane(categoriesPanel);
                        scrollPane.setBorder(new EmptyBorder(20, 20, 20, 20));
                        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
                        
                        panel.add(scrollPane, BorderLayout.SOUTH);
                        panel.revalidate();
                        panel.repaint();
                        break;
                    }
                }
            }
        }
    }
    
    private JPanel createIntroPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        panel.setBackground(new Color(240, 244, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(79, 70, 229), 0, 5, 0, 0),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel avatarLabel = new JLabel("🤖");
        avatarLabel.setFont(new Font("SansSerif", Font.PLAIN, 48));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout(0, 10));
        textPanel.setBackground(new Color(240, 244, 255));
        
        JLabel introTitle = new JLabel("Hey, I'm Lexi 👋");
        introTitle.setFont(new Font("Arial", Font.BOLD, 18));
        introTitle.setForeground(new Color(79, 70, 229));
        
        JTextArea introText = new JTextArea(
            "Your personal academic assistant here to help with college challenges. " +
            "First select a category, then choose a specific question for personalized guidance. " +
            "All your conversations are saved for future reference!"
        );
        introText.setFont(new Font("Arial", Font.PLAIN, 14));
        introText.setBackground(new Color(240, 244, 255));
        introText.setLineWrap(true);
        introText.setWrapStyleWord(true);
        introText.setEditable(false);
        
        textPanel.add(introTitle, BorderLayout.NORTH);
        textPanel.add(introText, BorderLayout.CENTER);
        
        panel.add(avatarLabel);
        panel.add(textPanel);
        
        return panel;
    }
    
    private JPanel createCategoriesGrid() {
        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        gridPanel.setBackground(new Color(248, 250, 252));
        
        for (Category category : categories.values()) {
            JPanel card = createCategoryCard(category);
            gridPanel.add(card);
        }
        
        return gridPanel;
    }
    
    private JPanel createCategoryCard(Category category) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(25, 25, 25, 25)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(240, 244, 255));
                card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(category.getColor(), 2),
                    new EmptyBorder(25, 25, 25, 25)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(229, 231, 235), 1),
                    new EmptyBorder(25, 25, 25, 25)
                ));
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                showQuestionsPanel(category.getKey());
            }
        });
        
        // Icon with colored background
        JLabel iconLabel = new JLabel(category.getIcon());
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 36));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setBackground(category.getColor());
        iconPanel.setPreferredSize(new Dimension(80, 80));
        iconPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        iconPanel.add(iconLabel);
        
        // Title
        JLabel titleLabel = new JLabel(category.getName());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(new Color(55, 65, 81));
        
        // Description
        JTextArea descArea = new JTextArea(category.getDescription());
        descArea.setFont(new Font("Arial", Font.PLAIN, 12));
        descArea.setForeground(new Color(107, 114, 128));
        descArea.setBackground(Color.WHITE);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Stats (loaded async)
        JLabel statsLabel = new JLabel("Loading...");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        statsLabel.setForeground(Color.GRAY);
        statsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Load stats in background
        loadCategoryStatsAsync(category.getKey(), statsLabel);
        
        // Layout
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(iconPanel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(descArea);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(statsLabel);
        
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void loadCategoryStatsAsync(String categoryKey, JLabel statsLabel) {
        executorService.execute(() -> {
            try {
                // Query database for stats
                String sql = """
                    SELECT 
                        COUNT(DISTINCT q.id) as total_questions,
                        COUNT(DISTINCT ch.question_id) as conversations
                    FROM questions q
                    LEFT JOIN chat_history ch ON q.id = ch.question_id
                    WHERE q.category_key = ?
                """;
                
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, categoryKey);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            int totalQuestions = rs.getInt("total_questions");
                            int conversations = rs.getInt("conversations");
                            
                            String statsText = String.format("%d questions • %d conversations", 
                                totalQuestions, conversations);
                            
                            SwingUtilities.invokeLater(() -> {
                                statsLabel.setText(statsText);
                            });
                        }
                    }
                }
            } catch (SQLException e) {
                SwingUtilities.invokeLater(() -> {
                    statsLabel.setText("Stats unavailable");
                });
            }
        });
    }
    
    // ==================== NAVIGATION METHODS ====================
    
    private void showQuestionsPanel(String categoryKey) {
        executorService.execute(() -> {
            try {
                List<Question> questions = questionsByCategory.get(categoryKey);
                Category category = categories.get(categoryKey);
                
                SwingUtilities.invokeLater(() -> {
                    JPanel questionsPanel = createQuestionsPanel(category, questions);
                    
                    // Check if panel already exists, if not add it
                    boolean panelExists = false;
                    for (Component comp : mainPanel.getComponents()) {
                        if (comp.getName() != null && comp.getName().equals("QUESTIONS_" + categoryKey)) {
                            panelExists = true;
                            break;
                        }
                    }
                    
                    if (!panelExists) {
                        questionsPanel.setName("QUESTIONS_" + categoryKey);
                        mainPanel.add(questionsPanel, "QUESTIONS_" + categoryKey);
                    }
                    
                    cardLayout.show(mainPanel, "QUESTIONS_" + categoryKey);
                    updateUserStats(categoryKey);
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    showError("Navigation Error", "Failed to load questions: " + e.getMessage());
                });
            }
        });
    }
    
    private JPanel createQuestionsPanel(Category category, List<Question> questions) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 250, 252));
        
        // Header with back button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JButton backButton = new JButton("← Back to Categories");
        backButton.setFont(new Font("Arial", Font.PLAIN, 12));
        backButton.setForeground(new Color(79, 70, 229));
        backButton.setBackground(Color.WHITE);
        backButton.setBorder(new EmptyBorder(5, 10, 5, 10));
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "CATEGORY"));
        
        JLabel titleLabel = new JLabel(category.getName());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(category.getColor());
        
        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Questions list
        JPanel questionsPanel = new JPanel();
        questionsPanel.setLayout(new BoxLayout(questionsPanel, BoxLayout.Y_AXIS));
        questionsPanel.setBackground(new Color(248, 250, 252));
        questionsPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel subtitle = new JLabel("Select a question to get personalized advice:");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(new Color(107, 114, 128));
        subtitle.setBorder(new EmptyBorder(0, 0, 20, 0));
        questionsPanel.add(subtitle);
        
        for (Question question : questions) {
            JPanel questionCard = createQuestionCard(question);
            questionsPanel.add(questionCard);
            questionsPanel.add(Box.createVerticalStrut(10));
        }
        
        JScrollPane scrollPane = new JScrollPane(questionsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createQuestionCard(Question question) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(15, 20, 15, 20)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(249, 250, 251));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                showChatPanel(question);
            }
        });
        
        JLabel questionLabel = new JLabel(question.getQuestion());
        questionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Load chat count async
        JLabel chatCountLabel = new JLabel("Loading...");
        chatCountLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        chatCountLabel.setForeground(Color.GRAY);
        
        loadChatCountAsync(question.getId(), chatCountLabel);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(chatCountLabel);
        
        card.add(questionLabel, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);
        
        return card;
    }
    
    private void loadChatCountAsync(int questionId, JLabel countLabel) {
        executorService.execute(() -> {
            try {
                String sql = "SELECT COUNT(*) as count FROM chat_history WHERE question_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setInt(1, questionId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            int count = rs.getInt("count");
                            String text = count > 0 ? "💬 " + count + " messages" : "New conversation";
                            
                            SwingUtilities.invokeLater(() -> {
                                countLabel.setText(text);
                            });
                        }
                    }
                }
            } catch (SQLException e) {
                // Ignore, keep default text
            }
        });
    }
    
    private void showChatPanel(Question question) {
        executorService.execute(() -> {
            try {
                List<ChatMessage> history = loadChatHistory(question.getId());
                
                SwingUtilities.invokeLater(() -> {
                    JPanel chatPanel = createChatPanel(question, history);
                    
                    // Check if panel already exists
                    boolean panelExists = false;
                    String panelName = "CHAT_" + question.getId();
                    for (Component comp : mainPanel.getComponents()) {
                        if (comp.getName() != null && comp.getName().equals(panelName)) {
                            panelExists = true;
                            break;
                        }
                    }
                    
                    if (!panelExists) {
                        chatPanel.setName(panelName);
                        mainPanel.add(chatPanel, panelName);
                    }
                    
                    cardLayout.show(mainPanel, panelName);
                });
                
            } catch (SQLException e) {
                SwingUtilities.invokeLater(() -> {
                    showError("Chat Error", "Failed to load chat: " + e.getMessage());
                });
            }
        });
    }
    
    private List<ChatMessage> loadChatHistory(int questionId) throws SQLException {
        List<ChatMessage> history = new ArrayList<>();
        String sql = "SELECT sender, message, timestamp FROM chat_history WHERE question_id = ? ORDER BY timestamp ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ChatMessage msg = new ChatMessage(
                        questionId,
                        rs.getString("sender"),
                        rs.getString("message"),
                        rs.getTimestamp("timestamp")
                    );
                    history.add(msg);
                }
            }
        }
        
        // If no history, add welcome message
        if (history.isEmpty()) {
            ChatMessage welcome = new ChatMessage(
                questionId,
                "assistant",
                "Hello! I'm here to help you with: " + 
                getQuestionText(questionId) + 
                "\n\nHow can I assist you today?",
                new Timestamp(System.currentTimeMillis())
            );
            history.add(welcome);
            saveChatMessage(welcome);
        }
        
        return history;
    }
    
    private String getQuestionText(int questionId) throws SQLException {
        String sql = "SELECT question FROM questions WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("question");
                }
            }
        }
        return "";
    }
    
    // ==================== UTILITY METHODS ====================
    
    private void updateUserStats(String categoryKey) {
        executorService.execute(() -> {
            try {
                String sql = """
                    INSERT INTO user_stats (category_key, questions_viewed, last_accessed) 
                    VALUES (?, 1, CURRENT_TIMESTAMP)
                    ON CONFLICT(category_key) DO UPDATE SET 
                        questions_viewed = questions_viewed + 1,
                        last_accessed = CURRENT_TIMESTAMP
                """;
                
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, categoryKey);
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                // Silently fail for stats updates
            }
        });
    }
    
    private void saveChatMessage(ChatMessage message) {
        executorService.execute(() -> {
            try {
                String sql = "INSERT INTO chat_history (question_id, sender, message) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setInt(1, message.getQuestionId());
                    pstmt.setString(2, message.getSender());
                    pstmt.setString(3, message.getMessage());
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("Failed to save message: " + e.getMessage());
            }
        });
    }
    
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // ==================== DATA CLASSES ====================
    
    private class Category {
        private String key;
        private String name;
        private String description;
        private String icon;
        private Color color;
        
        public Category(String key, String name, String description, String icon, Color color) {
            this.key = key;
            this.name = name;
            this.description = description;
            this.icon = icon;
            this.color = color;
        }
        
        public String getKey() { return key; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        public Color getColor() { return color; }
    }
    
    private class Question {
        private int id;
        private String categoryKey;
        private String question;
        private String prompt;
        private String tips;
        
        public Question(int id, String categoryKey, String question, String prompt, String tips) {
            this.id = id;
            this.categoryKey = categoryKey;
            this.question = question;
            this.prompt = prompt;
            this.tips = tips;
        }
        
        public int getId() { return id; }
        public String getCategoryKey() { return categoryKey; }
        public String getQuestion() { return question; }
        public String getPrompt() { return prompt; }
        public String getTips() { return tips; }
    }
    
    private class ChatMessage {
        private int questionId;
        private String sender;
        private String message;
        private Timestamp timestamp;
        
        public ChatMessage(int questionId, String sender, String message, Timestamp timestamp) {
            this.questionId = questionId;
            this.sender = sender;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        public int getQuestionId() { return questionId; }
        public String getSender() { return sender; }
        public String getMessage() { return message; }
        public Timestamp getTimestamp() { return timestamp; }
    }
    
    // ==================== MAIN METHOD ====================
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CodeController controller = new CodeController();
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(controller::shutdown));
        });
    }
    
    // ==================== CHAT PANEL CREATION ====================
    
    private JPanel createChatPanel(Question question, List<ChatMessage> history) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 250, 252));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JButton backButton = new JButton("← Back to Questions");
        backButton.setFont(new Font("Arial", Font.PLAIN, 12));
        backButton.setForeground(new Color(79, 70, 229));
        backButton.setBackground(Color.WHITE);
        backButton.setBorder(new EmptyBorder(5, 10, 5, 10));
        backButton.addActionListener(e -> showQuestionsPanel(question.getCategoryKey()));
        
        JLabel titleLabel = new JLabel(question.getQuestion());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(new Color(55, 65, 81));
        
        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Chat messages area
        JPanel chatArea = new JPanel();
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        chatArea.setBackground(Color.WHITE);
        
        for (ChatMessage msg : history) {
            JPanel messageBubble = createMessageBubble(msg);
            chatArea.add(messageBubble);
            chatArea.add(Box.createVerticalStrut(10));
        }
        
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JTextArea inputField = new JTextArea(3, 20);
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputField.setLineWrap(true);
        inputField.setWrapStyleWord(true);
        
        JScrollPane inputScroll = new JScrollPane(inputField);
        inputScroll.setBorder(new LineBorder(new Color(209, 213, 219), 1));
        
        JButton sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setBackground(new Color(79, 70, 229));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        sendButton.addActionListener(e -> {
            String message = inputField.getText().trim();
            if (!message.isEmpty()) {
                // Add user message
                ChatMessage userMsg = new ChatMessage(
                    question.getId(),
                    "user",
                    message,
                    new Timestamp(System.currentTimeMillis())
                );
                
                // Add to UI
                chatArea.add(createMessageBubble(userMsg));
                chatArea.add(Box.createVerticalStrut(10));
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                
                // Save to database
                saveChatMessage(userMsg);
                
                // Clear input
                inputField.setText("");
                
                // Generate AI response (async)
                generateAIResponseAsync(question.getId(), message, chatArea, scrollPane);
            }
        });
        
        inputPanel.add(inputScroll, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        panel.add(inputPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createMessageBubble(ChatMessage msg) {
        JPanel bubble = new JPanel(new BorderLayout());
        bubble.setBackground(Color.WHITE);
        bubble.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        JLabel senderLabel = new JLabel(msg.getSender().equals("user") ? "You" : "Lexi");
        senderLabel.setFont(new Font("Arial", Font.BOLD, 12));
        senderLabel.setForeground(msg.getSender().equals("user") ? new Color(79, 70, 229) : new Color(239, 68, 68));
        
        JTextArea messageArea = new JTextArea(msg.getMessage());
        messageArea.setFont(new Font("Arial", Font.PLAIN, 14));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setBackground(Color.WHITE);
        
        JLabel timeLabel = new JLabel(msg.getTimestamp().toString().substring(0, 16));
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);
        
        contentPanel.add(senderLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(messageArea);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(timeLabel);
        
        bubble.add(contentPanel, BorderLayout.CENTER);
        
        return bubble;
    }
    
    private void generateAIResponseAsync(int questionId, String userMessage, JPanel chatArea, JScrollPane scrollPane) {
        executorService.execute(() -> {
            try {
                // Simulate AI processing delay
                Thread.sleep(1000);
                
                // Generate response based on question
                String aiResponse = generateResponse(userMessage);
                
                ChatMessage aiMsg = new ChatMessage(
                    questionId,
                    "assistant",
                    aiResponse,
                    new Timestamp(System.currentTimeMillis())
                );
                
                SwingUtilities.invokeLater(() -> {
                    chatArea.add(createMessageBubble(aiMsg));
                    chatArea.add(Box.createVerticalStrut(10));
                    scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                });
                
                // Save to database
                saveChatMessage(aiMsg);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    private String generateResponse(String userMessage) {
        // Simple response generation
        String[] responses = {
            "I understand you're asking about that. Based on my knowledge, here's what I recommend...",
            "That's a common challenge in college. Many students find success with these strategies...",
            "Great question! Let me break this down into actionable steps for you...",
            "I can help with that. Here are some proven techniques that might work for you...",
            "Thanks for sharing. Based on your question, I'd suggest the following approach..."
        };
        
        Random rand = new Random();
        String baseResponse = responses[rand.nextInt(responses.length)];
        
        // Add some personalized advice
        String advice = "\n\nSome specific tips:\n";
        advice += "1. Break it down into smaller tasks\n";
        advice += "2. Set realistic deadlines\n";
        advice += "3. Don't forget to take breaks\n";
        advice += "4. Ask for help when needed\n";
        advice += "5. Celebrate small wins along the way";
        
        return baseResponse + advice;
    }
}
