import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;
import java.util.Vector;

public class Test extends JFrame{

    private JButton Reset = new JButton("Reset Settings");
    private JLabel ResultL;
    private JLabel StatusL;
    public JLabel status = new JLabel("Waiting Connect");
    public JLabel result = new JLabel("No result");
    private JComboBox comboBox1 = new JComboBox(ShowAllPorts());
    private JButton Test_con;
    private JButton Update;
    private JLabel isConnectionL;
    private JTable table_result;
    private DefaultTableModel tableModel = new DefaultTableModel();
    SerialPort serialPort;
    boolean isConnection;
    SerialPort[] ports = SerialPort.getCommPorts();
    private final String url = "jdbc:mysql://localhost:3306";
    private final String user = "root";
    private final String password = "Partner25";
    private final Connection con = DriverManager.getConnection(url,user,password);
    private final Statement stm = con.createStatement();
    Vector<Vector<String>> list_result = new Vector<>();
    Vector<String> headers = new Vector<>();

    public Test() throws IOException, SQLException {
        super("My First Window");
        setVisible(true);
        setSize(1000, 300);
        JPanel jPanel = new JPanel(new FlowLayout());
        init_headers();

        table_result = new JTable(tableModel);
        table_result.setDefaultEditor(Object.class, null);
        //table_result.setColumnModel();
        jPanel.add(comboBox1);
        jPanel.add(isConnectionL);
        jPanel.add(Test_con);
        jPanel.add(Update);
        jPanel.add(StatusL);
        jPanel.add(status);
        jPanel.add(ResultL);
        jPanel.add(result);
        jPanel.add(Reset, BorderLayout.NORTH);
        //jPanel.add(table_result.getTableHeader());
        jPanel.add(table_result);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Panel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
        this.setLayout(new BorderLayout());
        add(jPanel, BorderLayout.CENTER);
        Reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                String[] a = {};
                try {
                    main(a);
                } catch (IOException | SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        Test_con.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.fireTableDataChanged();
                int i = comboBox1.getSelectedIndex();
                serialPort = ports[i];
                serialPort.openPort();
                if(serialPort.isOpen()){
                    isConnectionL.setText("Connect");
                    isConnectionL.setForeground(Color.GREEN);
                    isConnection = true;
                    status.setText("Wait Arduino");
                    serialPort.addDataListener(new SerialPortDataListener() {
                        @Override
                        public int getListeningEvents() {
                            return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                        }

                        @Override
                        public void serialEvent(SerialPortEvent serialPortEvent) {
                            int size = serialPortEvent.getSerialPort().bytesAvailable();
                            byte[] buffer = new byte[size];
                            serialPortEvent.getSerialPort().readBytes(buffer,size);
                            try {
                                String str = new String(buffer,"UTF-8");
                                if(str.length()>2) {
                                    System.out.println(str.substring(0,str.length()));
                                    if (str.equals("Ready")) {
                                        status.setText("Ready");
                                        result.setText("Ready");
                                    } else if (str.equals("Start")) {
                                        status.setText("Started");
                                        result.setText("Ready finish");
                                    } else if (str.equals("Finish")) {
                                        status.setText("Finished");
                                        result.setText("Wait result");
                                    } else {
                                        status.setText("Have result");
                                        result.setText(str);
                                        add_row_table(1, str, "egor", 100, "Moscow");

                                    }
                                }
                            } catch (UnsupportedEncodingException e) {
                                throw new RuntimeException(e);
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    });
                }else{
                    isConnectionL.setText("No Connect");
                    isConnectionL.setForeground(Color.RED);
                    isConnection = false;
                    status.setText("Waiting Connect");
                }
            }
        });
        SQL_start();

        Update.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comboBox1.removeAllItems();
                try {
                    comboBox1.setModel(new JComboBox<>(ShowAllPorts()).getModel());
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });


    }


    public static void main(String[] args) throws IOException, SQLException {
        Test test = new Test();
        System.out.println("test tetete");
    }
    public String[] ShowAllPorts() throws UnsupportedEncodingException {
        int i = 0;
        ports = SerialPort.getCommPorts();
        String[] arr = new String[ports.length];
        for(SerialPort port :ports){
            String s = (i+1)+". "+port.getDescriptivePortName()+" ";
            System.out.println(s);
            arr[i] = new String(s.getBytes(),"UTF-8");
            i++;
        }
        return arr;
    }
    public void SQL_start() throws SQLException {
        stm.executeUpdate("create database if not exists result_db");
        stm.executeUpdate("use result_db");
        //stm.executeUpdate("create table result_table(" +
        //        "id int primary key )");
        ResultSet rs = stm.executeQuery("select count(*) from information_schema.TABLES\n" +
                "where TABLE_SCHEMA = 'result_db' and TABLE_NAME = 'result_table'");
        rs.next();
        int i = rs.getInt(1);
        if(i == 0)
            stm.executeUpdate("create table result_table(" +
                    "id int primary key auto_increment," +
                    "result varchar(10) default '0:00'," +
                    "name_sportsmen varchar(30) default 'noname'," +
                    "distance_meter int default 0," +
                    "location varchar(30))");
        ResultSet results = stm.executeQuery("select * from result_table");
        while (results.next()){
            Vector<String>v = new Vector<>();
            v.add(results.getString(1));
            v.add(results.getString(2));
            v.add(results.getString(3));
            v.add(results.getString(4));
            v.add(results.getString(5));
            list_result.add(v);
            tableModel.addRow(v);
        }
        tableModel.fireTableDataChanged();
    }
    public void init_headers(){
        headers.add("id");
        headers.add("Result");
        headers.add("Name of sportsmen");
        headers.add("Distance (meter)");
        headers.add("Location");
        tableModel.setColumnIdentifiers(headers);
    }
    public void add_row_table(int id, String result1, String name, int distance, String location) throws SQLException {
        stm.executeUpdate("insert into result_table (result) values ('"+result1+"')");
        Vector<String> v = new Vector<>();
        v.add(String.valueOf(10));
        v.add(result1);
        v.add(name);
        v.add(String.valueOf(distance));
        v.add(location);
        tableModel.addRow(v);;
        tableModel.fireTableDataChanged();
    }
}
