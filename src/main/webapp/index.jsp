<%@page contentType="text/html; charset=utf-8"%>
<%@page import="java.util.*"%>
<%
String path = request.getContextPath();
%>
<head>
	<title><%=org.langke.jetty.common.Config.get().get("jsp.title") %></title>
	<link href="<%=path %>/css/frame.css" rel="stylesheet" type="text/css" />
</head>
 	<frameset rows="57,*" border="0">
		<frame src="<%=path %>/head.jsp" name="topFrame" id="topFrame" title="topFrame" frameborder="no"  noresize="noresize" scrolling="no" />
		<frame src="<%=path %>/subframe.jsp" name="subFrame" id="subFrame" title="subFrame" frameborder="no" />
		<noframes>
		<body>
				
		</body>
		</noframes>
	</frameset>
</html>
