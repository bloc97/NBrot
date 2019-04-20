/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbrot;

import java.util.stream.IntStream;
import org.apfloat.Apcomplex;
import org.apfloat.Apfloat;
import org.apfloat.FixedPrecisionApfloatHelper;

/**
 *
 * @author bowen
 */
public class JuliaGrid {
    
    public static final long PRECISION = 100;
    public static final Apfloat TWO = new Apfloat("2", PRECISION);
    public static final FixedPrecisionApfloatHelper MATH = new FixedPrecisionApfloatHelper(PRECISION);
    
    public static int gridSizeInPixels = 64, maxIterations = 64;
    
    
    
    private final Apfloat x0, y0, x1, y1, xm, ym;
    
    private JuliaGrid x0y0, xmy0, x0ym, xmym;
    
    private Apfloat pixelScale;
    
    private Apcomplex[] xabcN;
    private Apcomplex[] d;
    
    private float[] df, xabcNf;
    private int[] it;
    private boolean[] divergent;
    private volatile boolean rendered = false;
    
    public JuliaGrid() {
        this(new Apfloat(-2), new Apfloat(-2), new Apfloat(2), new Apfloat(2));
    }
    
    private JuliaGrid(Apfloat x0, Apfloat y0, Apfloat x1, Apfloat y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.xm = MATH.divide(MATH.add(x0, x1), TWO);
        this.ym = MATH.divide(MATH.add(y0, y1), TWO);
        updateData();
    }
    
    public final void updateData() {
        final boolean sizeChanged = (it != null) ? it.length != (gridSizeInPixels * gridSizeInPixels) : true;
        
        if (sizeChanged) {
            //TODO: Save previous values and/or get values from children grids to save on compute time
            final Apfloat gridSize = new Apfloat(gridSizeInPixels);
            it = new int[gridSizeInPixels * gridSizeInPixels];
            divergent = new boolean[gridSizeInPixels * gridSizeInPixels];
            df = new float[it.length * 2];
            d = new Apcomplex[it.length];
            pixelScale = MATH.divide(MATH.subtract(x1, x0), gridSize);
            rendered = false;
            
            for (int i=0; i<it.length; i++) {
                int x = i % gridSizeInPixels;
                int y = i / gridSizeInPixels;
                
                d[i] = new Apcomplex(x0.add(pixelScale.multiply(new Apfloat(x))).subtract(xm), y0.add(pixelScale.multiply(new Apfloat(y))).subtract(ym));
                df[i * 2 + 0] = x0.add(pixelScale.multiply(new Apfloat(x))).subtract(xm).floatValue();
                df[i * 2 + 1] = y0.add(pixelScale.multiply(new Apfloat(y))).subtract(ym).floatValue();
            }
            
        }
        
        final boolean maxIterationsChanged = (xabcN != null) ? xabcN.length != (maxIterations * 4) : true;
        
        if (maxIterationsChanged) {
            //TODO: Save previous values if maxIterations increased and/or get values to save on compute time
            
            xabcN = new Apcomplex[maxIterations * 4];
            xabcNf = new float[maxIterations * 8];
            
            xabcN[0] = new Apcomplex(xm, ym);
            xabcN[1] = new Apcomplex(Apfloat.ONE, Apfloat.ZERO);
            xabcN[2] = new Apcomplex(Apfloat.ZERO, Apfloat.ZERO);
            xabcN[3] = new Apcomplex(Apfloat.ZERO, Apfloat.ZERO);
            
            for (int i=1; i<maxIterations; i++) {
                final int index = i * 4;
                
                
                xabcN[index + 0] = MATH.add(MATH.multiply(xabcN[index - 4], xabcN[index - 4]), xabcN[0]);
                xabcN[index + 1] = MATH.add(MATH.product(xabcN[index - 4], xabcN[index - 3], TWO), Apcomplex.ONE);
                xabcN[index + 2] = MATH.add(MATH.product(xabcN[index - 4], xabcN[index - 2], TWO), MATH.multiply(xabcN[index - 3], xabcN[index - 3]));
                xabcN[index + 3] = MATH.add(MATH.product(xabcN[index - 4], xabcN[index - 1], TWO), MATH.product(xabcN[index - 3], xabcN[index - 2], TWO));
                
                if (xabcN[index + 0].floatValue() >= 2) {
                    for (int j=i; j<maxIterations; j++) {
                        xabcN[j * 4] = xabcN[index + 0];
                        xabcN[j * 4 + 1] = xabcN[index + 1];
                        xabcN[j * 4 + 2] = xabcN[index + 2];
                        xabcN[j * 4 + 3] = xabcN[index + 3];
                    }
                    break;
                }
            }
            
            IntStream.range(0, maxIterations).parallel().forEach((i) -> {
                final int index4 = i * 4;
                final int index8 = i * 8;
                
                xabcNf[index8 + 0] = xabcN[index4 + 0].real().floatValue();
                xabcNf[index8 + 1] = xabcN[index4 + 0].imag().floatValue();
                xabcNf[index8 + 2] = xabcN[index4 + 1].real().floatValue();
                xabcNf[index8 + 3] = xabcN[index4 + 1].imag().floatValue();
                xabcNf[index8 + 4] = xabcN[index4 + 2].real().floatValue();
                xabcNf[index8 + 5] = xabcN[index4 + 2].imag().floatValue();
                xabcNf[index8 + 6] = xabcN[index4 + 3].real().floatValue();
                xabcNf[index8 + 7] = xabcN[index4 + 3].imag().floatValue();
            });
            
            
            rendered = false;
        }
        
    }
    
