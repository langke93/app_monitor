package org.langke.jetty.common.chart;


import org.jfree.chart.imagemap.URLTagFragmentGenerator;

/**
 * @version 创建时间：2006-5-13 15:34:27 类说明
 */
public class WindowURLTagFragmentGenerator implements URLTagFragmentGenerator {

    private String customJavaScript;

    private String frameName;

    private String divString;

    /**
     * 生成在新窗口展现的map.
     * 
     * @param urlText the URL.
     * @return 格式化的链接脚本
     */
    public String generateURLFragment(String urlText) {

        String javaScript = this.getCustomJavaScript();

        String divString = this.divString;
        String paramString = null;

        String paramValue1 = null;
        String paramValue2 = null;

        String resultString = null;

        if (urlText.indexOf("mycustfreechart.jsp") > -1) {
            if (divString != null && divString.length() > 0) {
                paramString = urlText.substring(urlText.indexOf("?") + 1,
                        urlText.length());
                paramValue1 = paramString.substring(
                        paramString.indexOf("=") + 1, paramString
                                .indexOf("&amp;"));
                paramValue2 = paramString.substring(paramString
                        .lastIndexOf("=") + 1);

                resultString = divString.replaceFirst(":PARA0:", paramValue1);
                resultString = resultString
                        .replaceFirst(":PARA1:", paramValue2);

                // <area shape="rect" coords="395,3,802,604" href="#"
                // onclick="showInfo('1','200601')" />

                resultString = " href=\"javascript:\" onclick=\""
                        + resultString + "\""; // + "\" onmouseover=\"" +
                // resultString + "\"";

                return resultString;
            }
        }
        
        if (urlText.indexOf("mycustfreechart1.jsp") > -1) {
            if (frameName != null && frameName.length() > 0) {
                paramString = urlText.substring(urlText.indexOf("?") + 1,
                        urlText.length());
                paramValue1 = paramString.substring(
                        paramString.indexOf("=") + 1, paramString
                                .indexOf("&amp;"));
                paramValue2 = paramString.substring(paramString
                        .lastIndexOf("=") + 1);

                resultString = frameName.replaceFirst(":PARA0:", paramValue1);
                resultString = resultString
                        .replaceFirst(":PARA1:", paramValue2);

                // <area shape="rect" coords="395,3,802,604" href="#"
                // onclick="showInfo('1','200601')" />

                resultString = " href=\"javascript:\" onmouseover=\""
                        + resultString + "\""; // + "\" onmouseover=\"" +
                // resultString + "\"";

                return resultString;
            }

        }

        if (javaScript != null && javaScript.length() > 0) {

            String urlStr = urlText.substring(urlText.indexOf("@") + 2);

            String str = " href=\"#\" onclick=\""
                    + getCustomJavaScript().replaceAll(":URL:", urlStr) + "\"";
            String returnString = " " + str.substring(1, str.length() - 2)
                    + "\"";
            return returnString;
        }
        else {
            if (this.frameName != null && this.frameName.length() > 0) {
                return " href=\"" + urlText + "\" target=\"" + this.frameName
                        + "\"";
            }
            else {
                return " href=\"" + urlText + "\" target=\"_blank\"";
            }
        }
    }

    public String getCustomJavaScript() {
        return customJavaScript;
    }

    public void setCustomJavaScript(String customJavaScript) {
        this.customJavaScript = customJavaScript;
    }

    public String getFrameName() {
        return frameName;
    }

    public void setFrameName(String frameName) {
        this.frameName = frameName;
    }

    public String getDivString() {
        return divString;
    }

    public void setDivString(String divString) {
        this.divString = divString;
    }
}
