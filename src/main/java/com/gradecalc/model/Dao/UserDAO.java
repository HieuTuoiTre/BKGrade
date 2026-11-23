package com.gradecalc.model.Dao;
import com.gradecalc.model.Bean.User;

public class UserDAO {
    boolean save(User user){
        return true;
    }

    User findByUsername(String username){

    }

    boolean checkUsernameExists(String username){
        return true;
    }
}
