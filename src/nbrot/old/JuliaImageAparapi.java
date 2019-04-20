/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbrot.old;

import com.aparapi.Kernel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 *
 * @author bowen
 */
public class JuliaImageAparapi implements JuliaImage {
    
    private static final double LOG2 = Math.log(2d);
    
    
    private final float[] zRe, zIm, cRe, cIm;
    private final int[] iterations;
    private int currentMaxIteration;
    private final boolean[] divergent;
    
    private final int width, height, size;
    private final double n;
    
    private boolean useMultithreading = true;
    
    private final BufferedImage image;
    
    private boolean[] useSingleIteration = new boolean[] {false};
    private int[] maxIterations = new int[]{100};
    
    private final JuliaImageAparapiKernel kernel;
        
    public JuliaImageAparapi(int width, int height) {
        this(width, height, 2d);
    }
    
    public JuliaImageAparapi(int width, int height, double n) {
        this.width = width;
        this.height = height;
        this.size = width * height;
        this.n = n;
        
        this.zRe = new float[size];
        this.zIm = new float[size];
        this.cRe = new float[size];
        this.cIm = new float[size];
        this.iterations = new int[size];
        this.divergent = new boolean[size];
        
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        this.kernel = new JuliaImageAparapiKernel(zRe, zIm, cRe, cIm, iterations, divergent, new float[]{(float)n});
    }
    
    @Override
    public void set(int index, double zRe, double zIm, double cRe, double cIm) {
        this.zRe[index] = (float)zRe;
        this.zIm[index] = (float)zIm;
        this.cRe[index] = (float)cRe;
        this.cIm[index] = (float)cIm;
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
    

    public boolean isUseMultithreading() {
        return useMultithreading;
    }

    public void setUseMultithreading(boolean useMultithreading) {
        this.useMultithreading = useMultithreading;
    }
    
    @Override
    public void iteration(int maxIterations) {
        kernel.maxIterations[0] = maxIterations;
        
        if (currentMaxIteration >= maxIterations) {
            return;
        }
        kernel.execute(size);
        
        if (useMultithreading) {
            currentMaxIteration = IntStream.range(0, size).parallel().map(i -> {
                return iterations[i];
            }).max().orElse(0);
        }
    }
    
    @Override
    public void iterations(int maxIterations) {
        kernel.maxIterations[0] = maxIterations;
        
        if (currentMaxIteration >= maxIterations) {
            return;
        }
        
        kernel.execute(size);
        
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
    public void draw(Graphics2D g2, boolean useSmoothing) {
        
        for (int n=0; n<size; n++) {
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
            
            g2.setColor(new Color(ratio, ratio, ratio));
            g2.drawRect(i, j, 1, 1);
        }
        
    }
    
}
