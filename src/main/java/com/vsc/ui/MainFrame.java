package com.vsc.ui;

import com.vsc.util.AppInfo;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Main window of the application.
 *
 * <p>Layout: header on top, navigation on the left, the selected screen in the
 * centre (a {@link CardLayout}), and a status bar at the bottom. Each screen is
 * a separate panel class under {@code com.vsc.ui} so team members can work on
 * one screen without touching the others.</p>
 *
 * <p>This class contains no SQL and no business rules - it only connects
 * screens, which keeps the layer rule from AGENTS.md intact.</p>
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final Map<String, String> screens = new LinkedHashMap<>();
    private final Map<String, JToggleButton> navButtons = new LinkedHashMap<>();
    private final JLabel currentScreenLabel = new JLabel();

    public MainFrame() {
        super(AppInfo.NAME + " " + AppInfo.VERSION);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buildScreens();
        setJMenuBar(buildMenuBar());
        setContentPane(buildContent());

        setSize(1000, 640);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);

        showScreen("dashboard");
    }

    /** Registers every screen once. Add new feature screens here. */
    private void buildScreens() {
        addScreen("dashboard", "Dashboard", new DashboardPanel());
        addScreen("customers", "Customers", new CustomerPanel());
        addScreen("vehicles", "Vehicles", new VehiclePanel());
        addScreen("job-cards", "Job cards", new JobCardPanel());
        addScreen("inventory", "Inventory", new InventoryPanel());
        addScreen("invoices", "Invoices", new InvoicePanel());
        addScreen("mechanics", "Mechanics", new MechanicPanel());
    }

    private void addScreen(String key, String label, JPanel panel) {
        screens.put(key, label);
        cards.add(panel, key);
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeader() {
        JLabel title = new JLabel(AppInfo.NAME);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JLabel stage = new JLabel(AppInfo.BUILD_STAGE);
        stage.setForeground(UiTheme.MUTED);
        stage.setFont(stage.getFont().deriveFont(11f));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(stage);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.BORDER),
                BorderFactory.createEmptyBorder(12, 22, 12, 22)));
        header.add(text, BorderLayout.WEST);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.add(buildNavPanel(), BorderLayout.WEST);
        body.add(cards, BorderLayout.CENTER);
        return body;
    }

    private JPanel buildNavPanel() {
        ButtonGroup group = new ButtonGroup();
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(Color.WHITE);
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UiTheme.BORDER),
                BorderFactory.createEmptyBorder(14, 10, 14, 10)));
        nav.setPreferredSize(new Dimension(190, 0));

        for (Map.Entry<String, String> screen : screens.entrySet()) {
            String key = screen.getKey();
            JToggleButton button = new JToggleButton(screen.getValue());
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setFocusPainted(false);
            button.setMargin(new Insets(8, 12, 8, 12));
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            button.addActionListener(e -> showScreen(key));

            group.add(button);
            navButtons.put(key, button);
            nav.add(button);
            nav.add(Box.createVerticalStrut(4));
        }

        nav.add(Box.createVerticalGlue());
        return nav;
    }

    private JPanel buildStatusBar() {
        JLabel running = UiTheme.status("Application running - backend ready, screens not wired yet");
        currentScreenLabel.setForeground(UiTheme.MUTED);
        currentScreenLabel.setFont(currentScreenLabel.getFont().deriveFont(11f));

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Color.WHITE);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.BORDER),
                BorderFactory.createEmptyBorder(6, 22, 6, 22)));
        statusBar.add(running, BorderLayout.WEST);
        statusBar.add(currentScreenLabel, BorderLayout.EAST);
        return statusBar;
    }

    private JMenuBar buildMenuBar() {
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> dispose());
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(exitItem);

        JMenu viewMenu = new JMenu("View");
        for (Map.Entry<String, String> screen : screens.entrySet()) {
            JMenuItem item = new JMenuItem(screen.getValue());
            item.addActionListener(e -> showScreen(screen.getKey()));
            viewMenu.add(item);
        }

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(
                this, AppInfo.aboutText(), "About", JOptionPane.INFORMATION_MESSAGE));
        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(aboutItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    /** Shows one screen by key and keeps the navigation and status bar in sync. */
    private void showScreen(String key) {
        String label = screens.get(key);
        if (label == null) {
            return;
        }
        cardLayout.show(cards, key);
        currentScreenLabel.setText(label + "  |  Java " + AppInfo.javaVersion());

        JToggleButton button = navButtons.get(key);
        if (button != null) {
            button.setSelected(true);
        }
    }
}
