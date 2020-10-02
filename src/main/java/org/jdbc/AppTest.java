package org.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Copyright: Copyright (c) 2020
 *
 * @ClassName: org.jdbc.AppTest
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: lipan
 * @date: 2020/10/2 18:54
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2020/10/2      lipan          v1.0.0               修改原因
 */

public class AppTest {


    public static void main(String[] args) {




        Pool pool = DBPool.getPool();
        Connection connection = pool.getConnection();

//        String sql="insert  into  person VALUES (?,?); ";
//        try {
//            PreparedStatement preparedStatement = connection.prepareStatement(sql);
//            preparedStatement.setObject(1,2);
//            preparedStatement.setObject(2,"阿毛");
//            preparedStatement.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

        for (int i = 0; i <100 ; i++) {
            System.out.println(pool.getConnection());
        }

    }
}
