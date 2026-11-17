package com.vsc.ui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;

/**
 * Base class for screens that exist in the navigation but are not connected to
 * the service layer yet.
 *
 * <p>Each concrete screen states what it will do and which service it will call,
 * so the next developer knows exactly where to continue. Delete nothing here:
 * replace the placeholder content with the real table/form once the DAO and
 * service classes exist.</p>
 */
public abstract class PlaceholderPanel extends JPanel {

    protected PlaceholderPanel(String title, String subtitle, String serviceNote, String... plannedFeatures) {
        super(new BorderLayout(0, 18));
        setBackground(UiTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(26, 30, 22, 30));

        add(buildHeader(title, subtitle), BorderLayout.NORTH);
        add(buildPlannedCard(plannedFeatures), BorderLayout.CENTER);
        add(UiTheme.body(serviceNote), BorderLayout.SOUTH);
    }

    private JPanel buildHeader(String title, String subtitle) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(UiTheme.title(title));
        header.add(UiTheme.body(subtitle));
        return header;
    }

    private JPanel buildPlannedCard(String[] plannedFeatures) {
        JPanel card = UiTheme.card();
        card.setLayout(new BorderLayout(0, 12));
        card.add(UiTheme.sectionHeading("Planned on this screen"), BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        for (String feature : plannedFeatures) {
            JLabel item = UiTheme.body("- " + feature);
            item.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            list.add(item);
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }
}
