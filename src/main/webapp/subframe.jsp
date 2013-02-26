<%@page contentType="text/html; charset=utf-8"%>
<%
String path = request.getContextPath();
%>
<head>
<title><%=org.langke.jetty.common.Config.get().get("jsp.title") %></title>
</head>
<frameset id="workspace" cols="15%,8,*" border="0" >
	<frame src="<%=path %>/menu/menuLeft.do" name="leftFrame" id="leftFrame" title="leftFrame" frameborder="no" noresize="noresize" />
	<frame src="<%=path %>/fbtn.jsp" name="btnFrame" id="btnFrame" title="btnFrame" frameborder="no" noresize="noresize" scrolling="no" />
	
	<frame src="<%=path %>/monitor/_report" name="mainFrame" id="mainFrame" title="mainFrame" frameborder="no" />
	<noframes>
	<body>
		your browser no support frames
	</body>
	</noframes>
</frameset>
</html>
