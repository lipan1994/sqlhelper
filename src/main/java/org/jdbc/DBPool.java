package org.jdbc;

/**
 * Copyright: Copyright (c) 2020
 *
 * @ClassName: org.jdbc.DBPool
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: lipan
 * @date: 2020/10/2 18:49
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2020/10/2      lipan          v1.0.0               修改原因
 */

public class DBPool extends Pool {


    //唯一实例
    protected static  Pool instance=null;

    protected DBPool(){
        super();
    }


    public synchronized  static  Pool  getPool(){

        if(null==instance){
            instance=new DBPool();
        }
        return  instance;

    }



}
