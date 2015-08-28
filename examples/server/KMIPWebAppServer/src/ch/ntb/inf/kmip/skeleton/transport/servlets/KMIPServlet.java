package ch.ntb.inf.kmip.skeleton.transport.servlets;
/**
 * KMIPServlet.java
 * -----------------------------------------------------------------
 *     __ __ __  ___________ 
 *    / //_//  |/  /  _/ __ \	  .--.
 *   / ,<  / /|_/ // // /_/ /	 /.-. '----------.
 *  / /| |/ /  / // // ____/ 	 \'-' .--"--""-"-'
 * /_/ |_/_/  /_/___/_/      	  '--'
 * 
 * -----------------------------------------------------------------
 * Description:
 * This servlet instanciates the skeleton (server-stub) and handles 
 * the GET and POST requests. 
 *
 * @author     Stefanie Meile <stefaniemeile@gmail.com>
 * @author     Michael Guster <michael.guster@gmail.com>
 * @org.       NTB - University of Applied Sciences Buchs, (CH)
 * @copyright  Copyright © 2013, Stefanie Meile, Michael Guster
 * @license    Simplified BSD License (see LICENSE.TXT)
 * @version    1.0, 2013/08/09
 * @since      Class available since Release 1.0
 *
 * 
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import ch.ntb.inf.klms.KLMS;
import ch.ntb.inf.klms.KLMSAdapter;
import ch.ntb.inf.klms.KLMSInterface;
import ch.ntb.inf.kmip.skeleton.KLMSAdapterInterface;
import ch.ntb.inf.kmip.skeleton.KMIPSkeleton;
import ch.ntb.inf.kmip.utils.KMIPUtils;

/**
 * Servlet implementation class KMIPServlet
 */
@WebServlet("/KMIPServlet")
public class KMIPServlet extends HttpServlet {

	private static final long serialVersionUID = 2092195673432330280L;
	private static final Logger logger = Logger.getLogger(KMIPServlet.class);
	private KMIPSkeleton skeleton;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public KMIPServlet() {
        super();
    }

       
    public void init(ServletConfig config) throws ServletException{
    	super.init(config);
        try {
        	ServletContext context = getServletContext();
        	
        	initLogger(config, context);
        	initClasses(context);
			
		} catch (Exception e) {									
			e.printStackTrace();		
		}
        
        logger.info("init() performed!");
    }
    
    private void initLogger(ServletConfig config, ServletContext context){
    	String log4jLocation = config.getServletContext().getRealPath("/")+context.getInitParameter("log4jLocation");
    	DOMConfigurator.configureAndWatch(log4jLocation);
    	
		logger.info("Hello KMIPWebAppServer! What a beatiful day;)");
    	logger.info("performing init() of KMIPServlet...");
    }
    
    private void initClasses(ServletContext context) throws Exception{
 		KLMSInterface klms = new KLMS();
		KLMSAdapterInterface adapter = new KLMSAdapter(klms);
		
		String encoderPath = context.getInitParameter("Encoder");
		String decoderPath = context.getInitParameter("Decoder");
		this.skeleton = new KMIPSkeleton(adapter, encoderPath, decoderPath);
    }
        
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("doGet() performed");
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head><title>KLMS Status</title></head>");
		out.println("<body>");
		out.println("<h1 style=\"background-color:#58D3F7;\">KLMS Status Information</h1>");
		
		writeStatus(out);
		
		out.println("</table>");
		out.println("</body>");
		out.println("</html>");
	}


	private void writeStatus(PrintWriter out) {
		HashMap<String, String> status = skeleton.getStatus();
		Iterator<Entry<String, String>> it = status.entrySet().iterator();
		
		out.println("<table border=\"1\">");
		out.println("<colgroup> <col width=\"200\"><col width=\"500\"></colgroup>");
		out.println("<tr>");
		out.println("<th align=\"left\" style=\"background-color:#F2F2F2;\">Description</th>");
		out.println("<th align=\"left\" style=\"background-color:#F2F2F2;\">Value</th>");
		out.println("</tr>");
		
		while(it.hasNext()){
			out.println("<tr>");
			Entry<String, String> pairs = it.next();
			out.println("<td><p>" + pairs.getKey() + "</p></td>");
			out.println("<td><p>" + pairs.getValue() + "</p></td>");
			it.remove(); 				// avoids a ConcurrentModificationException
			out.println("</tr>");
		}
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("\n-----------------------------------------");
		logger.info("perform doPost() of KMIPServlet...");
		
		String kmipRequest = request.getParameter("KMIPRequest");
		
		logger.info("Received Request (encoded):");
		logger.info(kmipRequest);
		
		ArrayList<Byte> al =  KMIPUtils.convertHexStringToArrayList(kmipRequest);
        ArrayList<Byte> kmipResponse = skeleton.processRequest(al);
        String kmipResponseString = KMIPUtils.convertArrayListToHexString(kmipResponse);
        
        logger.info("Send Response (encoded):");
        logger.info(kmipResponseString);
        
        sendPostResponse(response, kmipResponseString);
        
		logger.info("doPost() performed!");
		logger.info("-----------------------------------------");
	}


	private void sendPostResponse(HttpServletResponse response, String kmipResponseString) throws IOException {
        response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		out.println(kmipResponseString);
		out.flush();
		out.close();
	}
	

}
