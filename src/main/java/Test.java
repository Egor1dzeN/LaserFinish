import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

public class Test extends JFrame {

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
    private JCheckBox saveResult;
    private JComboBox sportsmensBox;
    String location_city = MyLocation.getMyLocation();
    private JLabel Location ;
    private DefaultTableModel tableModel = new DefaultTableModel();
    SerialPort serialPort;
    boolean isConnection;
    SerialPort[] ports = SerialPort.getCommPorts();
    private final String url = "jdbc:mysql://localhost:3306";
    private final String user = "root";
    private final String password = "Partner25";
    private final Connection con = DriverManager.getConnection(url, user, password);
    private final Statement stm = con.createStatement();
    Vector<Vector<String>> list_result = new Vector<>();
    Vector<String> headers = new Vector<>();
    JScrollPane jScrollPane;
    private boolean is_saving = true;

    public Test() throws IOException, SQLException {
        super("My First Window");
        setVisible(true);
        setSize(1000, 450);
        JPanel jPanel = new JPanel(new FlowLayout());
        init_headers();
        Location = new JLabel("Loc: "+location_city);
        table_result = new JTable(tableModel);
        table_result.setDefaultEditor(Object.class, null);
        table_result.getColumnModel().getColumn(5).setMinWidth(190);
        table_result.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        saveResult.setSelected(true);
        jPanel.add(comboBox1);
        jPanel.add(isConnectionL);
        jPanel.add(Test_con);
        jPanel.add(Update);
        jPanel.add(StatusL);
        jPanel.add(status);
        jPanel.add(ResultL);
        jPanel.add(result);
        jPanel.add(saveResult);
        jPanel.add(sportsmensBox);
        jPanel.add(Location);
        jPanel.add(Reset, BorderLayout.NORTH);
        jPanel.add(table_result);
        jScrollPane = new JScrollPane(table_result, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setPreferredSize(new Dimension(590, 280));
        jPanel.add(jScrollPane);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        saveResult.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {

            }
        });
        Test_con.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.fireTableDataChanged();
                int i = comboBox1.getSelectedIndex();
                serialPort = ports[i];
                serialPort.openPort();
                if (serialPort.isOpen()) {
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
                            serialPortEvent.getSerialPort().readBytes(buffer, size);
                            try {
                                String str = new String(buffer, "UTF-8");
                                if (str.length() > 2) {
                                    System.out.println("Данные с датчика: "+str);
                                    if (str.equals("Ready")) {
                                        status.setText("Ready");
                                        result.setText("Ready");
                                    } else if (str.equals("Start")) {
                                        status.setText("Started");
                                        result.setText("Ready finish");
                                    } else if (str.equals("Finish")) {
                                        status.setText("Finished");
                                        result.setText("Wait result");
                                    } else if (str.contains(":")) {
                                        status.setText("Have result");
                                        String[] arr = str.split(":");
                                        if (arr[1].length() < 2) {
                                            arr[1] = "0" + arr[1];
                                        }
                                        str = arr[0] + ":" + arr[1];
                                        result.setText(str);
                                        String name = (String) sportsmensBox.getSelectedItem();
                                        System.out.println("Сохранили данные спортсмена - "+name);
                                        if (saveResult.isSelected())
                                            add_row_table(1, str, name, 100, location_city);
                                    }
                                }
                            } catch (UnsupportedEncodingException e) {
                                throw new RuntimeException(e);
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    });
                } else {
                    isConnectionL.setText("No Connect");
                    isConnectionL.setForeground(Color.RED);
                    isConnection = false;
                    status.setText("Waiting Connect");
                }
            }
        });

        SQL_start();
        init_sportsmensBox();


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
        System.out.println("Программа запущена!");
    }

    public String[] ShowAllPorts() throws UnsupportedEncodingException {
        int i = 0;
        ports = SerialPort.getCommPorts();
        String[] arr = new String[ports.length];
        System.out.println("Доступные для выбора порты: ");
        for (SerialPort port : ports) {
            String s = (i + 1) + ". " + port.getDescriptivePortName() + " ";
            System.out.println(s);
            arr[i] = new String(s.getBytes(), "UTF-8");
            i++;
        }
        return arr;
    }

    public void SQL_start() throws SQLException {
        stm.executeUpdate("create database if not exists result_db");
        stm.executeUpdate("use result_db");
        stm.executeUpdate("create table if not exists sportsmens(" +
                "id int primary key auto_increment," +
                "name varchar(30)," +
                "surname varchar(30))");
        stm.executeUpdate("create table if not exists result_table(" +
                "id int primary key auto_increment," +
                "result varchar(10) default '0:00'," +
                "sportsmen_id int," +
                "distance_meter int default 0," +
                "location varchar(30) default 'No location'," +
                "time_result timestamp," +
                "foreign key(sportsmen_id) references sportsmens(id))");
        ResultSet results = stm.executeQuery("select * from result_table");
        while (results.next()) {
            Vector<String> v = new Vector<>();
            v.add(results.getString(1));
            v.add(results.getString(2));
            v.add(results.getString(3));
            v.add(results.getString(4));
            v.add(results.getString(5));
            v.add(results.getString(6));
            list_result.add(v);
            tableModel.addRow(v);
        }
        tableModel.fireTableDataChanged();
    }

    public void init_headers() {
        headers.add("id");
        headers.add("Result");
        headers.add("Name of sportsmen");
        headers.add("Distance (meter)");
        headers.add("Location");
        headers.add("Time_result");
        tableModel.setColumnIdentifiers(headers);
    }

    public void add_row_table(int id, String result1, String name, int distance, String location) throws SQLException {
        System.out.println("Добавили данные для "+name+", в городе - "+location);
        ResultSet rs1 = stm.executeQuery("select id   from sportsmens " +
                "where concat(name,' ',surname) = '" + name + "'");
        rs1.next();

        int id_sportsmen = rs1.getInt(1);
       // System.out.println(id_sportsmen);
        stm.executeUpdate("insert into result_table (result, sportsmen_id,location,time_result) values ('" + result1 + "'," + id_sportsmen + ",'"+location+"', now())");
        ResultSet rs = stm.executeQuery("select id from result_table order by id desc limit 1");
        //System.out.println("dasdasd");
        int i = 0;
        if (rs.next())
            i = rs.getInt(1) + 1;
        Vector<String> v = new Vector<>();
        v.add(String.valueOf(i));
        v.add(result1);
        v.add(name);
        v.add(String.valueOf(distance));
        v.add(location);
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        v.add(timeStamp);
        tableModel.addRow(v);
        tableModel.fireTableDataChanged();
       // System.out.println("okok");
    }

    public String[] init_sportsmensBox() throws SQLException {
        ResultSet rs = stm.executeQuery("select count(*) from sportsmens");
        rs.next();
        int size = rs.getInt(1);
        System.out.println("Количество спортсменов в бд - "+size+" "+Declansion.declantion_of_word(size));
        String[] arr_sportsmens = new String[size];
        rs = stm.executeQuery("select * from sportsmens");
        int i = 0;
        while (rs.next()) {
            String str = rs.getString(2) + " " + rs.getString(3);
            arr_sportsmens[i] = str;
            i++;
        }
        sportsmensBox.setModel(new DefaultComboBoxModel(arr_sportsmens));
        return arr_sportsmens;
    }
}
