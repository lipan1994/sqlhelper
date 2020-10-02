package org.jdbc;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

/**
 * Copyright: Copyright (c) 2020
 *
 * @ClassName: org.jdbc.Pool
 * @Description: 连接池抽象类，
 * 抽象类需要借助子类进行实例化，
 * 但是可以将一些公有的子类不想重复书写的代码在抽象类中实现
 * 除了差异化的代码，其他全部下沉到抽象父类。
 * @version: v1.0.0
 * @author: lipan
 * @date: 2020/10/2 9:39
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2020/10/2      lipan          v1.0.0               修改原因
 */

public abstract class Pool {



    //配置文件
    public static  final  String ConfigFile="/connection-INF.properties";



  //地址
  protected   String url;

  //用名
  protected   String userName;

  //密码
  protected   String password;


  protected   String driverName;


  //默认50初始化从配置文件中读取(配置成-1则不做限制）
  protected  int maxConnect=50;

  //连接池默认初始化连接数
  protected  int defaultConnect=10;


  //当前正在使用的连接数
  protected  int inUseConNum=0;


  //空闲连接数
  protected  int freeConnum=0;


  //连接请求数
  protected  int conNeed=0;


  //用来存放空闲连接
  Vector<Connection> freeConnections=new Vector<>();


 //数据库驱动类
  protected Driver driver=null;


  //子类实例化先调用父类构造方法
  protected  Pool(){
      try {

          init();
          loadDriver(driverName);
          //创建初始化的
          if(defaultConnect>maxConnect){
              throw  new RuntimeException("初始化连接数不能大于最大连接数");
          }
          for (int i = 0; i <defaultConnect ; i++) {
              Connection connection = newConnection();
              freeConnections.add(connection);
              freeConnum++;
          }
      } catch (Exception e) {
          e.printStackTrace();
      }
  }


  //从连接池中获取一个连接（获取失败直接抛出异常）
    public synchronized Connection getConnection(){
       Connection con=null;
       conNeed++;

       //从空闲的连接中获取
       if(freeConnections.size()>0){
           con=freeConnections.get(0);
           freeConnections.remove(0);
           try {
               if(con.isClosed()){
                   System.out.println("移除无效连接");
                   getConnection();
               }
           } catch (SQLException e) {
               e.printStackTrace();
           }
       }else if(inUseConNum<maxConnect || maxConnect==-1){
           con = newConnection();
       }else{
           //如果对连接实时性要求高那么应该直接抛异常。如果不高，为了支持等待一段时间拿到连接，此时应该什么都不做返回null
//           throw  new RuntimeException("连接获取失败，最大连接数:"+maxConnect+"已用连接数:"+inUseConNum+"请求连接数:"+conNeed);
       }

       if(null!=con){
           inUseConNum++;
       }

       return con;
    }



    //获取等待时间后的连接
    public synchronized Connection getConnection(long time){
        long beforeGetCon= System.currentTimeMillis();
        Connection con;
        if((con=getConnection())==null){
            try {
                //阻塞当前线程，因为没有连接释放，不需要再去做无意义的getConnect（）尝试,等一定时间后自动唤醒，
                // 因为存在被唤醒和超时唤醒，所以要判断一下时间
                wait(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
             //虽然被唤醒，当时时间过了。
            // 等号是考虑程序执行太快，毫秒整数存在精度丢失，比如等待3000ms 执行到这过了 3000.1ms 取正整数就丢失了精度
            if(System.currentTimeMillis()-beforeGetCon>=time){
                return  null;
            }
        }

        return con;

    }


    //将连接释放回去
    public synchronized  void  freeConnection(Connection connection){
        freeConnections.add(connection);
        inUseConNum--;
        conNeed--;
        freeConnum++;
        //多线程唤醒肯定是用这个，因为随机唤醒，不公平导致线程饥饿
        notifyAll();
    }







  //加载驱动已连接数配置
  private  void init() throws Exception{
      InputStream is =Pool.class.getResourceAsStream(ConfigFile);
      Properties properties=new Properties();
      properties.load(is);
      this.driverName=properties.getProperty("driverName");
      this.url=properties.getProperty("url");
      this.userName=properties.getProperty("username");
      this.password=properties.getProperty("password");

      if( properties.getProperty("maxConnect")!=null){

          this.maxConnect=Integer.parseInt(properties.getProperty("maxConnect"));
      }
      if( properties.getProperty("activeConnect")!=null){

          this.defaultConnect=Integer.parseInt(properties.getProperty("defaultConnect"));
      }

  }



    //清空连接池
    public  synchronized void  clearAll(){

        Iterator<Connection> iterator = freeConnections.iterator();
        while (iterator.hasNext()){
            try {
                iterator.next().close();
                freeConnum--;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        freeConnections.clear();
    }



  //注册驱动
    protected void  loadDriver(String driverName){
        try {
           driver= (Driver) Class.forName(driverName).newInstance();
           DriverManager.registerDriver(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //卸载驱动(是否需要考虑线程安全）
    protected void  release(){
        try {
            DriverManager.deregisterDriver(driver);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    //创建一个新的数据库连接
    protected Connection  newConnection(){
       Connection connection=null;
       //无用户
        try {
            if(userName==null){
               connection=DriverManager.getConnection(url);
            }else{
                connection=DriverManager.getConnection(url,userName,password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return  connection;
    }





}
