package com.gradecalc.model.Bo;
import com.gradecalc.model.Bean.User;
import com.gradecalc.model.Dao.UserDAO;
import org.mindrot.jbcrypt.BCrypt;

import javax.print.DocFlavor;
import java.sql.SQLException;

public class UserBO {
    private final UserDAO userDAO;

    public UserBO() {
        this.userDAO = new UserDAO();
    }

    public void register(User user, String rawPassword) throws SQLException, InterruptedException {
        //null username
        if (user.getUsername() == null || user == null || user.getUsername().trim().isEmpty()){
            throw new IllegalArgumentException("Username không được phép để trống.");
        }

        //null fullname
        if (user.getFullname() == null || user.getFullname().trim().isEmpty()){
            throw new IllegalArgumentException("Fullname không được phép để trống.");
        }

        //null password, too short
        if (rawPassword == null || rawPassword.length() < 6){
            throw new IllegalArgumentException("Mật khẩu không hợp lệ (để trống/ ít hơn 6 ký tự).");
        }

        String cleanUsername = user.getUsername().trim();
        if (userDAO.checkUsernameExists(cleanUsername)){
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
        }

        user.setUsername(cleanUsername);

        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
        user.setPassword(hashedPassword);

        userDAO.save(user);

        //delete password of object user from memory/storage
        user.setPassword(null);
    }

    public User login(String username, String rawPassword) throws SQLException, InterruptedException{
        //username/password is null
        if (username == null || username.trim().isEmpty() || rawPassword == null || rawPassword.isEmpty()) {
            return null;
        }

        User user = userDAO.findByUsername(username.trim());
        if (user != null && BCrypt.checkpw(rawPassword, user.getPassword())) {
            //delete password of object user from memory/storage
            user.setPassword(null);
            return user;
        }
        return null;
    }
}
