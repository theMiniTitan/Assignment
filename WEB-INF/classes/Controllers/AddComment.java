package Controllers;

import Models.Comment;
import Models.Issue;
import Models.Notification;
import Models.User;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Brendan on 19/10/2016.
 */
@WebServlet(urlPatterns = {"/AddComment"})
public class AddComment extends HttpServlet{

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.getSession().setAttribute("currentPage", "addComment");
        request.getSession().setAttribute("error", null);
        request.getSession().setAttribute("success", null);

        User user = (User) request.getSession().getAttribute("user");
        if(user == null || !user.isLoggedIn()){
            response.sendRedirect(getServletContext().getContextPath() + "/index.jsp");
            return;
        }

        if(!validateForm(request)) {
            response.sendRedirect("HomePage");
            return;
        }

        String issueID = request.getParameter("issueID");
        int numOfComments = 0;

        try{ //get all the comments

            System.out.println("add comment start");
            javax.sql.DataSource datasource = (javax.sql.DataSource) new
                    InitialContext().lookup("java:/comp/env/SENG2050");

            Connection connection = datasource.getConnection();
            Statement statement = connection.createStatement();
            String query = "SELECT COUNT(*) FROM UserComment"; //query for all the comments for that issue
            ResultSet result = statement.executeQuery(query);
            if(result.next()){
                numOfComments = result.getInt(1);
            }



            query = "INSERT INTO UserComment VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement prepStatement = connection.prepareStatement(query);
            prepStatement.setInt(1, numOfComments+1);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            String stringDate = dateFormat.format(date);
            prepStatement.setString(2, stringDate);
            prepStatement.setString(3, request.getParameter("commentContent"));
            prepStatement.setString(4, request.getParameter("commentType"));
            prepStatement.setString(5, user.getUsername());
            prepStatement.setString(6, request.getParameter("issueID"));

            prepStatement.executeUpdate();
            System.out.println("add comment end");

            connection.close();
            result.close();

            if(user.isStaff() && (request.getParameter("commentType").equals("Comment") || request.getParameter("commentType").equals("Proposed"))){ //notification needs to be set
                String content = "Waiting on Reporter";
                String username = user.getUsername();
                Notification notification = new Notification();
                notification.setContent(content);
                notification.setIssueID(Integer.parseInt(issueID));
                notification.setUsername(username);

                Database database = new Database();
                database.addNotification(notification);
            }



        }catch (Exception e) {
            String error = "Something went wrong when adding comment: "; //set an error
            request.setAttribute("error", error+e.getMessage());
        }

        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/Issue?issueID="+request.getParameter("issueID"));
        dispatcher.forward(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    private boolean validateForm(HttpServletRequest request){

        String error = null;
        if(request.getParameter("commentContent") == null)
            error = "No comment entered.";
        else if(request.getParameter("commentContent").length() > 1000)
            error = "Comment exceeds maximum length.";

        if(error != null){
            request.getSession().setAttribute("error", error);
            return false;
        }
        return true;


    }

}
