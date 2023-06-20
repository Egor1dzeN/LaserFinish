import arduino.Arduino;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Test extends JFrame {

    private JButton button1;
    public static String stat = "Waiting";
    public static String res = "No result";
    private JLabel ResultL;
    private JLabel StatusL;
    public JLabel status;
    public JLabel result;
    private JList list1;
    private JComboBox comboBox1;
    private JButton Test_con;
    static Arduino arduino = new Arduino("COM3", 9600);
    Thread thread;

    public Test() {
        super("My First Window");
        button1 = new JButton("Reset settings");

        setSize(250, 300);
        result.setText(res);
        status.setText(stat);
        JPanel jPanel = new JPanel(new FlowLayout());
        String[] test = {"opa", "test", "opa32"};
        comboBox1 = new JComboBox(test);
        jPanel.add(comboBox1);
        jPanel.add(Test_con);
        jPanel.add(StatusL);
        jPanel.add(status);
        jPanel.add(ResultL);
        jPanel.add(result);
        jPanel.add(button1, BorderLayout.NORTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(jPanel, BorderLayout.CENTER);
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] argd = {};
                setVisible(false);

                main(argd);
            }
        });


        Test_con.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(comboBox1.getSelectedItem());
            }
        });
    }

    public static void main(String[] args) {

        Test test = new Test();
        test.setVisible(true);
        boolean connection = arduino.openConnection();
        System.out.println("Connection: "+connection);
        int i = 0;
        while (true){
            i++;
            String str1 = arduino.serialRead();
            System.out.println(str1);
            System.out.println(i);
        }
    }
}
