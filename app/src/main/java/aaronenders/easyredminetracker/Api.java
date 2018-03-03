package aaronenders.easyredminetracker;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class Api {

    public String[][] loadedTimeEntries;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String[][] getIssues(String companyName, String apiKey, String userId)
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;

        String[][] issues = new String[25][4];
        loadedTimeEntries = loadTimeEntries(companyName, apiKey, userId);


        String avatar;
        int issueCount = 0;
        for(int offset=0; offset<=1; offset++) {
            String path = "https://"+companyName+".easyredmine.com/issues.xml?key="+apiKey+"&offset="+(offset * 50)+"&limit=50&page=&sort=closed_on&set_filter=1&assigned_to_id="+userId;
            try {
                db = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            try {
                InputStream xml = new URL(path).openStream();
                Document dom = db.parse(xml);
                //Element docEle = dom.getDocumentElement();
                Node projects = dom.getElementsByTagName("issues").item(0);
                NodeList nl = projects.getChildNodes();
                if (nl != null) {
                    int length = nl.getLength();
                    for (int i = 0; i < length; i++) {
                        if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            Element el = (Element) nl.item(i);
                            if (el.getNodeName().contains("issue")) {
                                String issueClosedOn = el.getElementsByTagName("closed_on").item(0).getTextContent();
                                if ("" == issueClosedOn){
                                    final String issueId = el.getElementsByTagName("id").item(0).getTextContent();
                                    String issueSubject = el.getElementsByTagName("subject").item(0).getTextContent();
                                    if (issueCount < 24) {
                                        issues[issueCount][0] = issueId;
                                        issues[issueCount][1] = issueSubject;
                                        String[] issueTimeEntry = getIssueTimeEntry(issueId);
                                        issues[issueCount][2] = issueTimeEntry[0];
                                        issues[issueCount][3] = issueTimeEntry[1];
                                        issueCount++;
                                    }
                                }else{
                                    xml.close();
                                    return issues;
                                }

                            }
                        }
                    }
                }

            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return issues;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String[] getIssueTimeEntry(String issueId){
        String[] IssueTimeEntry = new String[2];
        for (String[] timeEntry : loadedTimeEntries) {
            if (timeEntry[1] != null){
                if (timeEntry[1].equals(issueId)){
                    String timeEntryId = timeEntry[0];
                    String hours = timeEntry[2];
                    IssueTimeEntry[0] = timeEntryId;
                    IssueTimeEntry[1] = hours;
                }
            }

        }
        return IssueTimeEntry;
    }

    public String[][] loadTimeEntries(String company, String apiKey, String userId){
        String[][] timeEntries = new String[25][3];
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd");
        String date = mdformat.format(calendar.getTime());
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            URL url = new URL("https://" + company + ".easyredmine.com/time_entries.xml?user_id="+userId+"&key=" + apiKey+"&from="+date);

            Log.i("URL", "https://" + company + ".easyredmine.com/time_entries.xml?user_id="+userId+"&key=" + apiKey+"&from="+date);
            InputStream xml = new URL("https://" + company + ".easyredmine.com/time_entries.xml?user_id="+userId+"&key=" + apiKey+"&from="+date).openStream();
            Document dom = db.parse(xml);
            //Element docEle = dom.getDocumentElement();
            Node projects = dom.getElementsByTagName("time_entries").item(0);
            NodeList nodelist = projects.getChildNodes();

            if (nodelist != null) {
                int length = nodelist.getLength();
                for (int i = 0; i < length; i++) {
                    if (nodelist.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element el = (Element) nodelist.item(i);
                        if (el.getNodeName().contains("time_entry")) {
                            String entityType = el.getElementsByTagName("entity_type").item(0).getTextContent();
                            timeEntries[i][0] = el.getElementsByTagName("id").item(0).getTextContent();
                            timeEntries[i][1] = el.getElementsByTagName("issue").item(0).getAttributes().getNamedItem("id").getNodeValue();
                            double hoursDecimal = Double.valueOf(el.getElementsByTagName("hours").item(0).getTextContent());
                            int hours = (int) hoursDecimal;
                            int minutes = (int) (hoursDecimal * 60) % 60;
                            int seconds = (int) (hoursDecimal * (60*60)) % 60;
                            String timeSpent = hours+":"+minutes+":"+seconds;
                            timeEntries[i][2] = timeSpent;
                        }
                    }
                }
            }else{
                Log.d("No", "previous time entries found");
            }
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        Log.d("previous time entries", "deep arr: " + Arrays.deepToString(timeEntries));
        return timeEntries;
    }

    public String[] getUserInfos(String company, String userId, String apiKey) {
        String[] userInfos = new String[2];
        try {
            URL url = new URL("https://" + company + ".easyredmine.com/users/"+userId+".xml?key=" + apiKey);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();
            NodeList nodelist = doc.getElementsByTagName("user");
            Element el = (Element) nodelist.item(0);
            userInfos[0] = el.getElementsByTagName("firstname").item(0).getTextContent() + " "+el.getElementsByTagName("lastname").item(0).getTextContent();
            userInfos[1] = el.getElementsByTagName("avatar_url").item(0).getTextContent();
            return userInfos;
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
            return userInfos;
        }
    }

    public int createTimeRecord(Integer currentIssueId, String currentHours, String companyName, String userId, String apiKey) throws IOException, SAXException {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd");
        String date = mdformat.format(calendar.getTime());
        URL url = new URL("https://"+companyName+".easyredmine.com/time_entries.xml?key="+apiKey);
        String body = "<time_entry><issue_id>"+currentIssueId.toString()+"</issue_id><user_id>"+userId+"</user_id><hours>"+currentHours+"</hours><comments></comments>  <spent_on>"+date+"</spent_on></time_entry>";
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            //conn.setReadTimeout(10000);
            //conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Accept", "application/xml");
            conn.setRequestProperty("Content-Type", "application/xml");
            OutputStream output = new BufferedOutputStream(conn.getOutputStream());
            output.write(body.getBytes());
            output.flush();
            output.close();
        }finally {
            //conn.disconnect();
        }
        BufferedReader br;
        if (200 <= conn.getResponseCode() && conn.getResponseCode() <= 299) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        String response = new String();
        for (String line; (line = br.readLine()) != null; response += line);
        br.close();
        conn.disconnect();
        Log.i("ANTWORT: ", ":"+response);
        return getTimeEntryIdFromXml(response);
    }
    public Boolean updateTimeRecord(Integer timeEntryId, String currentHours, String companyName, String apiKey) throws IOException, SAXException {
        URL url = new URL("https://"+companyName+".easyredmine.com/time_entries/"+timeEntryId+".xml?key="+apiKey);
        String body = "<time_entry><hours>"+currentHours+"</hours></time_entry>";
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("PUT");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Accept", "application/xml");
            conn.setRequestProperty("Content-Type", "application/xml");
            OutputStream output = new BufferedOutputStream(conn.getOutputStream());
            output.write(body.getBytes());
            output.flush();
            output.close();
        }finally {
        }
        BufferedReader br;
        if (200 <= conn.getResponseCode() && conn.getResponseCode() <= 299) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            return false;
        }
        System.out.print("ANTWORT: "+br);
        String response = new String();
        for (String line; (line = br.readLine()) != null; response += line);
        br.close();
        conn.disconnect();
        return true;
    }

    public int getTimeEntryIdFromXml(String xml) throws IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        InputSource is = new InputSource(new StringReader(xml));
        Document dom = builder.parse(is);
        if (dom.getElementsByTagName("id").item(0) != null){
            int timeEntry = Integer.parseInt(dom.getElementsByTagName("id").item(0).getTextContent());
            return timeEntry;
        }else{
            return 0;
        }
    }
}
