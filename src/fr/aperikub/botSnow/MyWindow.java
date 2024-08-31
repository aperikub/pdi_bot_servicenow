package fr.aperikub.botSnow;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

public class MyWindow extends JFrame {

    public MyWindow(){
        super("My first swing application");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(new Dimension(600,400));
        //this.setLocation(new Point(100,100));
        this.setLocationRelativeTo(null);// centrer par rapport a l'ecran
        //this.getJMenuBar();//acceder au menu superieur
        JPanel contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(new FlowLayout());
        //contentPane.setLayout(new FlowLayout.RIGHT()); //aligné à droite
        //JButton bouton = new JButton("i'm real");
        contentPane.add(new JButton("push me"));
        contentPane.add(new JButton("click me"));
        contentPane.add(new JCheckBox("check me"));
        contentPane.add(new JTextField("edit me"));
    }



}
