/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbrot.old;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author bowen
 */
public interface JuliaImage {

    default BufferedImage getImage() {
        return getImage(true);
    }
    BufferedImage getImage(boolean useSmoothing);
    
    default void draw(Graphics2D g2) {
        draw(g2, true);
    }
    void draw(Graphics2D g2, boolean useSmoothing);

    void iteration(int maxIterations);
    void iterations(int maxIterations);
    
    void set(int index, double zRe, double zIm, double cRe, double cIm);
    void set(double[] zRe, double[] zIm, double[] cRe, double[] cIm);
}