    public void render() {
        if (isRendered()) {
            return;
        }
        IntStream.range(0, it.length).parallel().forEach((i) -> {
            
            Apcomplex d1 = d[i];
            Apcomplex d2 = d1.multiply(d1);
            Apcomplex d3 = d2.multiply(d1);
            
            Apcomplex a = xabcN[xabcN.length - 3];
            Apcomplex b = xabcN[xabcN.length - 2];
            Apcomplex c = xabcN[xabcN.length - 1];
            
            
            Apcomplex y = d1.multiply(a).add(d2.multiply(b)).add(d3.multiply(c)).add(xabcN[0]);
            
            it[i] = (y.real().multiply(y.real()).add(y.imag().multiply(y.imag())).compareTo(TWO.add(TWO)) >= 0) ? 0 : 10;
        
            
        });
        
        rendered = true;
    }
    
    public boolean isOutOfBounds(Apcomplex p) {
        return (p.real().compareTo(x0) < 0 || p.real().compareTo(x1) > 0 || p.imag().compareTo(y0) < 0 || p.imag().compareTo(y1) > 0); //Out of bounds
    }
    
    public int countChildren() {
        if (x0y0 == null) {
            return 1;
        } else {
            return x0y0.countChildren() + x0ym.countChildren() + xmy0.countChildren() + xmym.countChildren() + 1;
        }
    }
    
    public JuliaGrid getNode(Apcomplex p, Apfloat pixelScale) {
        if (p.imag().compareTo(Apfloat.ZERO) < 0) { //Mandelbrot is symmetrical, don't render twice
            return this.getNode(p.conj(), pixelScale);
        }
        
        if (isOutOfBounds(p)) { //Out of bounds
            return null;
        }
        
        if (pixelScale.compareTo(this.pixelScale) < 0) { //If not enough resolution, defer to children
            
            if (x0y0 == null) { //No children yet, create them
                x0y0 = new JuliaGrid(x0, y0, xm, ym);
                x0ym = new JuliaGrid(x0, ym, xm, y1);
                xmy0 = new JuliaGrid(xm, y0, x1, ym);
                xmym = new JuliaGrid(xm, ym, x1, y1);
            }
            
            if (p.real().compareTo(xm) < 0) { //Smaller than xm
                
                if (p.imag().compareTo(ym) < 0) { //Smaller than ym
                    return x0y0.getNode(p, pixelScale);
                } else { //Bigger or equal than ym
                    return x0ym.getNode(p, pixelScale);
                }
                
            } else { //Bigger or equal than xm
                
                if (p.imag().compareTo(ym) < 0) { //Smaller than ym
                    return xmy0.getNode(p, pixelScale);
                } else { //Bigger or equal than ym
                    return xmym.getNode(p, pixelScale);
                }
            }
        }
        
        //We found the node, it is the current node
        return this;
    }

    public boolean isRendered() {
        return rendered;
    }
    
    
    public int sample(Apcomplex p) {
        if (p.imag().compareTo(Apfloat.ZERO) < 0) { //Mandelbrot is symmetrical, don't render twice
            return this.sample(p.conj());
        }
        
        if (isOutOfBounds(p)) {
            return 0;
        }
        
        updateData();
        render();
        
        final float x = p.real().subtract(x0).divide(x1.subtract(x0)).floatValue();
        final float y = p.imag().subtract(y0).divide(y1.subtract(y0)).floatValue();
        
        int xi = (int)(x * gridSizeInPixels);
        int yi = (int)(y * gridSizeInPixels);
        
        if (xi >= gridSizeInPixels) {
            xi = gridSizeInPixels - 1;
        }        
        if (yi >= gridSizeInPixels) {
            yi = gridSizeInPixels - 1;
        }
        
        return it[yi * gridSizeInPixels + xi];
        
    }
    
    
    
}
