package main;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;  
  
public class mysqlDBHelper {  
	private static String url = "jdbc:mysql://localhost:3306/12306?"
            + "user=root&password=qq915096289&useUnicode=true&characterEncoding=UTF8&useSSL=true&serverTimezone=UTC"; 
    public static Connection conn = null;  
    public PreparedStatement pst = null;  
    
    private static String[] types = {"������", "һ����", "������", "����"};//��λ����
    private static int[] ext_price = {120,60,10,0};//��λ����۸�
    private static int seatNum=20;//ÿ�ڳ���20����λ
//    private static int[] seatNum = {10, 20, 20, 30};//��λ����
    private static int[] carNum = {2,2,4,1};//�����������
    
  
    private static void Connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    private static void closeDb() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    
    
    private static void insetRoute(){
    	Connect();
    	try {
    		ArrayList<String>result=IOHelper.readFile();
        	
        	String[]d=new String[result.size()];
        	int i=0;
        	for(String s:result){
        		d[i]=result.get(i);
        		i++;
        	}
        	String r[]=new String[2];
        	for(String x:d){
        		r=x.split(" ");
        		String sql = "insert into route(name,route) values(?,?)";
        		PreparedStatement ps = conn.prepareStatement(sql);
        		ps = conn.prepareStatement(sql);
                ps.setString(1, r[0]);
                ps.setString(2, r[1]);
                ps.executeUpdate();
        	
        	}
    		 
		} catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDb();
        }
    }
    public static void insertTicket() {
        Connect();
        Calendar ca = Calendar.getInstance();
        try {
            String sql = "select * from route;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString(1);
                String route = resultSet.getString(2);
                String[] routes = route.split("-");
                long start = System.currentTimeMillis()+3600000;
                for (int i = 0; i < routes.length; i++) {
                    for (int j = i + 1; j < routes.length; j++) {
                        for (int k = 0; k < types.length; k++) {
                        	long end = System.currentTimeMillis()+3600000*(j-i);
                            sql = "insert into ticket(name,start,end,myfrom,myto,type,price,total) values(?,?,?,?,?,?,?,?)"; 
                            ps = conn.prepareStatement(sql);
                            ps.setString(1, name);//�г���
                            ps.setLong(2, start);
                            ps.setLong(3, end);
                            ps.setString(4, routes[i]);
                            ps.setString(5, routes[j]);
                            ps.setString(6, types[k]);//��λ����
                            ps.setDouble(7, (j-i)*10+ext_price[k]);
                            switch(k){
                            	case 0: ps.setInt(8, seatNum*carNum[0]);break;
                            	case 1: ps.setInt(8, seatNum*carNum[1]);break;
                            	case 2: ps.setInt(8, seatNum*carNum[2]);break;
                            	default: ps.setInt(8, seatNum*carNum[3]);break;
                            }
                           
                            ps.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDb();
        }
    }
    
    
    private static void checktotal(String name,String type,String myfrom,String myto){
    	long s=System.currentTimeMillis();
    	Connect();
        PreparedStatement ps = null;
        String sql="";
        try{
        	sql = "select total from ticket where name=? and type=? and myfrom=? and myto=?;";
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, type);
            ps.setString(3, myfrom);
            ps.setString(4, myto);
            ResultSet result = ps.executeQuery();
            if (result.next()) {
                int total = result.getInt(1);
                System.out.println("��Ҫ���г���Ʊ�У�"+total);
                
            }
            
        }catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            closeDb();
            long e=System.currentTimeMillis();
            long time=e-s;
//            System.out.println("��ʱ��"+time+"����");
            System.err.println("��ʱ��"+time+"����");
        }
    }
    private static void buyOneTicket(String name,String type,String myfrom,String myto){
    	long st=System.currentTimeMillis();
	Connect();
    PreparedStatement ps = null;
    String sql="";
    try {
        sql = "select total,start,end,price from ticket where name=? and type=? and myfrom=? and myto=?;";
        ps = conn.prepareStatement(sql);
        ps.setString(1, name);
        ps.setString(2, type);
        ps.setString(3, myfrom);
        ps.setString(4, myto);
        ResultSet result = ps.executeQuery();
        //��ȡʣ��Ʊ��
        int total = 0;
        long start=0;
        long end=0;
        int price=0;
        if (result.next()) {
            total = result.getInt(1);
            start = result.getLong(2);
            end = result.getLong(3);
            price=result.getInt(4);
        }

        if(total==0){
        	System.out.println("û�з��ϴ�Ҫ��ĳ�Ʊ�ˣ��뻻��Ҫ�����ԡ�����");
        	return;
        }
    	Date date1 = new Date(start);
    	SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Date date2 = new Date(end);
    	SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
    	
        System.out.println("------------��Ʊ��Ϣ����-----------");
        System.out.println("����: " + name);
        System.out.println("�����أ�" + myfrom);
        System.out.println("����أ�" + myto);
        System.out.println("����ʱ�䣺"+dateFormat1.format(date1));
    	System.out.println("����ʱ�䣺"+dateFormat2.format(date2));
        System.out.println("��λ���" + type);
        int car_num=0;
        int seat_num = 0;
        if(type.equals("����")){
        	car_num=(total)%carNum[getIndex("������", types)]+1+carNum[0]+carNum[1];

        }else{
        	if(total%seatNum==0){
        		car_num=(total)/seatNum;

        	}else{
        		car_num=(total/seatNum)+1;

        	}
            int num=getIndex(type, types);
            for(int i=0;i<num;i++){
            	car_num+=carNum[i];
            }

            
            if(total%seatNum!=0){
            	seat_num=total % seatNum;
            }else{
            	seat_num=total % seatNum+seatNum;
            }
//            System.out.println("����ţ�"+ car_num);
            System.out.println("��λ�ţ�" + seat_num);
        }
        System.out.println("����ţ�"+ car_num);
        System.out.println("Ʊ�ۣ�"+price+"Ԫ");
        System.out.println("-------------------------------");

        
        
        //��ʱ�������ݿ�
        sql = "select * from route;";
        ps = conn.prepareStatement(sql);
        ResultSet resultSet = ps.executeQuery();
        while (resultSet.next()) {
            String trainName = resultSet.getString(1);
            String route = resultSet.getString(2);
            String[] routes = route.split("-");
            if(name.equals(trainName)){
            	boolean f=false;
                boolean t=false;
            	int i=-1;
            	int j=-1;
            	for(String s:routes){
            		i++;
            		if(s.equals(myfrom)){
            			f=true;
            			break;
            		}
            	}
            	for(String s:routes){
            		j++;
            		if(s.equals(myto)){
            			t=true;
            			break;
            		}
            	}
            	if(f&&t){
                	for(int k=i;k<j;k++){
                		sql = "update ticket set total = if(total>0,total-1,total) where name=? and type=? and myfrom=?;";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, name);
                        ps.setString(2, type);
                        ps.setString(3, routes[k]);
                        ps.executeUpdate();
                	}
                	for(int q=0;q<i;q++){
                		for(int w=i+1;w<routes.length;w++){
                			sql = "update ticket set total = if(total>0,total-1,total) where name=? and type=? and myfrom=? and myto=?;";
                            ps = conn.prepareStatement(sql);
                            ps.setString(1, name);
                            ps.setString(2, type);
                            ps.setString(3, routes[q]);
                            ps.setString(4, routes[w]);
                            ps.executeUpdate();
                		
//                		System.out.println("s");
                		}
                	}
                	break;
            }
            
            }
            	
            
        }
    } catch (SQLException e) {
        e.printStackTrace();
        try {
            conn.rollback();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    } finally {
        closeDb();
        long en=System.currentTimeMillis();
        long time=en-st;
//        System.out.println("��ʱ��"+time+"����");
        System.err.println("��ʱ��"+time+"����");
    }
}

private static int getIndex(String str,String[]arg){
	int temp=-1;
	for(String s:arg){
		temp++;
		if(s.equals(str))
			return temp;
	}
	return -1;
}
    
    public static void main(String[] args) {
//��ʼ������
//��ʼ����·����Ϣ
//    	insetRoute();
//��ʼ���복Ʊ��Ϣ
//    	insertTicket();
    	
    	
//��ѯ��Ʊ
//    	checktotal("G101", "����", "��ׯ", "�Ͼ���");
//��Ʊ
    	buyOneTicket("G101", "����", "��ׯ", "�Ͼ���");
	}
}  

