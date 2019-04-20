/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbrot.old;

/**
 *
 * @author bowen
 */
public class Julia {
    
    /**
     * @param z complex number
     * @param c complex number
     * Computes z = z^2 + c if |z| < 2
     * @return true if performed computation, false if |z| >= 2
     */
    public static boolean iteration(double[] z, double[] c) {
        return iteration(z, c, 2d);
    }
    /**
     * @param z complex number
     * @param c complex number
     * @param n real number
     * Computes z = z^n + c if |z| < 2
     * @return true if performed computation, false if |z| >= 2
     */
    public static boolean iteration(double[] z, double[] c, double n) {
        final double zr = z[0];
        final double zi = z[1];
        
        final double zr2 = zr * zr;
        final double zi2 = zi * zi;
        
        if (zr2 + zi2 >= 4) {
            return false;
        }
        
        if (n == 2d) {
            z[0] = zr2 - zi2 + c[0];
            z[1] = 2d * zr * zi + c[1];
        } else {
            final double a = Math.pow(zr2 + zi2, n / 2d);
            final double d = n * Math.atan2(zi, zr);
            
            z[0] = a * Math.cos(d) + c[0];
            z[1] = a * Math.sin(d) + c[1];
        }
        return true;
    }
    
    public static int iterations(double zr, double zi, double cr, double ci, int maxIterations) {
        return iterations(zr, zi, cr, ci, 2d, maxIterations);
    }
    
    public static int iterations(double zr, double zi, double cr, double ci, double n, int maxIterations) {
        int iterations = 0;
        
        final double[] z = new double[] {zr, zi};
        final double[] c = new double[] {cr, ci};
        
        while (iterations < maxIterations && iteration(z, c, n)) {
            iterations++;
        }
        
        return iterations;
    }
    
}
