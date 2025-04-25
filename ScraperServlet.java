package com.servlet;

import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

public class ScrapeServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String url = request.getParameter("url");
        String[] options = request.getParameterValues("option");

        Map<String, Object> scrapedData = new HashMap<>();

        try {
            Document doc = Jsoup.connect(url).get();

            if (options != null) {
                for (String opt : options) {
                    switch (opt) {
                        case "title":
                            scrapedData.put("title", doc.title());
                            break;
                        case "links":
                            Elements links = doc.select("a[href]");
                            List<String> linkList = new ArrayList<>();
                            links.forEach(el -> linkList.add(el.attr("abs:href")));
                            scrapedData.put("links", linkList);
                            break;
                        case "images":
                            Elements images = doc.select("img[src]");
                            List<String> imgList = new ArrayList<>();
                            images.forEach(img -> imgList.add(img.attr("abs:src")));
                            scrapedData.put("images", imgList);
                            break;
                    }
                }
            }

           
            HttpSession session = request.getSession();
            Integer visitCount = (Integer) session.getAttribute("visitCount");
            if (visitCount == null) visitCount = 0;
            session.setAttribute("visitCount", ++visitCount);

           
            response.setContentType("text/html");
            response.getWriter().println("<h3>You have visited this page " + visitCount + " times.</h3>");
            response.getWriter().println("<table border='1'>");

            for (Map.Entry<String, Object> entry : scrapedData.entrySet()) {
                response.getWriter().println("<tr><th>" + entry.getKey() + "</th><td>" + entry.getValue() + "</td></tr>");
            }

            response.getWriter().println("</table>");

            
            Gson gson = new Gson();
            String json = gson.toJson(scrapedData);
            response.getWriter().println("<pre>" + json + "</pre>");

            
            response.getWriter().println("<button onclick='downloadCSV()'>Download CSV</button>");
            response.getWriter().println("""
            <script>
            function downloadCSV() {
                const rows = document.querySelectorAll("table tr");
                let csv = "";
                rows.forEach(row => {
                    let cols = row.querySelectorAll("td, th");
                    let line = Array.from(cols).map(col => `"${col.innerText}"`).join(",");
                    csv += line + "\\n";
                });
                const blob = new Blob([csv], { type: "text/csv" });
                const url = URL.createObjectURL(blob);
                const a = document.createElement("a");
                a.href = url;
                a.download = "results.csv";
                a.click();
            }
            </script>
            """);

        } catch (Exception e) {
            response.getWriter().println("Error: " + e.getMessage());
        }
    }
}
