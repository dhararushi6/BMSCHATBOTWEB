public import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class CategoryPanel extends JPanel {
    private AchievoApp app;
    private Map<String, JButton> categoryButtons;
    
    public CategoryPanel(AchievoApp app) {
        this.app = app;
        categoryButtons = new HashMap<>();
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Achievo – Your College Companion");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 28));
        titleLabel.setForeground(new Color(79, 70, 229));
        headerPanel.add(titleLabel);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Intro panel
        JPanel introPanel = new JPanel();
        introPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        introPanel.setBackground(new Color(240, 244, 255));
        introPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(79, 70, 229)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel avatarLabel = new JLabel("🤖");
        avatarLabel.setFont(new Font("SansSerif", Font.PLAIN, 36));
        
        JPanel introTextPanel = new JPanel();
        introTextPanel.setLayout(new BorderLayout());
        introTextPanel.setBackground(new Color(240, 244, 255));
        
        JLabel introTitle = new JLabel("Hey, I'm Lexi 👋");
        introTitle.setFont(new Font("Inter", Font.BOLD, 18));
        introTitle.setForeground(new Color(79, 70, 229));
        
        JTextArea introText = new JTextArea("Your personal academic assistant here to help with college challenges. First select a category, then choose a specific question for personalized guidance.");
        introText.setFont(new Font("Inter", Font.PLAIN, 14));
        introText.setBackground(new Color(240, 244, 255));
        introText.setLineWrap(true);
        introText.setWrapStyleWord(true);
        introText.setEditable(false);
        
        introTextPanel.add(introTitle, BorderLayout.NORTH);
        introTextPanel.add(introText, BorderLayout.CENTER);
        
        introPanel.add(avatarLabel);
        introPanel.add(Box.createHorizontalStrut(20));
        introPanel.add(introTextPanel);
        
        add(introPanel, BorderLayout.CENTER);
        
        // Categories panel
        JPanel categoriesPanel = new JPanel();
        categoriesPanel.setLayout(new GridLayout(2, 3, 20, 20));
        categoriesPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        categoriesPanel.setBackground(new Color(248, 250, 252));
        
        // Create category cards
        String[] categories = {
            "Time Management", "Stress Management", "Study Techniques",
            "College Life", "Exam Preparation", "Health & Wellness"
        };
        
        String[] descriptions = {
            "Balancing classes, study, work and social life",
            "Coping with anxiety, pressure and overwhelm",
            "Effective learning, note-taking and exam prep",
            "Adjusting to campus, friends, and living away",
            "Strategies for tests, finals and performance",
            "Maintaining physical and mental health"
        };
        
        String[] icons = {"⏰", "🧠", "🎓", "🏠", "📝", "❤️"};
        String[] keys = {
            "time-management", "stress-management", "study-techniques",
            "college-life", "exam-preparation", "wellness"
        };
        
        for (int i = 0; i < categories.length; i++) {
            JPanel card = createCategoryCard(categories[i], descriptions[i], icons[i], keys[i]);
            categoriesPanel.add(card);
        }
        
        JScrollPane scrollPane = new JScrollPane(categoriesPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.SOUTH);
    }
    
    private JPanel createCategoryCard(String title, String description, String icon, String key) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(240, 244, 255));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(99, 102, 241)),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(229, 231, 235)),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)
                ));
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                app.showQuestionsPanel(key);
            }
        });
        
        // Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 36));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel iconPanel = new JPanel();
        iconPanel.setBackground(new Color(79, 70, 229));
        iconPanel.setPreferredSize(new Dimension(60, 60));
        iconPanel.setLayout(new GridBagLayout());
        iconPanel.add(iconLabel);
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Description
        JTextArea descArea = new JTextArea(description);
        descArea.setFont(new Font("Inter", Font.PLAIN, 12));
        descArea.setForeground(new Color(107, 114, 128));
        descArea.setBackground(Color.WHITE);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setAlignmentX(CENTER_ALIGNMENT);
        
        // Layout
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(iconPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(descArea);
        
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }
} {
    
}
