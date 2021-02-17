package org.apache.hadoop.hbase.master;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

public class QuestionsServlet extends HttpServlet {
    private static final Log LOG = LogFactory.getLog(QuestionsServlet.class);
    private static final String OUTPUT = "<html><body>" +
            "<h3>Submitted successfully.</h3>" +
            "</body></html>";
    private static final TableName COMMENTS_TABLE = TableName.valueOf("cdc_comments");
    private static final byte[] COL_FAM = new String("0").getBytes();
    private static final byte[] COL_NAME = new String("name").getBytes();
    private static final byte[] COL_COMMENT = new String("comment").getBytes();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String name = request.getParameter("name");
        String message = request.getParameter("message");

        HMaster master = (HMaster) getServletContext().getAttribute(HMaster.MASTER);
        assert master != null : "No Master in context!";

        Connection conn = master.getConnection();
        if (conn.getAdmin().tableExists(COMMENTS_TABLE)) {
            UUID uuid = UUID.randomUUID();
            Put put = new Put(uuid.toString().getBytes());
            put.addColumn(COL_FAM, COL_NAME, name.getBytes());
            put.addColumn(COL_FAM, COL_COMMENT, message.getBytes());
            conn.getTable(COMMENTS_TABLE).put(put);
            LOG.info("Input: " + name + " : " + message + " inserted successfully");
        } else {
            LOG.error("cdc_comments table doesn't exist...");
        }
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/questions.jsp");
        dispatcher.forward(request, response);
    }
}
