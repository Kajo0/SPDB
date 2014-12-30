package pl.edu.pw.elka.spdb;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet which serves routes.
 * 
 * @author Jan Zarzycki
 *
 */
@SuppressWarnings("serial")
public class FindRouteServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
        out.print("Find route servlet: Hello World");
	}
	
}
