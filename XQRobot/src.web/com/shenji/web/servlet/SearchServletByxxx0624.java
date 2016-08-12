package com.shenji.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.shenji.common.log.Log;
import com.shenji.robot.webservices.port.CommonServer;
import com.shenji.robot.webservices.port.OntoReasoning;
import com.shenji.robot.webservices.port.Search;
import com.shenji.search.control.DicControl;

/**
 * @author xxx0624
 * 
 */
public class SearchServletByxxx0624 extends HttpServlet {


	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}   

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ");
		out.print(this.getClass());
		out.println(", using the GET method");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String tagType = "1";
		String searchTxt = URLDecoder.decode(request.getParameter("searchTxt"), "utf-8");
		System.out.println("searchTxt:" + searchTxt);
		int tagTypeInt = Integer.parseInt(tagType);
		Log.getLogger().debug(tagType);
		String html = "";
		Cookie cookie = new Cookie("tagType", tagType); 
		response.addCookie(cookie);
		if(searchTxt.startsWith("如："))
			searchTxt=searchTxt.replace("如：", "");
		if(tagTypeInt==1){
			int radoiTypeInt = 10;
			html = new Search().searchHtml(searchTxt, 1, radoiTypeInt);
			out.write(html); 		
		}
		out.close();
	}

	private void createSession(HttpServletRequest request,
			HttpServletResponse response, String message)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		if (message == null)
			message = "没有结果！！";
		session.setAttribute("reStr", message);
		RequestDispatcher rd = request.getRequestDispatcher("/demo.jsp");
		rd.forward(request, response);
	}

	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *             if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
