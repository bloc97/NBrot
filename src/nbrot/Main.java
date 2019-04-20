/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbrot;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author bowen
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch();
    }
    
    private static void launch() {
        JFrame frame = new JFrame("NBrot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel panel = new DisplayPanel();
        panel.setPreferredSize(new Dimension(800, 600));
        
        frame.add(panel);
        
        frame.pack();
        frame.setVisible(true);
        
        frame.requestFocusInWindow();
        panel.requestFocusInWindow();
    }
    
}
