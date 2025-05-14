package com.example.library.forms;

import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;

public class UIStyleManager {
    public static void applyStyles() {
        // Define your color scheme
        Color backgroundColor = new Color(253, 251, 238); // #FDFBEE
        Color textColor = new Color(1, 85, 81); // #015551
        Color formBackgroundColor = new Color(255, 255, 255); // #ffffff
        Color borderColor = new Color(87, 180, 186); // #57B4BA
        Color buttonColor = new Color(87, 180, 186); // #57B4BA
        Color buttonHoverColor = new Color(1, 85, 81); // #015551
        Color buttonActiveColor = new Color(254, 79, 45); // #FE4F2D
        Color errorColor = Color.RED;

        // Apply styles to UI components
        UIManager.put("Panel.background", backgroundColor);
        UIManager.put("Label.foreground", textColor);
        UIManager.put("Button.background", buttonColor);

        // Check the operating system
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            UIManager.put("Button.foreground", textColor); // Use text color for Mac
        } else {
            UIManager.put("Button.foreground", Color.WHITE); // Use white for other OS
        }

        UIManager.put("Button.hoverBackground", buttonHoverColor);
        UIManager.put("Button.activeBackground", buttonActiveColor);
        UIManager.put("TextField.background", formBackgroundColor);
        UIManager.put("TextField.foreground", textColor);
        UIManager.put("TextField.border", borderColor);
        UIManager.put("TextField.margin", 15);
        UIManager.put("TextField.padding", 10);
        UIManager.put("TextField.borderRadius", 5);
        UIManager.put("ErrorLabel.foreground", errorColor);
        UIManager.put("ErrorLabel.fontSize", 0.9);
        UIManager.put("ErrorLabel.marginTop", -10);
        UIManager.put("ErrorLabel.marginBottom", 10);
        UIManager.put("Paragraph.textAlign", "center");

        // Set font
        Font font = new Font("Arial", Font.BOLD, 14);
        UIManager.put("Label.font", font);
        UIManager.put("Button.font", font);
        UIManager.put("TextField.font", font);
    }

}