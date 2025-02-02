package com.darwinsys.swingui;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.*;

/// Makes 4x2 panel of colors - 7 from COLORS and one
/// from a ColorPicker Icon
public class ColorPanel extends JPanel {
    protected Consumer<Color> setter;
    protected static final int SWATCH_SIZE = 40;
    protected static final int ROWS = 2, COLUMNS = 4;

    public final static Color[] COLORS = {
            Color.BLACK, Color.WHITE, Color.RED, Color.BLUE,
            Color.GREEN, Color.ORANGE, Color.MAGENTA,
    };

    public ColorPanel(Consumer<Color> setter) {
        this.setter = setter;
        if (ROWS * COLUMNS - 1 != COLORS.length) {
            throw new IllegalArgumentException("Color array mis-shapen");
        }
        setLayout(new GridLayout(ROWS,COLUMNS));
        for (Color c : COLORS) {
            var b = new JButton(){
                public Dimension getSize() {
                    return getPreferredSize();
                }
                @Override
                protected void paintComponent(Graphics g) {
                    var d = getSize();
                    g.fillRect(0, 0, d.width, d.height);
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(SWATCH_SIZE, SWATCH_SIZE);
                }

                @Override
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
            b.addActionListener(evt -> setter.accept(c));
            b.setSize(new Dimension(SWATCH_SIZE,SWATCH_SIZE));
            b.setForeground(c);
            b.setBackground(c);
            b.setOpaque(true);
            add(b);
        }

        // Create a JButton for the picker. If we can read the icon for it,
        // then we'll use it, else the default is a label of "..."
        JButton b = new JButton("...");
        final String iconName = "dsapi/color-picker.png";
        URL url = this.getClass().getResource(iconName);
        if (url != null) {
            try {
                Image image = ImageIO.read(url);
                b = new JButton(new ImageIcon(image));
            } catch (IOException e) {
				// Error handled below
            }
        }
		if (b.getText().equals("...")) {
			System.out.println("Unable to load " + iconName);
		}
        b.addActionListener(chooser);
        add(b);
    }

    ActionListener chooser = e -> {
        Color ch = JColorChooser.showDialog(
                null,             // parent
                "Pick a Drawing Color",
                getBackground()// title
        );
        if (ch != null) {
            setter.accept(ch);
        }
    };

    public static void main(String[] args) {
        JFrame jf = new JFrame("Dummy");
        jf.add(BorderLayout.EAST, new ColorPanel(System.out::println));
        jf.add(BorderLayout.WEST, new JLabel("Color"));
        jf.pack();
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.setVisible(true);
    }
}
