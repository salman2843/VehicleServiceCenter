package com.vsc.ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;

/**
 * Shared colours and label factories so every screen looks the same.
 * Keeping this in one place means a styling change is a one-line edit instead
 * of a search-and-replace across all panels.
 */
public final class UiTheme {

    public static final Color BACKGROUND = new Color(0xF7, 0xF8, 0xFA);
    public static final Color TEXT = new Color(0x1F, 0x23, 0x28);
    public static final Color MUTED = new Color(0x6B, 0x72, 0x80);
    public static final Color ACCENT = new Color(0x1B, 0x5E, 0xC4);
    public static final Color OK = new Color(0x1B, 0x7F, 0x3B);
    public static final Color BORDER = new Color(0xD5, 0xD9, 0xE0);

    private UiTheme() {
        // utility class, no instances
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 22f));
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel sectionHeading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel body(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        return label;
    }

    public static JLabel status(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(OK);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        return label;
    }

    /** A bordered, padded white card used to group content on the grey background. */
    public static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        return panel;
    }
}
