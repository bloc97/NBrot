/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbrot;

import nbrot.old.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.stream.IntStream;
import javax.swing.JPanel;
import org.apfloat.Apcomplex;
import org.apfloat.Apfloat;

/**
 *
 * @author bowen
 */
public class DisplayPanel extends JPanel {
    
    private boolean isInit = false;
    
    private double mandelScale = 0.001d;
    private double mandelX = -0.79d, mandelY = .15d;
    
    private double juliaScale = 0.001d;
    private double juliaX = 0, juliaY = 0;
    private double juliaCX = 0, juliaCY = 0;
    
    private int widthPixel = 1, heightPixel = 1;
    
    private int maxIterations = 128;
    
    private int mouseX = 0, mouseY = 0;
    private int mouseButton = MouseEvent.BUTTON1;
    
    private boolean isMandel = true, useSmoothing = true;
    private volatile boolean isMoved = true;
    
    private JuliaGrid grid = new JuliaGrid();
    
    public DisplayPanel() {
        
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mouseClicked(e);
                int newX = e.getXOnScreen();
                int newY = e.getYOnScreen();
                mouseX = newX;
                mouseY = newY;
                mouseButton = e.getButton();
            }

            
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                
                isMoved = true;
                
                int newX = e.getXOnScreen();
                int newY = e.getYOnScreen();
                double dx = -(newX - mouseX) * (isMandel ? mandelScale : juliaScale) * (!e.isControlDown() ? 1d : 0.03d);
                double dy = (newY - mouseY) * (isMandel ? mandelScale : juliaScale) * (!e.isControlDown() ? 1d : 0.03d);
                
                if (mouseButton == MouseEvent.BUTTON1) {
                    if (isMandel) {
                        mandelX += dx;
                        mandelY += dy;
                        juliaCX = mandelX;
                        juliaCY = mandelY;
                        
                        
                        
                    } else {
                        juliaX += dx;
                        juliaY += dy;
                    }
                } else {
                    juliaCX += dx;
                    juliaCY += dy;
                    mandelX = juliaCX;
                    mandelY = juliaCY;
                }
                


                mouseX = newX;
                mouseY = newY;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                super.mouseWheelMoved(e);
                isMoved = true;
                final double multiplier = 1.1d;
                if (e.getPreciseWheelRotation() > 0) {
                    if (isMandel) {
                        mandelScale *= multiplier;
                    } else {
                        juliaScale *= multiplier;
                    }
                } else {
                    if (isMandel) {
                        mandelScale /= multiplier;
                    } else {
                        juliaScale /= multiplier;
                    }
                }
                repaint();
            }
            
            
        };
        
        this.addMouseListener(mouseAdapter);
        this.addMouseMotionListener(mouseAdapter);
        this.addMouseWheelListener(mouseAdapter);
        
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                System.out.println(e);
                switch (e.getKeyChar()) {
                    case '+':
                    case '=':
                        maxIterations *= 2;
                        repaint();
                        break;
                    case '-':
                        maxIterations /= 2;
                        if (maxIterations < 2) {
                            maxIterations = 2;
                        }
                        repaint();
                        break;
                    case ' ':
                        isMoved = true;
                        if (isMandel) {
                            isMandel = false;
                            juliaX = 0;
                            juliaY = 0;
                            juliaScale = 0.001d;
                            resetJulia();
                        } else {
                            isMandel = true;
                        }
                        repaint();
                        break;
                    case 'r':
                        isMoved = true;
                        if (isMandel) {
                            resetMandel();
                        } else {
                            resetJulia();
                        }
                        repaint();
                        break;
                    case 'i':
                        isMoved = true;
                        useSmoothing = !useSmoothing;
                        repaint();
                        break;
                }
            }
            
            
            
        });
        
        
    }
    
    private void resetMandel() {
        mandelX = 0;
        mandelY = 0;
        mandelScale = 4d / (Math.max(widthPixel, heightPixel));
    }
    
    private void resetJulia() {
        juliaX = 0;
        juliaY = 0;
        juliaScale = 4d / ( Math.max(widthPixel, heightPixel));
    }
    
    
    BufferedImage image = new BufferedImage(widthPixel, heightPixel, BufferedImage.TYPE_INT_ARGB);
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        final double scaling = g2.getTransform().getScaleX();
        
        g2.setTransform(AffineTransform.getScaleInstance(1d, 1d));
        
        int newWidthPixel = (int)Math.ceil(getWidth() * scaling);
        int newHeightPixel = (int)Math.ceil(getHeight()* scaling);
        
        boolean isMoved = this.isMoved;
        
        if (newWidthPixel != widthPixel || newHeightPixel != heightPixel) {
            image = new BufferedImage(newWidthPixel, newHeightPixel, BufferedImage.TYPE_INT_ARGB);
        }
        
        widthPixel = newWidthPixel;
        heightPixel = newHeightPixel;
        
        if (!isInit) {
            resetMandel();
            isInit = true;
        }
        
        
        int size = widthPixel * heightPixel;
        
        
        if (isMoved) {
            IntStream.range(0, size).forEach((n) -> {
                int i = n % widthPixel;
                int j = n / widthPixel;
                Apcomplex position = new Apcomplex(new Apfloat(getXPos(i, mandelX, mandelScale)), new Apfloat(getYPos(j, mandelY, mandelScale)));
                JuliaGrid grid = this.grid.getNode(position, new Apfloat(mandelScale));
                
                boolean b = false;
                if (grid != null) {
                    b = grid.sample(position) > 0;
                }
                
                image.setRGB(i, j, (b ? 0xFFFFFFFF : 0xFF000000));
                
            });
            this.isMoved = false;
        }
        
        
        //TODO: Draw directly with g2 without image, reducing overhead.
        //jImage.draw(g2);
        g2.drawImage(image, 0, 0, this);
        
        g2.setFont(Font.getFont("Consolas"));
        
        g.setColor(Color.CYAN);
        g2.drawString("Re: " + mandelX, 10, 20);
        g2.drawString("Im: " + mandelY, 10, 40);
        
        g2.drawString("Zoom: " + (1d/mandelScale), 10, 60);
        g2.drawString("Max Iterations: " + maxIterations, 10, 80);
        
        g2.drawString("Octree children: " + grid.countChildren(), 10, 100);
        
        repaint();
    }
    
    private double getXPos(int xPixel, double tx, double scale) {
        return (xPixel - (widthPixel/2d)) * scale + tx;
    }
    private double getYPos(int yPixel, double ty, double scale) {
        return (yPixel - (heightPixel/2d)) * scale - ty;
    }
    
    
    
}
