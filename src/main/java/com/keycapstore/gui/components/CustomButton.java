package com.keycapstore.gui.components;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;


public class CustomButton extends JButton {


    private static final Color COLOR_PRIMARY_DARK  = new Color(62,  54,  46);   
    private static final Color COLOR_CREAM_LIGHT   = new Color(228, 220, 207);  
    private static final Color COLOR_GLASS_WHITE   = new Color(255, 252, 245);  
    private static final Color COLOR_SUCCESS_GREEN = new Color(46,  204, 113);  
    private static final Color COLOR_INFO_BLUE     = new Color(52,  152, 219);  
    private static final Color COLOR_DANGER_RED    = new Color(231, 76,  60);   
    private static final Color COLOR_WARNING_ORANGE= new Color(230, 126, 34);   
    private static final Color COLOR_TEXT_PRIMARY  = new Color(51,  51,  51);   


    public enum Variant {
       
        PRIMARY(
            new Color(62, 54, 46),
            new Color(90, 78, 64),
            new Color(40, 34, 28),
            Color.WHITE
        ),
        
         
        SUCCESS(
            new Color(39, 174, 96),
            new Color(46, 204, 113),
            new Color(30, 139, 76),
            Color.WHITE
        ),
       
        DANGER(
            new Color(192, 57, 43),
            new Color(231, 76, 60),
            new Color(150, 40, 30),
            Color.WHITE
        ),
        
        INFO(
            new Color(41, 128, 185),
            new Color(52, 152, 219),
            new Color(30, 100, 150),
            Color.WHITE
        ),
       
        WARNING(
            new Color(211, 84, 0),
            new Color(230, 126, 34),
            new Color(170, 65, 0),
            Color.WHITE
        ),
        
        SECONDARY(
            new Color(127, 140, 141),
            new Color(149, 165, 166),
            new Color(100, 110, 111),
            Color.WHITE
        ),
        
        GHOST(
            new Color(0, 0, 0, 0),
            new Color(62, 54, 46, 20),
            new Color(62, 54, 46, 40),
            new Color(62, 54, 46)
        );

        final Color base;      
        final Color hover;      
        final Color pressed;     
        final Color textColor;   

        Variant(Color base, Color hover, Color pressed, Color textColor) {
            this.base      = base;
            this.hover     = hover;
            this.pressed   = pressed;
            this.textColor = textColor;
        }
    }


    public enum Size {
        SMALL (new Font("Segoe UI", Font.BOLD, 12), 80,  32, 8,  new Insets(4,  12, 4,  12)),
        MEDIUM(new Font("Segoe UI", Font.BOLD, 14), 110, 40, 10, new Insets(6,  20, 6,  20)),
        LARGE (new Font("Segoe UI", Font.BOLD, 15), 150, 48, 12, new Insets(10, 28, 10, 28));

        final Font     font;
        final int      minWidth;
        final int      height;
        final int      arc;     
        final Insets   padding;

        Size(Font font, int minWidth, int height, int arc, Insets padding) {
            this.font     = font;
            this.minWidth = minWidth;
            this.height   = height;
            this.arc      = arc;
            this.padding  = padding;
        }
    }


    private Variant  variant;
    private Size     size;
    private boolean  hovered  = false;
    private boolean  isPressed = false;
    private Icon     leadingIcon;    


    public CustomButton(String text) {
        this(text, Variant.PRIMARY, Size.MEDIUM);
    }


    public CustomButton(String text, Variant variant) {
        this(text, variant, Size.MEDIUM);
    }

 
    public CustomButton(String text, Variant variant, Size size) {
        super(text);
        this.variant = variant;
        this.size    = size;
        initialize();
    }

    private void initialize() {
        
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(
            size.padding.top, size.padding.left,
            size.padding.bottom, size.padding.right
        ));

     
        setFont(size.font);
        setForeground(variant.textColor);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

    
        setPreferredSize(new Dimension(size.minWidth, size.height));

  
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    hovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered   = false;
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);

        int w   = getWidth();
        int h   = getHeight();
        int arc = size.arc;

        Color bgColor;
        if (!isEnabled()) {
            bgColor = new Color(200, 200, 200);
            setForeground(new Color(150, 150, 150));
        } else if (isPressed) {
            bgColor = variant.pressed;
            setForeground(variant.textColor);
        } else if (hovered) {
            bgColor = variant.hover;
            setForeground(variant.textColor);
        } else {
            bgColor = variant.base;
            setForeground(variant.textColor);
        }

       
        if (variant != Variant.GHOST && isEnabled() && !isPressed) {
            g2.setColor(new Color(0, 0, 0, 25));
            g2.fillRoundRect(2, 3, w - 4, h - 2, arc, arc);
        }

  
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, w - 1, h - 2, arc, arc);


        if (variant == Variant.GHOST) {
            g2.setColor(new Color(62, 54, 46, 120));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, w - 1, h - 2, arc, arc);
        }

        g2.dispose();
        super.paintComponent(g);
    }


    public void setVariant(Variant variant) {
        this.variant = variant;
        setForeground(variant.textColor);
        repaint();
    }

 
    public void setSize(Size size) {
        this.size = size;
        setFont(size.font);
        setPreferredSize(new Dimension(size.minWidth, size.height));
        revalidate();
        repaint();
    }


    public Variant getVariant() { return variant; }

    public Size getButtonSize()  { return size; }
}