package Controllers;

import Models.Comment;
import Models.Issue;
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
@WebServlet(urlPatterns = {"/ChangeCommentType"})
public class ChangeCommentType extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.getSession().setAttribute("currentPage", "changeCommentType");
        request.getSession().setAttribute("error", null);
        request.getSession().setAttribute("success", null);

        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !user.isLoggedIn()) {
            response.sendRedirect(getServletContext().getContextPath() + "/index.jsp");
            return;
        }

        try {

            javax.sql.DataSource datasource = (javax.sql.DataSource) new
                    InitialContext().lookup("java:/comp/env/SENG2050");

            Connection connection = datasource.getConnection();
            String query = "UPDATE UserComment SET commentType = ? WHERE commentID = ?";
            PreparedStatement prepStatement = connection.prepareStatement(query);
            prepStatement.setString(1, request.getParameter("commentType"));
            prepStatement.setString(2, request.getParameter("commentID"));
            prepStatement.executeUpdate();
            connection.close();

        } catch (Exception e) {
            String error = "Something went wrong when updating Issue State "; //set an error
            request.setAttribute("error", error + e.getMessage());
        }

        if(request.getParameter("commentType").equals("Rejected")){
            request.setAttribute("state", "In-Progess");
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/ChangeIssueState?issueID="+request.getParameter("issueID") +
            "&state=In-Progress");
            dispatcher.forward(request, response);
        }
        else if(request.getParameter("commentType").equals("Accepted")){
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/ChangeIssueState?issueID="+request.getParameter("issueID") +
                    "&state=Resolved");
            dispatcher.forward(request, response);
        }
        else{
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/Issue?issueID="+request.getParameter("issueID"));
            dispatcher.forward(request, response);
        }

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
        //nothing
    }

}
