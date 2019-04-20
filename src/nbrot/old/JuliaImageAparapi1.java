/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbrot.old;

import com.aparapi.Kernel;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 *
 * @author bowen
 */
public class JuliaImageAparapi1 extends Kernel implements JuliaImage {
    
    private static final double LOG2 = Math.log(2d);
    
    
    private final double[] zRe, zIm, cRe, cIm;
    private final int[] iterations;
    private int currentMaxIteration;
    private final boolean[] divergent;
    
    private final int width, height, size;
    private final double n;
    
    private boolean useMultithreading = true;
    
    private final BufferedImage image;
    
    private boolean[] useSingleIteration = new boolean[] {false};
    private int[] maxIterations = new int[]{100};
        
    public JuliaImageAparapi1(int width, int height) {
        this(width, height, 2d);
    }
    
    public JuliaImageAparapi1(int width, int height, double n) {
        this.width = width;
        this.height = height;
        this.size = width * height;
        this.n = n;
        
        this.zRe = new double[size];
        this.zIm = new double[size];
        this.cRe = new double[size];
        this.cIm = new double[size];
        this.iterations = new int[size];
        this.divergent = new boolean[size];
        
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }
    
    @Override
    public void set(int index, double zRe, double zIm, double cRe, double cIm) {
        this.zRe[index] = zRe;
        this.zIm[index] = zIm;
        this.cRe[index] = cRe;
        this.cIm[index] = cIm;
        this.iterations[index] = 0;
        this.divergent[index] = false;
        this.currentMaxIteration = 0;
    }
    
    @Override
    public void set(double[] zRe, double[] zIm, double[] cRe, double[] cIm) {
        System.arraycopy(zRe, 0, this.zRe, 0, width);
        System.arraycopy(zIm, 0, this.zIm, 0, width);
        System.arraycopy(cRe, 0, this.cRe, 0, width);
        System.arraycopy(cIm, 0, this.cIm, 0, width);
        Arrays.fill(iterations, 0);
        Arrays.fill(divergent, false);
        this.currentMaxIteration = 0;
    }
    
    private boolean iteration(int index, int maxIterations) {
        
        if (divergent[index] || iterations[index] >= maxIterations) {
            return false;
        }
        
        final double zr = zRe[index];
        final double zi = zIm[index];
        
        final double zr2 = zr * zr;
        final double zi2 = zi * zi;
        
        if (zr2 + zi2 >= 4) {
            divergent[index] = true;
            return false;
        }
        
        if (n == 2d) {
            zRe[index] = zr2 - zi2 + cRe[index];
            zIm[index] = 2d * zr * zi + cIm[index];
        } else {
            final double a = this.pow(zr2 + zi2, n / 2d);
            final double d = n * this.atan2(zi, zr);
            
            zRe[index] = a * this.cos(d) + cRe[index];
            zIm[index] = a * this.sin(d) + cIm[index];
        }
        iterations[index] = iterations[index] + 1;
        return true;
    }

    public boolean isUseMultithreading() {
        return useMultithreading;
    }

    public void setUseMultithreading(boolean useMultithreading) {
        this.useMultithreading = useMultithreading;
    }
    
    @Override
    public void iteration(int maxIterations) {
        this.maxIterations[0] = maxIterations;
        
        this.useSingleIteration[0] = true;
        this.execute(size);
        
        if (useMultithreading) {
            currentMaxIteration = IntStream.range(0, size).parallel().map(i -> {
                return iterations[i];
            }).max().orElse(0);
        }
    }
    
    @Override
    public void iterations(int maxIterations) {
        this.maxIterations[0] = maxIterations;
        
        this.useSingleIteration[0] = false;
        this.execute(size);
        
        if (useMultithreading) {
            currentMaxIteration = IntStream.range(0, size).parallel().map(i -> {
                return iterations[i];
            }).max().orElse(0);
        }
    }
    @Override
    public BufferedImage getImage() {
        return getImage(true);
    }
    
    @Override
    public BufferedImage getImage(boolean useSmoothing) {
        
        if (useMultithreading) {
            
            IntStream.range(0, size).parallel().forEach((n) -> {
                int i = n % width;
                int j = n / width;

                double iteration = (double) iterations[n];
                
                if (useSmoothing && iteration < currentMaxIteration) {
                    double log_zn = Math.log(zRe[n] * zRe[n] + zIm[n] * zIm[n]) / 2d;
                    double nu = Math.log(log_zn / LOG2) /LOG2;
                    iteration = iteration + 1d - nu;
                }
  
                final int ratio = (int)((iteration / currentMaxIteration) * 255d);
                final int argb = 0xFF << 24 | ratio << 16 | ratio << 8 | ratio;

                image.setRGB(i, j, argb);
            });
        }
        
        
        
        return image;
    }
    
    @Override
    public void run() {
        //int index = this.getGlobalId();
        //if (useSingleIteration[0]) {
            //iteration(index, maxIterations[0]);
        //} else {
            //while(iteration(index, maxIterations[0])) {
            //}
        //}
    }
    
    @Override
    public void draw(Graphics2D g2, boolean useSmoothing) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
