/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbrot.old;

import com.aparapi.Kernel;
import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

/**
 *
 * @author bowen
 */
public class JuliaImageAparapiKernel extends Kernel {
    
    private static final double LOG2 = Math.log(2d);
    
    
    private final float[] zRe, zIm, cRe, cIm, n;
    private final int[] iterations;
    private final boolean[] divergent;
    
    
    public int[] maxIterations = new int[]{100};

    public JuliaImageAparapiKernel(float[] zRe, float[] zIm, float[] cRe, float[] cIm, int[] iterations, boolean[] divergent, float[] n) {
        this.zRe = zRe;
        this.zIm = zIm;
        this.cRe = cRe;
        this.cIm = cIm;
        this.iterations = iterations;
        this.divergent = divergent;
        this.n = n;
    }
    
    
    private void iteration(int index, int maxIterations) {
        
        if (divergent[index] || iterations[index] >= maxIterations) {
            return;
        }
        
        final float zr = zRe[index];
        final float zi = zIm[index];
        
        final float zr2 = zr * zr;
        final float zi2 = zi * zi;
        
        if (zr2 + zi2 >= 4) {
            divergent[index] = true;
            return;
        }
        
        zRe[index] = zr2 - zi2 + cRe[index];
        zIm[index] = 2f * zr * zi + cIm[index];
        
        iterations[index] = iterations[index] + 1;
    }

    @Override
    public void run() {
        int index = this.getGlobalId();
        for (int i=0; i<maxIterations[0]; i++) {
            iteration(index, maxIterations[0]);
        }
    }
    
}
