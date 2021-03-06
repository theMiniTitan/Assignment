package Controllers;

import Models.User;
import javax.naming.NamingException;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.sql.*;
import javax.naming.InitialContext;

/**
 * Created by Brendan on 19/10/2016.
 */

//This servlet takes the information from the reportIssue.jsp and stores it in the sql database
@WebServlet(urlPatterns = {"/SubmittedReport"})
public class SubmittedReport extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //set all the needed information in the session
        request.getSession().setAttribute("currentPage", "submittedReport");
        request.getSession().setAttribute("error", null);
        request.getSession().setAttribute("success", null);

        //check if the user has logged in,
        User user = (User) request.getSession().getAttribute("user");
        if(user == null || !user.isLoggedIn()){
            response.sendRedirect(getServletContext().getContextPath() + "/index.jsp");
            return;
        }


        String statement;
        PreparedStatement prepStatement;

        try {
            //do the server side validation of the form submitted
            String error = validate(request);
            if(error != null){
                request.getSession().setAttribute("error", error);
                response.sendRedirect("HomePage");
                return;
            }

            //get connection details, from context.xml I take it
            javax.sql.DataSource datasource = (javax.sql.DataSource)new
                    InitialContext().lookup("java:/comp/env/SENG2050");
            //establish connection
            Connection connection = datasource.getConnection();

            //get current amount of issues in database for new issue number
            statement = "SELECT COUNT(*) FROM Issue";
            prepStatement = connection.prepareStatement(statement);
            ResultSet rs = prepStatement.executeQuery();
            int numOfIssues = 0;
            if(rs.next())
                 numOfIssues = rs.getInt(1);

            //preparing new issue insert statement with all request data from form
            statement = "INSERT INTO Issue" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

            prepStatement = connection.prepareStatement(statement);
            prepStatement.setInt(1, numOfIssues + 1);
            prepStatement.setString(2, "New");
            prepStatement.setString(3, request.getParameter("category"));
            prepStatement.setString(4, request.getParameter("title"));
            prepStatement.setString(5, request.getParameter("description"));
            prepStatement.setString(6, request.getParameter("location"));
            prepStatement.setString(7, request.getParameter("browser"));
            prepStatement.setString(8, request.getParameter("website"));
            prepStatement.setBoolean(9, request.getParameter("internalAccess").compareTo("yes")==0);
            prepStatement.setBoolean(10, request.getParameter("alternateBrowser").compareTo("yes")==0);
            prepStatement.setBoolean(11, request.getParameter("computerRestart").compareTo("yes")==0);
            prepStatement.setString(12, request.getParameter("errorMessage"));
            prepStatement.setString(13, "");
            //formatting current date and time
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            String stringDate = dateFormat.format(date);

            prepStatement.setString(14, stringDate);
            prepStatement.setString(15, "nil");
            prepStatement.setString(16, user.getUsername());
            prepStatement.setBoolean(17, false);
            //execution.
            prepStatement.executeUpdate();

            connection.close();
            rs.close();

            //set the success in the request
            request.getSession().setAttribute("success", "Successfully submitted report number "+(numOfIssues+1));

        } catch (Exception e) {
            request.getSession().setAttribute("error", "Something went wrong submitting the report");
        }


        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/HomePage"); //redirect back to the home page
        dispatcher.forward(request, response);
        return;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        doPost(request, response);
    }

    private String validate(HttpServletRequest request){
        String error = "";

        //validate the form submitted
        if(request.getParameter("category").equals("SelectACategory")) {
            return "Category wasn't selected";
        }else if(request.getParameter("title").equals(""))
            return "No title entered";
        else if (request.getParameter("internalAccess").equals("")){
            return "internal access is never assigned";
        }else if (request.getParameter("alternateBrowser").equals("")){
            return "alternate browser was not entered";
        }else if(request.getParameter("computerRestart").equals("")){
            return"computer restart was not entered";
        }else if(request.getParameter("description").equals("")){
            return "description is empty";
        }

        return null;
    }



}
