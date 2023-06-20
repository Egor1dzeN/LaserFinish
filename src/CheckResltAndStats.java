import arduino.Arduino;

import java.util.concurrent.TimeUnit;

public class CheckResltAndStats implements Runnable{
    public Arduino arduino;
    public boolean connection;
    public boolean work = true;
    public CheckResltAndStats(Arduino arduino,boolean conn){
        this.arduino = arduino;
        connection = conn;
    }
    @Override
    public void run() {
        System.out.println("Connection: "+connection);
        int t = 0;
        while(work){
            t++;
            String str1 = arduino.serialRead();
            if(!str1.equals("")){
                System.out.println(str1);
                if(str1.equals("Start\n")){
                }
            }
            System.out.println(t);
        }
        System.out.println("End thread!!");
    }
}
