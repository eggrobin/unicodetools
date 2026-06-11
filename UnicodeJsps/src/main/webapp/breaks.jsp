<%@ page import="org.owasp.encoder.Encode" %>
<html>

<head>
<%@ include file="header.jsp" %>
<title>Unicode Utilities: Breaks (Segmentation)</title>
<style>
<!--
td {vertical-align: top}
span.break   { border-right: 1px solid red;}
-->
</style>
</head>

<body>

<%
		request.setCharacterEncoding("UTF-8");

		String text = request.getParameter("a");
		if (text == null) text = "Sample Text.";
		String type = request.getParameter("type");
		if (type == null) type = "Word";
		String version = request.getParameter("version");
		String[] versions = UnicodeJsp.getSegmentationVersions();
		if (version == null) version = versions[0];
%>
<h1>Unicode Utilities: Breaks (Segmentation)</h1>
<%@ include file="subtitle.jsp" %>
<p><a target="help" href="https://unicode-org.github.io/unicodetools/help/breaks"><b>help</b></a> | <%@ include file="others.jsp" %></p>
<form name="myform" action="breaks.jsp" method="POST">
  <table border="1" cellpadding="0" cellspacing="0" style="border-collapse: collapse; width:100%">
    <tr>
      <td style="width:50%"><b>Input </b></td>
      <td style="width:50%">
      <select size="1" name="type" onchange="document.myform.submit();">
      <option <%= (type.equals("Grapheme") ? "selected" : "")%>>Grapheme Cluster</option>
      <option <%= (type.equals("Word") ? "selected" : "")%>>Word</option>
      <option <%= (type.equals("Line") ? "selected" : "")%>>Line</option>
      <option <%= (type.equals("Sentence") ? "selected" : "")%>>Sentence</option>
      </select>
      <select size="1" name="version" onchange="document.myform.submit();">
      <option <%= (version.equals("ICU") ? "selected" : "")%>>Current ICU</option>
<%
        for (String v : versions) {
%>
      <option <%= (version.equals(v) ? "selected" : "")%>>v</option>
<%
        }
%>
      </select>
      <input type="submit" value="Test" /></td>
    </tr>
    <tr>
      <td><textarea name="a" rows="30" cols="30" style="width:100%; height:100%"><%=Encode.forHtmlContent(text)%></textarea></td>
      <td>
      <%=UnicodeJsp.showBreaks(text, type, version)%>&nbsp;</td>
    </tr>
  </table>
</form>
<%@ include file="footer.jsp" %>
</body>

</html>
