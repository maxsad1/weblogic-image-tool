package com.oracle.weblogicx.imagebuilder.builder.util;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ARUUtil {

    /**
     * Return All WLS releases information
     *
     * @param userId userid for support account
     * @param password password for support account
     * @throws IOException  when failed to access the aru api
     */

    public static Document getAllWLSReleases(String userId, String password) throws IOException {
        return getAllReleases("wls", userId, password);
    }

    /**
     * Return release number of a WLS release by version
     *
     * @param version wls version 12.2.1.3.0 etc ...
     * @param userId  user id for support account
     * @param password password for support account
     * @return release number or empty string if not found
     * @throws IOException when failed to access the aru api
     */
    private static String getWLSReleaseNumber(String version, String userId, String password) throws
        IOException {
        return getReleaseNumber("wls", version, userId, password);
    }

    /**
     * Return release number of a FMW release by version
     *
     * @param version wls version 12.2.1.3.0 etc ...
     * @param userId  user id for support account
     * @param password password for support account
     * @return release number or empty string if not found
     * @throws IOException when failed to access the aru api
     */
    private static String getFMWReleaseNumber(String version, String userId, String password) throws
        IOException {
        return getReleaseNumber("fmw", version, userId, password);
    }


    /**
     * Return All FMW releases information
     *
     * @param userId userid for support account
     * @param password password for support account
     * @throws IOException  when failed to access the aru api
     */
    public static void getAllFMWReleases(String userId, String password) throws IOException {
        getAllReleases("fmw", userId, password);
    }

    /**
     * Download the latest WLS patches(PSU) for the release
     *
     * @param release release number
     * @param userId userid for support account
     * @param password password for support account
     * @throws IOException  when failed to access the aru api
     */
    public static void getLatestWLSPatches(String release, String userId, String password) throws IOException {
        getLatestPSU("wls", release, userId, password);
    }

    /**
     * Download the latest FMW patches(PSU) for the release
     *
     * @param release release number
     * @param userId userid for support account
     * @param password password for support account
     * @throws IOException  when failed to access the aru api
     */
    public static  void getLatestFMWPatches(String release, String userId, String password) throws IOException {
        getLatestPSU("fmw", release, userId, password);
    }

    /**
     * Download a list of WLS patches
     *
     * @param patches  A list of patches number
     * @param userId userid for support account
     * @param password password for support account
     * @throws IOException  when failed to access the aru api
     */
    public static  void getWLSPatches(List<String> patches, String userId, String password) throws
        IOException {
        for (String patch : patches)
            getPatch("wls", patch, userId, password);
    }

    /**
     * Download a list of FMW patches
     *
     * @param patches  A list of patches number
     * @param userId userid for support account
     * @param password password for support account
     * @throws IOException  when failed to access the aru api
     */
    public static  void getFMWPatches(String category, List<String> patches, String userId, String password) throws
        IOException {
        for (String patch : patches)
            getPatch("fmw", patch, userId, password);
    }

    /**
     *
     * @param patches  A list of patches number
     * @param category
     * @param version
     * @param userId userid for support account
     * @param password password for support account
     * @throws IOException  when failed to access the aru api
     */

    public static  void validatePatches(List<String> patches, String category, String version, String userId, String
        password) throws IOException {

        // TODO

        // find the release number first based on the version
        // build the xml

//        <conflict_check_request>
//  <platform>912</platform>
//  <target_patch_list>
//    <installed_patch/>
//  </target_patch_list>
//  <candidate_patch_list>
//    <patch_group rel_id="80111060" language_id="0">7044721</patch_group>
//    <patch_group rel_id="80111060" language_id="0">7156923</patch_group>
//    <patch_group rel_id="80111060" language_id="0">7210195</patch_group>
//    <patch_group rel_id="80111060" language_id="0">7256747</patch_group>
//  </candidate_patch_list>
//</conflict_check_request>

//   Run against POST  /Orion/Services/conflict_checks

    }

    private static  Document getAllReleases(String category, String userId, String password) throws IOException {

        //HTTP_STATUS=$(curl -v -w "%{http_code}" -b cookies.txt -L --header 'Authorization: Basic ${basicauth}'
       // "https://updates.oracle.com/Orion/Services/metadata?table=aru_releases" -o allarus.xml)

        Document allReleases = HttpUtil.getXMLContent("https://updates.oracle"
            + ".com/Orion/Services/metadata?table=aru_releases", userId, password);

        try {

            String expression;

            if ("wls".equalsIgnoreCase(category)) {
                expression = "/results/release[starts-with(text(), 'Oracle WebLogic Server')]";
            } else {
                expression = "/results/release[starts-with(text(), 'Fusion Middleware Upgrade')]";
            }
            NodeList nodeList = XPathUtil.applyXPathReturnNodeList(allReleases, expression);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element element = doc.createElement("results");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node n = nodeList.item(i);
                Node copyNode = doc.importNode(n, true);

                if (n instanceof Element )
                    element.appendChild(copyNode);
            }

            doc.appendChild(element);
            XPathUtil.prettyPrint(doc);

            return doc;

        } catch (XPathExpressionException | ParserConfigurationException xpe) {
            throw new IOException(xpe);
        }


    }

    private static  void getLatestPSU(String category, String release, String userId, String password) throws IOException {

        String xpath = "https://updates.oracle"
            + ".com/Orion/Services/search?product=%s&release=%s";
        String expression;
        if ("wls".equalsIgnoreCase(category))
            expression = String.format(xpath, "15991", release);
        else
            expression = String.format(xpath, "27638", release);

        Document allPatches = HttpUtil.getXMLContent(expression, userId, password);

        savepatch(allPatches, userId, password);
    }

    private static void getPatch(String category, String patchNumber, String userId, String password) throws
        IOException {

        //        HTTP_STATUS=$(curl -v -w "%{http_code}" -b cookies.txt -L --header 'Authorization: Basic ${basicauth}' "https://updates.oracle.com/Orion/Services/search?product=15991&release=$releaseid&include_prereqs=true" -o latestpsu.xml)

        String urlFormat = "https://updates.oracle"
            + ".com/Orion/Services/search?product=%s&bug=%s";
        String url;

        if ("wls".equalsIgnoreCase(category))
            url = String.format(urlFormat, "15991", patchNumber);
        else
            url = String.format(urlFormat, "27638", patchNumber);

        Document allPatches = HttpUtil.getXMLContent(url, userId, password);

        savepatch(allPatches, userId, password);



    }

    private static  void savepatch(Document allPatches, String userId, String password) throws IOException {
        try {

            // TODO: needs to make sure there is one and some filtering if not sorting

            String downLoadLink = XPathUtil.applyXPathReturnString(allPatches, "string"
                + "(/results/patch[1]/files/file/download_url/text())");

            String doloadHost = XPathUtil.applyXPathReturnString(allPatches, "string"
                + "(/results/patch[1]/files/file/download_url/@host)");

            String bugname  = XPathUtil.applyXPathReturnString(allPatches, "string"
                + "(/results/patch[1]/name");

            // TODO find the download location

            String fileName = bugname + ".zip";

            HttpUtil.downloadFile(doloadHost+downLoadLink, fileName, userId, password);

            // TODO need method to update the cache data table ?

        } catch (XPathExpressionException xpe) {
            throw new IOException(xpe);
        }

    }

    private static String getReleaseNumber(String category, String version, String userId, String password) throws
        IOException {
        Document allReleases = getAllReleases(category, userId, password);

        String expression = String.format("string(/results/release[@name = '%s']/@id)", version);
        try {
            return XPathUtil.applyXPathReturnString(allReleases, expression);
        } catch (XPathExpressionException xpe) {
            throw new IOException(xpe);
        }


    }

    public static void main(String args[]) throws Exception {
        String release = ARUUtil.getWLSReleaseNumber("121.2.1.3.0","johnny.shum@oracle.com", "iJCPiUah7jdmLk1E");
    }


}
