package com.vsc.ui;

import com.vsc.util.AppInfo;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

/**
 * Home screen. It gives the team a one-glance view of what the application can
 * already do and what is still missing, which is handy when presenting the
 * project or picking up the next task.
 */
public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        super(new BorderLayout(0, 16));
        setBackground(UiTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(26, 30, 22, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCards(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = UiTheme.title("Dashboard");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = UiTheme.body("Customers, job cards, inventory, invoices and payments for one workshop.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel version = UiTheme.status("Version " + AppInfo.VERSION + " - " + AppInfo.BUILD_STAGE);
        version.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(8));
        header.add(version);
        return header;
    }

    private JPanel buildCards() {
        JPanel cards = new JPanel(new GridLayout(1, 2, 16, 0));
        cards.setOpaque(false);
        cards.add(buildListCard("Working today", new String[]{
                "Application window and navigation",
                "Model classes for every entity (Customer, Vehicle, JobCard, ...)",
                "Database schema executed on MySQL 8.4 (10 tables)",
                "DAO layer: JDBC + PreparedStatement per table",
                "Service layer: validation, status rules, stock and billing",
                "End-to-end integration test against MySQL",
                "31 JUnit 5 tests: mvn test"
        }));
        cards.add(buildListCard("Next steps", new String[]{
                "Replace each placeholder screen with a real form and table",
                "Customers screen: search, register, open vehicles",
                "Job card screen: add services/parts, move status forward",
                "Invoice screen: generate bill, record payments",
                "Sample data and screenshots for the report"
        }));
        return cards;
    }

    private JPanel buildListCard(String heading, String[] items) {
        JPanel card = UiTheme.card();
        card.setLayout(new BorderLayout(0, 12));
        card.add(UiTheme.sectionHeading(heading), BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        for (String item : items) {
            JLabel label = UiTheme.body("- " + item);
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            list.add(label);
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }
}
